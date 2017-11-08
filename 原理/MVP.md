MVP: Model,View,Presenter.

1.
    Presenter起到的其实就是一个粘合剂的角色。

        它调度了UI逻辑和数据逻辑，然而UI逻辑和数据逻辑的具体实现，Presenter是不用关心的，
        只需要处理好如何调度，和状态处理即可。