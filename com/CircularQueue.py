# -*- coding:utf-8 -*-
from Queue import Queue
from threading import RLock

def synchronized_with_attr(lock_name):
    
    def decorator(method):
			
        def synced_method(self, *args, **kws):
            lock = getattr(self, lock_name)
            with lock:
                return method(self, *args, **kws)
                
        return synced_method
		
    return decorator
    
class CircularQueue():
	def __init__(self):
		self.lock = RLock()
		
		self._queue_a = Queue()
		self._queue_b = Queue()
		self._pointer = self._queue_a
		self._delete = []
		
		
	#获取与当前指针不同的队列	
	def __get_other(self):
		if self._pointer == self._queue_a : return self._queue_b
		else : return self._queue_a
		
	#指针改变引用对象
	def __change_pointer(self):
		self._pointer = self.__get_other()
	
	def qsize(self):
		#if 
		result = self._queue_a.qsize() + self._queue_b.qsize()
		return result
	'''
	def qsize_a(self):
		return self._queue_a.qsize
	def qsize_b(self):
		return self._queue_b.qsize
	'''
	@synchronized_with_attr("lock")	
	def put(self,o):
		self.__get_other().put(o)
	
	@synchronized_with_attr("lock")
	def get(self,origin=True):
		#print origin
		if self.qsize() == 0:
			return None
		if self._pointer.qsize() == 0 :
			self.__change_pointer()
		result = self._pointer.get()
		if len(self._delete) !=0 :#result==self._delete:
			try : 
				self._delete.remove(result)
				result = self.get(False)
			except ValueError,e:
				pass
				#self._delete = None
			
		if origin:
			self.put(result)
		return result
		
	@synchronized_with_attr("lock")
	def delete(self,delete):
		self._delete.append(delete)
'''
from sys import stdout
import threading
def g(q):
	stdout.write( '%d\n\r'%q.get())
	
		
if __name__ == "__main__":
	q = CircularQueue()
	
	print 'size:%d'%q.qsize()
	for x in xrange(4):
		q.put(x)
	print 'size:%d'%q.qsize()
	for x in xrange(10):
		print q.get()
	
		#threading.Thread(target=g,args=(q,)).start()
	q.delete(1)
	#q.delete(2)
	
	for x in xrange(10):
		print q.get()
	print 'size:%d'%q.qsize()
'''
	
	
	
	
