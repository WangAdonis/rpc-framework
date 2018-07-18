package cn.adonis.rpc.strategy.impl;

import cn.adonis.rpc.model.ProviderService;
import cn.adonis.rpc.strategy.ClusterStrategy;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PollingStrategyImpl implements ClusterStrategy {

    private int index = 0;
    private Lock lock = new ReentrantLock();

    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        try {
            lock.tryLock(10, TimeUnit.MILLISECONDS);
            if (index >= providerServices.size()) {
                index = 0;
            }
            return providerServices.get(index++);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return providerServices.get(0);
    }
}
