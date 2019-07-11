package com.wenbin.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;

/**
 * @Auther: wenbin
 * @Date: 2019/7/3 19:38
 * @Description:
 */
public class CustomByteBufHolder extends DefaultByteBufHolder {
    private String protocolName;

    public CustomByteBufHolder(String protocolName, ByteBuf data) {
        super(data);
        this.protocolName = protocolName;
    }

}
