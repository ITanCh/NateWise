## NataWise说明书   
### tianchi  
-------------

###NataWise简介  
NataWise是利用深度优先算法(DFS)对Android应用UI进行自动化探索测试的工具。该工具不依赖于Android Instrumentation工具，全程通过adb命令和UIautomator的相关的功能进行状态的获取和驱动。不需要对APP进行重签名等处理，但需要获得被测试应用的包名和启动Activity的名字。

###NataWise使用方法  

1. 运行环境：JAVA运行环境、android SDK(保证命令`adb devices`能够正确显示设备序号)。 

2. 首先保证被测试APP已经安装至Android设备，同时保证移动设备与计算机相连。

3. 运行命令   
```   
$ java -jar NataWise.jar [pkg name] [activity name] [adb path] [out path] 
```   
pkg name: 被测试应用包名。  
activity name: launcher Acitivity名称。
adb path: adb 的绝对路径。
out paht: 输出文件位置。

4. 运行结果。运行时间可能较长，最后生成结果报告web/，每个手机在web目录下都一个单独的报告，其中**index.html**和**actnet.html**文件为最后结果的展示。