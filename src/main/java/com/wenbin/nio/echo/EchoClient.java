package com.wenbin.nio.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @Auther: wenbin
 * @Date: 2019/6/12 12:46
 * @Description:
 */
public class EchoClient {
    String host = "127.0.0.1";
    int port = 8089;

    private Selector selector;
    private SocketChannel socketChannel;

    private String msg;

    private Charset utf8 = Charset.forName("UTF-8");

    public EchoClient(String msg) {
        this.msg = msg;
        try {
            // 打开多路复用器
            selector = Selector.open();
            // 打开soket通道
            socketChannel = SocketChannel.open();
            // 设置为非阻塞
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void start() {
        try {
            // 如果连接成功注册读事件.并肩消息通过通道写出去
            if (socketChannel.connect(new InetSocketAddress(host, port))) {
                socketChannel.register(selector, SelectionKey.OP_READ);

                byte[] bytes = msg.getBytes();
                ByteBuffer writeBuf = ByteBuffer.allocate(bytes.length);
                writeBuf.put(bytes);
                writeBuf.flip();

                socketChannel.write(writeBuf);
            } else {
                // 连接为成功注册连接时间,等待通知.
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean isClose = false;
        while (!isClose) {
            isClose = select(msg);
        }
    }

    private boolean select(String msg) {
        boolean rsp = false;
        try {
            // 没有事件会阻塞在这里
            selector.select();
            // 取出已经就绪的时间
            Set<SelectionKey> keySet = selector.selectedKeys();

            Iterator<SelectionKey> it = keySet.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                SocketChannel socketChannel = null;
                // 处理连接成功事件
                if (key.isConnectable()) {
                    // 获取事件对应的通道
                    socketChannel = (SocketChannel) key.channel();

                    // 这边等待三次握手成功
                    if (socketChannel.finishConnect()) {
                        // 如果连接成功则注册读事件
                        socketChannel.register(selector, SelectionKey.OP_READ);

                        // 将消息写入缓冲
                        byte[] bytes = msg.getBytes("UTF-8");
                        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
                        byteBuffer.put(bytes);
                        byteBuffer.flip();

                        // 这里的byteBuffer为堆内存,也就是用户态,通道的write方法会将缓冲内的数据写入到内核态
                        socketChannel.write(byteBuffer);
                    }
                }
                // 处理读事件
                if (key.isReadable()) {
                    // 获取事件对应的通道
                    socketChannel = (SocketChannel) key.channel();

                    // 准备好接受信息的缓存
                    ByteBuffer readBuf = ByteBuffer.allocate(1024);

                    int readSize = socketChannel.read(readBuf);

                    if (readSize > 0) {
                        readBuf.flip();

                        CharBuffer charBuffer = utf8.decode(readBuf);
                        System.out.println("Client:" + charBuffer.toString());
                        rsp = true;
                    } else if (readSize < 0) {
                        key.cancel();
                        socketChannel.close();
                    }
                }
            }
            return rsp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rsp;
    }

    public static void main(String[] args) {
        EchoClient echoClient = new EchoClient("我是要发送的信息");
        echoClient.start();
    }
}
