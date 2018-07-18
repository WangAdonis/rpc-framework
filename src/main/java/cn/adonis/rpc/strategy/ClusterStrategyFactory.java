package cn.adonis.rpc.strategy;

import cn.adonis.rpc.strategy.impl.*;

import java.util.HashMap;
import java.util.Map;

public class ClusterStrategyFactory {
    private static final Map<String, ClusterStrategy> MAP = new HashMap<String, ClusterStrategy>(){{
        put("Random", new RandomStrategyImpl());
        put("WeightRandom", new WeightRandomStrategyImpl());
        put("Polling", new PollingStrategyImpl());
        put("WeightPolling", new WeightPollingStrategyImpl());
        put("Hash", new HashStrategyImpl());
    }};

    public static ClusterStrategy create(String strategy) {
        return MAP.getOrDefault(strategy, MAP.get("WeightRandom"));
    }
}
