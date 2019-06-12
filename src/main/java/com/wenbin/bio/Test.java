package com.wenbin.bio;

import java.io.IOException;
import java.util.Random;

/**
 * @Auther: wenbin
 * @Date: 2019/2/7 11:13
 * @Description:
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            try {
                BIOServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // 防止客户端先于服务器启动前执行代码
        Thread.sleep(100);

        final char[] op = {'+', '-', '*', '/'};

        final Random random = new Random(System.currentTimeMillis());
        new Thread(() -> {
            while (true) {
                //随机产生算术表达式
                String expression = random.nextInt(10) + "" + op[random.nextInt(4)] +
                        (random.nextInt(10) + 1);
                Client.send(expression);
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
