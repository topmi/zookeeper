package com.tuling.zk_demo.client;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZkServiceProvider implements Watcher {
    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    private static ZooKeeper zooKeeper = null;
    private static String rootPath = "/zkTest";

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
            if (Event.EventType.None == watchedEvent.getType() && null == watchedEvent.getPath()) {
                countDownLatch.countDown();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        zooKeeper = new ZooKeeper("localhost:2181", 5000, new ZkServiceProvider());
//        zooKeeper = new ZooKeeper("zookeeper.localhost.com:2181", 5000, new ZkServiceProvider());
//        zooKeeper.addAuthInfo("digest", "xxxxx:xxxxx".getBytes(StandardCharsets.UTF_8));
        countDownLatch.await();
        zooKeeper.create(rootPath+"t4", "test t1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        System.out.println("创建集群节点t1:" + rootPath + "t1");
//        zooKeeper.getData(rootPath+"t4", true, stat);
        Thread.sleep(Integer.MAX_VALUE);
    }
}
