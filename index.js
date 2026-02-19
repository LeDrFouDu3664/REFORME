const { Client, GatewayIntentBits, EmbedBuilder, Partials } = require('discord.js');
const config = require("./config.json");

const client = new Client({
    intents: [
        GatewayIntentBits.Guilds,
        GatewayIntentBits.GuildMessages,
        GatewayIntentBits.MessageContent,
        GatewayIntentBits.DirectMessages
    ],
    partials: [Partials.Channel]
});

const prefix = config.prefix || "r ";

client.on('ready', () => {
    console.log(`Connecté en tant que ${client.user.tag}!`);
    client.user.setPresence({
        status: "online",
        activities: [{ name: "je me fais coder", type: 2 }] // type 2 is LISTENING
    });
});

client.on('messageCreate', async msg => {
    if (msg.author.bot) return;
    if (!msg.content.startsWith(prefix)) return;

    const command = msg.content.slice(prefix.length).trim().toLowerCase();

    if (command === 'ping') {
        await msg.reply('Pong!');
    }
    else if (command === 'salut') {
        await msg.reply('salut!');
    }
    else if (command === 'ça va ?') {
        await msg.reply('oui et toi ?');
    }
    else if (command === 'oui bien merci') {
        await msg.reply('de rien!');
    }
    else if (command === 'allez au revoir') {
        await msg.reply('oui au revoir et à bientôt!');
    }
    else if (command === 'help') {
        const helpEmbed = new EmbedBuilder()
            .setColor(0x0099FF)
            .setTitle('Voici les commandes pour vous aider avec le préfixe ' + prefix + ':')
            .setDescription("DISCUSSION(4)``` salut | ça va ? | Oui bien merci | allez au revoir ```")
            .setFooter({ text: 'si vous avez des problèmes avec les commandes vous voulez bien me prévenir en MP LeDrFouDu3664' });

        try {
            await msg.author.send({ embeds: [helpEmbed] });
            await msg.channel.send("les commandes d'aide vous ont été envoyées en privé");
        } catch (error) {
            await msg.channel.send("Je n'ai pas pu vous envoyer l'aide en privé. Vérifiez que vos MP sont ouverts.");
        }

        console.log("la commande d'aide a été demandée");
        if (msg.deletable) await msg.delete();
    }
});

client.login(config.token).catch(err => {
    console.error("Erreur de connexion : " + err.message);
});
