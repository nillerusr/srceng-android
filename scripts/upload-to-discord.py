import disc
import sys,os

CHANNEL_ID=801092155090927626
bot = disc.disc(os.getenv('DISCORD_TOKEN'))
print(open('text').read())
bot.send(CHANNEL_ID, open('text').read())
