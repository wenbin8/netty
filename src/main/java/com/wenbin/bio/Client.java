package com.wenbin.bio;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @Auther: wenbin
 * @Date: 2019/2/7 11:07
 * @Description:
 */
@Slf4j
public class Client {
    // 默认端口号
    private static int DEFAULT_SERVER_PORT = 7777;

    private static String DEFAULT_SERVER_IP = "127.0.0.1";

    public static void send(String expression) {
        send(DEFAULT_SERVER_PORT, expression);

    }

    public static void send(int port, String expression) {
        System.out.println("算数表达式为:" + expression);
        Socket socket = null;

        BufferedReader in = null;

        PrintWriter out = null;

        try {
            socket = new Socket(DEFAULT_SERVER_IP, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(expression);
            System.out.println("结果为:" + in.readLine());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
