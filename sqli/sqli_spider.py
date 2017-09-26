#-*- coding:u8 -*-
#爬取百度
#测试地址是否存在注入点
#---等下需要优化的，验证地址的地方添加多线程，考虑添加 argparse
##结果存放在 ../com/sqli_baidu_url.txt
import urllib2
from bs4 import BeautifulSoup as bs
#from os import getcwd

from sys import path as syspath
from os import path as ospath
parent_dir =  ospath.dirname(ospath.dirname(ospath.abspath(__file__)))
#脚本执行文件的上一级目录
print "%s/com"%parent_dir
syspath.append("%s/com"%parent_dir)
#添加公共目录
from user_agent_list import get_user_agent
from re import compile
from re import findall
from threading import Thread
class GetTestURLThread(Thread):
    def __init__(self,wd,pages):
        self.wd = wd
        self.pages
    def run(self):
        getTestURL

def confirm_url(url,keywords='.php?id='):
    '''
    查看取得的url是否符合要求\n
    return 符合要求应该大于0
    '''
    special_charater_list = ['.','?','+','|']
	#对其中的特殊字符添加转义\
    for c in special_charater_list:
        position = 0
        while keywords.find(c,position)>-1:
            length = len(keywords)
            position = keywords.find(c,position)
            if position==0 :
                keywords = '\\'+keywords
            elif position==length-1:
                keywords = keywords[0:position-1] + '\\' + keywords[-1]
            else:
                keywords = keywords[0:position] + '\\' + keywords[position:]
            position+=2
	
    #print len(findall(keywords,url))
    return len(findall(keywords,url))

def getTestURL(wd='inurl:.php?id=',pages=1,delay=5,keywords='.php?id='):
    '''
    page 为指定爬取的页数 \n
    wd 搜索关键词 \n
    pn 0 [1-9] 10 [11-20] 20 [21-30]  超出页数 [1-9]\n
    '''
    pn = 0
    count = 0 #实际抓取页数
    overpages = 0 #是否超出最大页数

    fo = open('%s/com/sqli_baidu_url.txt'%parent_dir,'w')
    fo.close()
    with open('%s/com/sqli_baidu_url.txt'%parent_dir,'a+') as fo:
		for p in xrange(pages):
		   
			url_explorer_raw = 'https://www.baidu.com/s?wd=!@#&pn=#@!'
			pattern_one = compile('!@#')
			pattern_two = compile('#@!')
			url_explorer = pattern_one.sub(wd,url_explorer_raw)
			url_explorer = pattern_two.sub('%d'%pn,url_explorer)

			headers = {'User-Agent':get_user_agent()}

			request = urllib2.Request(url_explorer,headers=headers)
			opener = urllib2.build_opener()
			try:
				response = opener.open(request)
			except Exception,e:
				print e
				continue
			#['__doc__', '__init__', '__iter__', '__module__', '__repr__', 'close', 'code', 'fileno', 'fp', 'getcode', 'geturl', 'headers', 'info', 'msg', 'next', 'read', 'readline', 'readlines', 'url']
			html = response.read()

			soup = bs(html,'html.parser')
			items = soup.findAll(name='div',attrs={'id':compile('\d?'),'class':'result c-container '})
			#抓取页面结果的超链接，之后取得链接的真正URL地址
			for item in items:
				id =  item.attrs.get('id')
				print id
				if id == '1':
					overpages=1 if overpages==0 else 2
				if overpages==2:
					print '[!]Overpages'
					return
				#break if overpages==2 else pass
				url_item_raw = item.h3.a.attrs.get('href')
				request = urllib2.Request(url_item_raw,headers=headers)
				opener = urllib2.build_opener()
				try:
					response = opener.open(request,timeout=5)
					url_item = response.geturl()
					print url_item
					
					if confirm_url(url_item,keywords) ==0 :
						print 'continue'
						continue
					fo.write(url_item+'\n')
					
				except:
					print '[-]Discard'
			fo.flush()   
			pn += 10
		#fo.close()
if __name__ == '__main__':
  
    #getTestURL()
    getTestURL(wd='inurl:.php?id=',pages=1000,delay=5,keywords='.php?id=')
    #print confirm_url('http://www.boardzone.cn/')
