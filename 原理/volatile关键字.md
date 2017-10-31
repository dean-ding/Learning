1.用在多线程同步变量时， 线程为了提高效率，将某成员变量(如A)拷贝了一份（如B），线程中对A的访问其实访问的是B。只在某些动作时才进行A和B的同步。
因此存在A和B不一致的情况。volatile就是用来避免这种情况的。volatile告诉jvm，它所修饰的变量不保留拷贝，直接访问主内存中的（也就是上面说的A) 

2.在Java内存模型中，有main memory，每个线程也有自己的memory (例如寄存器)。为了性能，一个线程会在自己的memory中保持要访问的变量的副本。
这样就会出现同一个变量在某个瞬间，在一个线程的memory中的值可能与另外一个线程memory中的值，或者main memory中的值不一致的情况。 

一个变量声明为volatile，就意味着这个变量是随时会被其他线程修改的，因此不能将它cache在线程memory中。

3.Volatile一般情况下不能代替sychronized，因为volatile不能保证操作的原子性，即使只是i++，实际上也是由多个原子操作组成：read i; inc; write i,
假如多个线程同时执行i++，volatile只能保证他们操作的i是同一块内存，但依然可能出现写入脏数据的情况。如果配合Java5增加的atomic wrapper classes，
对它们的increase之类的操作就不需要sychronized。

4.volatile关键字用于声明简单类型变量，如int、float、 boolean等数据类型。如果这些简单数据类型声明为volatile，对它们的操作就会变成原子级别的。

5.在使用volatile关键字时要慎重，并不是只要简单类型变量使用volatile修饰，对这个变量的所有操作都是原子操作，当变量的值由自身的上一个决定时，
如n=n+1、n++等，volatile关键字将失效，只有当变量的值和自身上一个值无关时对该变量的操作才是原子级别的，如n = m + 1，这个就是原级别的。