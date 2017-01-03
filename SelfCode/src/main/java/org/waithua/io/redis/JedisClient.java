package org.waithua.io.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by jch on 17/1/3.
 */
@Slf4j
public class JedisClient {

    private static String connString;

//    private static String serverlist = "redis://1f12ce4689894686:Kvst0re12308@1f12ce4689894686.m.cnhza.kvstore.aliyuncs.com:6379/0"; //线上地址
	private static String serverlist = "redis://127.0.0.1:6379/0"; //本地地址


    public static void setConnString(String connString) {
        JedisClient.connString = connString;
    }

    public static String getConnString() {
        try {
            if (StringUtils.isEmpty(connString)) {
                // 从配置文件中读取KVStore配置信息
                // ConfigurationManager cm = new ConfigurationManager();
                // String serverlist = cm.readConfig("jedis.serverlist");
//				String serverlist = "redis://fd210e4bc39b11e4:18575593355Com@fd210e4bc39b11e4.m.cnhza.kvstore.aliyuncs.com:6379/0";
                if (StringUtils.isEmpty(serverlist))
                    return "";
                setConnString(serverlist);
            }
        } catch (Exception ex) {
            log.error("getConnString:error:" + ex.getMessage());
        }
        return connString;
    }

    private static JedisPool pool = null;

    public static synchronized JedisPool getPool() {
        if (pool == null) {
            try {
                String urlString = getConnString();
                final URI uri = URI.create(urlString);
                String host = uri.getHost();
                int port = uri.getPort();
                String password = uri.getUserInfo();

                JedisPoolConfig config = getPoolConfig();
                pool = new JedisPool(config, host, port,
                        Protocol.DEFAULT_TIMEOUT, password);
            } catch (Exception e) {
                log.error("getPool:Exception:" + e.getMessage());
                e.printStackTrace();
            }
        }

        return pool;
    }

    /*
     * 连接池配置信息，您可以根据具体情况修改
     */
    private static JedisPoolConfig getPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        // 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        // config.setBlockWhenExhausted(true);
        // 设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
        // config.setEvictionPolicyClassName(
        // "org.apache.commons.pool2.impl.DefaultEvictionPolicy" );
        // 是否启用pool的jmx管理功能, 默认true
        // config.setJmxEnabled( true );
        // MBean ObjectName = new
        // ObjectName("org.apache.commons.pool2:type=GenericObjectPool,name=" +
        // "pool" + i); 默 认为"pool", JMX不熟,具体不知道是干啥的...默认就好.
        // config.setJmxNamePrefix( "pool" );
        // 是否启用后进先出, 默认true
        // config.setLifo( true );
        // 最大空闲连接数, 默认8个
        config.setMaxIdle(150);
        // 最小空闲连接数, 默认0
        config.setMinIdle(100);
        // 最大连接数, 默认8个
        config.setMaxTotal(750);
        // 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,
        // 默认-1
        config.setMaxWaitMillis(10000);

        // 在获取连接的时候检查有效性, 默认false
        config.setTestOnBorrow(true);
        // 返回一个jedis实例给连接池时，是否检查连接可用性（ping()）
        config.setTestOnReturn(true);
        // 在空闲时检查有效性, 默认false
        config.setTestWhileIdle(true);
        // 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        config.setMinEvictableIdleTimeMillis(1000L * 60L * 1L);
        // 对象空闲多久后逐出, 当空闲时间>该值 ，且 空闲连接>最大空闲数
        // 时直接逐出,不再根据MinEvictableIdleTimeMillis判断 (默认逐出策略)，默认30m
        config.setSoftMinEvictableIdleTimeMillis(1000L * 60L * 1L);
        // 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        config.setTimeBetweenEvictionRunsMillis(60000); // 1m
        // 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        config.setNumTestsPerEvictionRun(10);

        return config;
    }

    public static String get(final String key) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = getPool().getResource();
            return jedis.get(key);
        } catch (JedisException e) {
            log.error(e.getMessage());
            success = false;
            if (jedis != null)
                jedis.close();
            // throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            success = false;
            if (jedis != null)
                jedis.close();
            // throw e;
        } finally {
            if (success && jedis != null)
                jedis.close();
        }
        return null;
    }

    public static List<String> keys(final String key) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = getPool().getResource();
            Set<String> set = jedis.keys(key);
            List<String> list = null;
            if(set!=null && set.size()>0){
                list = new ArrayList<String>();
                for (String keyStr : set) {
                    list.add(keyStr);
                }
                return list;
            }
        } catch (JedisException e) {
            log.error(e.getMessage());
            success = false;
            if (jedis != null)
                jedis.close();
            // throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            success = false;
            if (jedis != null)
                jedis.close();
            // throw e;
        } finally {
            if (success && jedis != null)
                jedis.close();
        }
        return null;
    }


    public static List<String> gets(final String key) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = getPool().getResource();
            Set<String> set = jedis.keys(key);
            if(set!=null && set.size()>0){
                String[] strArr = new String[set.size()];
                int i = 0;
                for (String keyList : set) {
                    strArr[i] = keyList;
                    i++;
                }
                List<String> listRet = jedis.mget(strArr);
                if (listRet != null && listRet.size() > 0) {
                    return listRet;
                }
            }
        } catch (JedisException e) {
            log.error(e.getMessage());
            success = false;
            if (jedis != null)
                jedis.close();
            // throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            success = false;
            if (jedis != null)
                jedis.close();
            // throw e;
        } finally {
            if (success && jedis != null)
                jedis.close();
        }
        return null;
    }


    /**
     * 分页查询   没有验证
     * @param key  查询条件
     * @param start   页码
     * @param pageSize   每页数量
     * @return
     * @return List<String>
     *
     * @author plc
     * @date 2015年6月24日 下午1:19:28
     *
     */
    @Deprecated
    public static List<String> getsByPage(final String key,int start,int pageSize) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = getPool().getResource();

            SortingParams sortingParameters = new SortingParams();
            sortingParameters.desc();
            sortingParameters.alpha();
            sortingParameters.limit(start, pageSize);
//			sortingParameters.get(key);
            List<String> listRet = jedis.sort(key, sortingParameters);
            if (listRet != null && listRet.size() > 0) {
                return listRet;
            }
        } catch (JedisException e) {
            success = false;
            if (jedis != null)
                jedis.close();
            // throw e;
        } catch (Exception e) {
            success = false;
            if (jedis != null)
                jedis.close();
            // throw e;
        } finally {
            if (success && jedis != null)
                jedis.close();
        }
        return null;
    }


    public static String set(final String key, final int seconds,
                             final String value) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = getPool().getResource();
            return jedis.setex(key, seconds, value);
        } catch (JedisException e) {
            log.error(e.getMessage());
            success = false;
            if (jedis != null)
                jedis.close();
            // throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            success = false;
            if (jedis != null)
                jedis.close();
            // throw e;
        } finally {
            if (success && jedis != null)
                jedis.close();
        }
        return "";
    }

    public static long del(String key) {
        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = getPool().getResource();
            return jedis.del(key);
        } catch (JedisException e) {
            success = false;
            if (jedis != null)
                jedis.close();

            // throw e;
        } catch (Exception e) {
            success = false;
            if (jedis != null)
                jedis.close();
            // throw e;
        } finally {
            if (success && jedis != null)
                jedis.close();
        }
        return -1;
    }

    public static void dels(String pattern) {
        if (pattern == null || pattern.equals(""))
            return;
        if (!pattern.endsWith("*"))
            pattern += "*";

        Jedis jedis = null;
        boolean success = true;
        try {
            jedis = getPool().getResource();
            Set<String> keys = jedis.keys(pattern);

            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                jedis.del(key);
            }
        } catch (JedisException e) {
            success = false;
            if (jedis != null)
                jedis.close();
            // throw e;
        } catch (Exception e) {
            success = false;
            if (jedis != null)
                jedis.close();
            // throw e;
        } finally {
            if (success && jedis != null)
                jedis.close();
        }
    }
}
