const Discord = require('discord.js');
const client = new Discord.Client();
const config = require ("./config.json")
client.on('ready', () => {
    console.log(`Connecté en tant que ${client.user.tag}!`);
    client.user.setStatus("online")
  client.user.setActivity(" je me fais coder" , {type: 2})
  
  
  
  });
  
  client.on('message', msg => {
    if (msg.content === 'r ping') {
      msg.reply(' Pong!');
    }
  });
  
  client.on('message', msg => {
      if (msg.content === 'r salut') {
       msg.reply('salut!');
      }
  });
  
  client.on('message', msg => {
      if (msg.content === 'r ça va ?') {
       msg.reply('oui et toi ?');
      }
  });
  
  client.on('message', msg => {
      if (msg.content === 'r Oui bien merci') {
       msg.reply('de rien!');
      }
    });
  
  client.on('message', msg => {
      if (msg.content === 'r allez au revoir') {
        msg.reply('oui au revoir et à bientôt!');
      }
    });
  
    client.on("message", msg => {
      if (msg.content === 'r help') {
          var help_enbed = new Discord.RichEmbed()
          .setTitle('voici les commandes pour vous aider avec le préfixe r:')
          .setDescription('vous pouvez utiliser mes commandes ')
          .setDescription("DISSUSSION(4)``` salut |  ça va ? |  Oui bien merci |  allez au revoir```")
          .setFooter('si vous avez des problèmes avec les commandes vous voulez bien me prévenir en MP LeDrFouDu3664')
          msg.author.send(help_enbed)
          msg.channel.sendMessage("les commandes d'aide vous ont été envoyé en privé")
          console.log("les commande d'aide à été demandé") 
          msg.delete();
          }
        });
  
        client.login(config.token);
