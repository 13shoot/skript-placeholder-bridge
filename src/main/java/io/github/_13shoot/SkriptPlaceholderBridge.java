package io.github._13shoot;

import ch.njol.skript.Skript;
import ch.njol.skript.variables.Variables;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

public class SkriptPlaceholderBridge extends JavaPlugin {

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().severe("PlaceholderAPI not found! Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (getServer().getPluginManager().getPlugin("Skript") == null) {
            getLogger().severe("Skript not found! Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new SkriptBridgeExpansion().register();
        getLogger().info("SkriptPlaceholderBridge enabled.");
    }

    private static class SkriptBridgeExpansion extends PlaceholderExpansion {

        @Override
        public String getIdentifier() {
            return "skript";
        }

        @Override
        public String getAuthor() {
            return "_13shoot";
        }

        @Override
        public String getVersion() {
            return "1.0.0";
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onRequest(OfflinePlayer player, String identifier) {
            // identifier = insurance.status OR market.today.summary etc.
            String path = identifier.replace('_', '.');

            // 1) try player-scoped: {path::uuid}
            if (player != null) {
                Object val = Variables.getVariable(path, player, false);
                if (val != null) {
                    return String.valueOf(val);
                }
            }

            // 2) try global: {path}
            Object global = Variables.getVariable(path, null, false);
            if (global != null) {
                return String.valueOf(global);
            }

            return "";
        }
    }
}
