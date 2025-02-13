package com.nq.utils.redis;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.nq.vo.user.AlarmDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ShardedJedis;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class RedisShardedPoolUtils {

    private static final Logger log = LoggerFactory.getLogger(RedisShardedPoolUtils.class);

    public static String set(String key, String value) {

        ShardedJedis jedis = null;

        String result = null;

        try {

            jedis = RedisShardedPool.getJedis();

            result = jedis.set(key, value);

        } catch (Exception e) {

            log.error("redis set key: {} value: {} error", new Object[]{key, value, e});

            RedisShardedPool.returnBrokenResouce(jedis);

            return result;

        }

        RedisShardedPool.returnResouce(jedis);

        return result;

    }

    public static String get(String key) {

        ShardedJedis jedis = null;

        String result = null;


        try {

            jedis = RedisShardedPool.getJedis();

                result = jedis.get(key);


        } catch (Exception e) {

            log.error("redis get key: {} error", key, e);

            RedisShardedPool.returnBrokenResouce(jedis);

            return result;

        }

        RedisShardedPool.returnResouce(jedis);

        return result;

    }


    public static String setEx(String key, String value, int exTime) {

        ShardedJedis jedis = null;

        String result = null;

        try {

            jedis = RedisShardedPool.getJedis();

            result = jedis.setex(key, exTime, value);

        } catch (Exception e) {

            log.error("redis setEx key: {} value: {}   error...", new Object[]{key, value, e});

            RedisShardedPool.returnBrokenResouce(jedis);

            return result;

        }

        RedisShardedPool.returnResouce(jedis);

        return result;

    }


    public static Long expire(String key, int exTime) {

        ShardedJedis jedis = null;

        Long result = null;


        try {

            jedis = RedisShardedPool.getJedis();

            result = jedis.expire(key, exTime);

        } catch (Exception e) {

            log.error("redis expire key: {}  error ", key, e);

            RedisShardedPool.returnBrokenResouce(jedis);

            return result;

        }

        RedisShardedPool.returnResouce(jedis);

        return result;

    }


    public static Long del(String key) {

        ShardedJedis jedis = null;

        Long result = null;


        try {

            jedis = RedisShardedPool.getJedis();

            result = jedis.del(key);

        } catch (Exception e) {

            log.error("redis del key: {} error ", key, e);

            RedisShardedPool.returnBrokenResouce(jedis);

            return result;

        }

        RedisShardedPool.returnResouce(jedis);

        return result;

    }
    public static String set(String key, String value,int db) {
            //调用initPool

            String result = null;

        try {
            Jedis jedis = RedisPoolUtil.getConn();
            jedis.select(db);
            result = jedis.set(key, value);
        } catch (Exception e) {
            log.error("redis set key: {} value: {} db: {} error", new Object[]{key, value,db, e});
            return result;
        }
            RedisPoolUtil.closeConn();

            return result;
        }
public static String get(String key,int db) {
    String result = null;
    try {
        Jedis jedis = RedisPoolUtil.getConn();
        jedis.select(db);
        result = jedis.get(key);
    } catch (Exception e) {
        log.error("redis get key: {} db: {} error", new Object[]{key,db, e});
        return result;
    }
    RedisPoolUtil.closeConn();

    return result;
}
    public static String delAll(int db) {
        String result = null;
        try {
            Jedis jedis = RedisPoolUtil.getConn();
            jedis.select(db);
            result = jedis.flushDB();
        } catch (Exception e) {
            log.error("redis deall  db: {} error", new Object[]{db, e});
            return result;
        }
        RedisPoolUtil.closeConn();

        return result;
    }


    public static void lpush(String key, Object data) {
        String dataStr = JSONObject.toJSONString(data);

        ShardedJedis jedis = null;

        try {

            jedis = RedisShardedPool.getJedis();

            jedis.lpush(key, dataStr);

        } catch (Exception e) {

            log.error("redis lpush key: {} value: {} error", new Object[]{key, dataStr, e});

            RedisShardedPool.returnBrokenResouce(jedis);

        }

        RedisShardedPool.returnResouce(jedis);
    }

    public static <T> List<T> list(String key, Class<T> clazz) {

        ShardedJedis jedis = null;

        try {

            jedis = RedisShardedPool.getJedis();

            List<String> lrange = jedis.lrange(key, 0, -1);

            if (CollectionUtils.isEmpty(lrange)) {
                return Lists.newArrayList();
            }

            return lrange.stream().map(str -> JSONObject.parseObject(str, clazz)).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("redis lpush key: {}, error", new Object[]{key, e});

        } finally {
            RedisShardedPool.returnResouce(jedis);
        }

        return null;
    }




    public static <T> T rpop(String key, Class<T> clazz) {

        ShardedJedis jedis = null;

        String result = null;

        try {

            jedis = RedisShardedPool.getJedis();

            result = jedis.rpop(key);


        } catch (Exception e) {

            log.error("redis get key: {} error", key, e);

            RedisShardedPool.returnBrokenResouce(jedis);

            return null;

        }

        RedisShardedPool.returnResouce(jedis);

        if (StringUtils.isEmpty(result)) {
            return null;
        }

        return JSONObject.parseObject(result, clazz);
    }


    public static void main(String[] args) {
        ShardedJedis jedis = RedisShardedPool.getJedis();

        AlarmDTO dto = new AlarmDTO();
        dto.setType("RECHARGE");


        lpush("ALARM_5", dto);

        System.out.println("redis shaded pool utils end ...");

    }

}
