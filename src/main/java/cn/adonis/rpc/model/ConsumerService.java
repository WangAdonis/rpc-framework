package cn.adonis.rpc.model;

import java.io.Serializable;
import java.lang.reflect.Method;

public class ConsumerService implements Serializable {
    private Class<?> serviceInterface;
    private Object serviceImpl;
    private Method serviceMethod;
    private String invokerIp;
    private int invokerPort;
    private long timeout;
    //服务提供者唯一标识
    private String remoteAppKey;
    //服务分组组名
    private String groupName = "default";

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

    public Method getServiceMethod() {
        return serviceMethod;
    }

    public void setServiceMethod(Method serviceMethod) {
        this.serviceMethod = serviceMethod;
    }

    public String getInvokerIp() {
        return invokerIp;
    }

    public void setInvokerIp(String invokerIp) {
        this.invokerIp = invokerIp;
    }

    public int getInvokerPort() {
        return invokerPort;
    }

    public void setInvokerPort(int invokerPort) {
        this.invokerPort = invokerPort;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getRemoteAppKey() {
        return remoteAppKey;
    }

    public void setRemoteAppKey(String remoteAppKey) {
        this.remoteAppKey = remoteAppKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
