package me.waterarchery.key.sync.managers;

import lombok.Getter;
import me.waterarchery.key.sync.models.KeyModifyRecord;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class CacheManager {

    private static CacheManager instance;
    private final Set<UUID> lockedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<KeyModifyRecord> keyModifyRecords = ConcurrentHashMap.newKeySet();

    public static CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }

        return instance;
    }

    private CacheManager() {

    }

}
