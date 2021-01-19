import sys, json, requests, random, string
from threading import Thread

class DictWrap:
	"this wraps dict into object-like structure (dict.child1.child2)"
	def __init__(self,d):
		if not isinstance(d,dict):
			t = "dictwrap from " + str(d) + " " +str(type(d))
			print(t)
			raise Exception(t)
		self.__dict__['_dict'] = d

	def __getattr__(self,name):
		# shortcut for json
		if name == '_json':
			return json.dumps(unicodeWrap(self._dict), ensure_ascii = False)
		try:
			return D(self._dict[name])
		except KeyError:
			raise AttributeError(name)
	def __getitem__(self,name):
		if isinstance(name, int):
			return self._dict.keys()[name]
		return D(self._dict[name])
	def __setitem__(self,x,y):
		return self._dict.__setitem__(x,todict(y))
	def __setattr__(self,x,y):
		return self._dict.__setitem__(x,todict(y))
	def __repr__(self):
		return self._dict.__repr__()
	def __str__(self):
		return self._dict.__str__()
	def __dir__(self):
		return self._dict.__dir__()
	def __contains__(self,k):
		return self._dict.__contains__(k)
	def __iter__(self):
		for key in self._dict:
			yield (key, self._dict[key])

def todict(x):
	if isinstance(x,DictWrap):
		return x._dict
	return x

def tostr(x):
	if sys.version_info[0] == 2 and isinstance(x,unicode):
		return x.encode('utf-8')
	return x

def D(x = None, **kw):
	if x == None:
		x = kw
	if isinstance(x,dict):
		return DictWrap(x)
	elif isinstance(x,list):
		return [D(l) for l in x]
	return tostr(x)

def download_file_single(url, filename, formats=[]):
	try:
		with requests.get(url, stream=True) as r:
			h=r.headers['Content-Type'].split('/')[1]
			if formats and h not in formats:
				return ''
			elif formats:
				filename=filename+'.'+h
			print('downloading file '+filename)
			r.raise_for_status()
			with open(filename, 'wb') as f:
				for chunk in r.iter_content(chunk_size=8192):
					if chunk: f.write(chunk)
	except Exception as e:
		return ''
	return filename


#def download_files(urls, formats=[]):
#	threads_max=10
#	threads_n=0
#	for i in urls:
#		


def load_json(filename):
	return D(json.loads(open(filename, 'r').read()))

def args(message):
	return ' '.join(message.text.split(' ')[1:])

def get_random_string(length):
	letters = string.ascii_lowercase
	result_str = ''.join(random.choice(letters) for i in range(length))
	return result_str

