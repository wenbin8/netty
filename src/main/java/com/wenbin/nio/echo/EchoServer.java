package com.wenbin.nio.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @Auther: wenbin
 * @Date: 2019/6/12 11:28
 * @Description:
 */
public class EchoServer {

    private int port = 8089;

    private Selector selector = null;

    private ServerSocketChannel serverSocketChannel = null;

    private Charset utf8 = Charset.forName("UTF-8");



    public EchoServer()  {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void start() {
        try {
            selector.select();
            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> it = keySet.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                if (key.isAcceptable()) {
                    System.out.println("接到连接");
                    // 获得连接通道
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);

                    int register = SelectionKey.OP_READ;
//                    int register = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
                    socketChannel.register(selector, register);
                    System.out.println("连接读注册成功");
                }

                if (key.isReadable()) {
                    System.out.println("接到读事件");
                    SocketChannel socketChannel = (SocketChannel) key.channel();

                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    int readSize = socketChannel.read(readBuffer);

                    if (readSize > 0) {
                        readBuffer.flip();

                        CharBuffer charBuffer = utf8.decode(readBuffer);

                        System.out.println("收到消息:" + charBuffer.toString());

                        // 将回写消息放入附件
                        readBuffer.flip();

                        // 这种直接通过通道写回去
                        socketChannel.write(readBuffer);
                        /**
                         * 这里可以直接写回去.那还要写事件有什么用?
                         * 写事件何时触发?
                         *
                         * 写就绪相对有一点特殊，一般来说，你不应该注册写事件。写操作的就绪条件为底层缓冲区有空闲空间，
                         * 而写缓冲区绝大部分时间都是有空闲空间的，所以当你注册写事件后，写操作一直是就绪的，选择处理
                         * 线程全占用整个CPU资源。所以，只有当你确实有数据要写时再注册写操作，并在写完以后马上取消注册。
                         */
                        System.out.println("读事件处理成功");
                    } else if (readSize < 0) {
                        // 读取到小于0对方链路关闭
                        key.cancel();
                        socketChannel.close();
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        EchoServer echoServer = new EchoServer();

        while (true) {
            echoServer.start();
        }
    }
}
