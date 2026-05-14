# Feature: Manajemen Toko & Stok (Mevya Najwa)

## Tabel Supabase
- `stores`, `products` (untuk update stok)

## Bucket Storage
- `store-assets/logos/` — logo toko

## Yang harus dibuat di sini
```
store/
├── data/
│   ├── StoreRepository.kt         # pendaftaran toko, edit info toko
│   └── InventoryRepository.kt     # update stok per produk
├── viewmodel/
│   ├── StoreViewModel.kt
│   └── InventoryViewModel.kt
└── ui/
    ├── StoreRegisterScreen.kt     # form "Buka Toko"
    ├── MyStoreScreen.kt           # dashboard toko sendiri
    ├── StoreProfileScreen.kt      # profil toko publik
    ├── EditStoreInfoScreen.kt
    └── InventoryScreen.kt         # manajemen stok
```

## Checklist proposal
- [ ] Create: submit pendaftaran toko ke `stores` (status awal pending)
- [ ] Read: detail informasi toko berdasarkan ID
- [ ] List: daftar produk milik toko di halaman profil toko
- [ ] ViewModel: StoreViewModel, InventoryViewModel
