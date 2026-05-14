# LokalMart

Aplikasi marketplace hyperlocal untuk UMKM — Projek Akhir Pengembangan Aplikasi Mobile, Kelompok 4.

## Tech stack
- **UI**: Jetpack Compose (Material 3)
- **Bahasa**: Kotlin 2.0.21 (JDK 17)
- **Backend**: Supabase (Postgres + Auth + Storage + Realtime)
- **DI**: Hilt
- **Pola**: MVVM + Repository (per-feature)
- **Min SDK**: 24 · **Target SDK**: 35

## Setup awal

### 1. Clone & buka di Android Studio
- Pakai **Android Studio Ladybug** (2024.2.x) atau lebih baru
- File → Open → pilih folder `LokalMart` ini
- Tunggu Gradle sync selesai (download dependencies)

### 2. Konfigurasi Supabase
1. Buat file `local.properties` di root project (copy dari `local.properties.example`)
2. Isi `SUPABASE_URL` dan `SUPABASE_ANON_KEY` dari Supabase Dashboard
3. File `local.properties` SUDAH gitignored, jangan di-commit

### 3. Run di emulator/device
- Pilih device target → klik Run ▶
- App akan launch di placeholder Splash screen

## Struktur folder

```
com.kelompok4.lokalmart/
├── LokalMartApplication.kt   # @HiltAndroidApp
├── MainActivity.kt           # NavHost host
│
├── core/                     # Shared infrastructure (jangan diubah tanpa diskusi tim)
│   ├── di/                   # Hilt modules (SupabaseModule)
│   ├── network/              # SupabaseTables constants
│   ├── storage/              # StorageHelper (upload ke bucket)
│   ├── util/                 # Resource<T>, dll
│   └── common/
│       ├── components/       # Composable reusable
│       └── theme/            # Color, Typography, Theme
│
├── navigation/               # AppNavHost, Screen routes
├── data/model/               # Data class lintas-fitur (mirror tabel Supabase)
│
└── feature/                  # Satu folder per anggota
    ├── auth/        (Ahsan)
    ├── admin/       (Ahsan)
    ├── catalog/     (Putri)
    ├── store/       (Mevya)
    ├── search/      (Khoiriah)
    ├── checkout/    (Muna)
    └── review/      (Febrian)
```

Tiap folder `feature/<nama>/` punya struktur:
- `data/` — Repository spesifik fitur
- `viewmodel/` — ViewModel (di-inject pakai `@HiltViewModel`)
- `ui/` — Composable screens

Tiap folder ada README sendiri yang menjelaskan apa yang harus dibuat — baca dulu sebelum mulai!

## Pembagian tugas

| Anggota | NIM | Folder | Fitur utama |
|---|---|---|---|
| Ahsan Faqih | 245150707111027 | `feature/auth/` + `feature/admin/` | Autentikasi, Profil, Admin Panel |
| Putri Anastasya | 235150700111024 | `feature/catalog/` | Katalog produk + upload foto |
| Mevya Najwa | 245150700111012 | `feature/store/` | Manajemen toko & stok |
| Khoiriah Azizah Nur L | 245150701111016 | `feature/search/` | Pencarian, filter, keranjang |
| Muna Fatinah Atiqoh | 245150707111033 | `feature/checkout/` | Checkout, pembayaran, tracking |
| Febrian Faiq Putra | 235150707111037 | `feature/review/` | Review, rating, dashboard analitik |

## Aturan kerja tim

1. **Jangan edit file di folder anggota lain** tanpa diskusi — agar tidak terjadi merge conflict
2. **File di `core/`, `data/model/`, `navigation/Screen.kt`** boleh diedit, tapi commit terpisah & kabari tim di grup
3. **Selalu pull dulu sebelum push** — pakai branch per anggota: `feature/<nama>` (mis. `feature/ahsan-auth`)
4. **Convention**: PascalCase untuk Composable & class, camelCase untuk properti/fungsi
5. **Tabel Supabase**: jangan rename kolom tanpa diskusi (migrasi RLS bisa pecah)

## Status implementasi

- [x] Project setup, struktur folder, theme, navigation skeleton
- [ ] Supabase schema & RLS policy (tahap berikutnya)
- [ ] Auth & Profile (Ahsan)
- [ ] Catalog & Product (Putri)
- [ ] Store & Inventory (Mevya)
- [ ] Search & Cart (Khoiriah)
- [ ] Checkout & Payment (Muna)
- [ ] Review & Dashboard (Febrian)
- [ ] Admin Panel (Ahsan)
