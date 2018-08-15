package ml.bjorn.stackspawners;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import static ml.bjorn.stackspawners.Utils.*;

class SpawnerManager {
    private HashMap<String, Integer> spawners = new HashMap<>();
    private StackSpawners plugin = StackSpawners.plugin;
    private FileConfiguration config = plugin.getConfig();

    SpawnerManager() {
        Set<String> keys = StackSpawners.config.getKeys(false);
        keys.forEach((key)->spawners.put(key, StackSpawners.config.getInt(key)));
    }

    void add(Location location, Player player) {
        Location hloc = location.clone().add(0.5, 1.5, 0.5);
        String configLocation = locToStr(location, ",");
        String commandLocation = locToStr(location, " ");

        if (config.contains(configLocation)) {
            // Modify count in the config
            int count = config.getInt(configLocation);
            count++;
            config.set(configLocation, count);
            plugin.saveConfig();

            // Update hologram with new spawner count
            setHologram(hloc, count);

            // Change the actual mob spawn count
            setCount(count, commandLocation, player);
        } else {
            // Add the count value to the config
            config.set(configLocation, 2);
            plugin.saveConfig();

            // Create a new hologram with spawner count
            setHologram(hloc, 2);

            // Change the actual mob spawn count
            setCount(2, commandLocation, player);
        }
    }

    boolean remove(Location location, Player player) {
        Location hloc = location.clone().add(0.5, 1.5, 0.5);

        String configLocation = locToStr(location, ",");
        String commandLocation = locToStr(location, " ");

        if (!config.contains(configLocation)) {
            return false;
        }

        // Modify count in the config
        int count = config.getInt(configLocation);

        if(count == 2) {
            HologramsAPI.getHolograms(plugin)
                .stream()
                .filter( h -> isLocationEqual(h.getLocation(), hloc))
                .collect(Collectors.toList())
                .forEach((Hologram::delete));
            // Change the actual mob spawn count
            setCount(1, commandLocation, player);
            config.set(configLocation, null);
            plugin.saveConfig();
        } else {
            count--;
            config.set(configLocation, count);
            plugin.saveConfig();

            // Create a new hologram with spawner count
            setHologram(hloc, count);

            // Change the actual mob spawn count
            setCount(count, commandLocation, player);
        }

        // Drop an actual spawner on the floor
        ItemStack item = new ItemStack(Material.MOB_SPAWNER, 1);
        location.getWorld().dropItemNaturally(location, item);

        return true;
    }
}
