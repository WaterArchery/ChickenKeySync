package me.waterarchery.key.sync;

import com.chickennw.utils.ChickenUtils;
import me.waterarchery.key.sync.listeners.CrateOpenListeners;
import me.waterarchery.key.sync.listeners.PlayerJoinEvents;
import me.waterarchery.key.sync.managers.CrossServerManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChickenKeySync extends JavaPlugin {

    @Override
    public void onEnable() {
        ChickenUtils.setPlugin(this);

        getServer().getPluginManager().registerEvents(new PlayerJoinEvents(), this);
        getServer().getPluginManager().registerEvents(new CrateOpenListeners(), this);

        CrossServerManager.getInstance();
    }

    @Override
    public void onDisable() {
        ChickenUtils.disable();
    }

    public static ChickenKeySync getInstance() {
        return JavaPlugin.getPlugin(ChickenKeySync.class);
    }
}
