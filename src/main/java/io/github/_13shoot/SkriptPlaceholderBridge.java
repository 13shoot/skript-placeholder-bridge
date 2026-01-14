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

        // ---- SAFE reflection hook (no Event class reference) ----
        try {
            Class<?> variablesClass =
                    Class.forName("ch.njol.skript.variables.Variables");

            for (Method m : variablesClass.getMethods()) {
                if (!m.getName().equals("getVariable")) continue;
                Class<?>[] params = m.getParameterTypes();
                if (params.length >= 2 && params[0] == String.class) {
                    getVariableMethod = m;
                    break;
                }
            }

            if (getVariableMethod == null) {
                throw new NoSuchMethodException("No compatible getVariable method found");
            }

        } catch (Exception e) {
            getLogger().severe("Failed to hook into Skript Variables!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new SkriptExpansion().register();
        getLogger().info("SkriptPlaceholderBridge enabled.");
    }

    // =====================================================
    // PlaceholderAPI Expansion
    // =====================================================
    private static class SkriptExpansion extends PlaceholderExpansion {

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

            // IMPORTANT: Skript requires full variable name with {}
            String path = "{" + identifier.replace('_', '.') + "}";

            try {
                Object value = getVariableMethod.invoke(
                        null,
                        path,
                        null
                );

                if (value != null) {
                    return String.valueOf(value);
                }

            } catch (Exception ignored) {}

            return "";
        }

    }
}
