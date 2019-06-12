package com.wenbin.nio.channel;

import com.wenbin.nio.buffer.Buffers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * @Auther: wenbin
 * @Date: 2019/2/7 18:13
 * @Description: 服务器端:接受客户端发送过来的数据并显示
 * 服务器把接收到的数据加上"echo from service:"在发送回去
 */
public class ServiceSocketChannelDemo {
    public static void main(String[] args) throws InterruptedException, IOException {

//        System.out.println(Integer.toBinaryString(1 << 4));
//        System.out.println(Integer.toBinaryString(1 << 3));
//        System.out.println(Integer.toBinaryString(1 << 2));
//        System.out.println(Integer.toBinaryString(1 << 1));
//        System.out.println(Integer.toBinaryString(1 << 0));
//        System.out.println(Integer.toBinaryString(1 << 0 | 1 << 4));
//        System.out.println(Integer.toBinaryString(1 << 0 & 1 << 4));

        System.out.println(Integer.toBinaryString(1 << 2));
        System.out.println(Integer.toBinaryString(~1 << 2));
        System.out.println(Integer.toBinaryString((1 << 4  | 1<<2)));

        System.out.println(Integer.toBinaryString((1 << 4  | 1<<2) & (~1 << 2)));

        Thread thread = new Thread(new TCPEchoServer(8080));
        thread.start();
        Thread.sleep(100000);
        /*结束服务器线程*/
        thread.interrupt();

    }

    public static class TCPEchoServer implements Runnable {

        private InetSocketAddress localAddress;

        public TCPEchoServer(int port) throws IOException {
            this.localAddress = new InetSocketAddress(port);
        }

        @Override
        public void run() {

            Charset utf8 = Charset.forName("UTF-8");

            ServerSocketChannel serverSocketChannel = null;
            Selector selector = null;

            Random rnd = new Random();

            try {
                // 创建选择器
                selector = Selector.open();

                // 创建服务器通道
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(false);

                // 设置监听服务器的端口, 设置最大连接缓冲数为100
                serverSocketChannel.bind(localAddress, 100);

                // 服务器通道只能对tcp链接事件感兴趣
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            System.out.println("server start with address : " + localAddress);

            // 服务器线程被中断后会退出
            try {
                while (!Thread.currentThread().isInterrupted()) {

                    int n = selector.select();
                    if (n == 0) {
                        continue;
                    }

                    Set<SelectionKey> keySet = selector.selectedKeys();
                    Iterator<SelectionKey> it = keySet.iterator();
                    SelectionKey key = null;

                    while (it.hasNext()) {
                        key = it.next();
                        // 防止下次select方法返回已处理过的通道
                        it.remove();/*若发现异常，说明客户端连接出现问题,但服务器要保持正常*/
                        try{
                            // serverSocketChannel通道只能对链接事件感兴趣
                            if (key.isAcceptable()) {
                                // accept方法会返回一个普通通道
                                SocketChannel socketChannel = serverSocketChannel.accept();
                                socketChannel.configureBlocking(false);

                                // 向选择器注册这个通道感兴趣的时间,同事提供这个心通道相关的缓冲区
                                int interestSet = SelectionKey.OP_READ;
                                socketChannel.register(selector, interestSet, new Buffers(256, 256));

                                System.out.println("accept from " + socketChannel.getRemoteAddress());

                            }

                            //（普通）通道感兴趣读事件且有数据可读
                            if (key.isReadable()) {
                                System.out.println("读 事件-触发 ");
                                // 通过SelectionKey获取通道对应的缓冲区
                                Buffers buffers = (Buffers) key.attachment();
                                ByteBuffer readBuffer = buffers.getReadBuffer();
                                ByteBuffer writeBuffer = buffers.getWriteBuffer();

                                SocketChannel socketChannel = (SocketChannel) key.channel();

                                // 从底层socket读缓冲区中的数据
                                // TODO 内核态 复制到 用户态
                                socketChannel.read(readBuffer);
                                readBuffer.flip();

//                                // 获取utf8的编解码器
//                                CharBuffer charBuffer = utf8.decode(readBuffer);
//
//                                // array()返回的就是内部的数组引用,编码以后的有效长度是0~limit
//                                char[] charArr = Arrays.copyOf(charBuffer.array(), charBuffer.limit());
//                                System.out.println("读:" + charArr);

                                //解码显示,客户端发送来的信息
                                // 先写入"echo :" 在写入收到的信息
                                writeBuffer.put("echo from service:".getBytes("UTF-8"));
                                writeBuffer.put(readBuffer);


                                readBuffer.clear();

                                // 设置通道写事件
                                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                            }

                            // 通道感兴趣写事件且底层缓冲区有空闲
                            if (key.isWritable()) {
                                System.out.println("写 事件-触发 ");
                                Buffers buffers = (Buffers) key.attachment();
                                ByteBuffer writeBuffer = buffers.getWriteBuffer();

                                writeBuffer.flip();

                                SocketChannel socketChannel = (SocketChannel) key.channel();

                                int len = 0;
                                // 不能保证buf内数据被完全写入所以要循环写.
                                // 这里参考ByteBuffer和socketChannel的API 这里深坑... 不理了.
                                // 我只想整明白原理,然后用netty....
                                while (writeBuffer.hasRemaining()) {
                                    // TODO 用户态 复制到 内核态
                                    len = socketChannel.write(writeBuffer);
                                    // 说明底层socket写缓冲已满
                                    if (len == 0) {
                                        break;
                                    }
                                }

                                writeBuffer.compact();

                                // 说明数据全部写入到底层的socket写缓冲区
                                if (len != 0) {
                                    // 取消通道写时间
                                    key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));

                                }

                            }
                        }catch(IOException e){
                            System.out.println("service encounter client error");
                            /*若客户端连接出现异常，从Seletcor中移除这个key*/
                            key.cancel();
                            key.channel().close();
                        }
                    }
                    Thread.sleep(rnd.nextInt(500));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    selector.close();
                } catch (IOException e) {
                    System.out.println("selector close failed");
                } finally {
                    System.out.println("server close");
                }
            }
        }
    }
}
