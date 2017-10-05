package co.neweden.HubManager;

import co.neweden.HubManager.JumpPads.JumpPads;
import co.neweden.HubManager.Portals.Portals;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Weather;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Main extends JavaPlugin implements Listener {

    private Portals portals;
    private boolean preventFallingIntoVoid;
    private boolean preventPlayerDamage;
    private boolean preventPlayerHunger;
    private boolean joinInAdventureMode;

    private boolean forceTime;
    private long timeToForce;

    private boolean forceWeather;
    private boolean forceWeatherStorm;

    private Location joinLocation;

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

        joinInAdventureMode = getConfig().getBoolean("joinInAdventureMode", false);
        if (joinInAdventureMode) getLogger().info("Enabled joining in Adventure Mode");

        String joinLocation = getConfig().getString("forceJoinLocation.location", null);
        if (joinLocation != null) {
            try {
                this.joinLocation = Util.parseLocationFromString(joinLocation);
            } catch (IllegalArgumentException e) {
                getLogger().log(Level.SEVERE, "An error occurred while loading 'forceJoinLocation' from config.", e);
            }
        }
        if (this.joinLocation != null) getLogger().info("Enabled forcing join location");

        setEnvironmentForcing();
    }

    private void setEnvironmentForcing() {
        forceTime = getConfig().getBoolean("forceTime.enabled", false);
        if (forceTime) {
            timeToForce = getConfig().getLong("forceTime.timeToForce", 6000);
            getLogger().info("Forcing time to " + timeToForce + " ticks");
        }

        forceWeather = getConfig().getBoolean("forceWeather.enabled", false);
        if (forceWeather) {
            String weatherFromConfig = getConfig().getString("forceWeather.weatherToForce", "false");
            switch (weatherFromConfig.toUpperCase()) {
                case "SUN": forceWeatherStorm = false; break;
                case "STORM": forceWeatherStorm = true; break;
                default: forceWeather = false; getLogger().info("Tried to force weather but '" + weatherFromConfig + "' is not a valid value, valid values asre: SUN, STORM"); break;
            }
            if (forceWeather)
                getLogger().info("Forcing weather to " + weatherFromConfig.toUpperCase());
        }

        if (!forceTime && !forceWeather) return;

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (forceTime) world.setTime(timeToForce);
                    if (forceWeather) world.setStorm(forceWeatherStorm);
                }
            }
        }, 0, 20);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
        Bukkit.getScheduler().cancelTasks(this);
        portals.cleanup();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!preventFallingIntoVoid) return;
        Player player = event.getPlayer();
        if (player.getLocation().getY() <= 0)
            player.teleport(player.getWorld().getSpawnLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (preventPlayerDamage) event.getPlayer().setHealth(20);
        if (preventPlayerHunger) event.getPlayer().setFoodLevel(20);
        if (joinInAdventureMode) event.getPlayer().setGameMode(GameMode.ADVENTURE);
        if (joinLocation != null) event.getPlayer().teleport(joinLocation);
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
