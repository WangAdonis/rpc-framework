package cn.adonis.rpc.consumer;

import cn.adonis.rpc.model.RpcResponse;
import cn.adonis.rpc.model.RpcResponseHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientInvokeHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
        RpcResponseHolder.putResultValue(response);
    }
}
