package cn.adonis.rpc.register;

import cn.adonis.rpc.model.ProviderService;

import java.util.List;
import java.util.Map;

public interface ProviderRegister {
    /**
     * 服务端将服务提供者信息注册到zookeeper对应节点下
     * @param serviceMetaData
     */
    void registerProvider(final ProviderService serviceMetaData);

    /**
     * 服务端获取服务提供者信息
     * @return
     */
    Map<String, List<ProviderService>> getProviderServiceMap();
}
