package net.astralis;

import net.astralis.database.MongoDBManager;
import net.astralis.database.RedisManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class AstralisAPI {

    private final YamlConfiguration config;

    private MongoDBManager mongoDBManager;
    private RedisManager redisManager;

    public AstralisAPI() {
        File configFile = new File("plugins/AstralisCore/config.yml");

        // Falls config.yml nicht existiert, versuchen zu erstellen
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                if (configFile.createNewFile()) {
                    Bukkit.getLogger().info("[AstralisAPI] Neue config.yml erstellt. Bitte füllen!");
                }
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, "[AstralisAPI] Konnte config.yml nicht erstellen", e);
            }
        }

        this.config = YamlConfiguration.loadConfiguration(configFile);

        initMongo();
        initRedis();
    }

    private void initMongo() {
        try {
            String uri = config.getString("mongodb.uri", "mongodb://localhost:27017");
            String database = config.getString("mongodb.database", "astralis");

            this.mongoDBManager = new MongoDBManager(uri, database);
            Bukkit.getLogger().info("[AstralisAPI] MongoDB verbunden → " + database);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[AstralisAPI] Fehler bei MongoDB-Verbindung", e);
        }
    }

    private void initRedis() {
        try {
            String host = config.getString("redis.host", "localhost");
            int port = config.getInt("redis.port", 6379);

            this.redisManager = new RedisManager(host, port);
            Bukkit.getLogger().info("[AstralisAPI] Redis verbunden → " + host + ":" + port);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[AstralisAPI] Fehler bei Redis-Verbindung", e);
        }
    }

    public MongoDBManager getMongoDBManager() {
        if (mongoDBManager == null) {
            initMongo();
        }
        return mongoDBManager;
    }

    public RedisManager getRedisManager() {
        if (redisManager == null) {
            initRedis();
        }
        return redisManager;
    }

    public void shutdown() {
        if (mongoDBManager != null) {
            mongoDBManager.close();
        }
        if (redisManager != null) {
            redisManager.close();
        }
    }
}
