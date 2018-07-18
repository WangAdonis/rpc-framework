package cn.adonis.rpc.consumer;

import cn.adonis.rpc.model.RpcRequest;
import cn.adonis.rpc.model.RpcResponse;
import cn.adonis.rpc.model.RpcResponseHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ConsumerServiceCallable implements Callable<RpcResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerServiceCallable.class);
    private Channel channel;
    private InetSocketAddress socketAddress;
    private RpcRequest rpcRequest;

    private ConsumerServiceCallable(InetSocketAddress socketAddress, RpcRequest rpcRequest) {
        this.socketAddress = socketAddress;
        this.rpcRequest = rpcRequest;
    }

    public static ConsumerServiceCallable of(InetSocketAddress socketAddress, RpcRequest rpcRequest) {
        return new ConsumerServiceCallable(socketAddress, rpcRequest);
    }
    @Override
    public RpcResponse call() throws Exception {
        RpcResponseHolder.initResponseData(rpcRequest.getUniqueKey());
        NettyChannelPoolFactory channelPoolFactory = NettyChannelPoolFactory.singleton();
        BlockingQueue<Channel> blockingQueue = channelPoolFactory.acquire(socketAddress);
        try {
            if (channel == null) {
                channel = blockingQueue.poll(rpcRequest.getTimeout(), TimeUnit.MILLISECONDS);
            }
            // 若获取的channel不可用，则重新获取一个
            while (!channel.isOpen() || !channel.isActive() || !channel.isWritable()) {
                channel = blockingQueue.poll(rpcRequest.getTimeout(), TimeUnit.MILLISECONDS);
                if (channel == null) {
                    channel = channelPoolFactory.registerChannel(socketAddress);
                }
            }
            ChannelFuture channelFuture = channel.writeAndFlush(rpcRequest);
            channelFuture.syncUninterruptibly();

            long invokeTimeout = rpcRequest.getTimeout();
            return RpcResponseHolder.getValue(rpcRequest.getUniqueKey(), invokeTimeout);
        } catch (Exception e) {
            LOGGER.error("service invoke error.");
        } finally {
            channelPoolFactory.release(blockingQueue, channel, socketAddress);
        }
        return null;
    }
}
