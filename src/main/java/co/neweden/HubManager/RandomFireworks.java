package co.neweden.HubManager;

import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;

import java.util.Random;

class RandomFireworks {

    static void setupRandomFireworks(Plugin plugin, ConfigurationSection config) {
        if (!config.getBoolean("enabled", false)) return;
        for (String key : config.getKeys(false)) {
            if (key.equalsIgnoreCase("enabled")) continue;
            ConfigurationSection fConfig = config.getConfigurationSection(key);
            if (fConfig != null)
                new RandomFireworks(plugin, key, fConfig);
        }
    }

    private Location location;

    private RandomFireworks(Plugin plugin, String name, ConfigurationSection config) {
        Validate.notNull(plugin);
        Validate.notNull(config);

        String location = config.getString("location", null);
        if (location == null) {
            plugin.getLogger().warning("Tried to load Random Fireworks '" + name + "' but location is null");
            return;
        }
        try {
            this.location = Util.parseLocationFromString(location, true);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Tried to load Random Fireworks '" + name + "' but location is not valid");
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                createRandomFirework();
            }
        }, 0, config.getInt("delayBetweenFirework", 5));
        plugin.getLogger().info("Setup Random Fireworks '" + name + "'");
    }

    private void createRandomFirework() {
        Firework f = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fMeta = f.getFireworkMeta();
        Random random = new Random();

        FireworkEffect effect = FireworkEffect.builder()
                .flicker(random.nextBoolean())
                .withColor(getColor(random.nextInt(17) + 1))
                .withFade(getColor(random.nextInt(17) + 1))
                .with(Type.values()[random.nextInt(Type.values().length)])
                .trail(random.nextBoolean()).build();

        fMeta.addEffect(effect);
        fMeta.setPower(random.nextInt(2) + 1);
        f.setFireworkMeta(fMeta);
    }

    private Color getColor(int i) {
        switch (i) {
            case 1: return Color.AQUA;
            case 2: return Color.BLACK;
            case 3: return Color.BLUE;
            case 4: return Color.FUCHSIA;
            case 5: return Color.GRAY;
            case 6: return Color.GREEN;
            case 7: return Color.LIME;
            case 8: return Color.MAROON;
            case 9: return Color.NAVY;
            case 10: return Color.OLIVE;
            case 11: return Color.ORANGE;
            case 12: return Color.PURPLE;
            case 13: return Color.RED;
            case 14: return Color.SILVER;
            case 15: return Color.TEAL;
            case 16: return Color.WHITE;
            case 17: return Color.YELLOW;
            default: return Color.WHITE;
        }
    }

}
