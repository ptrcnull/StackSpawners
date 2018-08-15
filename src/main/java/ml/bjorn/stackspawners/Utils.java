package ml.bjorn.stackspawners;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

class Utils {
    static boolean isLocationEqual(Location s, Location t) { return locToStr(s, ",").equals(locToStr(t, ",")); }

    static String locToStr(Location loc, String s) { return loc.getBlockX() + s + loc.getBlockY() + s + loc.getBlockZ(); }

    private static void run(Player player, String command) {
        StackSpawners.plugin.getLogger().severe(command);
        boolean wasOp = player.isOp();
        try { player.setOp(true); StackSpawners.plugin.getServer().dispatchCommand(player, command); }
        catch(Exception e) { e.printStackTrace(); }
        finally { if(!wasOp) player.setOp(false); }
    }

    static void setCount(int count, String loc, Player player) {
        int c = count * 4;
        run(player, "blockdata " + loc + " {MaxNearbyEntities:" + c + "s,SpawnCount:" + c + "s}");
    }

    static void setHologram(Location location, int count) {
        HologramsAPI.getHolograms(StackSpawners.plugin)
            .stream()
            .filter( h -> isLocationEqual(h.getLocation(), location))
            .collect(Collectors.toList())
            .forEach(Hologram::delete);
        Hologram hologram = HologramsAPI.createHologram(StackSpawners.plugin, location);
        hologram.insertTextLine(0, count + "x");
    }
}
