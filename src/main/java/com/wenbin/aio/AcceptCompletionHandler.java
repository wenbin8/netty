package com.wenbin.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @Auther: wenbin
 * @Date: 2019/6/13 11:33
 * @Description:
 */
public class AcceptCompletionHandler
        implements CompletionHandler<AsynchronousSocketChannel, AsyncTimeServerHandler>{

    @Override
    public void completed(AsynchronousSocketChannel result, AsyncTimeServerHandler attachment) {

        System.out.println("Accept Thread:" + Thread.currentThread().getName());

        // 次accept只能与一个客户端通信，并且处理接收请求的线程最后都被标记为interrupted。
        // 所以每次accept方法要重新注册使用。
        attachment.asynchronousServerSocketChannel.accept(attachment, this);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 连接进来了发起一个读操作
        result.read(buffer, buffer, new ReadCompletionHandler(result));
    }

    @Override
    public void failed(Throwable exc, AsyncTimeServerHandler attachment) {
        exc.printStackTrace();
        attachment.latch.countDown();
    }
}
