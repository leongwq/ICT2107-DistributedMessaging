package whatschat;

import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

public class JedisConnection {
	//redis server
    private static final String redisHost = "redis-17860.c1.ap-southeast-1-1.ec2.cloud.redislabs.com";
    private static final Integer redisPort = 17860;
    private static final Integer redisTimeout = 1000;
    private static final String redisPassword = "diSYSggi0risTjxA7JFmjhoPg8fFGEyl";
 
    //the jedis connection pool..
    private static JedisPool pool = null;
 
    public JedisConnection() {
        //configure our pool connection
    	JedisPoolConfig poolConfig = new JedisPoolConfig();
        pool = new JedisPool(poolConfig, redisHost, redisPort, redisTimeout, redisPassword);

    }
    
    public void pushChatContent(String ip, String msg) {
    	Jedis jedis = pool.getResource();
    	
    	try {
    		jedis.rpush(ip, msg);
    	} catch (JedisException e) {
    		//if something wrong happen, return it back to the pool
    		if (null != jedis) {
    			jedis.close();
    		}
    	} finally {
    		if (null != jedis) {
    			jedis.close();
    		}
    	}

    }
    
    public List<String> getChatContent(String ip) {
    	Jedis jedis = pool.getResource();
    	
    	try {
    		List<String> messages = jedis.lrange(ip, -10, -1);
    		return messages;
    	} catch (JedisException e) {
    		//if something wrong happen, return it back to the pool
    		if (null != jedis) {
    			jedis.close();
    		}
    	} finally {
    		if (null != jedis) {
    			jedis.close();
    		}
    	}
		return null;
    }
    
    public void flush() {
    	Jedis jedis = pool.getResource();
  
    	try {
    		jedis.flushAll();
    	} catch (JedisException e) {
    		//if something wrong happen, return it back to the pool
    		if (null != jedis) {
    			jedis.close();
    		}
    	} finally {
    		if (null != jedis) {
    			jedis.close();
    		}
    	}

    }
}
