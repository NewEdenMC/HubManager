package co.neweden.HubManager;

 import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.validation.constraints.NotNull;
import java.util.logging.Logger;

public class InventoryListener implements Listener {

    private boolean preventPickup;
    private boolean preventDrop;
    private boolean preventMove;
    private boolean clearOnJoin;

    public InventoryListener(@NotNull Logger logger, ConfigurationSection config) {
        if (config == null) return;

        preventPickup = config.getBoolean("preventItemPickup", false);
        if (preventPickup) logger.info("Enabled Item Pickup restrictions.");

        preventDrop = config.getBoolean("preventItemDrop", false);
        if (preventDrop) logger.info("Enabled Item Drop restrictions.");

        preventMove = config.getBoolean("preventMoveInOwnInventory", false);
        if (preventMove) logger.info("Enabled Item Move restrictions.");

        clearOnJoin = config.getBoolean("clearOnJoin", false);
        if (clearOnJoin) logger.info("Enabled clearing Player Inventories on join");
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (preventPickup && event.getEntity() instanceof Player)
            event.setCancelled(true);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (preventDrop) event.setCancelled(true);
    }

    @EventHandler
    public void onMoveItem(InventoryMoveItemEvent event) {
        if (preventMove && (event.getSource().getHolder() instanceof Player || event.getDestination().getHolder() instanceof Player))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (clearOnJoin) event.getPlayer().getInventory().clear();
    }

}
