package cn.adonis.rpc.model;

import java.util.concurrent.*;

public class RpcResponseWrapper {
//    private BlockingQueue<RpcResponse> responseBlockingQueue = new ArrayBlockingQueue<>(1);
    private BlockingQueue<RpcResponse> responseBlockingQueue = new SynchronousQueue<>();

    private long responseTime;

    public boolean isExpire() {
        RpcResponse response = responseBlockingQueue.peek();
        if (response == null) {
            return false;
        }
        long timeout = response.getInvokeTimeout();
        if ((System.currentTimeMillis() - responseTime) > timeout) {
            return true;
        }
        return false;
    }

    public static RpcResponseWrapper of() {
        return new RpcResponseWrapper();
    }

    public BlockingQueue<RpcResponse> getResponseBlockingQueue() {
        return responseBlockingQueue;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
}
