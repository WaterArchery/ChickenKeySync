package me.waterarchery.key.sync.managers;

import lombok.extern.slf4j.Slf4j;
import me.waterarchery.cross.api.ChickenCrossApi;
import me.waterarchery.cross.api.redis.DefaultRedisDatabase;
import me.waterarchery.key.sync.ChickenKeySync;
import me.waterarchery.key.sync.models.KeyModifyRecord;
import org.json.JSONObject;

import java.util.UUID;

@Slf4j
public class CrossServerManager {

    private static CrossServerManager instance;
    private final DefaultRedisDatabase redisDatabase;

    public static CrossServerManager getInstance() {
        if (instance == null) {
            instance = new CrossServerManager();
        }

        return instance;
    }

    private CrossServerManager() {
        redisDatabase = ChickenCrossApi.getInstance().getRedisDatabase();

        ChickenKeySync plugin = ChickenKeySync.getInstance();
        redisDatabase.registerRunnable(plugin.getName(), (message) -> {
            JSONObject jsonObject = new JSONObject(message);
            String method = jsonObject.getString("method");
            String senderServer = jsonObject.getString("server");

            ChickenCrossApi chickenCrossApi = ChickenCrossApi.getInstance();
            String server = chickenCrossApi.getServerName();
            if (senderServer.equals(server)) return;

            if (method.equalsIgnoreCase("modify-keys")) {
                String rawUserId = jsonObject.getString("userId");
                String keyId = jsonObject.getString("keyId");
                int key = jsonObject.getInt("key");
                UUID playerId = UUID.fromString(rawUserId);

                CacheManager cacheManager = CacheManager.getInstance();
                KeyModifyRecord keyModifyRecord = cacheManager.getKeyModifyRecords()
                    .stream()
                    .filter(record -> record.getPlayer().equals(playerId) && record.getKeyId().equalsIgnoreCase(keyId))
                    .findFirst()
                    .orElse(null);
                if (keyModifyRecord == null) {
                    keyModifyRecord = new KeyModifyRecord(playerId, keyId, key);
                    cacheManager.getKeyModifyRecords().add(keyModifyRecord);
                } else {
                    keyModifyRecord.setKeyAmount(key);
                }
            }
        });
    }

    public void publishModifyKeys(UUID playerId, String keyId, int key) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method", "modify-keys");
        jsonObject.put("userId", playerId.toString());
        jsonObject.put("keyId", keyId);
        jsonObject.put("key", key);
        jsonObject.put("server", ChickenCrossApi.getInstance().getServerName());
        jsonObject.put("plugin", ChickenKeySync.getInstance().getName());
        redisDatabase.publish(jsonObject.toString());
    }

}
