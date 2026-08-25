# 02 — Fungsi Per Code

Peta isi kode FarLands: dua class Java + satu config.

## 1. `FarLandsPlugin.java` (class utama)

| Anggota | Fungsi |
|---|---|
| `onEnable()` | Baca config (`saveDefaultConfig` + `load()`), lalu pasang `FarLandsPopulator` ke semua world yang hidup. Log ringkasan zona. |
| `reload()` | Dipanggil command `reload`: `reloadConfig()` → baca ulang nilai → pasang ulang populator (idempoten). |
| `load()` | Parsing config + pengaman: `end` dipatok maks 29.999.984 (batas dunia); kalau `end < start` maka `end = start`; `ramp` minimal 1; `wall-thickness` minimal 0. |
| `apply(World)` | Pasang populator jika belum ada — scan `w.getPopulators()`, skip bila `FarLandsPopulator` sudah terdaftar. |
| Getter `start() end() ramp() wallT() holes() blobs() debug()` | Jembatan config → populator (populator membaca lewat plugin, bukan menyimpan salinan). |
| `onCommand(...)` | Tanpa argumen = tampilkan status zona; `reload` = muat ulang config. |

## 2. `FarLandsPopulator.java` (otak terrain)

### `populate(World, Random, Chunk)` — pintu masuk per chunk

```java
long dNear = ...; long dFar = ...;
if (dFar < start || dNear > end) return;   // di luar zona: nol sentuhan
```

- Hitung jarak Chebyshev (sumbu terjauh) terdekat & terjauh sudut-sudut chunk terhadap koordinat 0.
- Chunk di luar zona langsung keluar — murah.
- Loop 256 kolom (16×16); tiap kolom di dalam zona diproses sesuai `Plan`:
  - `wall` → isi batu penuh dari dasar sampai hampir sky-limit.
  - `targetH` lebih tinggi dari permukaan → timbun (`matFor`).
  - lebih rendah → pangkas udara.
  - `hole` → jurang dari dasar sampai permukaan baru.
  - `blob` → bola batu melayang.

### `plan(seed, minY, maxY, bx, bz) → Plan` — rencana satu kolom

Record `Plan(targetH, wall, hole, blob)`:

```java
if (d <= start + wallT()) return new Plan(maxY, true, false, false); // Great Wall
double t = min(1, (d - start) / ramp);                              // progres ramp
double frac = 0.24 + 0.52*n1 + 0.12*(n2-0.5);                       // campur noise kasar + halus
frac = clamp(0.5 + t*(frac-0.5), 0.03, 0.97);                       // makin jauh makin ekstrem
int th = minY + (int)(frac * (maxY-minY));                          // tinggi target kolom
hole  = holes && vhash(...) < 0.055*t;                              // peluang naik seiring jarak
blob  = blobs && vhash(...) < 0.05*t && posisi di tengah chunk;
```

- `t = 0` (tepat di garis start) → `frac ≈ 0.5` (wajar); `t = 1` → penuh kegilaan noise.
- `n1` frekuensi rendah (skala ÷110) membentuk pegunungan besar; `n2` frekuensi tinggi (÷17) memberi tekstur gerigi.

### `topMaterial(env, sea, th)` — material permukaan per dimensi

- Nether → `NETHERRACK`; End → `END_STONE`.
- Overworld: di bawah laut → `GRAVEL` (dalam) / `SAND` (dangkal); daratan → `GRASS_BLOCK`; sangat tinggi → `SNOW_BLOCK`.

### `matFor(random, y, th, top, fill, normal)` — isi bawah permukaan

Lapisan: `y == th` → material atas; 4 blok di bawahnya → fill (dirt/dll); sisanya batu + taburan ore:

- Diamond hanya di `y < -46` (peluang ~0,16%),
- Iron di kedalaman > 12,
- Coal di kedalaman > 5.

### `blob(chunk, random, bx, bz, surf, minY, maxY)` — pulau melayang

Bola batu radius 2–4 pada ketinggian `permukaan + 14..34`. Dibatasi `(lx >> 4) != cx → skip` supaya tidak meluber ke chunk tetangga (populator hanya boleh menyentuh chunk-nya sendiri).

### `vhash(seed, x, z)` — hash posisi → angka acak deterministik

Varian splitmix64: perkalian konstanta prima, XOR-shift, hasil dinormalisasi ke `[0,1)`. Sumber semua "keacakan" — murni fungsi dari (seed, x, z), tanpa state.

### `vnoise(seed, x, z)` — value noise halus

Interpolasi bilinear 4 sudut grid dengan smoothstep `u = xf²(3-2xf)`. Dipakai di `plan()` dua kali (skala besar & kecil).

### `set(chunk, x, y, z, m)`

Wrapper `setType(m, false)` — `false` = jangan update fisika cahaya/redstone per blok (cepat).

## 3. `config.yml`

Semua kunci dijelaskan di [04-fitur-dan-effect.md](04-fitur-dan-effect.md); `debug: false` opsional untuk logging chunk yang masuk zona.
