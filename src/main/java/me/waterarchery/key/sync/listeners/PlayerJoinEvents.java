package me.waterarchery.key.sync.listeners;

import lombok.extern.slf4j.Slf4j;
import me.waterarchery.key.sync.managers.CacheManager;
import me.waterarchery.key.sync.managers.CrossServerManager;
import me.waterarchery.key.sync.models.KeyModifyRecord;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import su.nightexpress.excellentcrates.CratesAPI;
import su.nightexpress.excellentcrates.key.CrateKey;
import su.nightexpress.excellentcrates.key.KeyManager;
import su.nightexpress.excellentcrates.user.CrateUser;
import su.nightexpress.excellentcrates.user.UserManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class PlayerJoinEvents implements Listener {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @EventHandler
    public void onUserLogin(AsyncPlayerPreLoginEvent event) {
        CacheManager cacheManager = CacheManager.getInstance();
        cacheManager.getLockedPlayers().add(event.getUniqueId());
    }

    @EventHandler
    public void onUserQuit(PlayerQuitEvent event) {
        CacheManager cacheManager = CacheManager.getInstance();
        cacheManager.getLockedPlayers().remove(event.getPlayer().getUniqueId());

        Player player = event.getPlayer();
        UserManager userManager = CratesAPI.getUserManager();
        CrateUser crateUser = userManager.getOrFetch(player.getUniqueId());
        if (crateUser == null) return;

        crateUser.getKeysMap().forEach((key, amount) -> {
            CrossServerManager crossServerManager = CrossServerManager.getInstance();
            crossServerManager.publishModifyKeys(player.getUniqueId(), key, amount);
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CacheManager cacheManager = CacheManager.getInstance();
        List<KeyModifyRecord> keyModifyRecords = cacheManager.getKeyModifyRecords()
            .stream()
            .filter(record -> record.getPlayer().equals(player.getUniqueId()))
            .toList();

        if (keyModifyRecords.isEmpty()) {
            unlock(player);
            return;
        }

        executorService.submit(() -> {
            try {
                UserManager userManager = CratesAPI.getUserManager();
                CrateUser user = userManager.getUserDataAsync(player.getUniqueId()).join();

                keyModifyRecords.forEach(record -> {
                    KeyManager keyManager = CratesAPI.getKeyManager();
                    CrateKey keyById = keyManager.getKeyById(record.getKeyId());
                    if (keyById == null) {
                        unlock(player);
                        throw new RuntimeException("Key is null: " + record.getKeyId());
                    }

                    keyManager.setKey(user, keyById, record.getKeyAmount());
                    unlock(player);
                });
            } catch (Exception ex) {
                log.error("Error while setting keys: ", ex);
            }
        });
    }

    private void unlock(Player player) {
        CacheManager cacheManager = CacheManager.getInstance();
        cacheManager.getLockedPlayers().remove(player.getUniqueId());
    }

}
