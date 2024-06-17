import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final DatagramSocket socket;  // 定义UDP套接字
    private final ExecutorService executorService;  // 线程池，用于处理客户端请求

    public Server(int port) throws Exception {
        socket = new DatagramSocket(port);  // 使用指定端口初始化UDP套接字
        executorService = Executors.newCachedThreadPool();  // 创建一个可缓存线程池
        System.out.println("服务器在已经运行在 " + port);  // 打印服务器启动信息
    }

    public void run() {
        try {
            while (true) {  // 持续接收数据包
                byte[] receiveBuffer = new byte[64];  // 定义接收缓冲区
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);  // 创建接收数据包
                socket.receive(receivePacket);  // 接收数据包
                executorService.execute(new ClientHandler(receivePacket));  // 为每个数据包创建一个新的线程进行处理
            }
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());  // 打印错误信息
            executorService.shutdown();  // 关闭线程池
        } finally {
            socket.close();  // 关闭UDP套接字
        }
    }

    private class ClientHandler implements Runnable {
        private final DatagramPacket packet;  // 定义接收到的数据包

        public ClientHandler(DatagramPacket packet) {
            this.packet = packet;  // 初始化数据包
        }

        @Override
        public void run() {
            try {
                InetAddress clientAddress = packet.getAddress();  // 获取客户端IP地址
                int clientPort = packet.getPort();  // 获取客户端端口号

                // 处理接收到的数据
                byte[] data = packet.getData();  // 获取数据包中的数据

                byte[] first8Bytes = Arrays.copyOfRange(data, 0, 8);  // 读取前8个字节
                String str = new String(first8Bytes, StandardCharsets.UTF_8);  // 将前8个字节转换为字符串
                if (str.equals("shutdown")) {
                    System.out.println("客户端请求关闭连接");  // 打印关闭连接请求信息
                    shutdown(clientAddress, clientPort);  // 调用关闭方法
                } else if (str.equals("GoodBye!")) {
                    System.out.println("客户端已关闭连接");  // 打印客户端已关闭连接信息
                } else {
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);  // 创建字节数组输入流
                    DataInputStream dataIntputStream = new DataInputStream(byteArrayInputStream);  // 创建数据输入流
                    short sequenceNumber = dataIntputStream.readShort();  // 读取消息序列号
                    byte[] messageBytes = new byte[dataIntputStream.available()];  // 获取剩余的消息字节
                    dataIntputStream.readFully(messageBytes);  // 读取完整的消息
                    String message = new String(messageBytes).trim();  // 将消息转换为字符串并去除空格

                    System.out.println("从客户端接收到: " + message + " 它的序列号为: " + sequenceNumber);  // 打印接收到的消息及其序列号

                    // 模拟网络丢包
                    Random random = new Random();  // 创建随机数生成器
                    if (random.nextInt(10) > 5) sendAck(sequenceNumber, clientAddress, clientPort);  // 发送确认消息
                    //else System.out.println("Packet loss, sequence number: " + sequenceNumber);  // 模拟丢包（注释掉）
                }
            } catch (Exception e) {
                System.out.println("管理错误: " + e.getMessage());  // 打印管理错误信息
            }
        }

        private void sendAck(short sequenceNumber, InetAddress clientAddress, int clientPort) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  // 创建字节数组输出流
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);  // 创建数据输出流
            try {
                // 写入确认消息的序列号和内容
                dataOutputStream.writeShort(sequenceNumber);  // 写入序列号
                String ackMessage = "ACK的序列号为: " + sequenceNumber;  // 创建确认消息
                dataOutputStream.write(ackMessage.getBytes(StandardCharsets.UTF_8));  // 将确认消息写入输出流

                // 将数据转换为字节数组并发送
                byte[] sendBuffer = byteArrayOutputStream.toByteArray();  // 获取字节数组
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);  // 创建发送数据包
                socket.send(sendPacket);  // 发送数据包
            } catch (Exception e) {
                System.out.println("发送ACK错误: " + e.getMessage());  // 打印发送ACK错误信息
            }
        }
    }

    public void shutdown(InetAddress clientAddress, int clientPort) {
        try {
            // 发送关闭消息
            byte[] buffer = "Accepted".getBytes(StandardCharsets.UTF_8);  // 创建关闭消息
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);  // 创建发送数据包
            socket.send(packet);  // 发送数据包
        } catch (Exception e) {
            System.out.println("发送断开连接消息错误: " + e.getMessage());  // 打印发送断开连接消息错误信息
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(12345);  // 初始化服务器
            server.run();  // 运行服务器
        } catch (Exception e) {
            System.out.println("不能初始化服务器: " + e.getMessage());  // 打印服务器初始化错误信息
        }
    }
}