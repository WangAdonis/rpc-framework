package cn.adonis.rpc.register;

import cn.adonis.rpc.model.ConsumerService;
import cn.adonis.rpc.model.ProviderService;

import java.util.List;
import java.util.Map;

public interface ConsumerRegister {
    /**
     * 消费端初始化服务提供者信息本地缓存
     */
    void initProviderMap();

    /**
     * 消费端获取服务提供者信息
     * @return
     */
    Map<String, List<ProviderService>> getConsumerServiceMap();

    /**
     * 消费端将消费者信息注册到zookeeper对应的节点下
     * @param consumerService
     */
    void registerConsumer(final ConsumerService consumerService);
}
