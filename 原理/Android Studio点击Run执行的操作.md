点击Run按钮后，可以看到的效果是当前执行的Module运行。

1、Module是Android Phone则启动APP

    1）检查项目以及基本配置，比如多语言是否完整，代码错误等
    2）编译。使用Gradle build编译多个task
    3）如果有需要的话加上混淆
    4）打包APK，如有需要还要加上签名
    5）使用adb命令把打包好的APK传入到手机中，然后安装，启动Activity
    
2、Module是Java Library则找到Main入口开始运行

3、Module是Android Library