package com.resolve.sso.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.resolve.sso.entities.redis.RedisCacheEntity;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * This is a utility class, that takes the responsibility to publish messages
 * into the Redis Destination.
 *
 * @author Surath
 *
 */
@Component
public class RedisUtil {
	private static Logger logger = LogManager.getLogger(RedisUtil.class);
	
	final static JedisPoolConfig poolConfig;
    final static JedisPool jedisPool;
    final static int DEFAULT_REDIS_PORT = 6379;
    final static int MAX_SCAN_SIZE = 1000000;
    final static int MAX_SCAN_KEYS_SIZE = 1000000;
    
    
    
    static {
        String redisHostname = System.getenv("REDIS_HOSTNAME");
        String redisPort = "";
        if (null == redisHostname) {
            logger.warn("REDIS_HOSTNAME is not set... so please set this environment variable or "
                        + "localhost will be taken as the value for this...");
            redisHostname = "localhost";
            redisPort = System.getenv("REDIS_PORT");
            poolConfig = new JedisPoolConfig();
            jedisPool = new JedisPool(poolConfig, redisHostname, stringToInt(redisPort, DEFAULT_REDIS_PORT), 0);;
        } else {
            logger.info("REDIS_HOSTNAME is set, get the value and initialize the resources accordingly...");
            redisPort = System.getenv("REDIS_PORT");
            poolConfig = new JedisPoolConfig();
            jedisPool = new JedisPool(poolConfig, redisHostname, stringToInt(redisPort, DEFAULT_REDIS_PORT), 0);
        }
    }
    
    /*
     * Utility method to get the value for a RedisCacheEntity stored in Redis Cache
     * @input - RedisCacheEntity e
     * @output - String value, where the following is stored in redis -
     * "key" => "value"
     */

    public static String get(RedisCacheEntity e) {
        return get(e.getRedisKey());
    }

    /*
     * Utility method which should be used by classes using redis to get value for
     * a key already stored in redis. Redis stores everything as key-value pairs,
     * so user class should also provide the map name
     * @input - String mapName, String key
     * @output - String value, where the following is stored in redis -
     * "mapName,key" => "value"
     */
    public static String get(String mapName, String key) {
        return get(mapName + "," + key);
    }

    /*
     * Utility method which should be used by classes using redis to get value for
     * a key already stored in redis. Redis stores everything as key-value pairs,
     * so user class should also provide the map name
     * @input - String String key
     * @output - String value, where the following is stored in redis -
     * "mapName,key" => "value"
     */
    public static String get(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.get(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
    
    /*
     * Utility method which should be used by classes using redis to get all keys
     * for a regx key stored in redis.
     * @input - String regxKey
     * @output - Set<String> keys, set of all the keys matched with the filter
     * criteria
     */
    public static Set<String> getKeys(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.keys(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /*
     * Utility method which should be used by classes using redis to get all keys
     * for a regx key stored in redis.
     * @input - String mapName, String regxKey
     * @output - Set<String> keys, set of all the keys matched with the filter
     * criteria
     */
    public static Set<String> getKeys(String mapName, String regxKey) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.keys(mapName + "," + regxKey);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
    
    public static Object getObject(String mapName, String key) {
        String objectAsString = get(mapName, key);
        if (objectAsString != null) {

            ByteArrayInputStream byteArrayInputStream = null;
            ObjectInputStream objectInputStream = null;
            try {
                byte[] byteArray = Base64.getDecoder().decode(objectAsString);
                byteArrayInputStream = new ByteArrayInputStream(byteArray);
                objectInputStream = new ObjectInputStream(byteArrayInputStream);
                Object object = objectInputStream.readObject();
                return object;
            } catch (Exception e) {
                if (logger.isDebugEnabled())
                    logger.debug(e.getMessage(), e);
            } finally {
                try {
                    if (objectInputStream != null) {
                        objectInputStream.close();
                    }
                } catch (Exception ex) {
                }
                try {
                    byteArrayInputStream.close();
                } catch (Exception ex) {
                }
            }
        }
        return null;
    }
    
    /*
     * Utility method to get the value for a RedisCacheEntity stored in Redis Cache
     * @input - RedisCacheEntity e
     * @output - String value, where the following is stored in redis -
     * "key" => "value"
     */

    public static void set(RedisCacheEntity e) {
        set(e.getRedisKey(), e.getRedisValue());
    }

    /*
     * Utility method which should be used by classes using redis to store
     * key-value pairs in a particular hashmap. Redis stores everything as
     * key-value pairs, so user class should also provide the map name
     * @input - String mapName, String key, String value
     * This method will construct a redisKey = "mapName,key" and store "value" for
     * this redisKey in redis
     */
    public static void set(String mapName, String key, String value) {
        set(mapName + "," + key, value);
    }

    /*
     * Utility method which should be used by classes using redis to store
     * key-value pairs in a particular hashmap. Redis stores everything as
     * key-value pairs, so user class should also provide the map name
     * @input - String key, String value
     * This method will construct a redisKey = "mapName,key" and store "value" for
     * this redisKey in redis
     */
    public static void set(String key, String value) {
        if (value == null)
            return;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.set(key, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
    
    public static void set(String mapName, String key, Object value) {
        if (value == null) {
            logger.error("Error: IN JEDIS, Object recieved null ");
            return;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(value);
            byte[] byteArray = bos.toByteArray();
            String valueAsString = Base64.getEncoder().encodeToString(byteArray);

            set(mapName, key, valueAsString);

        } catch (Exception e) {
            logger.error(" Error while converting object to byteArray ", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception ex) {
            }
            try {
                bos.close();
            } catch (Exception ex) {
            }
        }
    }
    
    /**
     * Publish messages
     *·
     * @param channel
     * @param message
     */
    public static void publishMessage(String channel, String message) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.publish(channel, message);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    /**
     * Publish messages - Return subscriber count
     *
     * @param channel
     * @param message
     * @return NUmber of subscribers listening
     */
    public static long publish(String channel, String message) {
        long subscribers = 0;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            subscribers = jedis.publish(channel, message);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return subscribers;
    }
    
    /**
     * Just a utility method.
     *·
     * @param value
     * @param defaultRedisPort
     * @return
     */
    private static int stringToInt(String value, int defaultRedisPort) {
        try {
            if (null != value) {
                return Integer.parseInt(value);
            } else {
                return defaultRedisPort;
            }
        } catch (NumberFormatException e) {
            logger.error("REDIS_PORT envioronment variable is set with a improper value... "
                         + "It should be an INT... So set the default value..." + e);
            return defaultRedisPort;
        }
    }
    
    public static long clearMaps(String... maps) {
        Jedis jedis = null;
        if (null == maps || maps.length == 0) {
            return 0;
        }
        long response = 0;
        try {
            jedis = jedisPool.getResource();
            for (String mapName : maps) {
                Set<String> keySet = jedis.keys(mapName + ",*");
                response += (clearKeys(keySet.toArray(new String[keySet.size()])));
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return response;
    }

    public static long clearKeys(String... keys) {
        Jedis jedis = null;
        if (null == keys || keys.length == 0) {
            return 0;
        }
        try {
            jedis = jedisPool.getResource();
            return jedis.del(keys);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
    
    public static void lset(String mapName, String key, long index, String value) {
        if (logger.isDebugEnabled())
            logger.debug("IN JEDIS, SET CALLED: " + mapName + " KEY: " + key + "INDEX: " + index);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.lset(mapName + "," + key, index, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    public static void lpush(String mapName, String key, String value) {
        if (logger.isDebugEnabled())
            logger.debug("IN JEDIS, PUSH CALLED: " + mapName + " KEY: " + key);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.lpush(mapName + "," + key, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    public static void sadd(String key, String... value) {
        if (logger.isDebugEnabled())
            logger.debug("IN JEDIS, SADD CALLED: KEY: " + key);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.sadd(key, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
    
    public static Set<String> smembers(String mapName, String key) {
        return smembers(mapName + RedisConstants.COMMA_SEPERATOR + key);
    }

    public static Set<String> smembers(String key) {
        if (logger.isDebugEnabled())
            logger.debug("IN JEDIS, SMEMBERS CALLED: KEY: " + key);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.smembers(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public static Long srem(String mapName, String key, String member) {
        if (logger.isDebugEnabled())
            logger.debug("IN JEDIS, SREM CALLED: " + mapName + " KEY: " + key);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.srem(mapName + "," + key, member);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public static String type(String mapName, String key) {
        if (logger.isDebugEnabled())
            logger.debug("IN JEDIS, TYPE CALLED: " + mapName + " KEY: " + key);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.type(mapName + "," + key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
    
    public static void lrem(String mapName, String key, long count, String value) {
        if (logger.isDebugEnabled())
            logger.debug("IN JEDIS, LREM  CALLED: " + mapName + " KEY: " + key + "COUNT: " + count);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.lrem(mapName + "," + key, count, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    public static void del(String mapName, String key) {
        del(mapName + "," + key);
    }

    public static void del(String key) {
        if (logger.isDebugEnabled())
            logger.debug("IN JEDIS, Delete KEY: " + key);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public static void expire(String key, int seconds) {
        if (logger.isDebugEnabled())
            logger.debug("IN JEDIS, Expire KEY: " + key + "in " + seconds + " seconds");
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.expire(key, seconds);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
    
    public static List<String> lrange(String mapName, String key, long start, long end) {
        if (logger.isDebugEnabled())
            logger.debug("IN JEDIS, LRANGE CALLED: " + mapName + " KEY: " + key + " START: " + start + " END: " + end);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.lrange(mapName + "," + key, start, end);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public static void setex(String key, int expirySecs, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.setex(key, expirySecs, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public static void sadd(RedisCacheEntity e) {
        sadd(e.getRedisKey(), e.getRedisValue());
    }

    /**
     * Creates String representation of a Set which can be used to store as a
     * String value in Redis hashmap eg input: "abc,def,xyz" output: HashSet
     * <String> having "abc", "def", "xyz"
     *·
     * @param value
     * @return
     */
    private static HashSet<String> parseStringToSet(String value) {
        if (value == null) {
            return null;
        }
        HashSet<String> k = new HashSet<String>();
        String[] v = value.split(RedisConstants.COMMA_SEPERATOR);
        for (String s : v) {
            k.add(s);
        }
        return k;
    }

    /*
     * Utility method which should be used by classes using redis to scan value for
     * a key already stored in redis. Redis stores everything as key-value pairs,
     * so user class should also provide the map name
     * @input - String String key
     * @output - List<String value, where the following is stored in redis -
     * "mapName,key" => "value"
     */
    public static List<String> scan(String key) {
        Jedis jedis = null;
        ScanParams scanParams = new ScanParams().count(MAX_SCAN_SIZE);
        scanParams.match(key);
        String cursor = redis.clients.jedis.ScanParams.SCAN_POINTER_START;
        try {
            jedis = jedisPool.getResource();
            return jedis.scan(cursor, scanParams).getResult();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
    
    /**
     *·
     * @param key
     * @return
     */
    public static Set<String> scanKeys(String key) {
        Jedis jedis = null;
        List<String> result = new ArrayList<String>();
        ScanParams scanParams = new ScanParams().count(MAX_SCAN_KEYS_SIZE);
        scanParams.match(key);
        logger.debug("[scanKeys] : Match Key  : " + key);
        try {
            jedis = jedisPool.getResource();
            String cursor = redis.clients.jedis.ScanParams.SCAN_POINTER_START;
            ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
            if (!scanResult.getResult().isEmpty())
                result.addAll(scanResult.getResult());
            cursor = scanResult.getStringCursor();
            logger.debug("[scanKeys] : result : List Size : " + result.size());
            Set<String> keys = new HashSet<>();
            if (!result.isEmpty())
                keys.addAll(result);

            return keys;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

	public static void main(String args[]) {

		System.out.println("Before(get): " + DateTime.now().toString());
		System.out.println(getKeys("*").size());
		System.out.println("After(get): " + DateTime.now().toString());

		System.out.println("Before(scan): " + DateTime.now().toString());
		System.out.println(scanKeys("*").size());
		System.out.println("After(scan): " + DateTime.now().toString());

	}
    
    
    
}

 