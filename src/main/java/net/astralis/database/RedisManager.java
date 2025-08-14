package net.astralis.database;

import redis.clients.jedis.Jedis;
public class RedisManager {

    private final String host;
    private final int port;
    private Jedis jedis;

    public RedisManager(String host, int port) {
        this.host = host;
        this.port = port;
        connect();
    }

    private void connect() {
        jedis = new Jedis(host, port);
    }

    public void set(String key, String value) {
        jedis.set(key, value);
    }

    public String get(String key) {
        return jedis.get(key);
    }

    public void del(String key) {
        jedis.del(key);
    }

    public void close() {
        jedis.close();
    }
}
