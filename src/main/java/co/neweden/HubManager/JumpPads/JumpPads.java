package co.neweden.HubManager.JumpPads;

import co.neweden.HubManager.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JumpPads implements Listener {

    private Map<Block, JPadMeta> pads = new HashMap<>();
    private Set<Player> jumpedPlayers = new HashSet<>();

    public JumpPads(@NotNull Logger logger, ConfigurationSection config) {
        if (config == null) return;

        logger.info("Loading Jump Pads");
        for (String key : config.getKeys(false)) {
            ConfigurationSection padConfig = config.getConfigurationSection(key);
            Block pad;
            try {
                pad = Util.parseLocationFromString(padConfig.getString("padLocation", null)).getBlock();
            } catch (IllegalArgumentException e) {
                logger.log(Level.SEVERE, "An error occurred while loading Jump Pad \"" + key + "\", continuing to the next Jump Pad.", e);
                continue;
            }
            if (pad.getWorld() == null) {
                logger.warning("Unable to load Jump Pad \"" + key + "\" as the world is not loaded.");
                continue;
            }
            JPadMeta meta = new JPadMeta();
            meta.y = padConfig.getDouble("height", 0.2);
            meta.distance = padConfig.getInt("distance", 3);
            meta.yaw = (float) padConfig.getDouble("yaw", 90);
            meta.pitch = (float) padConfig.getDouble("pitch", 0);
            pads.put(pad, meta);
            logger.info("Loaded Jump Pad \"" + key + "\"");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo().getBlock().getType() != Material.GOLD_PLATE) return;

        JPadMeta meta = pads.get(event.getTo().getBlock());
        if (meta == null) return; // Block is not a jump pad

        Player player = event.getPlayer();
        Location loc = player.getLocation();
        loc.setYaw(meta.yaw);
        loc.setPitch(meta.pitch);
        player.setVelocity(loc.getDirection().setY(meta.y).multiply(meta.distance));
        jumpedPlayers.add(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || event.getCause() != DamageCause.FALL) return;
        Player player = (Player) event.getEntity();
        if (jumpedPlayers.contains(player)) {
            event.setDamage(0);
            jumpedPlayers.remove(event.getEntity());
        }
    }

}
