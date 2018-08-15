package ml.bjorn.stackspawners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Collectors;

public final class StackSpawners extends JavaPlugin {

    static StackSpawners plugin;
    static FileConfiguration config;

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
        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            getLogger().severe("*** ProtocolLib is not installed or not enabled. ***");
            getLogger().severe("*** This plugin will be disabled. ***");
            this.setEnabled(false);
            return;
        }

        if (!config.isSet("world")) config.set("world", getServer().getWorlds().get(0).getName());
        saveConfig();
        String worldName = config.getString("world");

        World world = getServer().getWorlds()
            .stream().filter(w -> w.getName().equals(worldName)).collect(Collectors.toList())
            .get(0);

        for (String key : config.getKeys(false)) {
            if (key.equals("world")) continue;

            int count = config.getInt(key);

            String[] locArr = key.split(",");
            double x = Double.parseDouble(locArr[0]);
            double y = Double.parseDouble(locArr[1]);
            double z = Double.parseDouble(locArr[2]);

            Location loc = new Location(world, x, y, z);

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
        ProtocolLibrary.getProtocolManager().addPacketListener(
            new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    if (event.getPacketType() == PacketType.Play.Server.CHAT) {
                        PacketContainer packet = event.getPacket();
                        StructureModifier<WrappedChatComponent> chatComponents = packet.getChatComponents();
                        String message = chatComponents.read(0).getJson();

                        if (message.contains("MaxNearbyEntities")) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        );

    }

    @Override
    public void onDisable() {
        saveConfig();
        plugin = null;
    }
}
