package com.wenbin.nio.buffer;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @Auther: wenbin
 * @Date: 2019/2/7 17:40
 * @Description:
 */
public class BufferDemo {
    public static void deCode(String str) throws UnsupportedEncodingException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        byteBuffer.put(str.getBytes("UTF-8"));
        byteBuffer.flip();

        // 获取utf8的编解码器
        Charset utf8 = Charset.forName("UTF-8");
        CharBuffer charBuffer = utf8.decode(byteBuffer);

        // array()返回的就是内部的数组引用,编码以后的有效长度是0~limit
        char[] charArr = Arrays.copyOf(charBuffer.array(), charBuffer.limit());
        System.out.println(charArr);

    }

    public static void enCode(String str) {
        CharBuffer charBuffer = CharBuffer.allocate(128);
        charBuffer.append(str);
        charBuffer.flip();

        // 获取utf8的编解码器
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuffer byteBuffer = utf8.encode(charBuffer);

        byte[] bytes = Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
        System.out.println(Arrays.toString(bytes));

    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        BufferDemo.deCode("董文斌");
        BufferDemo.enCode("董文斌");
    }
}
