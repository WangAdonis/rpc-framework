package cn.adonis.rpc.provider;

import cn.adonis.rpc.model.ProviderService;
import cn.adonis.rpc.model.RpcRequest;
import cn.adonis.rpc.model.RpcResponse;
import cn.adonis.rpc.register.ProviderRegister;
import cn.adonis.rpc.register.RegisterCenter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class NettyServerInvokeHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerInvokeHandler.class);

    /**
     * 服务端限流
     */
    private static final Map<String, Semaphore> SERVICE_KEY_SEMAPHORE_MAP = new ConcurrentHashMap<>();

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
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        if (ctx.channel().isWritable()) {
            ProviderService metaData = request.getProviderService();
            long timeout = request.getTimeout();
            final String methodName = request.getMethodName();
            String serviceKey = metaData.getServiceInterface().getName();
            // 获取限流工具类
            int workerThread = metaData.getWorkerThreads();
            Semaphore semaphore = SERVICE_KEY_SEMAPHORE_MAP.get(serviceKey);
            // 初始化流控基础设施
            if (semaphore == null) {
                synchronized (SERVICE_KEY_SEMAPHORE_MAP) {
                    semaphore = SERVICE_KEY_SEMAPHORE_MAP.get(serviceKey);
                    if (semaphore == null) {
                        semaphore = new Semaphore(workerThread);
                        SERVICE_KEY_SEMAPHORE_MAP.put(serviceKey, semaphore);
                    }
                }
            }
            ProviderRegister providerRegister = RegisterCenter.singleton();
            ProviderService providerService = providerRegister.getProviderServiceMap().get(serviceKey);
            Object serviceObject = providerService.getServiceObject();
            Method method = providerService.getMethod(methodName);
            Object result = null;
            boolean acquire = false;
            try {
                acquire = semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
                if (acquire) {
                    result = method.invoke(serviceObject, request.getArgs());
                }
            } catch (Exception e) {
                result = e;
            } finally {
                if (acquire) {
                    semaphore.release();
                }
            }
            RpcResponse response = new RpcResponse();
            response.setInvokeTimeout(timeout);
            response.setUniqueKey(request.getUniqueKey());
            response.setResult(result);
            ctx.writeAndFlush(response);
        } else {
            LOGGER.error("channel closed !");
        }

    }
}
