# 01 — Cara Pembuatan Plugin FarLands

Dokumen ini menjelaskan bagaimana plugin ini dibuat dari nol, sehingga bisa ditiru atau dimodifikasi.

## Langkah 0 — Persiapan alat

- **JDK 21** (Paper 1.20.5+ butuh Java 21).
- **paper-api-1.21.8.jar** sebagai library compile (tidak perlu Gradle/Maven).
- Editor apa pun; semua contoh di sini pakai shell bash.

## Langkah 1 — Struktur folder

```
farlands-plugin/
├── src/
│   ├── main/java/id/kuru/farlands/
│   │   ├── FarLandsPlugin.java      # class utama + command
│   │   └── FarLandsPopulator.java   # otak pembentuk terrain
│   └── main/resources/
│       ├── plugin.yml               # metadata plugin
│       └── config.yml               # default config
├── build.sh                         # script compile
└── README.md
```

## Langkah 2 — plugin.yml

```yaml
name: FarLands
version: 1.0.1
main: id.kuru.farlands.FarLandsPlugin
api-version: '1.21'
folia-supported: true          # deklarasi Folia (plugin ini aman: tanpa scheduler global)
author: kuru
description: Menghidupkan kembali Far Lands dengan jarak mulai/maks yang bisa diatur.
commands:
  farlands:
    description: Kontrol zona Far Lands
    permission: farlands.admin
permissions:
  farlands.admin:
    default: op
```

Poin penting:

- `api-version: '1.21'` supaya server tahu plugin ini memakai API modern.
- `folia-supported: true` hanya boleh dipakai jika plugin **tidak** memakai scheduler global Bukkit untuk logic dunia. FarLands aman karena satu-satunya kerjanya lewat `BlockPopulator` yang dipanggil server saat generate chunk.

## Langkah 3 — Ide inti: BlockPopulator

Far Lands versi asli adalah bug floating-point di generator lawas. Mereproduksi bug itu butuh mod level engine; cara pragmatisnya adalah **menimpa terrain setelah chunk dibuat** memakai `BlockPopulator`:

```java
public void populate(World w, Random random, Chunk chunk) { ... }
```

Server memanggil method ini untuk tiap chunk baru. Di dalamnya kita:

1. Cek apakah chunk masuk zona Far Lands (jarak Chebyshev dari koordinat 0).
2. Kalau ya, bentuk ulang kolom-kolom blok sesuai "rencana" terrain.
3. Kalau bukan, keluar lebih awal — biaya nol.

Populator dipasang saat `onEnable()`:

```java
for (World w : Bukkit.getWorlds()) apply(w);

private void apply(World w) {
    for (BlockPopulator p : w.getPopulators())
        if (p instanceof FarLandsPopulator) return;   // idempoten
    w.getPopulators().add(new FarLandsPopulator(this));
}
```

`apply()` selalu dicek dulu supaya `/farlands reload` bisa dipanggil berkali-kali tanpa menumpuk populator dobel.

## Langkah 4 — Config default

`config.yml` disalin otomatis oleh `saveDefaultConfig()` saat pertama kali jalan:

```yaml
start: 12550821        # jarak (blok) Far Lands mulai terbentuk
end: 29999984          # batas maksimal (= batas world)
ramp: 2000             # lebar transisi normal -> kacau
wall-thickness: 48     # tebal Great Wall di garis start
holes: true            # jurang maut
blobs: true            # pulau batu melayang
```

## Langkah 5 — Compile & paket

`build.sh`:

```bash
CP="...paper-api-1.21.8.jar;..."        # classpath dipisah ';' di Windows
find src -name '*.java' > sources.txt
javac --release 21 -encoding UTF-8 -cp "$CP" -d classes @sources.txt
cp src/main/resources/*.yml classes/
jar cf FarLands-1.0.1.jar -C classes .  # plugin.yml & config.yml HARUS di root JAR
```

Hasil: `FarLands-1.0.1.jar` (~8 KB). Taruh di `plugins/`, restart.

## Catatan desain

- **Hanya chunk baru** yang berubah — chunk lama tidak ditulis ulang (aman untuk dunia existing, tapi makanya `reload` tidak mengubah area yang sudah dijelajahi).
- Semua angka deterministik terhadap `world.getSeed()`: dunia yang sama selalu menghasilkan Far Lands identik, beda seed = beda bentuk.
- `ponytail:` 1 chunk = 256 kolom `setBlock` sinkron; oke untuk SMP. Kalau TPS turun saat eksplorasi cepat, naikkan `start` atau pindah ke pendekatan noise-router NMS per versi server.
