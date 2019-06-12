package com.wenbin.bio2.thread.pool;

import com.wenbin.bio2.TimeServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Auther: wenbin
 * @Date: 2019/6/12 05:55
 * @Description:
 */
public class TimeServer {

    public static void main(String[] args) {
        int port = 8080;

        try {
            ServerSocket serverSocket = new ServerSocket(port);

            Socket socket = null;
            TimeServerHandlerExecutePool executor =
                    new TimeServerHandlerExecutePool(50, 10000);

            while (true) {
                // 没有连接接入的时候阻塞在这里等待连接.
                socket = serverSocket.accept();
                executor.execute(new TimeServerHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
