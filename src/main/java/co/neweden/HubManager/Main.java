package co.neweden.HubManager;

import co.neweden.HubManager.JumpPads.JumpPads;
import co.neweden.HubManager.Portals.Portals;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    Portals portals;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new JumpPads(getLogger(), getConfig().getConfigurationSection("jumpPads")), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(getLogger(), getConfig().getConfigurationSection("playerInventory")), this);
        portals = new Portals(this, getConfig().getConfigurationSection("portals"));
        Bukkit.getPluginManager().registerEvents(portals, this);
        if (getConfig().getBoolean("preventFallingIntoVoid", false)) {
            Bukkit.getPluginManager().registerEvents(this, this);
            getLogger().info("Prevent Falling Into Void check enabled");
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
        portals.cleanup();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().getY() <= 0)
            player.teleport(player.getWorld().getSpawnLocation());
    }

}
