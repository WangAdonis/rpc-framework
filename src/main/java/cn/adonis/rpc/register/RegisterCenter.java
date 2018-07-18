package cn.adonis.rpc.register;

import cn.adonis.rpc.helper.IPHelper;
import cn.adonis.rpc.helper.PropertyConfigeHelper;
import cn.adonis.rpc.model.ConsumerService;
import cn.adonis.rpc.model.ProviderService;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegisterCenter implements ProviderRegister, ConsumerRegister {

    private static final RegisterCenter CENTER = new RegisterCenter();

    private static final Map<String, List<ProviderService>> PROVIDER_SERVICE_MAP = new ConcurrentHashMap<>();

    private static final Map<String, List<ProviderService>> CONSUMER_SERVICE_MAP = new ConcurrentHashMap<>();

    private static final String ZK_SERVICE = PropertyConfigeHelper.getZkService();

    private static final int ZK_SESSION_TIME_OUT = PropertyConfigeHelper.getZkSessionTimeout();

    private static final int ZK_CONNECTION_TIME_OUT = PropertyConfigeHelper.getZkConnectionTimeout();

    private static final String ROOT_PATH = "/config_register";

    public static final String PROVIDER_TYPE = "/provider";

    public static final String CONSUMER_TYPE = "/consumer";

    private static volatile ZkClient zkClient = null;

    public static RegisterCenter singleton() {
        return CENTER;
    }

    @Override
    public void initProviderMap() {

    }

    @Override
    public Map<String, List<ProviderService>> getConsumerServiceMap() {
        return CONSUMER_SERVICE_MAP;
    }

    @Override
    public void registerConsumer(ConsumerService consumerService) {

    }

    @Override
    public void registerProvider(ProviderService serviceMetaData) {
        if (serviceMetaData == null || CollectionUtils.isEmpty(serviceMetaData.getServiceMethods())) {
            return;
        }
        synchronized (RegisterCenter.class) {
            String interfaceKey = serviceMetaData.getServiceInterface().getName();
            List<ProviderService> providers = PROVIDER_SERVICE_MAP.get(interfaceKey);
            if (providers == null) {
                PROVIDER_SERVICE_MAP.put(interfaceKey, new ArrayList<ProviderService>(){{
                    add(serviceMetaData);
                }});
            } else {
                providers.add(serviceMetaData);
            }
            if (zkClient == null) {
                zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
            }
            //创建 ZK命名空间/当前部署应用APP命名空间/
            String appKey = serviceMetaData.getAppKey();
            final String zkPath = ROOT_PATH + "/" + appKey;
            if (!zkClient.exists(zkPath)) {
                zkClient.createPersistent(zkPath, true);
            }

            //服务分组
            String groupName = serviceMetaData.getGroupName();
            //创建服务提供者
            String serviceNode = interfaceKey;
            String servicePath = zkPath + "/" + groupName + "/" + serviceNode + "/" + PROVIDER_TYPE;
            if (!zkClient.exists(servicePath)) {
                zkClient.createPersistent(servicePath, true);
            }

            //创建当前服务器节点
            int serverPort = serviceMetaData.getServerPort(); //服务端口
            int weight = serviceMetaData.getWeight(); //服务权重
            int workerThreads = serviceMetaData.getWorkerThreads(); //服务工作线程
            String localIp = IPHelper.localIp();
            String currentServiceIpNode = servicePath + "/" + localIp + "|" + serverPort + "|" + weight + "|" + workerThreads + "|" + groupName;
            if (!zkClient.exists(currentServiceIpNode)) {
                //注意,这里创建的是临时节点
                zkClient.createEphemeral(currentServiceIpNode);
            }

            //监听注册服务的变化,同时更新数据到本地缓存
            zkClient.subscribeChildChanges(servicePath, new IZkChildListener() {
                @Override
                public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
//                        if (currentChilds == null) {
//                            currentChilds = Lists.newArrayList();
//                        }
//
//                        //存活的服务IP列表
//                        List<String> activityServiceIpList = Lists.newArrayList(Lists.transform(currentChilds, new Function<String, String>() {
//                            @Override
//                            public String apply(String input) {
//                                return StringUtils.split(input, "|")[0];
//                            }
//                        }));
//                        refreshActivityService(activityServiceIpList);
                }
            });
        }
    }


    @Override
    public Map<String, List<ProviderService>> getProviderServiceMap() {
        return PROVIDER_SERVICE_MAP;
    }
}
