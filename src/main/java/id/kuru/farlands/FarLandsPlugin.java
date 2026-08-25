package id.kuru.farlands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.java.JavaPlugin;

public final class FarLandsPlugin extends JavaPlugin {

    private long start, end;
    private int ramp, wallT;
    private boolean holes, blobs;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        load();
        for (World w : Bukkit.getWorlds()) apply(w);
        getLogger().info("Far Lands aktif: jarak " + start + " s/d " + end
                + " (ramp " + ramp + ", wall " + wallT + ")");
    }

    public void reload() {
        reloadConfig();
        load();
        for (World w : Bukkit.getWorlds()) apply(w);
    }

    private boolean debug;

    private void load() {
        debug = getConfig().getBoolean("debug", false);
        start = getConfig().getLong("start", 12_550_821L);
        end = Math.min(getConfig().getLong("end", 29_999_984L), 29_999_984L);
        if (end < start) end = start; // konfigurasi kacau: jangan crash, cukup clamp
        ramp = Math.max(1, getConfig().getInt("ramp", 2000));
        wallT = Math.max(0, getConfig().getInt("wall-thickness", 48));
        holes = getConfig().getBoolean("holes", true);
        blobs = getConfig().getBoolean("blobs", true);
    }

    /** Pasang populator kalau belum ada (idempoten, aman dipanggil ulang). */
    private void apply(World w) {
        for (BlockPopulator p : w.getPopulators()) {
            if (p instanceof FarLandsPopulator) return;
        }
        w.getPopulators().add(new FarLandsPopulator(this));
    }

    public long start() { return start; }
    public long end()   { return end; }
    public int ramp()   { return ramp; }
    public int wallT()  { return wallT; }
    public boolean holes() { return holes; }
    public boolean blobs() { return blobs; }
    public boolean debug() { return debug; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (a.length == 1 && a[0].equalsIgnoreCase("reload")) {
            reload();
            s.sendMessage("§a[FarLands] §fZona baru: " + start + " s/d " + end
                    + ", ramp " + ramp + ". Chunk BARU saja yang berubah.");
            return true;
        }
        s.sendMessage("§a[FarLands] §fstart=" + start + " end=" + end + " ramp=" + ramp
                + " wall=" + wallT + " holes=" + holes + " blobs=" + blobs);
        return true;
    }
}
