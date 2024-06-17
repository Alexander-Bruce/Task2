import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {

    private static DatagramSocket socket;  // 定义UDP套接字
    private static String serverIP;  // 服务器IP地址
    private static int serverPort;  // 服务器端口号

    public static boolean first_shutdownServer(int count) {
        try {
            String shutdownCommand = "shutdown";  // 关闭服务器的命令
            byte[] data = shutdownCommand.getBytes(StandardCharsets.UTF_8);  // 将命令转换为字节数组
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(serverIP), serverPort);  // 创建UDP数据包
            socket.send(packet);  // 发送数据包
            return third_shutdownServer(count);  // 调用第三阶段关闭服务器的方法
        } catch (IOException e) {
            if(count == 0)
                System.out.println("未能成功断开: " + e.getMessage());  // 打印错误信息
        }
        return false;  // 返回关闭失败
    }

    public static boolean third_shutdownServer(int count) {
        byte[] buf = new byte[64];  // 创建缓冲区
        DatagramPacket packet = new DatagramPacket(buf, buf.length);  // 创建UDP数据包
        try {
            socket.setSoTimeout(100);  // 设置100ms超时
            socket.receive(packet);  // 接收数据包
            System.out.println("连接关闭");  // 打印连接关闭信息
            String shutdownCommand = "GoodBye!";  // 发送关闭确认信息
            byte[] sending_data = shutdownCommand.getBytes(StandardCharsets.UTF_8);  // 将信息转换为字节数组
            DatagramPacket sending_packet = new DatagramPacket(sending_data, sending_data.length, InetAddress.getByName(serverIP), serverPort);  // 创建UDP数据包
            socket.send(sending_packet);  // 发送数据包
            return true;  // 返回关闭成功
        } catch (IOException e) {
            if(count == 0)
                System.out.println("未能成功断开: " + e.getMessage());  // 打印错误信息
        }
        return false;  // 返回关闭失败
    }

    public static void main(String[] args) throws Exception {
        socket = new DatagramSocket();  // 初始化UDP套接字
        Scanner scanner = new Scanner(System.in);  // 创建输入扫描器
        System.out.print("请输入服务器IP地址: ");  // 提示输入服务器IP地址
        String serverIP = scanner.nextLine();  // 获取服务器IP地址
        System.out.print("请输入服务器端口号: ");  // 提示输入服务器端口号
        int serverPort = scanner.nextInt();  // 获取服务器端口号
        ConcurrentLinkedQueue<byte[]> message_queue = new ConcurrentLinkedQueue<>();  // 创建线程安全的消息队列

        Receiver receiver = new Receiver(socket, 12);  // 初始化接收器
        Sender sender = new Sender(socket, serverIP, serverPort, message_queue, receiver);  // 初始化发送器
        System.out.println("连接成功");  // 打印连接成功信息

        sender.start();  // 启动发送线程

        sender.join();  // 等待发送线程结束

        Thread.sleep(1000);  // 等待一段时间

        int count = 0;
        while (!Client.first_shutdownServer(count) && count <= 5) { count++; }  // 尝试关闭服务器

        System.out.println("丢包率: " + receiver.getLossRate() + "%");  // 打印丢包率
        System.out.println("最大RTT: " + receiver.max_RTT() + "ms");  // 打印最大RTT
        System.out.println("最小RTT: " + receiver.min_RTT() + "ms");  // 打印最小RTT
        System.out.println("平均RTT: " + receiver.getAverageRTT() + "ms");  // 打印平均RTT
        System.out.println("RTT的标准差: " + receiver.deviation() + "ms");  // 打印RTT的标准差
    }
}

class Sender extends Thread {
    private final DatagramSocket socket;  // 定义UDP套接字
    private final String ip;  // 服务器IP地址
    private final int port;  // 服务器端口号
    private final ConcurrentLinkedQueue<byte[]> message_queue;  // 消息队列
    private short count = 1;  // 发送包的计数器
    private final Receiver receiver;  // 接收器

    public Sender(DatagramSocket socket, String ip, int port, ConcurrentLinkedQueue<byte[]> message_queue, Receiver receiver) {
        this.socket = socket;
        this.ip = ip;
        this.port = port;
        this.message_queue = message_queue;
        this.receiver = receiver;
    }

    public void run() {
        while (count <= 12) {  // 发送12个包
            addMessageToQueue(LocalDateTime.now().toString());  // 将当前时间作为消息添加到队列
        }
        int number = 0;
        while (!message_queue.isEmpty()) {  // 发送队列中的所有消息
            number++;
            byte[] message = message_queue.poll();  // 从队列中获取消息
            try {
                DatagramPacket packet = new DatagramPacket(message, message.length, InetAddress.getByName(ip), port);  // 创建UDP数据包
                int count = 1;
                boolean flag = false;
                while(count <= 3 && !flag) {
                    System.out.println(count);  // 打印当前尝试次数
                    socket.send(packet);  // 发送数据包
                    LocalDateTime sendTime = LocalDateTime.now();  // 记录发送时间
                    flag = receiver.run(sendTime);  // 检查是否收到ACK
                    count++;
                }
                if(!flag) System.out.println("序列号 " + number + ", 响应超时");  // 打印超时信息
            } catch (Exception e) {
                System.out.println("数据包发送错误");  // 打印错误信息
            }
        }
    }

    private void addMessageToQueue(String message) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  // 创建字节数组输出流
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);  // 创建数据输出流
        try {
            dataOutputStream.writeShort(count);  // 写入消息序列号
            dataOutputStream.write(message.getBytes(StandardCharsets.UTF_8));  // 写入消息内容
            message_queue.add(byteArrayOutputStream.toByteArray());  // 将消息添加到队列
            count++;  // 增加序列号
        } catch (Exception e) {
            System.out.println("写入信息错误: " + e.getMessage());  // 打印错误信息
        }
    }
}

class Receiver {
    private final DatagramSocket socket;  // 定义UDP套接字
    private final ArrayList<Integer> time_duration;  // 用于存储每个包的RTT
    private final int totalPackets;  // 总包数
    private final int[] ack;  // 用于记录已接收的ACK

    public Receiver(DatagramSocket socket, int totalPackets) {
        this.socket = socket;
        this.totalPackets = totalPackets;
        this.ack = new int[totalPackets];
        this.time_duration = new ArrayList<>();
        for(int i = 0; i < 12; i++)
            time_duration.add(Integer.MAX_VALUE);  // 初始化RTT数组
    }

    public boolean run(LocalDateTime sendTime) {
        byte[] buf = new byte[64];  // 创建缓冲区
        DatagramPacket packet = new DatagramPacket(buf, buf.length);  // 创建UDP数据包
        try {
            socket.setSoTimeout(100);  // 设置100ms超时
            socket.receive(packet);  // 接收数据包
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(packet.getData());  // 创建字节数组输入流
            DataInputStream dataInputStream = new DataInputStream(byteInputStream);  // 创建数据输入流
            short sequenceNumber = dataInputStream.readShort();  // 读取序列号
            if (sequenceNumber > 0 && sequenceNumber <= totalPackets) {
                ack[sequenceNumber - 1] = 1;  // 标记为已接收
                LocalDateTime receiveTime = LocalDateTime.now();  // 记录接收时间
                long rtt = Duration.between(sendTime, receiveTime).toMillis();  // 计算RTT
                time_duration.set(sequenceNumber - 1, (int) rtt);  // 存储RTT
                System.out.println("接收到的ACK序列号为: " + sequenceNumber + " 它的RTT为: " + time_duration.get(sequenceNumber - 1) + " ms");  // 打印RTT
                return true;  // 返回ACK接收成功
            }
        } catch (Exception e) {
            return false;  // 返回ACK接收失败
        }
        return false;  // 返回ACK接收失败
    }

    public double getLossRate() {
        int lost = 0;
        for (int status : ack) {
            if (status == 0) lost++;  // 计算丢失的包数
        }
        return 100.0 * lost / totalPackets;  // 计算丢包率
    }

    public long max_RTT() {
        long max_rtt = Integer.MIN_VALUE;
        for(long status: time_duration) {
            if(status != Integer.MAX_VALUE && status > max_rtt)
                max_rtt =  status;  // 获取最大RTT
        }
        return max_rtt;
    }

    public long min_RTT() {
        long min_rtt = Integer.MAX_VALUE;
        for(long status: time_duration) {
            if(status != Integer.MAX_VALUE && status < min_rtt)
                min_rtt =  status;  // 获取最小RTT
        }
        if(min_rtt == Integer.MAX_VALUE) return 0;
        return min_rtt;
    }

    public double deviation() {
        double avg = getAverageRTT();  // 获取平均RTT
        double sum = 0;
        int validRTTs = 0;
        for (int i = 0; i < totalPackets; i++) {
            if (ack[i] == 1) {
                long rtt = time_duration.get(i);
                sum += Math.pow(rtt - avg, 2);  // 计算RTT偏差平方和
                validRTTs++;
            }
        }
        return validRTTs > 0 ? Math.sqrt(sum / validRTTs) : 0;  // 计算标准差
    }

    public double getAverageRTT() {
        long totalRTT = 0;
        int validRTTs = 0;
        for (int i = 0; i < totalPackets; i++) {
            if (ack[i] == 1) {
                long rtt = time_duration.get(i);
                totalRTT += rtt;  // 计算RTT总和
                validRTTs++;
            }
        }
        return validRTTs > 0 ? (double) totalRTT / validRTTs : 0;  // 计算平均RTT
    }
}
