package org.waithua.io.redis;

/**
 * Created by jch on 17/1/3.
 *
 * 假如你已经安装好了redis
 */
public class JedisClientTest {

    public static void main(String[] args) throws InterruptedException {
        String a = JedisClient.set("1234",30*60, "2345");
        a = JedisClient.get("1234");
        a = JedisClient.set("123", 1, "23456");
        a = JedisClient.get("123");
        Thread.sleep(2000);
        a = JedisClient.get("123");
        a = JedisClient.get("1234");
    }
}
