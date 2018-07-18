package cn.adonis.rpc.model;

import java.util.Map;
import java.util.concurrent.*;

public class RpcResponseHolder {
    private static final Map<String, RpcResponseWrapper> RESPONSE_WRAPPER_MAP = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService REMOVE_EXPIRE_KEY_EXECUTOR = Executors.newScheduledThreadPool(1);
    static {
        REMOVE_EXPIRE_KEY_EXECUTOR.scheduleAtFixedRate(() -> {
            for (Map.Entry<String, RpcResponseWrapper> entry : RESPONSE_WRAPPER_MAP.entrySet()) {
                boolean isExpire = entry.getValue().isExpire();
                if (isExpire) {
                    RESPONSE_WRAPPER_MAP.remove(entry.getKey());
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    public static void initResponseData(String requestUniqueKey) {
        RESPONSE_WRAPPER_MAP.put(requestUniqueKey, RpcResponseWrapper.of());
    }

    public static void putResultValue(RpcResponse response) {
        long currentTime = System.currentTimeMillis();
        RpcResponseWrapper responseWrapper = RESPONSE_WRAPPER_MAP.get(response.getUniqueKey());
        responseWrapper.setResponseTime(currentTime);
        responseWrapper.getResponseBlockingQueue().add(response);
        RESPONSE_WRAPPER_MAP.put(response.getUniqueKey(), responseWrapper);
    }

    public static RpcResponse getValue(String requestUniqueKey, long timeout) {
        RpcResponseWrapper responseWrapper = RESPONSE_WRAPPER_MAP.get(requestUniqueKey);
        try {
            return responseWrapper.getResponseBlockingQueue().poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            RESPONSE_WRAPPER_MAP.remove(requestUniqueKey);
        }
    }
}
