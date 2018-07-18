package cn.adonis.rpc.consumer;

import cn.adonis.rpc.model.ProviderService;
import cn.adonis.rpc.model.RpcRequest;
import cn.adonis.rpc.model.RpcResponse;
import cn.adonis.rpc.register.ProviderRegister;
import cn.adonis.rpc.register.RegisterCenter;
import cn.adonis.rpc.strategy.ClusterStrategy;
import cn.adonis.rpc.strategy.ClusterStrategyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ConsumerProxyBeanFactory implements InvocationHandler {
    private ExecutorService fixedThreadPool;
    private Class<?> targetInterface;
    private int timeout;
    private static int threadNumber = 10;
    private String clusterStrategy;

    public ConsumerProxyBeanFactory(Class<?> targetInterface, int timeout, String clusterStrategy) {
        this.targetInterface = targetInterface;
        this.timeout = timeout;
        this.clusterStrategy = clusterStrategy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String serviceKey = targetInterface.getName();
        ProviderRegister providerRegister = RegisterCenter.singleton();
        List<ProviderService> providerServices = providerRegister.getProviderServiceMap().get(serviceKey);
        ClusterStrategy clusterStrategy = ClusterStrategyFactory.create(this.clusterStrategy);
        ProviderService providerService = clusterStrategy.select(providerServices).copy();
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setUniqueKey(UUID.randomUUID().toString() + "-" + Thread.currentThread().getId());
        rpcRequest.setProviderService(providerService.copy());
        rpcRequest.setTimeout(timeout);
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setArgs(args);
        try {
            if (fixedThreadPool == null) {
                synchronized (ConsumerProxyBeanFactory.class) {
                    if (fixedThreadPool == null) {
                        fixedThreadPool = Executors.newFixedThreadPool(threadNumber);
                    }
                }
            }
            String serverIp = rpcRequest.getProviderService().getServerIp();
            int serverPort = rpcRequest.getProviderService().getServerPort();
            InetSocketAddress socketAddress = new InetSocketAddress(serverIp, serverPort);
            Future<RpcResponse> responseFuture = fixedThreadPool.submit(ConsumerServiceCallable.of(socketAddress, rpcRequest));
            RpcResponse response = responseFuture.get(rpcRequest.getTimeout(), TimeUnit.MILLISECONDS);
            if (response != null) {
                return response.getResult();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{targetInterface}, this::invoke);
    }

    private static volatile  ConsumerProxyBeanFactory singleton;

    public static ConsumerProxyBeanFactory singleton(Class<?> targetInterface, int timeout, String clusterStrategy) {
        if (singleton == null) {
            synchronized (ConsumerProxyBeanFactory.class) {
                if (singleton == null) {
                    singleton = new ConsumerProxyBeanFactory(targetInterface, timeout, clusterStrategy);
                }
            }
        }
        return singleton;
    }
}
