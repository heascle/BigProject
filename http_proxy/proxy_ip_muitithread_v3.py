
# -*- coding:utf-8 -*-
#增加的指定延迟 +做了一些完善
#对HTTPS做了修改，但是目前发现并没有什么好的方法可以 检测HTTPS代理IP
#python proxy_ip_muitithread_v3.py -t 20 -d 8 -p 3
###bug  文件目录
import urllib
import urllib2
import re
import socket
import threading
from Queue import Queue
import argparse
from time import sleep
from bs4 import BeautifulSoup as BS
from sys import stdout

from os import path as ospath
from sys import path as syspath
parent_dir =  ospath.dirname(ospath.dirname(ospath.abspath(__file__)))
#import sys
	#get each page's poxy ip ,include	"protocol", "ip"  "port" and  "site
	#获取每一页
def get_proxy_ip(page_count):
	#create a file ,or overwrite the old one
	fobo=open('%s/com/proxy_list.txt'%parent_dir, 'w')
	fobo.close()
	for page in range(1,page_count+1):
		url = "http://www.xicidaili.com/nn/" + str(page)
		headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0'}
		
		fobo=open('%s/com/proxy_list.txt'%parent_dir, 'a')
		try:
			request = urllib2.Request(url, headers=headers)
			response = urllib2.urlopen(request)
			content = response.read()
			
			soup = BS(str(content),'html.parser')
			
			#
			trs = soup.find_all(name='tr',attrs={'class':re.compile('|[^odd]')})
			
			for tr in trs:
				try:
					soup_td = BS(str(tr),'html.parser')
					tds = soup_td(name='td',attrs={})
					ip = tds[1].string							#ip
					port = tds[2].string						#port
					site = tds[3].a.string.encode('utf8') 		#site
					#tds[4]										#anonymous
					protocol = tds[5].string.lower()			#protocol
					if tds is not None:
						#fobo.write(tds[5].string+","+tds[1].string+","+tds[2].string+","+tds[3].a.string)
						fobo.write(protocol);
						fobo.write(',');
						fobo.write(ip);
						fobo.write(',');
						fobo.write(port);
						fobo.write(',');
						fobo.write(site);
						fobo.write('\n')
					else:
						pass
				except :
					continue
			#fobo.close()
		except Exception, e:
			
			if hasattr(e, "code"):
				print e.code
			if hasattr(e, "reason"):
				print e.reason
		finally:
			fobo.close()



class MyThread(threading.Thread):
	
	def __init__(self,queue_read):
		global END
		threading.Thread.__init__(self)
		self._queue_read = queue_read
		#self._queue_write = _queue_write
	def run(self):
		while not self._queue_read.empty():
			if END :
				break;
			proxyip = self._queue_read.get_nowait()
			check_ip(proxyip)
	
	
	
def read_file():
	global queue_read
	fproxyip = open("%s/com/proxy_list.txt"%parent_dir, "r")
	proxy=fproxyip.readlines()
	for proxyip in proxy:
		proxyip=proxyip.strip('\n').split(',')
		queue_read.put(proxyip)
	fproxyip.close()
	
#read the ip list which wait for verfitication,and put them to the queue_read
def write_file():
	global queue_write
	global END
	fip = open("%s/com/available_ip.txt"%parent_dir, "a")

	while  not END:
		if not queue_write.empty():
			str = queue_write.get_nowait()
			fip.writelines(str)
			fip.flush()
		else:
			sleep(0.1)
			#count +=1
	fip.close()
def check_ip(proxyip):
	url_http = 'http://www.baidu.com/s?ie=UTF-8&wd=%E6%9F%A5%E8%AF%A2ip'
	url_https = 'https://www.rong360.com/'
	#fip = open("ip.txt", "w")
	global socket_delay
	global output_type
	socket.setdefaulttimeout(socket_delay)
	try:
		host = proxyip[0]+"://"+proxyip[1]+":"+proxyip[2]
		proxy_host = {proxyip[0]:host}
		# print proxy_temp
		if proxyip[0] == 'http' :
			html = urllib.urlopen(url_http,proxies=proxy_host).read()
			if html:
			#print html
				real_ip =  re.findall(r'fk=\"(.*?)\"',html)
				#real_ip = real_ip.replace('\'','').replace('[','').replace(']','')
				real_ip = real_ip[0]
				if real_ip == proxyip[1]:
					#stdout.write ('[^__^]'+'\n')
					#stdout.write ("[+]avaiable: "+host+'\n')
					
					if output_type==0:
						queue_write.put( proxyip[0]+","+proxyip[1]+","+proxyip[2]+","+proxyip[3]+'\n')
					else:
						queue_write.put( proxyip[0]+"://"+proxyip[1]+":"+proxyip[2]+'\n')
				else:
					#stdout.write( "[- -]useless: " + host+'\n')
					pass
			else:
				#stdout.write( "[-   -]useless: " +host+'\n')
				pass
		else:
			status_code = requests.get(url_https,proxies=proxy_host).status_code
			if status_code == 200:
				#stdout.write( '[^__^]'+'\n')
				#stdout.write( "[+]avaiable: "+host+'\n')
				#queue_write.put(host+' , '+proxyip[3]+'\n')
				pass
			else:
				#stdout.write( "[- -]useless: " + host+'\n'	)
				pass
		
	except Exception,e:
		#stdout.write( "[ -    -]useless: " + host+'\n')
		pass
#checkIP()

AMOUNT = 0
LEFT = 0
def mission_hint():
	global AMOUNT 
	global LEFT 
	global queue_read;
	global END
	while not END :
		LEFT = queue_read.qsize()
		per = float(float(AMOUNT-LEFT)/float(AMOUNT))*100
		stdout.write('\r'+' %8d LEFT -  %8d AMOUNT %3.f%s'%(LEFT,AMOUNT,per,'%') )
		sleep(0.1)
		if LEFT == 0:
			END = True
			stdout.write('\r\n'+'-----------------WORK OVER---------------------') 

if __name__ == '__main__':

	print """
 ____  ____   _____  ____   __  ___ ____            _____ 
|  _ \|  _ \ / _ \ \/ /\ \ / / |_ _|  _ \  __   __ |___ / 
| |_) | |_) | | | \  /  \ V /   | || |_) | \ \ / /   |_ \ 
|  __/|  _ <| |_| /  \   | |    | ||  __/   \ V /   ___) |
|_|   |_| \_\\___/_/\_\  |_|   |___|_|       \_/   |____/ 
                                                          
							author:Heascle version:3
	"""
	queue_read = Queue()
	queue_write = Queue()
	socket_delay = 5
	END = False
	parser = argparse.ArgumentParser()
	help = 'Please input pages\' amount'
	parser.add_argument('-p','--pages',help=help,type=int,default=1)
	help = 'Thread\'s numbers'
	parser.add_argument('-t','--threads',help=help,type=int,default=1)
	help = 'Socket\'s block wait time'
	parser.add_argument('-d','--delays',help=help,type=int,default=1)
	help = 'The output data type'
	parser.add_argument('--type',help=help,type=int,default=0)
	
	args = parser.parse_args()
	
	page_count = args.pages
	thread_count = args.threads
	socket_delay = args.delays
	output_type = args.type
	
	print '[*]Set page_count as %d'%page_count

	if thread_count > 20:
		'[*]Set thread number as 20'
		thread_count = 20
	else:
		print '[*]Set thread_count as %d'%thread_count
	print '[*]Set socket_delay as %d'%socket_delay
	
	
	get_proxy_ip(page_count)

	read_file()
	AMOUNT = queue_read.qsize()
	
	threading.Thread(target=mission_hint).start()
	#print 'page_count:%d'%page_count
	#print 'thread_count:%d'%thread_count
	threading.Thread(target=write_file,).start()
	
	threads = []
	for x in xrange(thread_count):
		threads.append(MyThread(queue_read=queue_read))
	for t in threads:
		t.start()
	#for t in threads:
	#	t.join()
	
	#print "checking proxy ip \n\r"
	
	
	
	#监听用户键盘输入，如果为[Ctrl+C]则退出
	
	while not END:
		try:
			sleep(1)
		except KeyboardInterrupt,e:
			END = True
			print '[*]end by KeyboardInterrupt...'
			print '--------------wait -------------'
			break
		
