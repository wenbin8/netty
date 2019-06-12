package com.wenbin.bio.msg;

import com.sun.tools.javac.util.StringUtils;
import com.wenbin.bio.msg.client.Client;
import com.wenbin.bio.msg.server.MsgServer;
import io.netty.util.internal.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Scanner;

/**
 * @Auther: wenbin
 * @Date: 2019/2/7 12:27
 * @Description:
 */
@Getter
@Setter
public class User {
    private String  name ;
    private int port ;


    public void inMsgServerStart() throws InterruptedException {
        User user2 = this;
        // 启动接受信息服务
        new Thread(() -> {
            try {
                MsgServer.start(user2.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(1000);
    }


    public void sendMsg(User target) {
        // 连接好友
        User user = target;
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.print("发送消息给" + target.getName() + ":");

            String read = scan.nextLine();
            if ("".equals(read)) {
                continue;
            }

            Client.send(user.getPort(), read);

        }
    }



}
