package co.neweden.HubManager.Portals;

import co.neweden.HubManager.Util;
import co.neweden.menugui.MenuGUI;
import co.neweden.menugui.menu.Menu;
import com.sun.istack.internal.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Level;

public class Portals implements Listener {

    private Collection<Portal> portals = new HashSet<>();
    private Plugin plugin;
    private Map<Player, Location> playersInMenu = new HashMap<>();

    public Portals(@NotNull Plugin plugin, ConfigurationSection config) {
        if (config == null) return;

        this.plugin = plugin;
        for (String key : config.getKeys(false)) {
            ConfigurationSection portalConfig = config.getConfigurationSection(key);
            Portal portal = new Portal();

            portal.world = Bukkit.getWorld(portalConfig.getString("worldName", null));
            if (portal.world == null) {
                plugin.getLogger().warning("Unable to load Portal \"" + key + "\" as the world name was not specified or world is not loaded.");
                continue;
            }

            Location pos1; Location pos2;
            try {
                pos1 = Util.parseLocationFromString(portalConfig.getString("pos1", null), false);
                pos2 = Util.parseLocationFromString(portalConfig.getString("pos2", null), false);
                if (portalConfig.isString("teleportToOnEnter"))
                    portal.teleportToOnEnter = Util.parseLocationFromString(portalConfig.getString("teleportToOnEnter", null));
                if (portalConfig.isString("teleportToOnMenuClose"))
                    portal.teleportToOnMenuClose = Util.parseLocationFromString(portalConfig.getString("teleportToOnMenuClose", null));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred while loading Portal \"" + key + "\", continuing to the next Portal.", e);
                continue;
            }

            Location[] loc = Util.generateUntangledPoints(pos1, pos2);
            portal.pos1 = loc[0]; portal.pos2 = loc[1];
            portal.menuToOpenOnEnter = portalConfig.getString("menuToOpenOnEnter", null);
            portal.spawnOnMenuClose = portalConfig.getBoolean("spawnOnMenuClose", false);
            portal.hidePlayerWhileInMenu = portalConfig.getBoolean("hidePlayerWhileInMenu", false);

            portals.add(portal);
            plugin.getLogger().info("Loaded Portal \"" + key + "\"");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Block block = event.getPlayer().getLocation().getBlock();
        if (block.getType() != Material.STATIONARY_WATER && block.getType() != Material.WATER) return;

        for (Portal portal : Collections.unmodifiableCollection(portals)) {
            if (!Util.isObjectColidingWithBox(event.getPlayer().getLocation(), portal.pos1, portal.pos2)) continue;

            if (portal.teleportToOnEnter != null) event.getPlayer().teleport(portal.teleportToOnEnter, TeleportCause.PLUGIN);

            Menu menu = MenuGUI.getMenu(portal.menuToOpenOnEnter);
            if (menu == null) return;
            Location loc = portal.spawnOnMenuClose ? portal.world.getSpawnLocation() : portal.teleportToOnMenuClose;
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    menu.openMenu(event.getPlayer());
                    playersInMenu.put(event.getPlayer(), loc);
                    if (portal.hidePlayerWhileInMenu) hidePlayer(event.getPlayer());
                }
            }, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Location loc = playersInMenu.get(player);
        if (loc == null) return; // the player should not be teleported
        playersInMenu.remove(player);
        player.teleport(loc, TeleportCause.PLUGIN);
        showPlayer(player);
    }

    public void cleanup() {
        for (Player player : Collections.unmodifiableCollection(playersInMenu.keySet())) {
            player.closeInventory();
            showPlayer(player);
        }
    }

    public void hidePlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(player);
        }
    }

    public void showPlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Player playerInMenu : Collections.unmodifiableCollection(playersInMenu.keySet())) {
            event.getPlayer().hidePlayer(playerInMenu);
        }
    }

}
