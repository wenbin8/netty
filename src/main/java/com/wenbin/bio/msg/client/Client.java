package com.wenbin.bio.msg.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @Auther: wenbin
 * @Date: 2019/2/7 12:23
 * @Description:
 */
public class Client {

    private static Socket socket;

    private static String DEFAULT_SERVER_IP = "127.0.0.1";


    public static void send(int port, String msg) {
        PrintWriter out = null;

        try {

            if (socket == null) {
                socket = new Socket(DEFAULT_SERVER_IP, port);
            }

            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
                out = null;

            }

            if (socket != null) {

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;

            }
        }
    }
}
