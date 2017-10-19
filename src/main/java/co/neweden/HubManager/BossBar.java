package co.neweden.HubManager;

import com.sun.istack.internal.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class BossBar implements Listener {

    private org.bukkit.boss.BossBar bar;
    private List<String> messages = new ArrayList<>();
    private int currentMessageIndex = 0;

    public BossBar(@NotNull Plugin plugin, ConfigurationSection config) {
        if (config == null || !config.getBoolean("enabled", false)) return;
        List<String> rawMessages = config.getStringList("messages");
        if (rawMessages.size() <= 0) return;
        for (String message : rawMessages) {
            messages.add(ChatColor.translateAlternateColorCodes('&', message));
        }

        BarColor colour = BarColor.PURPLE;
        BarStyle style = BarStyle.SOLID;
        double progress;
        try {
            colour = BarColor.valueOf(config.getString("barColour", "PURPLE"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Tried to load Boss Bar Colour from config but '" + config.getString("barColour", "PURPLE") + "' is not a valid value, Boss Bar will be created with default colour of PURPLE.");
        }
        try {
            style = BarStyle.valueOf(config.getString("barStyle", "SOLID"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Tried to load Boss Bar Style from config but '" + config.getString("barStyle", "SOLID") + "' is not a valid value, Boss Bar will be created with default style of SOLID.");
        }
        progress = config.getDouble("barProgress", 1.0);
        if (progress < 0 || progress > 1) {
            plugin.getLogger().warning("Tried to load Boss Bar Progress from config but '" + progress + "' is not a valid value, valid values are between 0.0 and 1.0, Boss Bar will be created with default value of 1.0.");
            progress = 1.0;
        }

        bar = Bukkit.createBossBar(messages.get(0), colour, style, BarFlag.DARKEN_SKY);
        bar.setProgress(progress);

        int messageDelay = config.getInt("numberOfTicksBetweenMessages", 200);
        if (messages.size() > 1) {
            Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
                @Override
                public void run() {
                    bar.setTitle(messages.get(currentMessageIndex));
                    currentMessageIndex++;
                    if (currentMessageIndex >= messages.size()) currentMessageIndex = 0;
                }
            }, 0, messageDelay);
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("BossBar setup with " + messages.size() + " messages at a delay of " + messageDelay + " server ticks.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        bar.addPlayer(event.getPlayer());
    }

    void cleanup() {
        bar.removeAll();
    }

}
