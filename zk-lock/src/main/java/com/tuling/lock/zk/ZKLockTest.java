package com.tuling.lock.zk;

import com.tuling.lock.Lock;
import com.tuling.service.OrderCodeGenerator;


/**
 * @Author Fox
 *
 */
public class ZKLockTest implements Runnable{

    private  OrderCodeGenerator orderCodeGenerator = new OrderCodeGenerator();

    //private  Lock lock = new ZkDistributedLock("localhost:2181", "/lock");
    private Lock lock = new ZkDistributedLock2("localhost:2181", "/locks");

    @Override
    public void run() {

        lock.lock();
        try {
            String orderCode = orderCodeGenerator.getOrderCode();
            System.out.println("生成订单号 "+orderCode);
        }finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        for(int i=0;i<30;i++){
            new Thread(new ZKLockTest()).start();
        }

        Thread.currentThread().join();
    }


}
