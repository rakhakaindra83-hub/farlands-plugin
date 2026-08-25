# 03 — Sistem Command

FarLands sengaja kecil: satu command induk, dua sub-perintah.

## Daftar command

| Command | Argumen | Permission | Default |
|---|---|---|---|
| `/farlands` | — | `farlands.admin` | op |
| `/farlands reload` | `reload` | `farlands.admin` | op |

Tidak ada tab-completion custom — jumlah argumen cuma satu, tidak perlu.

## `/farlands` (tanpa argumen) — status zona

Menampilkan nilai aktif di memori:

```
[FarLands] start=12550821 end=29999984 ramp=2000 wall=48 holes=true blobs=true
```

Gunakan ini untuk memastikan hasil `reload` benar-benar terbaca (bandingkan dengan isi `config.yml`).

## `/farlands reload` — muat ulang config

Alur internal:

```
/farlands reload
  → FarLandsPlugin.reload()
      → reloadConfig()              # baca file dari disk
      → load()                      # parse + clamp pengaman
      → apply(w) utk tiap world     # no-op kalau populator sudah ada
  → balasan ke sender
```

Balasan:

```
[FarLands] Zona baru: 10000 s/d 29999984, ramp 2000. Chunk BARU saja yang berubah.
```

### Efek & batasan

- Berlaku **hanya untuk chunk yang belum pernah di-generate**. Chunk lama sudah tersimpan di disk dan tidak ditulis ulang — ini by design agar dunia pemain lama tidak rusak.
- Populator tidak didaftarkan ulang (`apply` idempoten), jadi `reload` berkali-kali aman.
- Kalau `end < start` di file config, `load()` otomatis menyamakan `end = start` — zona jadi kosong, bukan crash.

## Contoh pemakaian nyata

**Uji coba cepat dekat spawn (world baru khusus tes):**

```
# config.yml
start: 5000
ramp: 500
```
```
/farlands reload
```
lalu jalan/teleport ke x=5050 — chunk baru di sana langsung bergaya Far Lands.

**Produksi SMP (default):** biarkan `start: 12550821`. Pemain biasa tak akan melihat efeknya; penjelajah jauh akan menemukan dinding & chaos asli.
