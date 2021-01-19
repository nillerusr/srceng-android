# -*- coding: utf-8 -*-
import requests,json,time,random,os,traceback,sys,websocket,threading
from utils import *

class disc:
	def __init__(self, token):
		self.token = token
		self.headers={'Authorization':'Bot ' + self.token}
		self.id = 0

	def get(self, method, **args):
		ret = requests.get('https://discord.com/api/v7/'+method, data=args, headers=self.headers)
		try:
			return D(ret.json())
		except:
			return ret.text

	def post(self, method, **args):
		ret = requests.post('https://discord.com/api/v7/'+method, data=args, headers=self.headers)
		try:
			return D(ret.json())
		except:
			return ret.text

	def send(self, chat_id, text):
		return self.post('channels/'+str(chat_id)+'/messages', content=text)

	def gw_loop(self, func):
		identify = { "op":2, "d": { "token": self.token, "intents": 513, "properties": { "$os": "linux", "$browser": "my_library", "$device": "my_library" } } }
		resume = {"op": 6, "d": { "token": self.token, "session_id": "", "seq": 0} }
		url = self.get('gateway').url
		self.last_sequence = 0
		self.heartbeat_interval=0
		self.session_id = ''
		heartbeat_time = 0

		def on_error(ws, error):
			print('gw error: ',error)

		def on_close(ws):
			print('on_close')

		def on_open(ws):
			print('on_open')
			if self.session_id:
				resume['session_id'] = self.session_id
				resume['seq'] = self.last_sequence
				ws.send(json.dumps(resume))
			else:
				ws.send(json.dumps(identify))

		def on_message(ws, message):
			try:
				resp=D(json.loads(str(message)))
				if not resp.s: last_sequence = 0
				else: last_sequence = resp.s

				if 't' in resp._dict.keys() and resp.t == 'READY':
					self.id = resp.d.user.id
					session_id = resp.d.session_id
				elif resp.op == 10:
					self.heartbeat_interval = int(resp.d.heartbeat_interval)/1000
				elif not ('MESSAGE' in resp.t and resp.d.author.id == self.id ):
					func(resp)
			except Exception as e:
				print(e)

		ws = websocket.WebSocketApp(url, on_message = on_message, on_error = on_error, on_close = on_close)
		ws.on_open = on_open

		def loop(ws):
			while True:
				try:
					ws.run_forever()
				except Excption as e:
					print('loop error '+str(e))

		wst = threading.Thread(target=loop, args=(ws,))
		wst.daemon = True
		wst.start()

		while True:
			if self.heartbeat_interval and time.time() - heartbeat_time >= self.heartbeat_interval:
				if ws.sock.connected:
					hb=json.dumps({'op':1, 'd':self.last_sequence})
					#print('sending heartbeat: '+hb)
					heartbeat_time = time.time()
					ws.send(hb)

			time.sleep(0.01)
