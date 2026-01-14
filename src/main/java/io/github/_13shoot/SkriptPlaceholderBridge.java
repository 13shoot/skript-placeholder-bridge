package io.github._13shoot;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

public class SkriptPlaceholderBridge extends JavaPlugin {

    private static Method getVariableMethod;

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

        try {
            Class<?> variablesClass = Class.forName("ch.njol.skript.variables.Variables");
            getVariableMethod = variablesClass.getMethod(
                    "getVariable",
                    String.class,
                    Object.class,
                    boolean.class
            );
        } catch (Exception e) {
            getLogger().severe("Failed to hook into Skript Variables!");
            e.printStackTrace();
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
            return "13shoot";
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
            String path = identifier.replace('_', '.');

            // 1) Try player-scoped variable {path::uuid}
            if (player != null && getVariableMethod != null) {
                try {
                    Object val = getVariableMethod.invoke(
                            null,
                            path,
                            player,
                            false
                    );
                    if (val != null) {
                        return String.valueOf(val);
                    }
                } catch (Exception ignored) {}
            }

            // 2) Try global variable {path}
            try {
                Object global = getVariableMethod.invoke(
                        null,
                        path,
                        null,
                        false
                );
                if (global != null) {
                    return String.valueOf(global);
                }
            } catch (Exception ignored) {}

            return "";
        }
    }
}
