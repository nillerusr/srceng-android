import disc
import sys,os

text = ' '.join(sys.argv[1::]).replace('\\n', '\n')
chanid=os.getenv('CHANNEL_ID')
bot = disc.disc(os.getenv('DISCORD_TOKEN'))
bot.send(chanid, text)
