package cn.adonis.rpc.consumer;

import cn.adonis.rpc.helper.PropertyConfigeHelper;
import cn.adonis.rpc.model.ProviderService;
import cn.adonis.rpc.model.RpcResponse;
import cn.adonis.rpc.provider.NettyDecoderHandler;
import cn.adonis.rpc.provider.NettyEncoderHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;


public class NettyChannelPoolFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyChannelPoolFactory.class);
    private static final NettyChannelPoolFactory FACTORY = new NettyChannelPoolFactory();
    private static final Map<InetSocketAddress, ArrayBlockingQueue<Channel>> CHANNEL_POOL_MAP = new ConcurrentHashMap<>();
    private static final int CHANNEL_CONNECT_SIZE = PropertyConfigeHelper.getChannelConnectSize();
    private static final String SERIALIZE_TYPE = PropertyConfigeHelper.getSerializeType();
    private ProviderService providerService;

    private NettyChannelPoolFactory() {}

    /**
     * 初始化Netty channel 连接队列Map
     * @param providerServiceMap
     */
    public void initChannelPoolFactory(Map<String, ProviderService> providerServiceMap) {
        Collection<ProviderService> services = providerServiceMap.values();
        Set<InetSocketAddress> socketAddressSet = new HashSet<>();
        for (ProviderService providerService : services) {
            String serviceIp = providerService.getServerIp();
            int servicePort = providerService.getServerPort();
            InetSocketAddress socketAddress = new InetSocketAddress(serviceIp, servicePort);
            socketAddressSet.add(socketAddress);
        }
        for (InetSocketAddress socketAddress : socketAddressSet) {
            try {
                int realChannelConnectSize = 0;
                while (realChannelConnectSize < CHANNEL_CONNECT_SIZE) {
                    Channel channel = null;
                    while (channel == null) {
                        channel = registerChannel(socketAddress);
                    }
                    realChannelConnectSize++;
                    ArrayBlockingQueue<Channel> channels = CHANNEL_POOL_MAP.get(socketAddress);
                    if (channels == null) {
                        channels = new ArrayBlockingQueue<>(CHANNEL_CONNECT_SIZE);
                        CHANNEL_POOL_MAP.put(socketAddress, channels);
                    }
                    channels.offer(channel);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 根据服务提供者地址获取对应的Netty Channel阻塞队列
     * @param socketAddress
     * @return
     */
    public ArrayBlockingQueue<Channel> acquire(InetSocketAddress socketAddress) {
        return CHANNEL_POOL_MAP.get(socketAddress);
    }

    /**
     * Channel 使用完毕后，回收到阻塞队列
     * @param blockingQueue
     * @param channel
     * @param socketAddress
     */
    public void release(BlockingQueue<Channel> blockingQueue, Channel channel, InetSocketAddress socketAddress) {
        if (blockingQueue == null) {
            return;
        }
        if (channel == null || !channel.isActive() || !channel.isOpen() || !channel.isWritable()) {
            if (channel != null) {
                channel.deregister().syncUninterruptibly().awaitUninterruptibly();
                channel.closeFuture().syncUninterruptibly().awaitUninterruptibly();
            }
            Channel newChannel = null;
            while (newChannel == null) {
                LOGGER.debug("register new channel");
                newChannel = registerChannel(socketAddress);
            }
            blockingQueue.offer(channel);
            return;
        }
        blockingQueue.offer(channel);
    }

    public Channel registerChannel(InetSocketAddress socketAddress) {
        try {
            EventLoopGroup group = new NioEventLoopGroup(10);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.remoteAddress(socketAddress);
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NettyEncoderHandler(SERIALIZE_TYPE));
                            socketChannel.pipeline().addLast(new NettyDecoderHandler(RpcResponse.class, SERIALIZE_TYPE));
                            socketChannel.pipeline().addLast(new NettyClientInvokeHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            final Channel newChannel = channelFuture.channel();
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            final List<Boolean> isSuccessHolder = new ArrayList<>(1);
            channelFuture.addListener((future -> {
                if (future.isSuccess()) {
                    isSuccessHolder.add(true);
                } else {
                    future.cause().printStackTrace();
                    isSuccessHolder.add(false);
                }
                countDownLatch.countDown();
            }));
            countDownLatch.await();
            if (isSuccessHolder.get(0)) {
                return newChannel;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static NettyChannelPoolFactory singleton() {
        return FACTORY;
    }
}
