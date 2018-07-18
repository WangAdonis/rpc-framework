package cn.adonis.rpc.strategy.impl;

import cn.adonis.rpc.model.ProviderService;
import cn.adonis.rpc.strategy.ClusterStrategy;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

public class RandomStrategyImpl implements ClusterStrategy {
    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        int index = RandomUtils.nextInt(0, providerServices.size() - 1);
        return providerServices.get(index);
    }
}
