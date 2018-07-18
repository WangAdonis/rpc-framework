package cn.adonis.rpc.strategy;

import cn.adonis.rpc.model.ProviderService;

import java.util.List;

public interface ClusterStrategy {
    /**
     * 根据软负载均衡策略选取服务端
     * @param providerServices
     * @return
     */
    ProviderService select(List<ProviderService> providerServices);
}
