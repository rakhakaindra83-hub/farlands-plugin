package id.kuru.farlands;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

/**
// ponytail: rekreasi Far Lands via BlockPopulator (bukan bug float-point asli —
// itu butuh mod engine-level). Ceiling: 1 chunk = 256 kolom setBlock sinkron,
// oke untuk SMP; kalau TPS drop saat eksplorasi zona, naikkan `start` atau
// ganti ke pendekatan NMS noise-router per versi server.
 */
final class FarLandsPopulator extends BlockPopulator {

    record Plan(int targetH, boolean wall, boolean hole, boolean blob) {}

    private final FarLandsPlugin pl;

    FarLandsPopulator(FarLandsPlugin pl) { this.pl = pl; }

    @Override
    public void populate(World w, Random random, Chunk chunk) {
        long start = pl.start(), end = pl.end();
        int minX = chunk.getX() << 4, minZ = chunk.getZ() << 4;
        int maxX = minX + 15, maxZ = minZ + 15;
        long dNear = Math.max(minX > 0 ? minX : (maxX < 0 ? -(long) maxX : 0),
                              minZ > 0 ? minZ : (maxZ < 0 ? -(long) maxZ : 0));
        long dFar = Math.max(Math.max(Math.abs((long) minX), Math.abs((long) maxX)),
                             Math.max(Math.abs((long) minZ), Math.abs((long) maxZ)));
        if (dFar < start || dNear > end) return; // chunk di luar zona: sentuh sama sekali
        if (pl.debug()) pl.getLogger().info("FL chunk " + minX + "," + minZ + " masuk zona");

        long seed = w.getSeed();
        int minY = w.getMinHeight(), maxY = w.getMaxHeight() - 1;
        int sea = w.getSeaLevel();
        boolean normal = w.getEnvironment() == Environment.NORMAL;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int bx = minX + x, bz = minZ + z;
                long d = Math.max(Math.abs((long) bx), Math.abs((long) bz));
                if (d < start || d > end) continue;
                Plan p = plan(seed, minY, maxY, bx, bz);
                if (p.wall()) {
                    for (int y = minY + 1; y <= maxY - 6; y++) set(chunk, x, y, z, Material.STONE);
                    continue;
                }
                int th = p.targetH();
                int vh = w.getHighestBlockYAt(bx, bz);
                Material top = topMaterial(w.getEnvironment(), sea, th);
                Material fill = th < sea ? Material.GRAVEL : (normal ? Material.DIRT : top);
                if (th > vh) {
                    for (int y = vh + 1; y <= th; y++) set(chunk, x, y, z, matFor(random, y, th, top, fill, normal));
                } else if (th < vh) {
                    for (int y = th + 1; y <= vh; y++) set(chunk, x, y, z, Material.AIR);
                }
                set(chunk, x, th, z, top);
                if (p.hole()) for (int y = minY + 6; y <= th; y++) set(chunk, x, y, z, Material.AIR);
                if (p.blob()) blob(chunk, random, bx, bz, th, minY, maxY);
            }
        }
    }

    Plan plan(long seed, int minY, int maxY, int bx, int bz) {
        long d = Math.max(Math.abs((long) bx), Math.abs((long) bz));
        long start = pl.start();
        if (d < start || d > pl.end()) return new Plan(0, false, false, false);
        if (d <= start + pl.wallT()) return new Plan(maxY, true, false, false);
        double t = Math.min(1.0, (d - start) / (double) pl.ramp());
        double n1 = vnoise(seed, bx / 110.0, bz / 110.0);
        double n2 = vnoise(seed ^ 0x5DEECE66DL, bx / 17.0, bz / 17.0);
        double frac = 0.24 + 0.52 * n1 + 0.12 * (n2 - 0.5);
        frac = Math.max(0.03, Math.min(0.97, 0.5 + t * (frac - 0.5)));
        int th = minY + (int) (frac * (maxY - minY));
        boolean hole = pl.holes() && vhash(seed ^ 0x51L, bx, bz) < 0.055 * t;
        boolean blob = pl.blobs() && vhash(seed ^ 0xB0L, bx, bz) < 0.05 * t
                && (bx & 15) >= 4 && (bx & 15) <= 11 && (bz & 15) >= 4 && (bz & 15) <= 11;
        return new Plan(th, false, hole, blob);
    }

    static Material topMaterial(Environment env, int sea, int th) {
        return switch (env) {
            case NETHER -> Material.NETHERRACK;
            case THE_END -> Material.END_STONE;
            default -> th < sea ? (th < sea - 12 ? Material.GRAVEL : Material.SAND)
                    : (th > sea + 80 ? Material.SNOW_BLOCK : Material.GRASS_BLOCK);
        };
    }

    private Material matFor(Random r, int y, int th, Material top, Material fill, boolean normal) {
        if (y == th) return top;
        if (y > th - 4) return fill;
        if (!normal) return Material.STONE;
        int depth = th - y;
        float f = r.nextFloat();
        if (y < -46 && f < 0.0016f) return Material.DIAMOND_ORE;
        if (depth > 12 && f < 0.010f) return Material.IRON_ORE;
        if (depth > 5 && f < 0.016f) return Material.COAL_ORE;
        return Material.STONE;
    }

    private void blob(Chunk ch, Random r, int bx, int bz, int surf, int minY, int maxY) {
        int cy = Math.min(maxY - 6, surf + 14 + r.nextInt(20));
        int rad = 2 + r.nextInt(3);
        int cx = ch.getX(), cz = ch.getZ();
        for (int dx = -rad; dx <= rad; dx++) {
            for (int dy = -rad; dy <= rad; dy++) {
                for (int dz = -rad; dz <= rad; dz++) {
                    if (dx * dx + dy * dy + dz * dz > rad * rad) continue;
                    int y = cy + dy;
                    int lx = bx + dx, lz = bz + dz;
                    if (y <= minY || y >= maxY) continue;
                    if ((lx >> 4) != cx || (lz >> 4) != cz) continue; // jangan meluber ke chunk tetangga
                    set(ch, lx & 15, y, lz & 15, Material.STONE);
                }
            }
        }
    }

    private static void set(Chunk ch, int x, int y, int z, Material m) {
        ch.getBlock(x, y, z).setType(m, false);
    }

    private static double vhash(long seed, int x, int z) {
        long h = seed ^ (x * 0x27D4EB2DL) ^ (z * 0x165667B1L);
        h *= 0x9E3779B97F4A7C15L;
        h ^= h >>> 29;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 32;
        return (h >>> 11) / (double) (1L << 53);
    }

    private static double vnoise(long seed, double x, double z) {
        int xi = (int) Math.floor(x), zi = (int) Math.floor(z);
        double xf = x - xi, zf = z - zi;
        double u = xf * xf * (3 - 2 * xf), v = zf * zf * (3 - 2 * zf);
        double a = vhash(seed, xi, zi), b = vhash(seed, xi + 1, zi);
        double c = vhash(seed, xi, zi + 1), e = vhash(seed, xi + 1, zi + 1);
        return a + (b - a) * u + (c - a) * v + (a - b - c + e) * u * v;
    }
}
