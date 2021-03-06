## 项目简介
Sophorose是一个仿真西门子S7-300/400系列PLC的中交互水平蜜罐系统。它完整地实现了S7和Modbus协议，具有访问设备信息、数据读写、块上传下载等功能。它还具有端口扫描检测功能。
Sophorose是基于Snap7、NpCap、MySQL数据库及其Java连接器、Tomcat服务器等工具实现的。它包括三个子系统：蜜罐服务器、数据库服务器和web服务器。子系统间通过网络连接，因此可以运行在不同的计算机上。其中蜜罐服务器要求运行环境为Windows系统，并且需要安装NpCap抓包驱动。
## 代码组成
Sophorose由五个程序组成，其中前三个归属于蜜罐服务器子系统，后两个归属于web服务器子系统。
* 数据库连接器
文件夹：PagodaTree
语言：Java
代码量：629行
功能：端口扫描检测的后一半工作；将蜜罐数据上传到数据库。
使用方法：以命令行参数“数据库URL，用户名，密码”在命令行中运行连接器。
* 蜜罐主机程序
文件夹：plc
语言：C++
代码量：869行（不包括Snap7部分）
功能：提供S7和Modbus服务，记录访问事件。
使用方法：在连接器中输入“可执行程序名称+@cmd”命令即可启动并连接，然后输入蜜罐服务器代号@蜜罐主机程序的编号。
* NpCap抓包程序
文件夹：cap
语言：C++
代码量：177行
功能：端口扫描检测的前一半工作，即驱动NpCap从操作系统网络栈中抓包。
使用方法：在连接器中输入“可执行程序名称+@cmd”命令即可启动并连接，程序会列出当前可抓包的网络接口，然后输入需要抓包的网络接口编号@蜜罐主机程序的编号。
* 网页后端
文件夹：Sardine
语言：Java
代码量：2010行
功能：用中文解释事件码的内容；端口扫描分析。
使用方法：将jar包复制到tomcat服务器的lib目录下即可。
* 网页前端
文件夹：ROOT
语言：html, jsp, js, css
功能：呈现用户界面。
使用方法：将网页文件及相关资源复制到tomcat服务器的webapps/ROOT目录下。