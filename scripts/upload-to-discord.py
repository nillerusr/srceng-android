import disc
import sys,os

print(sys.argv[1])

CHANNEL_ID=801092155090927626
bot = disc.disc(os.getenv('DISCORD_TOKEN'))
bot.send(CHANNEL_ID, sys.argv[1])
