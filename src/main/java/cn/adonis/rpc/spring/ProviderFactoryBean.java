package cn.adonis.rpc.spring;

import cn.adonis.rpc.helper.IPHelper;
import cn.adonis.rpc.model.ProviderService;
import cn.adonis.rpc.provider.NettyServer;
import cn.adonis.rpc.register.ProviderRegister;
import cn.adonis.rpc.register.RegisterCenter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 服务端spring入口
 */
public class ProviderFactoryBean implements FactoryBean, InitializingBean {

    /**
     * 服务接口
     */
    private Class<?> serviceInterface;
    /**
     * 服务实现
     */
    private Object serviceImpl;
    /**
     * 服务端口
     */
    private int serverPort;
    /**
     * 超时时间
     */
    private long timeout;
    /**
     * 服务代理对象
     */
    private Object serviceProxyObject;
    /**
     * 服务提供者唯一标识
     */
    private String appKey;
    /**
     * 服务分组组名
     */
    private String groupName = "default";
    /**
     * 服务提供者权重，默认为1，范围1-100
     */
    private int weight = 1;
    /**
     * 服务端线程数，默认为10
     */
    private int workerThreads = 10;

    @Nullable
    @Override
    public Object getObject() throws Exception {
        return serviceProxyObject;
    }

    @Nullable
    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ProviderRegister providerRegister = RegisterCenter.singleton();
        NettyServer.singleton().start(this.serverPort);
        ProviderService providerService = buildProviderService();
        providerRegister.registerProvider(providerService);
    }

    private ProviderService buildProviderService() {
        ProviderService providerService = new ProviderService();
        Method[] methods = serviceImpl.getClass().getMethods();
        providerService.setServiceInterface(serviceInterface);
        providerService.addMethods(methods);
        providerService.setServerIp(IPHelper.localIp());
        providerService.setServerPort(serverPort);
        providerService.setTimeout(timeout);
        providerService.setWeight(weight);
        providerService.setWorkerThreads(workerThreads);
        providerService.setAppKey(appKey);
        providerService.setGroupName(groupName);
        return providerService;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public Object getServiceImpl() {
        return serviceImpl;
    }

    public void setServiceImpl(Object serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Object getServiceProxyObject() {
        return serviceProxyObject;
    }

    public void setServiceProxyObject(Object serviceProxyObject) {
        this.serviceProxyObject = serviceProxyObject;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }
}
