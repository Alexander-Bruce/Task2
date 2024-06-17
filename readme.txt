---
# Task2

## 项目概述
Task2 是一个基于UDP协议的简单网络通信项目，包括了一个服务器端（Server）和一个客户端（Client）。

## 运行环境
- Java 开发环境
- 支持 Java 的操作系统（Windows/Linux/macOS）

### 服务器端（Server）
1. **IP 地址和端口号配置**
   - 默认使用本地 IP 地址 `127.0.0.1` 和端口号 `12345`，可根据需要修改。
   - 在 `Server.java` 文件中修改 `serverIP` 和 `serverPort` 变量。
2. **运行服务器**
Windows
    编译 Server.java 文件
    打开命令提示符（Command Prompt），导航到存放 Server.java 文件的目录。
	cd/src
使用 javac 命令编译 Server.java 文件。
javac Server.java
如果编译成功，将生成 Server.class 文件。
在同一命令提示符窗口中，运行以下命令来启动 Server。
    java Server
Server 程序将开始执行，并在命令提示符窗口中显示输出信息。
Linux / macOS
    -编译 Server.java 文件
    -打开终端（Terminal），导航到存放 Server.java 文件的目录。
cd /src
-使用 javac 命令编译 Server.java 文件。
javac Server.java
如果编译成功，将生成 Server.class 文件。
-运行 Server
在同一终端窗口中，运行以下命令来启动 Server。
java Server
Server 程序将开始执行，并在终端窗口中显示输出信息。

### 客户端（Client）

1. **IP 地址和端口号配置**
   - 默认使用服务器的本地 IP 地址 `127.0.0.1` 和端口号 `12345`。
   - 在 `Client.java` 文件中修改 `serverIP` 和 `serverPort` 变量。

2. **运行客户端**
   Windows
    编译 Client.java 文件
    打开命令提示符（Command Prompt），导航到存放 Client.java 文件的目录。
	cd/src
使用 javac 命令编译 Client.java 文件。
javac Client.java
如果编译成功，将生成 Client.class 文件。
在同一命令提示符窗口中，运行以下命令来启动 Server。
    java Client
Client 程序将开始执行，并在命令提示符窗口中显示输出信息。
Linux / macOS
    -编译 Client.java 文件
    -打开终端（Terminal），导航到存放 Client.java 文件的目录。
cd /src
-使用 javac 命令编译 Client.java 文件。
javac Client.java
如果编译成功，将生成 Server.class 文件。
-运行 Client
在同一终端窗口中，运行以下命令来启动 Client。
java Client
Client程序将开始执行，并在终端窗口中显示输出信息。


## 使用方法
1. **启动服务器**
   - 运行 `Server.java` 文件以启动服务器。
   - 服务器将监听指定端口，并接收来自客户端的消息。
2. **启动客户端**
   - 运行 `Client.java` 文件以启动客户端。
   - 客户端将连接到指定的服务器，并发送一系列消息。
3. **查看输出**
   - 在客户端和服务器的控制台中，可以查看到消息的发送、接收状态以及网络统计信息（如丢包率、RTT等）。

## 注意事项
- 确保服务器和客户端在运行时处于相同的网络环境中，或能够相互访问。
- 如果需要修改消息发送的频率、数量等参数，可以在代码中适当调整。

## 版权和许可
该项目仅用于学习和教育目的。欢迎任何人根据需要修改和使用。
---
