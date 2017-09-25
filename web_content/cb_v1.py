#-*- coding:utf8 -*-
#author:Heascle
#version 1
#python cb_v1.py -u http://192.168.52.129/dvwa/ -f PHP.txt -t 1  -p ../com/available_ip.txt
#python cb_v1.py -u http://bbs.boardzone.cn -f PHP.txt -e php html -p ../com/available_ip.txt -t 10
#扫描到的目录，结果存放在，../com/content.txt
import argparse
from threading import Thread
from sys import stdout
from Queue import Queue
import urllib2
from time import sleep

from sys import path as syspath
from os import path as ospath
parent_dir =  ospath.dirname(ospath.dirname(ospath.abspath(__file__)))
#脚本执行文件的上一级目录
syspath.append("%s/com"%parent_dir)

from  user_agent_list import get_user_agent	
import CircularQueue
from socket import setdefaulttimeout

##特别说明，这里的代理存放在一个循环队列中，即同时使用多个代理的方式。
##主要是因为代理本身就十分不稳定，想要开多个线程还是需要使用多个代理
##所以建议前期收集的代理多点，后面发现代理不可用会丢弃代理。
##需要改进的地方：
##1.如果代理忙碌，怎么处理，直接丢弃，还是考虑放入到一个低等级的队列中
##目前处理方法是直接丢弃，但是线程多导致代理满荷，可能导致恶心循环到饿死
##2.读取出来的代理IP，给它一个数据结构，不然每次取循环队列的数据都需要
##进行字符传处理，十分浪费CPU
##3.剩余代理IP数量不要用循环队列本身的方法，不然有问题



def exit_programmer(number=-1):
	'''
	程序退出banner
	'''
	print '\n\n--------WORK OVER-----------------'
	print exit(number)

def check_args(args):
	'''
	检测参数，并向用户确认
	'''
	url =  args.url
	file = args.file
	extension = args.extension
	threads = args.threads
	proxies_file = args.proxies
	delay = args.delay
	
	print '[*]Please check your arguments..'
	print ''
	print 'URL: %s'%url
	
	print 'File name: %s' %file.name
	print 'Delay :%d'%delay
	if extension is not None:
		print 'Extension: %s' %extension
	
	#python 线程开启到20个就足够了，采用的sqlmap
	#毕竟python线程是GIL控制的，实际上还是一个线程，太多并不好
	if threads > 20 :
		threads = 20
	print 'Thread(s): %d' %threads
	
	if proxies_file is not None:
		print 'Proxies: %s' %proxies_file.name
		#l = proxies_file.readlines()
		#print l[0]
	print ''
	try:
		sure = raw_input(' ...Y  /  n').lower()
		#print ord(sure)
		if sure =='y' or sure == '':
			return url,file,extension,threads,proxies_file,delay
	except KeyboardInterrupt,e:	
		exit_programmer()
	exit_programmer()

#arg[0] threads_num : threads number
def create_threads(threads_num,queue_read,url,extension,queue_write,loop_queue):
	'''
	创建线程，并启动
	'''
	threads = []
	for x in xrange(threads_num):
		threads.append(cb_thread(END,queue_read,url,extension,queue_write,loop_queue))
	
	for t in threads:
		t.start()
	#for t in threads:
	#	t.join()
	

def dir_bruter(END,url_ex,extension,queue_read,queue_write,loop_queue):
	'''
	发送HTTP GET请求，破解网站目录
	'''
	item = queue_read.get_nowait()
	#socket.setdefaulttimeout(8)
	urls = []
	if '.' not in item :
		url = '%s/%s'%(url_ex,item)
		urls.append(url)
		if extension:
			for e in extension:
				url = '%s/%s.%s'%(url_ex,item,e)
				urls.append(url)
	else :
		url = '%s/%s'%(url_ex,item)
		urls.append(url)
	
	
	for url in urls:

		proxy_support = None
		#根据需求设置代理
		if loop_queue is not None:
			item = loop_queue.get()
			if item is  None:
				END[0] = True
				return
			#http,106.59.204.186,8118,云南
			columns = item.split(',')

			request = urllib2.Request(url=url)
			k = columns[0]
			v = '%s://%s:%s'%(columns[0],columns[1],columns[2])
			proxies = {k:v}
			#print proxies
			proxy_support = urllib2.ProxyHandler(proxies)			
			
		request = urllib2.Request(url=url,headers={'UserAgent':get_user_agent()})
	
		if proxy_support is not None:
			opener = urllib2.build_opener(HTTPErrorProcessor(),proxy_support)
		else:
			opener = urllib2.build_opener(HTTPErrorProcessor())
		urllib2.install_opener(opener)
		try :
			response = urllib2.urlopen(request)
			html = response.read()
			if len(html):
				stdout.write ( "\r                                                        ")
				print( "\r\n[+]%d => %s"%(response.code,url))
				queue_write.put( ("[%d]==> %s" %(response.code,url)+'\n') )
			else:
				stdout.write ( "\r                                                        ")
				print( "\r\n[?]%d => %s"%(response.code,url))
				queue_write.put( ("[%d]=?> %s" %(response.code,url)+'\n') )
		except urllib2.HTTPError,e:
			#print '[2]%s'%e
			if e.code < 500 and e.code >399:
				continue
			print( "\r\n[-]%d --> %s"%(response.code,url))
			queue_write.put( ("[%d]--> %s" %(response.code,url)+'\n') )	
		except urllib2.URLError,e:
			print "\r\n[!]%s"%'Discard one proxy ip address'
			if loop_queue is not None:
				loop_queue.delete(item)
			queue_read.put(item)
		except Exception,e:
			print '[?]%s'%e
			


#将取得的有效目录及文件写入文件中，记录下来
def write_file(END,queue_write):
	#global END
	fip = open("../com/content.txt", "a")
	
	while  not END[0]:
		if not queue_write.empty():
			str = queue_write.get_nowait()
			fip.writelines(str)
			
		else:
			sleep(0.1)
			#count +=1
	fip.close()

#线程类	
class cb_thread(Thread):
	#global END
	def __init__(self,END,queue_read,url,extension,queue_write,loop_queue):
		Thread.__init__(self)
		self.END = END
		
		self.queue_read = queue_read
		self.url = url
		self.extension = extension
		self.queue_write = queue_write
		self.loop_queue = loop_queue
	def run(self):
		#stdout.write('1'+'\n\r')
		
		while not self.queue_read.empty() and not self.END[0]:
			
			#item = self.queue_read.get_nowait()
			dir_bruter(END,self.url,self.extension,self.queue_read,queue_write,loop_queue)
			#stdout.write(str)
			

##put data	to the queue_read
def queue_put(queue_read,items):
	'''
	将读取到的后缀放入读队列中
	'''
	for item in items:
		item = item.rstrip()
		if item == '':
			continue
		queue_read.put(item)

def loop_queue_put(loop_queue,proxies_items):
	for item in proxies_items:
		item = item.rstrip()
		if item == '':
			continue
		loop_queue.put(item)
		
		
def mission_hint(END,amount,queue_read,loop_queue):
	'''
	提示当前任务进度
	'''
	while not END[0] :
		left = queue_read.qsize()
		per = float(float(amount-left)/float(amount))*100
		if loop_queue is not None:
			if loop_queue.qsize != 0:
				stdout.write('\r %8d LEFT -  %8d AMOUNT %5.2f%s - %d Proxies '%(left,amount,per,'%',loop_queue.qsize()) )
			else:
				END[0] = True
				return
		else:
			stdout.write('\r %8d LEFT -  %8d AMOUNT %5.2f%s'%(left,amount,per,'%') )
		#stdout.write( '\r\n Proxies number: %d'%loop_queue.qsize())
		sleep(1)
		
		if left == 0:
			END[0] = True
			stdout.write('\r\n'+'-----------------WORK OVER---------------------') 	


class HTTPErrorProcessor(urllib2.HTTPErrorProcessor):
	'''
	对HTTP错误处理类重写，获取到重定向301、302时不要自动跟踪
	'''
	def http_response(self, request, response):
		code, msg, hdrs = response.code, response.msg, response.info()

		# only add this line to stop 302 redirection.
		if code in (301,302) : return response

		if not (200 <= code < 300):
			response = self.parent.error(
				'http', request, response, code, msg, hdrs)
		return response

	https_response = http_response
			
#程序的终止标志			
END = []	
END.append(False)
if __name__ == "__main__":
	
	'''
	1.url
	2.filename
	3.extension   php  asp
	4.threads
	'''
	parser = argparse.ArgumentParser(description="Tools for bruting web contents")

	##url
	parser.add_argument('-u','--url',type=str,required=True,help='Set target\'s url')
	##delay
	parser.add_argument('-d','--delay',type=int,required=False,help='Set the socket timeout max time',default=5)
	##filename
	parser.add_argument('-f','--file',type=argparse.FileType('rb'),required=True,help="Content wordlist")# --files
	##extension
	parser.add_argument('-e','--extension',nargs='*',required=False,help="Type php ,asp  ..etc..")
	##thread
	parser.add_argument('-t','--threads',type=int,required=False,default=1,help="the programer's threads,default is 1")
	##resume
	#parser.add_argument('-r','--resume',type=bool,required=False,default=False,help='If ')
	#use proxy to connect the target url
	parser.add_argument('-p','--proxies',type=argparse.FileType('rb'),required=False,help="The file of the proxies")
	
	try:
		args = parser.parse_args()
	except IOError,e:
		print e
		exit_programmer()

	url,file,extension,threads,proxies_file,delay = check_args(args)
	
	setdefaulttimeout(delay)
	
	#this queue is to load the items file :[]
	queue_read = Queue()
	#this queue is to write the url found to a file : content.txt
	queue_write = Queue()
	#this queue is for store proxy ip
	loop_queue = None
	if proxies_file is not None:
		loop_queue = CircularQueue.CircularQueue()
	items = file.readlines()
	
	
	queue_put(queue_read,items)
	if proxies_file is not None:
		proxies_items = proxies_file.readlines()
		loop_queue_put(loop_queue,proxies_items)
	
	amount = queue_read.qsize()
	Thread(target=mission_hint,args=(END,amount,queue_read,loop_queue) ).start()
	Thread(target=write_file,args=(END,queue_write), ).start()
	
	
	
	create_threads(threads,queue_read,url,extension,queue_write,loop_queue)
	
	

	#监听用户键盘输入，如果为[Ctrl+C]则退出
	while not END[0]:
		try:
			sleep(1)
		except KeyboardInterrupt,e:
			END[0] = True
			print '[*]end by KeyboardInterrupt...'
			print '--------------wait -------------'
			#exit(0)
	exit_programmer()
		
