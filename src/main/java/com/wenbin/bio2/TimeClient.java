package com.wenbin.bio2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @Auther: wenbin
 * @Date: 2019/6/12 06:10
 * @Description:
 */
public class TimeClient {

    public static void main(String[] args) {
        int port = 8080;

        try {
            // 连接
            Socket socket = new Socket("127.0.0.1", port);

            // 从socket中获得输入流
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // 从socket中获得输出流
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // 把消息写入输出流
            out.println("QUERY TIME ORDER");
            System.out.println("Send order 2 server succeed.");
            // 从输入流读取消息,因为都是英文不涉及enCode deCode
            String resp = in.readLine();
            System.out.println("Now is :" + resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
