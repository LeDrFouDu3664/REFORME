require('dotenv').config();
const {
    Client,
    GatewayIntentBits,
    Partials,
    EmbedBuilder,
    ActionRowBuilder,
    ButtonBuilder,
    ButtonStyle,
    ChannelType,
    PermissionsBitField,
    Collection,
    ModalBuilder,
    TextInputBuilder,
    TextInputStyle
} = require('discord.js');
const transcript = require('discord-html-transcripts');
const config = require('./config.json');
const fs = require('fs');

const client = new Client({
    intents: [
        GatewayIntentBits.Guilds,
        GatewayIntentBits.GuildMessages,
        GatewayIntentBits.MessageContent,
        GatewayIntentBits.GuildMembers,
    ],
    partials: [Partials.Channel, Partials.Message],
});

// Persistence
let ticketData = new Map();
let activeTickets = new Map();
const DATA_FILE = './tickets.json';

function loadData() {
    if (fs.existsSync(DATA_FILE)) {
        try {
            const data = JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'));
            ticketData = new Map(Object.entries(data.ticketData || {}));
            activeTickets = new Map(Object.entries(data.activeTickets || {}));
        } catch (e) {
            console.error("Could not load data:", e);
        }
    }
}

function saveData() {
    const data = {
        ticketData: Object.fromEntries(ticketData),
        activeTickets: Object.fromEntries(activeTickets)
    };
    try {
        fs.writeFileSync(DATA_FILE, JSON.stringify(data, null, 2));
    } catch (e) {
        console.error("Could not save data:", e);
    }
}

loadData();

// Tracking
const cooldowns = new Collection();

const categories = {
    'ticket_commande': { label: 'Commande', color: '#3498db', description: 'Pour toute demande concernant une commande.', slug: 'commande' },
    'ticket_direction': { label: 'Direction', color: '#e74c3c', description: 'Pour contacter la direction directement.', slug: 'direction' },
    'ticket_partenariat': { label: 'Partenariat', color: '#2ecc71', description: 'Pour les propositions de partenariat.', slug: 'partenariat' },
    'ticket_recrutement': { label: 'Recrutement', color: '#9b59b6', description: 'Pour postuler au sein de notre équipe.', slug: 'recrute' },
    'ticket_moderation': { label: 'Modération', color: '#95a5a6', description: 'Pour signaler un joueur ou un problème de modération.', slug: 'mod' },
};

client.once('ready', () => {
    console.log(`Connecté en tant que ${client.user.tag}!`);
    client.user.setActivity("Gérer les tickets", { type: 2 });
});

async function logAction(guild, action, user, channel, extra = {}) {
    const logChannel = guild.channels.cache.get(config.logChannelId);
    if (!logChannel) return;

    const embed = new EmbedBuilder()
        .setTitle(`Log: ${action}`)
        .addFields(
            { name: 'Utilisateur', value: user ? `${user.tag} (${user.id})` : 'N/A', inline: true },
            { name: 'Salon', value: channel ? `${channel.name} (${channel.id})` : 'N/A', inline: true },
            { name: 'Date', value: `<t:${Math.floor(Date.now() / 1000)}:F>`, inline: false }
        )
        .setColor(extra.color || '#2c3e50')
        .setTimestamp();

    if (extra.staff) embed.addFields({ name: 'Staff responsable', value: `${extra.staff.tag}`, inline: true });
    if (extra.reason) embed.addFields({ name: 'Raison', value: extra.reason, inline: false });
    if (extra.content) embed.addFields({ name: 'Contenu', value: extra.content.substring(0, 1024), inline: false });

    await logChannel.send({ embeds: [embed] }).catch(console.error);
}

async function logAudit(guild, action, user, extra = {}) {
    const logChannel = guild.channels.cache.get(config.auditLogChannelId || config.logChannelId);
    if (!logChannel) return;

    const embed = new EmbedBuilder()
        .setTitle(`Audit: ${action}`)
        .addFields(
            { name: 'Utilisateur', value: user ? `${user.tag} (${user.id})` : 'N/A', inline: true },
            { name: 'Date', value: `<t:${Math.floor(Date.now() / 1000)}:F>`, inline: false }
        )
        .setColor(extra.color || '#f39c12')
        .setTimestamp();

    if (extra.channel) embed.addFields({ name: 'Salon', value: `${extra.channel.name} (${extra.channel.id})`, inline: true });
    if (extra.oldContent) embed.addFields({ name: 'Ancien Contenu', value: extra.oldContent.substring(0, 1024), inline: false });
    if (extra.newContent) embed.addFields({ name: 'Nouveau Contenu', value: extra.newContent.substring(0, 1024), inline: false });
    if (extra.content) embed.addFields({ name: 'Contenu', value: extra.content.substring(0, 1024), inline: false });

    await logChannel.send({ embeds: [embed] }).catch(console.error);
}

// Audit Listeners
client.on('messageUpdate', async (oldMsg, newMsg) => {
    if (oldMsg.author?.bot) return;
    if (oldMsg.content === newMsg.content) return;
    await logAudit(newMsg.guild, 'Message Modifié', newMsg.author, {
        channel: newMsg.channel,
        oldContent: oldMsg.content,
        newContent: newMsg.content,
        color: '#f1c40f'
    });
});

client.on('messageDelete', async (message) => {
    if (message.author?.bot) return;
    await logAudit(message.guild, 'Message Supprimé', message.author, {
        channel: message.channel,
        content: message.content,
        color: '#e74c3c'
    });
});

client.on('channelDelete', async (channel) => {
    // Sync state if a ticket channel is deleted manually
    const data = ticketData.get(channel.id);
    if (data) {
        activeTickets.delete(data.userId);
        ticketData.delete(channel.id);
        saveData();
        await logAudit(channel.guild, 'Salon Ticket Supprimé Manuellement', null, {
            channel: channel,
            color: '#d35400'
        });
    }
});

client.on('messageCreate', async (message) => {
    if (message.author.bot || !message.content.startsWith(config.prefix)) return;

    const args = message.content.slice(config.prefix.length).trim().split(/ +/);
    const command = args.shift().toLowerCase();

    if (command === 'setup' && message.member.permissions.has(PermissionsBitField.Flags.Administrator)) {
        const embed = new EmbedBuilder()
            .setTitle('🎫 Système de Tickets')
            .setDescription('Cliquez sur un bouton ci-dessous pour ouvrir un ticket selon votre besoin.\n\n⚠️ **Un seul ticket actif par personne.**')
            .setColor('#2F3136')
            .setFooter({ text: 'Système de gestion interne' });

        const row1 = new ActionRowBuilder()
            .addComponents(
                new ButtonBuilder().setCustomId('ticket_commande').setLabel('Commande').setEmoji('🛒').setStyle(ButtonStyle.Primary),
                new ButtonBuilder().setCustomId('ticket_direction').setLabel('Direction').setEmoji('👑').setStyle(ButtonStyle.Danger),
                new ButtonBuilder().setCustomId('ticket_partenariat').setLabel('Partenariat').setEmoji('🤝').setStyle(ButtonStyle.Success),
            );

        const row2 = new ActionRowBuilder()
            .addComponents(
                new ButtonBuilder().setCustomId('ticket_recrutement').setLabel('Recrutement').setEmoji('📝').setStyle(ButtonStyle.Primary),
                new ButtonBuilder().setCustomId('ticket_moderation').setLabel('Modération / Signalement').setEmoji('⚖️').setStyle(ButtonStyle.Secondary),
            );

        await message.channel.send({ embeds: [embed], components: [row1, row2] }).catch(console.error);
        message.delete().catch(() => {});
    }
});

client.on('interactionCreate', async (interaction) => {
    if (interaction.isButton()) {
        const category = categories[interaction.customId];

        // Ticket Creation
        if (category) {
            if (cooldowns.has(interaction.user.id)) {
                const expirationTime = cooldowns.get(interaction.user.id) + 60000;
                if (Date.now() < expirationTime) {
                    return interaction.reply({ content: `Veuillez attendre encore ${Math.ceil((expirationTime - Date.now()) / 1000)}s avant d'ouvrir un autre ticket.`, ephemeral: true });
                }
            }

            if (activeTickets.has(interaction.user.id)) {
                return interaction.reply({ content: 'Vous avez déjà un ticket actif !', ephemeral: true });
            }

            await interaction.deferReply({ ephemeral: true });

            try {
                const ticketChannel = await interaction.guild.channels.create({
                    name: `${category.slug}-${interaction.user.username}`,
                    type: ChannelType.GuildText,
                    parent: config.ticketCategoryId,
                    permissionOverwrites: [
                        { id: interaction.guild.id, deny: [PermissionsBitField.Flags.ViewChannel] },
                        { id: interaction.user.id, allow: [PermissionsBitField.Flags.ViewChannel, PermissionsBitField.Flags.SendMessages, PermissionsBitField.Flags.ReadMessageHistory] },
                        { id: config.staffRoleId, allow: [PermissionsBitField.Flags.ViewChannel, PermissionsBitField.Flags.SendMessages, PermissionsBitField.Flags.ReadMessageHistory] },
                    ],
                });

                activeTickets.set(interaction.user.id, ticketChannel.id);
                ticketData.set(ticketChannel.id, { userId: interaction.user.id, status: 'open', type: interaction.customId });
                cooldowns.set(interaction.user.id, Date.now());
                saveData();

                const welcomeEmbed = new EmbedBuilder()
                    .setTitle(`Ticket: ${category.label} - OUVERT`)
                    .setDescription(`${category.description}\n\n**Règles:**\n- Soyez respectueux\n- Soyez patient\n- Un seul ticket par demande`)
                    .setColor(category.color)
                    .setTimestamp()
                    .setFooter({ text: `Utilisateur: ${interaction.user.tag}` });

                const row1 = new ActionRowBuilder()
                    .addComponents(
                        new ButtonBuilder().setCustomId('claim_ticket').setLabel('Réclamer').setEmoji('🙋‍♂️').setStyle(ButtonStyle.Success),
                        new ButtonBuilder().setCustomId('close_ticket_modal').setLabel('Fermer').setEmoji('🔒').setStyle(ButtonStyle.Danger),
                    );

                const row2 = new ActionRowBuilder()
                    .addComponents(
                        new ButtonBuilder().setCustomId('add_member_modal').setLabel('Ajouter Membre').setEmoji('👤').setStyle(ButtonStyle.Secondary),
                        new ButtonBuilder().setCustomId('remove_member_modal').setLabel('Retirer Membre').setEmoji('🚫').setStyle(ButtonStyle.Secondary),
                    );

                const components = [row1, row2];

                if (interaction.customId === 'ticket_recrutement') {
                    const rowRecrute = new ActionRowBuilder()
                        .addComponents(
                            new ButtonBuilder().setCustomId('accept_recrute').setLabel('Accepter').setEmoji('✅').setStyle(ButtonStyle.Success),
                            new ButtonBuilder().setCustomId('refuse_recrute').setLabel('Refuser').setEmoji('❌').setStyle(ButtonStyle.Danger),
                        );
                    components.push(rowRecrute);
                    await ticketChannel.send({ content: `<@${interaction.user.id}> | <@&${config.staffRoleId}>`, embeds: [welcomeEmbed], components: components });
                } else {
                    await ticketChannel.send({ content: `<@${interaction.user.id}> | <@&${config.staffRoleId}>`, embeds: [welcomeEmbed], components: components });
                }

                await interaction.editReply({ content: `Votre ticket a été créé : ${ticketChannel}` });
                await logAction(interaction.guild, 'Ouverture Ticket', interaction.user, ticketChannel);
            } catch (err) {
                console.error(err);
                await interaction.editReply({ content: "Erreur lors de la création du ticket. Vérifiez les permissions du bot." });
            }
        }

        // Management Buttons
        if (interaction.customId === 'claim_ticket') {
            if (!interaction.member.roles.cache.has(config.staffRoleId)) return interaction.reply({ content: 'Seul le staff peut réclamer un ticket.', ephemeral: true });
            const data = ticketData.get(interaction.channel.id);
            if (!data) return interaction.reply({ content: 'Erreur: Données du ticket introuvables.', ephemeral: true });
            if (data.claimedBy) return interaction.reply({ content: 'Ce ticket est déjà pris en charge.', ephemeral: true });

            data.claimedBy = interaction.user.id;
            saveData();

            const embed = EmbedBuilder.from(interaction.message.embeds[0]).addFields({ name: 'Pris en charge par', value: `${interaction.user.tag}` });
            await interaction.message.edit({ embeds: [embed] }).catch(() => {});
            await interaction.reply({ content: `Le ticket est maintenant pris en charge par ${interaction.user}.` });
            await logAction(interaction.guild, 'Claim Ticket', interaction.user, interaction.channel, { staff: interaction.user });
        }

        if (interaction.customId === 'close_ticket_modal') {
            const modal = new ModalBuilder().setCustomId('modal_close_ticket').setTitle('Fermeture du Ticket');
            const reasonInput = new TextInputBuilder().setCustomId('close_reason').setLabel('Raison de la fermeture').setStyle(TextInputStyle.Paragraph).setRequired(true);
            modal.addComponents(new ActionRowBuilder().addComponents(reasonInput));
            await interaction.showModal(modal);
        }

        if (interaction.customId === 'add_member_modal') {
            if (!interaction.member.roles.cache.has(config.staffRoleId)) return interaction.reply({ content: 'Seul le staff peut faire cela.', ephemeral: true });
            const modal = new ModalBuilder().setCustomId('modal_add_member').setTitle('Ajouter un membre');
            const userInput = new TextInputBuilder().setCustomId('user_id').setLabel('ID de l\'utilisateur').setStyle(TextInputStyle.Short).setRequired(true);
            modal.addComponents(new ActionRowBuilder().addComponents(userInput));
            await interaction.showModal(modal);
        }

        if (interaction.customId === 'remove_member_modal') {
            if (!interaction.member.roles.cache.has(config.staffRoleId)) return interaction.reply({ content: 'Seul le staff peut faire cela.', ephemeral: true });
            const modal = new ModalBuilder().setCustomId('modal_remove_member').setTitle('Retirer un membre');
            const userInput = new TextInputBuilder().setCustomId('user_id').setLabel('ID de l\'utilisateur').setStyle(TextInputStyle.Short).setRequired(true);
            modal.addComponents(new ActionRowBuilder().addComponents(userInput));
            await interaction.showModal(modal);
        }

        if (interaction.customId === 'reopen_ticket') {
            if (!interaction.member.roles.cache.has(config.staffRoleId)) return interaction.reply({ content: 'Seul le staff peut réouvrir un ticket.', ephemeral: true });
            const data = ticketData.get(interaction.channel.id);
            if (!data) return interaction.reply({ content: 'Données introuvables.', ephemeral: true });

            try {
                const user = await client.users.fetch(data.userId);
                await interaction.channel.permissionOverwrites.edit(user, { ViewChannel: true, SendMessages: true });
                data.status = 'open';
                saveData();
                await interaction.reply({ content: 'Le ticket a été réouvert.' });
                await logAction(interaction.guild, 'Réouverture Ticket', user, interaction.channel, { staff: interaction.user });
            } catch (err) {
                await interaction.reply({ content: "Erreur lors de la réouverture.", ephemeral: true });
            }
        }

        if (interaction.customId === 'delete_ticket') {
            if (!interaction.member.roles.cache.has(config.adminRoleId)) return interaction.reply({ content: 'Seul un administrateur peut supprimer un ticket.', ephemeral: true });
            const data = ticketData.get(interaction.channel.id);
            if (data) activeTickets.delete(data.userId);
            ticketData.delete(interaction.channel.id);
            saveData();
            await interaction.reply('Suppression du salon dans 5 secondes...');
            setTimeout(() => interaction.channel.delete().catch(() => {}), 5000);
        }

        if (interaction.customId === 'transcript_ticket') {
            await interaction.deferReply();
            try {
                const attachment = await transcript.createTranscript(interaction.channel);
                await interaction.editReply({ files: [attachment] });
            } catch (err) {
                await interaction.editReply("Erreur lors de la génération du transcript.");
            }
        }

        // Recruitment specific
        if (interaction.customId === 'accept_recrute') {
            if (!interaction.member.roles.cache.has(config.recruitmentRoleId) && !interaction.member.permissions.has(PermissionsBitField.Flags.Administrator)) {
                return interaction.reply({ content: 'Vous n\'avez pas la permission d\'accepter une candidature.', ephemeral: true });
            }
            const data = ticketData.get(interaction.channel.id);
            try {
                const user = await interaction.guild.members.fetch(data.userId);
                await user.roles.add(config.recruitmentRoleId).catch(console.error);
                await user.send("Félicitations ! Votre candidature a été acceptée.").catch(() => {});

                // Requirement: creation of necessary channels
                const welcomeChannel = await interaction.guild.channels.create({
                    name: `bienvenue-${user.user.username}`,
                    type: ChannelType.GuildText,
                    parent: config.recruitmentCategoryId || config.ticketCategoryId,
                    permissionOverwrites: [
                        { id: interaction.guild.id, deny: [PermissionsBitField.Flags.ViewChannel] },
                        { id: user.id, allow: [PermissionsBitField.Flags.ViewChannel, PermissionsBitField.Flags.SendMessages] },
                        { id: config.staffRoleId, allow: [PermissionsBitField.Flags.ViewChannel] },
                    ],
                });

                await welcomeChannel.send(`Bienvenue dans l'équipe ${user} ! Ce salon est dédié à ton intégration.`);

                await interaction.reply({ content: `La candidature de ${user} a été acceptée. Salon créé : ${welcomeChannel}` });
                await logAction(interaction.guild, 'Candidature Acceptée', user.user, interaction.channel, { staff: interaction.user, color: '#2ecc71' });
            } catch (err) {
                await interaction.reply({ content: "Erreur lors de l'acceptation.", ephemeral: true });
            }
        }

        if (interaction.customId === 'refuse_recrute') {
            if (!interaction.member.roles.cache.has(config.recruitmentRoleId) && !interaction.member.permissions.has(PermissionsBitField.Flags.Administrator)) {
                return interaction.reply({ content: 'Vous n\'avez pas la permission de refuser une candidature.', ephemeral: true });
            }
            const data = ticketData.get(interaction.channel.id);
            try {
                const user = await interaction.guild.members.fetch(data.userId);
                await user.send("Désolé, votre candidature a été refusée.").catch(() => {});
                await interaction.reply({ content: `La candidature de ${user} a été refusée.` });
                await logAction(interaction.guild, 'Candidature Refusée', user.user, interaction.channel, { staff: interaction.user, color: '#e74c3c' });
            } catch (err) {
                await interaction.reply({ content: "Erreur lors du refus.", ephemeral: true });
            }
        }
    }

    if (interaction.isModalSubmit()) {
        if (interaction.customId === 'modal_close_ticket') {
            const reason = interaction.fields.getTextInputValue('close_reason');
            const data = ticketData.get(interaction.channel.id);
            if (!data) return interaction.reply({ content: "Erreur: Données du ticket introuvables.", ephemeral: true });

            try {
                const user = await client.users.fetch(data.userId);
                await interaction.channel.permissionOverwrites.edit(user, { ViewChannel: false });
                data.status = 'closed';
                saveData();

                const closedEmbed = new EmbedBuilder().setTitle('Ticket Fermé').setDescription(`Raison: ${reason}\nPar: ${interaction.user.tag}`).setColor('#e74c3c').setTimestamp();
                const row = new ActionRowBuilder().addComponents(
                    new ButtonBuilder().setCustomId('reopen_ticket').setLabel('Réouvrir').setEmoji('🔓').setStyle(ButtonStyle.Success),
                    new ButtonBuilder().setCustomId('transcript_ticket').setLabel('Transcript').setEmoji('📜').setStyle(ButtonStyle.Primary),
                    new ButtonBuilder().setCustomId('delete_ticket').setLabel('Supprimer').setEmoji('🗑️').setStyle(ButtonStyle.Danger),
                );
                await interaction.reply({ embeds: [closedEmbed], components: [row] });
                await logAction(interaction.guild, 'Fermeture Ticket', user, interaction.channel, { staff: interaction.user, reason, color: '#e67e22' });

                const attachment = await transcript.createTranscript(interaction.channel);
                const logChannel = interaction.guild.channels.cache.get(config.logChannelId);
                if (logChannel) await logChannel.send({ content: `Transcript pour le ticket ${interaction.channel.name}`, files: [attachment] });
            } catch (err) {
                await interaction.reply({ content: "Erreur lors de la fermeture.", ephemeral: true });
            }
        }

        if (interaction.customId === 'modal_add_member') {
            const userId = interaction.fields.getTextInputValue('user_id');
            try {
                const user = await interaction.guild.members.fetch(userId);
                await interaction.channel.permissionOverwrites.edit(user, { ViewChannel: true, SendMessages: true });
                await interaction.reply({ content: `${user} a été ajouté au ticket.` });
                await logAction(interaction.guild, 'Ajout Membre', user.user, interaction.channel, { staff: interaction.user });
            } catch (e) { await interaction.reply({ content: 'Utilisateur introuvable.', ephemeral: true }); }
        }

        if (interaction.customId === 'modal_remove_member') {
            const userId = interaction.fields.getTextInputValue('user_id');
            try {
                const user = await interaction.guild.members.fetch(userId);
                await interaction.channel.permissionOverwrites.edit(user, { ViewChannel: false });
                await interaction.reply({ content: `${user} a été retiré du ticket.` });
                await logAction(interaction.guild, 'Retrait Membre', user.user, interaction.channel, { staff: interaction.user });
            } catch (e) { await interaction.reply({ content: 'Utilisateur introuvable.', ephemeral: true }); }
        }
    }
});

client.login(process.env.DISCORD_TOKEN);
