package cn.adonis.rpc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderService implements Serializable {

    private Class<?> serviceInterface;
    private transient Object serviceObject;
    @JsonIgnore
    private transient Map<String, Method> serviceMethods = new ConcurrentHashMap<>();
    private String serverIp;
    private int serverPort;
    private long timeout;
    //该服务提供者权重
    private int weight;
    //服务端线程数
    private int workerThreads;
    //服务提供者唯一标识
    private String appKey;
    //服务分组组名
    private String groupName;

    public ProviderService copy() {
        ProviderService providerService = new ProviderService();
        providerService.setServiceInterface(this.serviceInterface);
        providerService.setServiceObject(this.serviceObject);
        providerService.setServiceMethods(this.serviceMethods);
        providerService.setServerIp(this.serverIp);
        providerService.setServerPort(this.serverPort);
        providerService.setTimeout(this.timeout);
        providerService.setWeight(this.weight);
        providerService.setWorkerThreads(this.workerThreads);
        providerService.setAppKey(this.appKey);
        providerService.setGroupName(this.groupName);
        return providerService;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public void setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
    }

    public Map<String, Method> getServiceMethods() {
        return serviceMethods;
    }

    public void setServiceMethods(Map<String, Method> serviceMethods) {
        this.serviceMethods = serviceMethods;
    }

    public void addMethod(Method method) {
        this.serviceMethods.put(method.getName(), method);
    }

    public void addMethods(Method[] methods) {
        for (Method method : methods) {
            addMethod(method);
        }
    }

    public Method getMethod(String methodName) {
        return this.serviceMethods.get(methodName);
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
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
}
