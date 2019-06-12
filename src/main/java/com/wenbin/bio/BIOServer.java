package com.wenbin.bio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Auther: wenbin
 * @Date: 2019/2/7 10:50
 * @Description:
 */
@Slf4j
public class BIOServer {

    // 默认端口号
    private static int DEFAULT_PORT = 7777;

    // 单例的ServerSocker
    private static ServerSocket serverSocket;

    // 根据传入参数设置监听端口,如果没有参数调用一下方法并使用默认值
    public static void start() throws IOException {
        start(DEFAULT_PORT);
    }

    public synchronized static void start(int port) throws IOException {
        if (serverSocket != null) {
            return;
        }

        try {
            // 通过构造函数穿件ServerSocket
            // 如果端口合法并且空闲,服务端就监听成功
            serverSocket = new ServerSocket(port);
            System.out.println("服务端已启动,端口号:" + port);

            // 通过无限循环监听客户端连接
            // 如果没有客户端接入,将阻塞在accept操作上.
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ServerHandler(socket)).start();

            }
        } finally {
            if (serverSocket != null) {
                System.out.println("服务端已关闭");
                serverSocket.close();
                serverSocket = null;
            }
        }

    }
}
