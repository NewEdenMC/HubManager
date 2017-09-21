package co.neweden.HubManager.JumpPads;

import co.neweden.HubManager.Util;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JumpPads implements Listener {

    private Map<Block, JPadMeta> pads = new HashMap<>();

    public JumpPads(Logger logger, ConfigurationSection config) {
        Validate.notNull(logger);
        Validate.notNull(config);
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
        player.setVelocity(player.getLocation().getDirection().setY(meta.y).multiply(meta.distance));
    }

}
