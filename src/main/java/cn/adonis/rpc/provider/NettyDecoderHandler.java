package cn.adonis.rpc.provider;

import cn.adonis.rpc.serialize.Serializer;
import cn.adonis.rpc.serialize.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class NettyDecoderHandler extends ByteToMessageDecoder {

    private Class<?> genericClass;

    private String serializeType;

    public NettyDecoderHandler(Class<?> genericClass, String serializeType) {
        this.genericClass = genericClass;
        this.serializeType = serializeType;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (dataLength < 0) {
            channelHandlerContext.close();
        }
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        Serializer serializer = SerializerFactory.create(this.serializeType);
        Object obj = serializer.deserialize(data, this.genericClass);
        list.add(obj);
    }
}
