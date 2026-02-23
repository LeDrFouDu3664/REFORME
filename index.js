const { Client, GatewayIntentBits, EmbedBuilder, Partials, PermissionsBitField } = require('discord.js');
const config = require("./config.json");

const client = new Client({
    intents: [
        GatewayIntentBits.Guilds,
        GatewayIntentBits.GuildMessages,
        GatewayIntentBits.MessageContent,
        GatewayIntentBits.GuildMembers,
        GatewayIntentBits.DirectMessages
    ],
    partials: [Partials.Channel]
});

const prefix = config.prefix || "r ";

client.on('ready', () => {
    console.log(`Connecté en tant que ${client.user.tag}!`);
    client.user.setPresence({
        status: "online",
        activities: [{ name: "Gérer le serveur Faction", type: 0 }] // type 0 is PLAYING
    });
});

client.on('messageCreate', async msg => {
    if (msg.author.bot) return;
    if (!msg.content.startsWith(prefix)) return;

    const args = msg.content.slice(prefix.length).trim().split(/ +/);
    const command = args.shift().toLowerCase();

    // Discussion commands
    if (command === 'ping') return msg.reply('🏓 Pong!');
    if (command === 'salut') return msg.reply('Salut à toi ! 👋');
    if (command === 'ça' && args[0] === 'va' && args[1] === '?') return msg.reply('Oui et toi ? 😊');

    // Moderation commands
    if (command === 'ban') {
        if (!msg.member.permissions.has(PermissionsBitField.Flags.BanMembers)) {
            return msg.reply("❌ Vous n'avez pas la permission de bannir des membres.");
        }
        const member = msg.mentions.members.first();
        if (!member) return msg.reply("❌ Veuillez mentionner un membre à bannir.");
        if (!member.bannable) return msg.reply("❌ Je ne peux pas bannir ce membre.");

        const reason = args.slice(1).join(" ") || "Aucune raison fournie";
        await member.ban({ reason });

        const embed = new EmbedBuilder()
            .setColor(0xFF0000)
            .setTitle("Utilisateur Banni")
            .setDescription(`**${member.user.tag}** a été banni du serveur.`)
            .addFields({ name: "Raison", value: reason })
            .setTimestamp();

        return msg.channel.send({ embeds: [embed] });
    }

    if (command === 'kick') {
        if (!msg.member.permissions.has(PermissionsBitField.Flags.KickMembers)) {
            return msg.reply("❌ Vous n'avez pas la permission d'expulser des membres.");
        }
        const member = msg.mentions.members.first();
        if (!member) return msg.reply("❌ Veuillez mentionner un membre à expulser.");
        if (!member.kickable) return msg.reply("❌ Je ne peux pas expulser ce membre.");

        const reason = args.slice(1).join(" ") || "Aucune raison fournie";
        await member.kick(reason);

        const embed = new EmbedBuilder()
            .setColor(0xFFA500)
            .setTitle("Utilisateur Expulsé")
            .setDescription(`**${member.user.tag}** a été expulsé du serveur.`)
            .addFields({ name: "Raison", value: reason })
            .setTimestamp();

        return msg.channel.send({ embeds: [embed] });
    }

    if (command === 'mute') {
        if (!msg.member.permissions.has(PermissionsBitField.Flags.ModerateMembers)) {
            return msg.reply("❌ Vous n'avez pas la permission de mute des membres.");
        }
        const member = msg.mentions.members.first();
        if (!member) return msg.reply("❌ Veuillez mentionner un membre à mute.");

        // Timeout for 1 hour by default
        const duration = 60 * 60 * 1000;
        await member.timeout(duration, "Mute par le bot");

        return msg.reply(`✅ **${member.user.tag}** a été rendu muet pour 1 heure.`);
    }

    if (command === 'help') {
        const helpEmbed = new EmbedBuilder()
            .setColor(0x0099FF)
            .setTitle('Commandes du Bot Faction')
            .setDescription(`Préfixe actuel : \`${prefix}\``)
            .addFields(
                { name: '💬 Discussion', value: '`ping`, `salut`, `ça va ?`' },
                { name: '🛡️ Modération', value: '`ban @user [raison]`, `kick @user [raison]`, `mute @user`' },
                { name: 'ℹ️ Information', value: '`stats`, `help`' }
            )
            .setFooter({ text: 'Bot de gestion TPCFaction' });

        return msg.channel.send({ embeds: [helpEmbed] });
    }

    if (command === 'stats') {
        const statsEmbed = new EmbedBuilder()
            .setColor(0x00FF00)
            .setTitle('Statistiques du Serveur')
            .addFields(
                { name: 'Membres', value: `${msg.guild.memberCount}`, inline: true },
                { name: 'Canaux', value: `${msg.guild.channels.cache.size}`, inline: true },
                { name: 'Rôles', value: `${msg.guild.roles.cache.size}`, inline: true }
            )
            .setTimestamp();

        return msg.channel.send({ embeds: [statsEmbed] });
    }
});

client.login(config.token).catch(err => {
    console.error("Erreur de connexion : " + err.message);
});
