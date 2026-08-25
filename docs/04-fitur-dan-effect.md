# 04 — Fitur dan Effect

Rincian tiap fitur FarLands: fungsinya apa, efeknya in-game seperti apa, dan config mana yang mengaturnya.

## Ringkasan config → fitur

| Kunci config | Default | Fitur yang dikontrol |
|---|---|---|
| `start` | 12550821 | Titik mulai zona |
| `end` | 29999984 | Batas akhir zona |
| `ramp` | 2000 | Lebar transisi normal→kacau |
| `wall-thickness` | 48 | Tebal Great Wall |
| `holes` | true | Jurang maut |
| `blobs` | true | Pulau batu melayang |
| `debug` | false | Log chunk yang masuk zona |

## 1. Zona Far Lands bisa diatur (`start`, `end`)

**Fungsi:** menentukan rentang jarak (blok dari koordinat 0, sumbu X/Z mana pun) tempat terrain ditulis ulang.

**Effect in-game:** di luar zona = dunia normal 100%. Masuk zona = permukaan meliuk-liuk ekstrem: gunungan menjulang sampai mendekati sky-limit lalu lembah dalam. Karena pakai jarak Chebyshev, zona berbentuk "bingkai" persegi mengelilingi spawn.

**Catatan:** `end` dipaksa ≤ 29.999.984 (batas dunia); konfigurasi kacau tidak bikin crash.

## 2. Transisi bertingkat (`ramp`)

**Fungsi:** parameter `t = (jarak − start) / ramp` mencampur terrain wajar (frac≈0,5) dengan chaos penuh.

**Effect in-game:** beberapa ratus–ribu blok setelah garis start, bentuk aneh muncul sedikit demi sedikit — dulu bukit kecil, lalu menara, akhirnya dinding vertikal tak karuan. `ramp` kecil = tembok keanehan mendadak; besar = pergeseran halus.

## 3. Great Wall (`wall-thickness`)

**Fungsi:** strip selebar `wall-thickness` blok tepat di garis `start` diisi batu penuh dari dasar dunia sampai `maxY − 6`.

**Effect in-game:** dinding raksasa nyaris tanpa celah sebagai "gerbang" masuk Far Lands — penghormatan sekaligus penghalang dramatis. Naik ke puncaknya (atau tunnel Ender Pearls) adalah mini-challenge sendiri.

## 4. Jurang maut (`holes`)

**Fungsi:** peluang per kolom naik seiring jarak (`< 0.055 × t`), membuang semua blok dari dasar hingga permukaan.

**Effect in-game:** lubang-jurang vertikal tak terlihat sampai kamu jatuh. Bikin perjalanan darat di zona berisiko; sepatu Feather Falling jadi sahabat.

## 5. Pulau batu melayang (`blobs`)

**Fungsi:** peluang `< 0.05 × t` per kolom pusat (hanya di area tengah chunk), menempatkan bola batu radius 2–4 di udara (permukaan +14..34).

**Effect in-game:** siluet khas Far Lands lama — bebatuan mengambang. Bisa dipakai jembatan alami kalau beruntung posisinya.

## 6. Dukungan 3 dimensi

**Fungsi:** `topMaterial()` memilih material sesuai environment; Nether & End juga ikut dipopulasi.

**Effect in-game:** Far Lands versi netherrack di Nether dan end-stone di End — zona tetap konsisten dengan tema dimensinya.

## 7. Ore di kedalaman

**Fungsi:** `matFor()` menyisipkan coal (kedalaman >5), iron (>12), diamond (y < −46).

**Effect in-game:** menambang di dalam Far Lands tetap looting — bahkan diamond sedikit lebih mudah dijumpai karena terrain mengangkat lapisan dalam ke permukaan.

## 8. Deterministik per seed

**Fungsi:** semua bentuk lahir dari `vhash/vnoise(worldSeed, x, z)` — fungsi murni tanpa state.

**Effect in-game:** seed sama = Far Lands identik persis (bisa direproduksi untuk video/konten); ganti seed = peta chaos baru total. Reload plugin tidak mengubah bentuk yang sudah ada.

## 9. Debug log (`debug: true`)

**Effect:** tiap chunk yang masuk zona tercatat di console (`FL chunk x,z masuk zona`) — berguna memastikan zona aktif tanpa harus terbang kesana.

## 10. Kompatibilitas Folia

**Fungsi:** `folia-supported: true`; seluruh pekerjaan lewat `BlockPopulator` yang dipanggil thread-region milik server, tanpa scheduler global.

**Effect:** plugin bisa jalan di Paper maupun Folia tanpa modifikasi.
