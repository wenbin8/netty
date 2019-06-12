package com.wenbin.bio.msg.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Auther: wenbin
 * @Date: 2019/2/7 11:31
 * @Description:
 */
public class MsgServer {


    private static ServerSocket serverSocket;

    public synchronized static void start(int port) throws IOException {
        if (serverSocket != null) {
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("消息接收服务端已启动,端口号:" + port);

            // 无限循环监听客户端连接
            while (true) {
                final Socket socket = serverSocket.accept();

                new MsgHandler(socket).run();
            }
        } finally {
            //一些必要的清理工作
            if (serverSocket != null) {
                System.out.println("服务端已关闭。");
                serverSocket.close();
                serverSocket = null;
            }
        }
    }


}
