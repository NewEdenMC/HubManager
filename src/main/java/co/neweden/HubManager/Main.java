package co.neweden.HubManager;

import co.neweden.HubManager.JumpPads.JumpPads;
import co.neweden.HubManager.Portals.Portals;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    private Portals portals;
    private boolean preventFallingIntoVoid;
    private boolean preventPlayerDamage;
    private boolean preventPlayerHunger;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new JumpPads(getLogger(), getConfig().getConfigurationSection("jumpPads")), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(getLogger(), getConfig().getConfigurationSection("playerInventory")), this);
        portals = new Portals(this, getConfig().getConfigurationSection("portals"));
        Bukkit.getPluginManager().registerEvents(portals, this);
        Bukkit.getPluginManager().registerEvents(this, this);

        preventFallingIntoVoid = getConfig().getBoolean("preventFallingIntoVoid", false);
        if (preventFallingIntoVoid) getLogger().info("Prevent Falling Into Void check enabled");

        preventPlayerDamage = getConfig().getBoolean("preventPlayerDamage", false);
        if (preventPlayerDamage) getLogger().info("Prevent Player Damage check enabled");

        preventPlayerHunger = getConfig().getBoolean("preventPlayerHunger", false);
        if (preventPlayerHunger) getLogger().info("Prevent Player Hunger check enabled");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
        portals.cleanup();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!preventFallingIntoVoid) return;
        Player player = event.getPlayer();
        if (player.getLocation().getY() <= 0)
            player.teleport(player.getWorld().getSpawnLocation());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (preventPlayerDamage) event.getPlayer().setHealth(20);
        if (preventPlayerHunger) event.getPlayer().setFoodLevel(20);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (preventPlayerDamage && event.getEntity() instanceof Player) {
            event.setCancelled(true);
            event.setDamage(20);
        }
    }

    @EventHandler
    public void onPlayerFoodLevelChange(FoodLevelChangeEvent event) {
        if (preventPlayerHunger && event.getEntity() instanceof Player) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }

}
