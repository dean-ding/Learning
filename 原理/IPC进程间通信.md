IPC(Inter-Process-Communication):进程间通信或者跨进程通信

一 Binder机制：

    Binder机制由三部分组成：
        1.Client;
        2.Server;
        3.ServiceManager。
    
    三部分组件之间的关系:
        1.Client、Server、ServiceManager均在用户空间中实现，而Binder驱动程序则是在内核空间中实现的；
        2.在Binder通信中，Server进程先注册一些Service到ServiceManager中，ServiceManager负责管理
          这些Service并向Client提供相关的接口；
        3.Client进程要和某一个具体的Service通信，必须先从ServiceManager中获取该Service的相关信息，
          Client根据得到的Service信息与Service所在的Server进程建立通信，之后Clent就可以与Service进行交互了；
        4.Binder驱动程序提供设备文件/dev/binder与用户空间进行交互，Client、Server和ServiceManager
          通过open和ioctl文件操作函数与Binder驱动程序进行通信；
        5.Client、Server、ServiceManager三者之间的交互都是基于Binder通信的，所以通过任意两者这件的关系，
          都可以解释Binder的机制。