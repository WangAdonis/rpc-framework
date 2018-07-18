package cn.adonis.rpc.strategy.impl;

import cn.adonis.rpc.helper.IPHelper;
import cn.adonis.rpc.model.ProviderService;
import cn.adonis.rpc.strategy.ClusterStrategy;

import java.util.List;

public class HashStrategyImpl implements ClusterStrategy {
    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        String ip = IPHelper.localIp();
        int hashCode = ip.hashCode();
        int size = providerServices.size();
        return providerServices.get(hashCode % size);
    }
}
