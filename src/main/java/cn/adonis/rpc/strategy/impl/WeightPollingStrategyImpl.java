package cn.adonis.rpc.strategy.impl;

import cn.adonis.rpc.model.ProviderService;
import cn.adonis.rpc.strategy.ClusterStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WeightPollingStrategyImpl implements ClusterStrategy {
    private int index;
    private Lock lock = new ReentrantLock();

    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        try {
            lock.tryLock(10, TimeUnit.MILLISECONDS);
            List<ProviderService> weightedList = new ArrayList<>(providerServices.size());
            for (ProviderService service : providerServices) {
                int weight = service.getWeight();
                for (int i = 0; i < weight; i++) {
                    weightedList.add(service.copy());
                }
            }
            if (index >= weightedList.size()) {
                index = 0;
            }
            return weightedList.get(index ++);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return providerServices.get(0);
    }
}
