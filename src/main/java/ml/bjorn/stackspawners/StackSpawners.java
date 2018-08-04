package ml.bjorn.stackspawners;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class StackSpawners extends JavaPlugin {

    public static StackSpawners plugin;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        plugin = this;
        config = getConfig();
        if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            getLogger().severe("*** HolographicDisplays is not installed or not enabled. ***");
            getLogger().severe("*** This plugin will be disabled. ***");
            this.setEnabled(false);
            return;
        }
        for (String key : config.getKeys(false)) {
            int count = config.getInt(key);

            String[] locArr = key.split(",");
            double x = Double.parseDouble(locArr[0]);
            double y = Double.parseDouble(locArr[1]);
            double z = Double.parseDouble(locArr[2]);

            Location loc = new Location(getServer().getWorlds().get(0), x, y, z);

            if (loc.getBlock().getType() != Material.MOB_SPAWNER) {
                config.set(key, null);
                saveConfig();
                continue;
            }
            Location hloc = loc.add(0.5, 1.5, 0.5);
            Hologram hologram = HologramsAPI.createHologram(plugin, hloc);
            hologram.appendTextLine(count + "x");
        }
        getServer().getPluginManager().registerEvents(new EventListener(), this);

    }

    @Override
    public void onDisable() {
        saveConfig();
        plugin = null;
    }
}
