package com.tuling.lock.zk;

import com.tuling.lock.AbstractLock;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * @Author Fox
 *
 */
public class ZkDistributedLock extends AbstractLock implements IZkDataListener{

    private ZkClient zkClient ;
    private String path = "/lock";

    private CountDownLatch countDownLatch ;
    private String config;


    public  ZkDistributedLock(String config,String path){
        zkClient = new ZkClient(config);
        this.config = config;
        this.path = path;
    }


    @Override
    public boolean tryLock() {
        try {
            if(zkClient ==null){
                //建立连接
                zkClient = new ZkClient(config);
            }
            // 创建临时节点
            zkClient.createEphemeral(path);

        }catch (Exception e){
            //存在节点
            return false;
        }
        return true;
    }



    @Override
    public void waitLock() {
        //注册监听
        zkClient.subscribeDataChanges(path,this);
        if(zkClient.exists(path)){
            countDownLatch = new CountDownLatch(1);
            try {
                countDownLatch.await();  //计数器变为0之前，都会阻塞
                // 解除监听
                zkClient.unsubscribeDataChanges(path,this);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void unlock() {

        if(zkClient !=null){
            zkClient.delete(path);
            System.out.println("-----释放锁资源----");
        }

    }


    @Override
    public void handleDataChange(String dataPath, Object data) throws Exception {

    }

    @Override
    public void handleDataDeleted(String dataPath) throws Exception {
        countDownLatch.countDown();
    }
}
