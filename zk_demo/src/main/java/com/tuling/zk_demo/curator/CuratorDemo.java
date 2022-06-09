package com.tuling.zk_demo.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;

/**
 * @Author Fox
 */
public class CuratorDemo {

    private static final  String  CLUSTER_CONNECT_STR="zookeeper.localhost.com";

    public static void main(String[] args) throws Exception {
        //构建客户端实例
        CuratorFramework curatorFramework= CuratorFrameworkFactory.builder()
                .connectString(CLUSTER_CONNECT_STR)
                .sessionTimeoutMs(4000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .namespace("")
                .authorization("digest", "xxxxx:xxxx".getBytes(StandardCharsets.UTF_8))
                .build();
        //启动客户端
        curatorFramework.start();
        Stat stat=new Stat();
        //查询节点数据
        byte[] bytes = curatorFramework.getData().storingStatIn(stat).forPath("/user");
        System.out.println(new String(bytes));

        curatorFramework.close();
    }
}
