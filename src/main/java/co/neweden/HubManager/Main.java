package co.neweden.HubManager;

import co.neweden.HubManager.JumpPads.JumpPads;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new JumpPads(getLogger(), getConfig().getConfigurationSection("jumpPads")), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

}
