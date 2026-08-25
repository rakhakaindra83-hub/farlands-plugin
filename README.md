# FarLands

Plugin Minecraft **Paper / Folia 1.21+** — menghidupkan kembali legenda **Far Lands**: terrain kacau menjulang tinggi, dinding raksasa, jurang maut, dan pulau batu melayang di kejauhan dunia. Dibuat via `BlockPopulator` (bukan mod engine), jadi cukup drop JAR dan jelajah.

> v1.0.1 — `folia-supported: true`

## Fitur

- Zona Far Lands dengan jarak mulai/akhir yang bisa diatur (`start`, `end`)
- Transisi halus terrain normal → kacau (`ramp`), makin jauh makin gila bentuknya
- **Great Wall**: dinding batu setinggi hampir sky-limit tepat di garis awal zona
- **Jurang maut** (`holes`) dan **pulau batu melayang** (`blobs`) di dalam zona
- Bekerja di 3 dimensi: Overworld (rumput/pasir/salju + ore), Nether (netherrack), End (end stone)
- Terrain deterministik per world-seed — dunia sama = Far Lands sama
- `/farlands` untuk lihat zona aktif, `/farlands reload` tanpa restart
- Hanya chunk **baru** yang terpengaruh (chunk lama tidak ditulis ulang)

## Cara mencicipi

1. Taruh `FarLands-1.0.1.jar` di folder `plugins/`, restart server.
2. Main biasa dulu — Far Lands asli mulai ±12,55 juta blok dari titik 0.
3. Malas jalan kaki? Kecilkan `start` di `config.yml` (misal `10000`), lalu `/farlands reload`, dan jelajahi chunk baru sekitar koordinat itu.

## Perintah (`farlands.admin`, default op)

| Perintah | Fungsi |
|---|---|
| `/farlands` | Lihat zona aktif (start, end, ramp, wall, holes, blobs) |
| `/farlands reload` | Muat ulang config; berlaku untuk chunk baru |

## Build

Tanpa Gradle/Maven:

    bash build.sh   # javac --release 21 + jar cf, butuh JDK21 & paper-api di $LOCALAPPDATA/tools/mc-libs

## Dokumentasi lengkap

Lihat folder `docs/`:

1. [01-cara-pembuatan.md](docs/01-cara-pembuatan.md) — langkah membuat plugin ini dari nol
2. [02-fungsi-per-code.md](docs/02-fungsi-per-code.md) — fungsi tiap file/class/method
3. [03-sistem-command.md](docs/03-sistem-command.md) — sistem command detail
4. [04-fitur-dan-effect.md](docs/04-fitur-dan-effect.md) — tiap fitur & efeknya in-game
