package me.waterarchery.key.sync.listeners;

import com.chickennw.utils.utils.ChatUtils;
import me.waterarchery.key.sync.managers.CacheManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import su.nightexpress.excellentcrates.api.event.CrateOpenEvent;

public class CrateOpenListeners implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onCrateOpen(CrateOpenEvent event) {
        Player player = event.getPlayer();

        CacheManager cacheManager = CacheManager.getInstance();
        boolean isLocked = cacheManager.getLockedPlayers().contains(player.getUniqueId());

        if (isLocked) {
            event.setCancelled(true);
            ChatUtils.sendMessage(player, "<red>Kasa açmadan önce biraz beklemelisin.");
        }
    }

}
