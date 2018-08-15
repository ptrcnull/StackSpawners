package ml.bjorn.stackspawners;

import org.bukkit.Material;
import org.bukkit.block.Block;
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
    private SpawnerManager manager = new SpawnerManager();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockAgainst();

        if (event.isCancelled()) return;

        if (block.getType() == Material.MOB_SPAWNER && event.getBlock().getType() == Material.MOB_SPAWNER) {
            event.setCancelled(true);
            Inventory inv = event.getPlayer().getInventory();
            ItemStack mainHandItemStack = ((PlayerInventory) inv).getItemInMainHand();
            mainHandItemStack.setAmount(mainHandItemStack.getAmount() - 1);

            manager.add(block.getLocation(), event.getPlayer());
        }
    }

    @EventHandler
    public void onBlockDestroy(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.MOB_SPAWNER) {
            if(manager.remove(block.getLocation(), event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }
}
