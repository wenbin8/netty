package com.wenbin.bio.msg;

import com.wenbin.bio.msg.User;

/**
 * @Auther: wenbin
 * @Date: 2019/2/7 12:48
 * @Description:
 */
public class Test2 {

    public static void main(String[] args) throws InterruptedException {
        User user = new User();
        user.setPort(7780);
        user.setName("小明");

        user.inMsgServerStart();

        User user2 = new User();
        user2.setPort(7781);
        user2.setName("小花");

        user.sendMsg(user2);

    }



}
