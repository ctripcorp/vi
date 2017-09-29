Cornerstone（基石）
================


[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Cornerstone（基石）是携程框架部门研发的内部可视化组件VI的开源版本，VI是validate internal的缩写，字面意思可以理解为“内部验证”,你也可以将VI理解为应用的窗口。 VI的一个目标就是把原本的应用黑盒子，变成一个透明的盒子。
在携程，VI主要是一个应用及应用相关环境的可视化工具，和应用健康状态及启动管理的工具（目前已是ctrip集团健康检测和点火标准）。
将开源版本命名为cornerstone是希望在这个组件里解决更多的基础问题，让开发和运营更简单和高效。

# Features
* **无中心，无强依赖(仅强依赖slf4j和gson)**
* **小而独立**
* **寄生在应用里**
* **WEB展示和数据收集都在一个组件里**
* **数据实时**
* **数据覆盖面广（目标是暴露应用相关的一切）**
* **对应用影响小（不访问时，不消耗运行资源）**
* **极强扩展性**
* **启动管理**
* **缓存管理**
* **日志查看**
* **GC日志分析**
* **多环境配置管理**
* **大部分jdk tool web化**
* **支持无埋点实时metrics**
* **支持无埋点实时"debug"**


# 和JMX的关系
* **cornerstone里使用大量标准jmx mbean获取数据，并提供自动将特有的component status类(极易使用)转换为mbean并注册（大大简化mbean开发成本）。**
* **和jconsole以及jvisualvm 比的优势：**
   * 使用公司sso认证
   * 无需应用开启额外端口.
   * 图形展示上更灵活
   * 数据更丰富
 
 
# 何时需要Cornerstone
当想了解应用健康状况。当想了解应用启动过程。当想了解使用组件的运行情况时。当需要定位问题时。当你需要暴露应用运行数据时。当想了解应用各类资源占用情况时。当想查找各类配置时。当需要给应用性能调优时 。。。
总之，你所关心应用运行的一切，都可以通过这个窗口了解和控制。

# Screenshots
![默认界面](https://raw.githubusercontent.com/ctripcorp/cornerstone/master/doc/imgs/cs-main.png)
![启动日志](https://raw.githubusercontent.com/ctripcorp/cornerstone/master/doc/imgs/cs-ignite.png)
![实时metrics](https://raw.githubusercontent.com/ctripcorp/cornerstone/master/doc/imgs/cs-metrics.png)
![动态埋点](https://raw.githubusercontent.com/ctripcorp/cornerstone/master/doc/imgs/cs-debug.png)
![GC日志分析](https://raw.githubusercontent.com/ctripcorp/cornerstone/master/doc/imgs/cs-gc.png)

# License
The project is licensed under the [Apache 2 license](https://github.com/ctripcorp/apollo/blob/master/LICENSE).
