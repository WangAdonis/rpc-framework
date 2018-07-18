package cn.adonis.rpc.provider;

import cn.adonis.rpc.helper.PropertyConfigeHelper;
import cn.adonis.rpc.model.RpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyServer {
    private static final NettyServer SERVER = new NettyServer();
    /**
     * boss线程组
     */
    private EventLoopGroup bossGroup;
    /**
     * worker线程组
     */
    private EventLoopGroup workerGroup;

    private String serializeType = PropertyConfigeHelper.getSerializeType();

    public void start(final int port) {
        synchronized (NettyServer.class) {
            if (this.bossGroup != null || this.workerGroup != null) {
                return;
            }
            this.bossGroup = new NioEventLoopGroup();
            this.workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NettyDecoderHandler(RpcRequest.class, serializeType));
                            socketChannel.pipeline().addLast(new NettyEncoderHandler(serializeType));
                            socketChannel.pipeline().addLast(new NettyServerInvokeHandler());
                        }
                    });
            try {
                bootstrap.bind(port).sync().channel();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private NettyServer(){}

    public static NettyServer singleton() {
        return SERVER;
    }
}
