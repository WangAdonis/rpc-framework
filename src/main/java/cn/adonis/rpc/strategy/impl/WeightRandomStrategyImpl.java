package cn.adonis.rpc.strategy.impl;

import cn.adonis.rpc.model.ProviderService;
import cn.adonis.rpc.strategy.ClusterStrategy;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;

public class WeightRandomStrategyImpl implements ClusterStrategy {
    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        List<ProviderService> weightedList = new ArrayList<>(providerServices.size());
        for (ProviderService service : providerServices) {
            int weight = service.getWeight();
            for (int i = 0; i < weight; i++) {
                weightedList.add(service);
            }
        }
        int index = RandomUtils.nextInt(0, weightedList.size() - 1);
        return weightedList.get(index);
    }
}
