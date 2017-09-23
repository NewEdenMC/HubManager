package co.neweden.HubManager.Portals;

import co.neweden.HubManager.Util;
import co.neweden.menugui.MenuGUI;
import co.neweden.menugui.menu.Menu;
import org.apache.commons.lang.Validate;
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;

public class Portals implements Listener {

    private Collection<Portal> portals = new HashSet<>();
    private Plugin plugin;
    private Collection<Player> playersInMenu = new HashSet<>();

    public Portals(Plugin plugin, ConfigurationSection config) {
        Validate.notNull(plugin);
        Validate.notNull(config);

        this.plugin = plugin;
        for (String key : config.getKeys(false)) {
            ConfigurationSection portalConfig = config.getConfigurationSection(key);
            Portal portal = new Portal();

            portal.world = Bukkit.getWorld(portalConfig.getString("worldName", null));
            if (portal.world == null) {
                plugin.getLogger().warning("Unable to load Portal \"" + key + "\" as the world name was not specified or world is not loaded.");
                continue;
            }

            Location pos1; Location pos2; Location teleportTo = null;
            try {
                pos1 = Util.parseLocationFromString(portalConfig.getString("pos1", null), false);
                pos2 = Util.parseLocationFromString(portalConfig.getString("pos2", null), false);
                if (portalConfig.isString("teleportTo"))
                    teleportTo = Util.parseLocationFromString(portalConfig.getString("teleportTo", null));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred while loading Portal \"" + key + "\", continuing to the next Portal.", e);
                continue;
            }

            portal.x1 = pos1.getBlockX(); portal.y1 = pos1.getBlockY(); portal.z1 = pos1.getBlockZ();
            portal.x2 = pos2.getBlockX(); portal.y2 = pos2.getBlockY(); portal.z2 = pos2.getBlockZ();
            portal.teleportTo = teleportTo;
            portal.menuToOpen = portalConfig.getString("menuToOpen", null);
            portal.spawnOnMenuClose = portalConfig.getBoolean("spawnOnMenuClose", false);
            portal.hidePlayerWhileInMenu = portalConfig.getBoolean("hidePlayerWhileInMenu", false);

            portals.add(portal);
            plugin.getLogger().info("Loaded Portal \"" + key + "\"");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Block block = event.getPlayer().getLocation().getBlock();
        if ((block.getType() != Material.STATIONARY_WATER && block.getType() != Material.WATER) || event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        for (Portal portal : Collections.unmodifiableCollection(portals)) {
            if (block.getX() < portal.x1 || block.getX() > portal.x2 &&
                block.getY() < portal.y1 || block.getY() > portal.y2 &&
                block.getZ() < portal.z1 || block.getZ() > portal.z2) continue;

            if (portal.teleportTo != null) event.getPlayer().teleport(portal.teleportTo, TeleportCause.PLUGIN);

            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    Menu menu = MenuGUI.getMenu(portal.menuToOpen);
                    if (menu == null) return;
                    menu.openMenu(event.getPlayer());
                    if (portal.spawnOnMenuClose) playersInMenu.add(event.getPlayer());
                    if (portal.hidePlayerWhileInMenu) hidePlayer(event.getPlayer());
                }
            }, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!playersInMenu.contains(player)) return;
        playersInMenu.remove(player);
        player.teleport(player.getWorld().getSpawnLocation(), TeleportCause.PLUGIN);
        showPlayer(player);
    }

    public void cleanup() {
        for (Player player : Collections.unmodifiableCollection(playersInMenu)) {
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
        for (Player playerInMenu : Collections.unmodifiableCollection(playersInMenu)) {
            event.getPlayer().hidePlayer(playerInMenu);
        }
    }

}
