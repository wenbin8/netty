package com.wenbin.aio;

/**
 * @Auther: wenbin
 * @Date: 2019/6/13 10:58
 * @Description:
 */
public class TimeClienAio {

    public static void main(String[] args) {
        int port = 8091;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {

            }
        }

        // 新建个线程不用管了请求发送和响应都会在内部处理
        new Thread(new AsyncTimeClientHandler("127.0.0.1", port),
                "AIO-AsyncTimeClientHandler-001").start();
    }
}
