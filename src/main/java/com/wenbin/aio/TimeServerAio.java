package com.wenbin.aio;

/**
 * @Auther: wenbin
 * @Date: 2019/6/13 11:29
 * @Description:
 */
public class TimeServerAio {
    public static void main(String[] args) {
        int port = 8091;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        AsyncTimeServerHandler timeServer = new AsyncTimeServerHandler(port);
        new Thread(timeServer, "AIO-AsyncTimeServerHandler-001").start();

    }
}
