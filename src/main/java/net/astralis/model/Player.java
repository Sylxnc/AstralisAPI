package net.astralis.model;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import net.astralis.database.MongoDBManager;
import net.astralis.database.RedisManager;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.UUID;

public class Player {

    private UUID uuid;
    private String name;
    private double stars;
    private int votes;
    private int voteStreak;
    private LocalDateTime lastTimeJoined;
    private LocalDateTime firstTimeJoined;
    private int voteCases;
    private int starCases;
    private int lightCases;

    private final RedisManager redis;
    private final MongoCollection<Document> collection;

    public Player(UUID uuid, MongoDBManager mongo, RedisManager redis) {
        this.uuid = uuid;
        this.redis = redis;
        this.collection = mongo.getDatabase().getCollection("players");

        loadFromMongo();
    }

    private void loadFromMongo() {
        Document doc = collection.find(Filters.eq("uuid", uuid.toString())).first();
        if (doc == null) {
            this.name = "Unknown";
            this.stars = 0.0;
            saveToMongo();
        } else {
            this.name = doc.getString("name");
            this.stars = doc.getDouble("stars");
            this.votes = doc.getInteger("votes", 0);
            this.voteStreak = doc.getInteger("voteStreak", 0);
            this.voteCases = doc.getInteger("voteCases", 0);
            this.starCases = doc.getInteger("starCases", 0);
            this.lightCases = doc.getInteger("lightCases", 0);
        }
    }

    public void saveToMongo() {
        Document doc = new Document()
                .append("uuid", uuid.toString())
                .append("name", name)
                .append("stars", stars)
                .append("votes", votes)
                .append("voteStreak", voteStreak)
                .append("voteCases", voteCases)
                .append("starCases", starCases)
                .append("lightCases", lightCases);

        collection.updateOne(Filters.eq("uuid", uuid.toString()), new Document("$set", doc), new UpdateOptions().upsert(true));
    }

    // Redis cache
    public void setOnline(boolean online) {
        redis.set("player:" + uuid + ":isOnline", String.valueOf(online));
    }

    public boolean isOnline() {
        String val = redis.get("player:" + uuid + ":isOnline");
        return val != null && Boolean.parseBoolean(val);
    }

    public void setActiveServer(String server) {
        redis.set("player:" + uuid + ":activeServer", server);
    }

    public String getActiveServer() {
        return redis.get("player:" + uuid + ":activeServer");
    }

    public void connect() {
        this.lastTimeJoined = LocalDateTime.now();
        this.saveToMongo();
        this.setOnline(true);
    }
    public void disconnect() {
        this.setOnline(false);
        this.saveToMongo();
    }
}
