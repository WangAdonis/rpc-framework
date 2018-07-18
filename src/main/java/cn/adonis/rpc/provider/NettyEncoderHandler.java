package cn.adonis.rpc.provider;

import cn.adonis.rpc.serialize.Serializer;
import cn.adonis.rpc.serialize.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoderHandler extends MessageToByteEncoder {
    private String serializeType;

    public NettyEncoderHandler(String serializeType) {
        this.serializeType = serializeType;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        Serializer serializer = SerializerFactory.create(serializeType);
        byte[] data = serializer.serialize(o);
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
