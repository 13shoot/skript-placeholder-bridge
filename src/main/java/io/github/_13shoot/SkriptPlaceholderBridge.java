package io.github._13shoot;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

public class SkriptPlaceholderBridge extends JavaPlugin {

    // Reflection handle to Skript Variables.getVariable(...)
    private static Method getVariableMethod;

    @Override
    public void onEnable() {

        // --- Dependency check ---
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

        // --- Hook into Skript Variables (Skript 2.13.x compatible) ---
        try {
            Class<?> variablesClass =
                    Class.forName("ch.njol.skript.variables.Variables");
            Class<?> eventClass =
                    Class.forName("ch.njol.skript.lang.Event");

            // Skript 2.13.x signature:
            // Variables.getVariable(String name, Event event)
            getVariableMethod = variablesClass.getMethod(
                    "getVariable",
                    String.class,
                    eventClass
            );

        } catch (Exception e) {
            getLogger().severe("Failed to hook into Skript Variables!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // --- Register PlaceholderAPI expansion ---
        new SkriptExpansion().register();
        getLogger().info("SkriptPlaceholderBridge enabled.");
    }

    // =====================================================
    // PlaceholderAPI Expansion
    // =====================================================
    private static class SkriptExpansion extends PlaceholderExpansion {

        @Override
        public String getIdentifier() {
            // %skript_<path>%
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

            // Convert skript_market.today -> market.today
            String path = identifier.replace('_', '.');

            // --- Global variable only (safe baseline) ---
            try {
                Object value = getVariableMethod.invoke(
                        null,
                        path,
                        null   // null Event = global scope
                );

                if (value != null) {
                    return String.valueOf(value);
                }

            } catch (Exception ignored) {
            }

            return "";
        }
    }
}
