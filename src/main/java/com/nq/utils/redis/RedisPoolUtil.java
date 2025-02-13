package com.nq.utils.redis;


import java.util.Properties;

import com.nq.utils.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis连接池工具类
 */
public class RedisPoolUtil {
    private static JedisPool jedisPool = null;
    private static String redisConfigFile = "application.properties";
    //把redis连接对象放到本地线程中
    private static ThreadLocal<Jedis> local=new ThreadLocal<Jedis>();

    //不允许通过new创建该类的实例
    private RedisPoolUtil() {
    }

    /**
     * 初始化Redis连接池
     */
    public static void initialPool() {
        try {
            Properties props = new Properties();
            //加载连接池配置文件
            props.load(RedisPoolUtil.class.getClassLoader().getResourceAsStream(redisConfigFile));
            // 创建jedis池配置实例
            JedisPoolConfig config = new JedisPoolConfig();
            // 设置池配置项值
            config.setMaxTotal(Integer.valueOf(PropertiesUtil.getProperty("redis.max.total", "20")));
            config.setMaxIdle(Integer.valueOf(PropertiesUtil.getProperty("redis.max.idle", "10")));
            config.setMaxWaitMillis(Long.valueOf(PropertiesUtil.getProperty("redis.max.wait.millis", "10000")));
            config.setTestOnBorrow(Boolean.valueOf(PropertiesUtil.getProperty("redis.test.borrow", "true")));
            config.setTestOnReturn(Boolean.valueOf(PropertiesUtil.getProperty("redis.test.return", "true")));
            // 根据配置实例化jedis池
            jedisPool = new JedisPool(config, PropertiesUtil.getProperty("redis1.ip"),
                    Integer.valueOf(PropertiesUtil.getProperty("redis1.port")),
                    Integer.valueOf(PropertiesUtil.getProperty("redis1.timeout", "1000")),
                    PropertiesUtil.getProperty("redis1.pwd"));
            System.out.println("线程池被成功初始化");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得连接
     * @return Jedis
     */
    public static Jedis getConn() {
        //Redis对象
        Jedis jedis =local.get();
        if(jedis==null){
            if (jedisPool == null) {
                initialPool();
            }
            jedis = jedisPool.getResource();
            local.set(jedis);
        }
        return jedis;
    }

    //新版本用close归还连接
    public static void closeConn(){
        //从本地线程中获取
        Jedis jedis =local.get();
        if(jedis!=null){
            jedis.close();
        }
        local.set(null);
    }

    //关闭池
    public static void closePool(){
        if(jedisPool!=null){
            jedisPool.close();
        }
    }
}