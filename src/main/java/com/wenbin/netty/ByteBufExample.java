package com.wenbin.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ByteProcessor;

import java.nio.charset.Charset;

/**
 * @Auther: wenbin
 * @Date: 2019/7/3 12:20
 * @Description:
 */
public class ByteBufExample {

    private static final ByteBuf BYTE_BUF_FROM_SOMEWHERE = Unpooled.buffer(60);


    public static void heapBuffer() {
        ByteBuf heapBuf = Unpooled.buffer(60);
        heapBuf.writeBytes("heapBuffer".getBytes());
        // 检查ByteBuf是有支撑数组
        if (heapBuf.hasArray()) {
            // 获取支撑数组的引用
            // 当heapBuf.hasArray()返回false时,尝试访问支撑数组将处罚UnsupportedOperationException
            byte[] array = heapBuf.array();
            // 计算第一个字节的偏移量,也就是可以开始读的字节
            int offset = heapBuf.arrayOffset() + heapBuf.readerIndex();
            // 获得可读字节数
            int length = heapBuf.readableBytes();
            handleArray(array, offset, length);
        }
    }

    public static void directBuffer() {
        ByteBuf directBuf = Unpooled.directBuffer();
        directBuf.writeBytes("directBuffer".getBytes());
        // 检查ByteBuf是否由数组支撑.如果不是,则这是一个直接缓存区
        if (!directBuf.hasArray()) {
            // 获取可读字节数
            int length = directBuf.readableBytes();
            // 声明一个新的数组来保存具有该长度的字节数据
            byte[] array = new byte[length];
            // 将字节复制到该数组
            directBuf.getBytes(directBuf.readerIndex(), array);
            handleArray(array, 0, length);
        }
    }

    public static void byteBufComposite() {
        //﻿Netty使用了CompositeByteBuf来优化套接字的I/O操作，
        // 尽可能地消除了由JDK的缓冲区实现所导致的性能以及内存使用率的惩罚。
        CompositeByteBuf messageBuf = Unpooled.compositeBuffer();
        ByteBuf headerBuf = Unpooled.buffer(60); // can be backing or direct
        headerBuf.writeBytes("headerBuf".getBytes());
        ByteBuf bodyBuf = Unpooled.directBuffer();   // can be backing or direct
        bodyBuf.writeBytes("bodyBuf".getBytes());
        // 将ByteBuf实例追加到CompositeByteBuf
        messageBuf.addComponents(true, headerBuf, bodyBuf);

        // 访问CompositeByteBuf中的数据和访问直接缓冲区的模式相同,应为混合模式不支持访问支撑数组
        int length = messageBuf.readableBytes();
        byte[] array = new byte[length];
        messageBuf.getBytes(messageBuf.readerIndex(), array);
        handleArray(array, 0, length);

        // CompositeByteBuf像是一个ByteBuf的容器
        // 循环遍历所有ByteBuf
        messageBuf.removeComponent(0); // remove the header
        for (ByteBuf buf : messageBuf) {
            length = buf.readableBytes();
            byte[] array1 = new byte[length];
            messageBuf.getBytes(buf.readerIndex(), array1);
            handleArray(array1, 0, length);
        }

        // 重新写入ByteBuff
        messageBuf.writeBytes("byteBufComposite".getBytes());

        length = messageBuf.readableBytes();
        byte[] array1 = new byte[length];
        messageBuf.getBytes(messageBuf.readerIndex(), array1);

        handleArray(array1, 0, length);

    }

    public static void byteBufRelativeAccess() {
        // readXXX()和writeXXX()方法将会推进其对应的索引readerIndex和writerIndex。自动推进
        // getXXX()和setXXX()方法用于访问数据，对writerIndex和readerIndex无影响
        ByteBuf buffer = BYTE_BUF_FROM_SOMEWHERE;

        // writeXXX方法会推进writetIndex
        buffer.writeBytes("byteBufRelativeAccess".getBytes());
        buffer.writeBytes("1234".getBytes());

        // setXXX方法对writerIndex无影响,使用setXXXX方法在下标30的位置写入
        buffer.setBytes(30, "setXXX".getBytes());
        getPrint(buffer);

        // 再次通过write方法写入. 使用set方法写入的数据被覆盖
        buffer.writeBytes("1234567890".getBytes());
        getPrint(buffer);

        // 使用read方法剩余一个未被覆盖的X字符,不应该被输出
        readPrint(buffer);
    }

    /**
     * 可丢弃字节区域是指:[0，readerIndex)之间的区域。可调用discardReadBytes()方法丢弃已经读过的字节。
     * <p>
     * 1. discardReadBytes()效果 ----- 将可读字节区域(CONTENT)[readerIndex, writerIndex)往前
     * 移动readerIndex位，同时修改读索引和写索引。
     * 2. discardReadBytes()方法会移动可读字节区域内容(CONTENT)。如果频繁调用，会有多次数据复制开销，
     * 对性能有一定的影响
     */
    public static void byteBufDiscardReadBytes() {

        ByteBuf buffer = BYTE_BUF_FROM_SOMEWHERE;
        buffer.writeBytes("1234".getBytes());
        // 使用read读取,移动readerIndex索引,
        readPrint(buffer);

        // 写入一些未被读取过的数据
        buffer.writeBytes("5678".getBytes());

        System.out.println("此时第一个1234为<可丢弃字节区域>");
        getPrint(buffer);
        System.out.println("调用discardReadBytes(),1234被清理");
        buffer.discardReadBytes();
        getPrint(buffer);
        // 写入abcd,在之前存入5678的地方会被新数据覆盖掉
        buffer.writeBytes("abcd".getBytes());
        getPrint(buffer);

    }

    private static void getPrint(ByteBuf buffer) {
        for (int i = 0; i < buffer.capacity(); i++) {
            // getXXX()方法用于访问数据对readerIndex无影响
            byte b = buffer.getByte(i);
            System.out.print((char) b);
        }

        System.out.println("");
        System.out.println("------------------");
    }

    private static void readPrint(ByteBuf buffer) {
        // 这里实现了如何读取所有可以读的字节
        while (buffer.isReadable()) {
            byte b = buffer.readByte();
            System.out.print((char) b);
        }

        System.out.println("");
        System.out.println("------------------");
    }

    public static void writeAndGetPrint() {
        ByteBuf buffer = BYTE_BUF_FROM_SOMEWHERE;
        String str = "123456789";
        // 确定写缓冲区是否还有足够的空间
        while (buffer.writableBytes() >= 9) {
            buffer.writeBytes(str.getBytes());
        }
        getPrint(buffer);
    }


    public static void byteBufIndexManager() {
        ByteBuf buffer = BYTE_BUF_FROM_SOMEWHERE;
        buffer.writeBytes("1234567890".getBytes());
        getPrint(buffer);
        // markReaderIndex()+resetReaderIndex() ----- markReaderIndex()是先备份当前的readerIndex，
        // resetReaderIndex()则是将刚刚备份的readerIndex恢复回来。常用于dump ByteBuf的内容，又不想影响原
        // 来ByteBuf的readerIndex的值
        buffer.markReaderIndex();       // 读取前标记
        readPrint(buffer);              // 读取一次
        buffer.resetReaderIndex();      // reset复位ReaderIndex
        readPrint(buffer);              // 在读取一次


        buffer.writeBytes("123456789".getBytes());
        getPrint(buffer);
        // 设置readerIndex为固定的值
        buffer.readerIndex(12);
        readPrint(buffer);

        // clear() ----- 效果是: readerIndex=0, writerIndex(0)。不会清除内存
        buffer.clear();
        // 使用read不会独处任何数据
        readPrint(buffer);
        // 使用get方法
        getPrint(buffer);
    }

    public static void byteProcessor() {
        ByteBuf buffer = Unpooled.buffer(); //get reference form somewhere
        byte[] b = new byte[]{(byte) 8, (byte) 9, (byte) 10};
        buffer.writeBytes(b);

        // 使用indexOf()方法来查找
        int i = buffer.indexOf(buffer.readerIndex(), buffer.writerIndex(), (byte) 9);
        System.out.println(i);
        // 使用ByteProcessor查找给定的值
        int index = buffer.forEachByte(ByteProcessor.FIND_CR);
        System.out.println(index);
    }


    public static void byteBufSlice() {
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        ByteBuf sliced = buf.slice(0, 15);
        System.out.println(sliced.toString(utf8));
        // 改变下标0位置的判断两个ByteBuf值是否一样
        buf.setByte(0, (byte) 'J');
        // 是同一套数据,所以相等.
        System.out.println(buf.getByte(0) == sliced.getByte(0));
    }


    public static void byteBufCopy() {
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", utf8);
        ByteBuf copy = buf.copy(0, 15);
        System.out.println(copy.toString(utf8));
        // 改变下标0位置的判断两个ByteBuf值是否一样
        buf.setByte(0, (byte) 'J');
        // 不是同一数据.所以为false
        System.out.println(buf.getByte(0) == copy.getByte(0));

        getPrint(buf);
        getPrint(copy);
    }

    public static void releaseReferenceCountedObject() {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        // 输出引用计数
        System.out.println(buffer.refCnt());
        // 引用计数加1
        buffer.retain();
        // 输出引用计数
        System.out.println(buffer.refCnt());
        // 引用计数减1
        buffer.release();
        // 输出引用计数
        System.out.println(buffer.refCnt());
    }

    public static void main(String[] args) {
//        // 堆缓冲
//        ByteBufExample.heapBuffer();
//        // 直接缓冲
//        ByteBufExample.directBuffer();
//        // 复合缓冲
//        ByteBufExample.byteBufComposite();
//        // 字节级操作
//        // 随机访问
//        ByteBufExample.byteBufRelativeAccess();
//        // 可丢弃字节区域
//        ByteBufExample.byteBufDiscardReadBytes();
//        // 可写字节
//        ByteBufExample.writeAndGetPrint();
//        // 索引管理
        ByteBufExample.byteBufIndexManager();
//        // 查找操作
//        ByteBufExample.byteProcessor();
//        // 派生缓冲区
//        ByteBufExample.byteBufSlice();
//        ByteBufExample.byteBufCopy();
//        // 引用计数
//        ByteBufExample.releaseReferenceCountedObject();
    }

    private static void handleArray(byte[] array, int offset, int length) {
        for (int i = offset; i < length; i++) {
            byte b = array[i];
            System.out.print((char) b);

        }
        System.out.println("");
        System.out.println("------------------");
    }

    public void createByteBuf(ChannelHandlerContext ctx) {
        // 1. 通过Channel创建ByteBuf
        ByteBuf buf1 = ctx.channel().alloc().buffer();
        // 2. 通过ByteBufAllocator.DEFAULT创建
        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer();
        // 3. 通过Unpooled创建
        ByteBuf buf3 = Unpooled.buffer();
        // 4. 创建一个视图，返回一个包装了给定数据的ByteBuf。非常实用
        ByteBuf buf4 = Unpooled.wrappedBuffer("返回一个包装了指定数据的ByteBuf".getBytes());
    }

}
