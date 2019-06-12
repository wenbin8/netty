package com.wenbin.bio.msg;

/**
 * @Auther: wenbin
 * @Date: 2019/2/7 12:49
 * @Description:
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        User user = new User();
        user.setPort(7780);
        user.setName("小明");

        User user2 = new User();
        user2.setPort(7781);
        user2.setName("小花");
        user2.inMsgServerStart();

        user2.sendMsg(user);

    }
}
