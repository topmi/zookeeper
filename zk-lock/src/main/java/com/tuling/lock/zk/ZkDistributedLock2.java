package com.tuling.lock.zk;

import com.tuling.lock.AbstractLock;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author Fox
 *
 */
public class ZkDistributedLock2 extends AbstractLock implements IZkDataListener {

    private ZkClient zkClient;
    private String path;
    private String config;

    private CountDownLatch countDownLatch ;
    private String beforePath; // 当前节点前一个节点
    private String currentPath; // 当前节点


    public ZkDistributedLock2(String config, String path){
        this.path = path;
        this.config = config;
        zkClient = new ZkClient(config);
        if (!zkClient.exists(path)) {
            // 如果根节点不存在，则创建根节点
            zkClient.createPersistent(path);
        }
    }

    @Override
    public void lock(){
        //尝试获取锁
        boolean locked = tryLock();
        if(locked){
            System.out.println("---------获取锁---------");
        }
        while (!locked){
            //等待锁 阻塞
            waitLock();
            //重试
            //获取等待的子节点列表
            List<String> childrens = zkClient.getChildren(path);
            //判断，是否加锁成功
            if (checkLocked(childrens)) {
                locked = true;
            }
        }

    }



    @Override
    public boolean tryLock() {
        try {
            //创建临时有序的节点  -e -s
            currentPath = zkClient.createEphemeralSequential(path+"/",null);
            //获取到所有子节点
            List<String> childrens = zkClient.getChildren(path);
            //获取等待的子节点列表，判断自己是否第一个
            if (checkLocked(childrens)) {
                return true;
            }
            // 若不是第一个，则找到自己的前一个节点
            int index =  Collections.binarySearch(childrens,
                    currentPath.substring(currentPath.lastIndexOf("/") + 1));
            if(index<0){
                throw new Exception(currentPath+"节点没有找到" );
            }
            //如果自己没有获得锁，则要监听前一个节点
            beforePath = path + "/" + childrens.get(index-1);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    private boolean checkLocked(List<String> childrens) {

        //节点按照编号，升序排列
        Collections.sort(childrens);

        // 如果是第一个，代表自己已经获得了锁
        if (currentPath.equals(path + "/" +childrens.get(0))) {
            System.out.println("成功的获取分布式锁,节点为"+ currentPath);
            return true;
        }
        return false;
    }

    @Override
    public void waitLock() {
        try {
            countDownLatch = new CountDownLatch(1);
            if(zkClient.exists(beforePath)) {
                //订阅比自己次小顺序节点的删除事件   index-1
                zkClient.subscribeDataChanges(beforePath, this);
                countDownLatch.await();
                zkClient.unsubscribeDataChanges(beforePath, this);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void unlock() {

        if(zkClient !=null){
            //删除临时节点
            zkClient.delete(currentPath,-1);
            System.out.println(currentPath+" 节点释放锁资源");
        }
    }


    @Override
    public void handleDataChange(String dataPath, Object data) throws Exception {

    }

    @Override
    public void handleDataDeleted(String dataPath) throws Exception {
        countDownLatch.countDown(); //减1
    }
}
