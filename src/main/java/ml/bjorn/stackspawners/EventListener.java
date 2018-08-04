package ml.bjorn.stackspawners;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.stream.Collectors;

public class EventListener implements Listener {
    private StackSpawners plugin = StackSpawners.plugin;
    private FileConfiguration config = plugin.getConfig();
    private boolean isLocationEqual(Location s, Location t) { return s.getX() == t.getX() && s.getY() == t.getY() && s.getZ() == t.getZ(); }
    private String locToStr(Location loc, String s) { return loc.getBlockX() + s + loc.getBlockY() + s + loc.getBlockZ(); }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block blockAgainst = event.getBlockAgainst();
        Block blockPlaced = event.getBlock();

        if (event.isCancelled()) return;

        if (blockAgainst.getType() == Material.MOB_SPAWNER && blockPlaced.getType() == Material.MOB_SPAWNER) {
            event.setCancelled(true);
            Inventory inv = event.getPlayer().getInventory();
            ItemStack mainHandItemStack = ((PlayerInventory) inv).getItemInMainHand();
            mainHandItemStack.setAmount(mainHandItemStack.getAmount() - 1);

            Location loc = blockAgainst.getLocation();
            Location hloc = blockAgainst.getLocation().add(0.5, 1.5, 0.5);

            String configLocation = locToStr(loc, ",");
            String commandLocation = locToStr(loc, " ");

            if (config.contains(configLocation)) {
                // Modify count in the config
                int count = config.getInt(configLocation);
                count++;
                config.set(configLocation, count);
                plugin.saveConfig();

                // Find an existing hologram
                List<Hologram> list = HologramsAPI.getHolograms(plugin)
                        .stream()
                        .filter( h -> isLocationEqual(h.getLocation(), hloc))
                        .collect(Collectors.toList());

                if (list.size() < 1) {
                    plugin.getLogger().severe("Hologram missing at " + locToStr(hloc, ",") + "!");
                    // Create a new hologram
                    Hologram hologram = HologramsAPI.createHologram(plugin, hloc);
                    hologram.appendTextLine(count + "x");
                } else {
                    Hologram hologram = list.get(0);
                    // Clear lines and add a new line with spawner count
                    hologram.clearLines();
                    hologram.appendTextLine(count + "x");
                }

                // Change the actual mob spawn count
                int scount = count * 4;
                plugin.getServer().dispatchCommand(
                        plugin.getServer().getConsoleSender(),
                        "blockdata " + commandLocation + " {MaxNearbyEntities:" + scount + "s,SpawnCount:" + scount + "s}"
                );
            } else {
                // Add the count value to the config
                config.set(configLocation, 2);
                plugin.saveConfig();

                // Create a new hologram with spawner count
                Hologram hologram = HologramsAPI.createHologram(plugin, hloc);
                hologram.appendTextLine("2x");

                // Change the actual mob spawn count
                plugin.getServer().dispatchCommand(
                        plugin.getServer().getConsoleSender(),
                        "blockdata " + commandLocation + " {MaxNearbyEntities:8s,SpawnCount:8s}"
                );
            }
        }
    }

    @EventHandler
    public void onBlockDestroy(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.MOB_SPAWNER) {
            Location loc = block.getLocation();
            Location hloc = loc.add(0.5, 1.5, 0.5);

            String configLocation = loc.getBlockX() + "," + (loc.getBlockY() - 1) + "," + loc.getBlockZ();

            if (config.contains(configLocation)) {
                config.set(configLocation, null);
                plugin.saveConfig();
            }

            List<Hologram> list = HologramsAPI.getHolograms(plugin)
                    .stream()
                    .filter( h -> isLocationEqual(h.getLocation(), hloc))
                    .collect(Collectors.toList());

            if (list.size() > 0) list.get(0).delete();
        }
    }
}
