# Feature: Admin Panel (Ahsan Faqih)

## Tabel Supabase
- `stores`, `store_verifications`, `products`, `profiles`

## Bucket Storage
- `store-assets/docs/` — dokumen verifikasi toko (akses terbatas)

## Yang harus dibuat di sini
```
admin/
├── data/
│   └── AdminRepository.kt         # approve/reject toko, suspend produk/user
├── viewmodel/
│   └── AdminViewModel.kt
└── ui/
    ├── AdminPanelScreen.kt
    ├── StoreVerificationScreen.kt
    ├── ProductModerationScreen.kt
    └── UserManagementScreen.kt
```

## Checklist proposal
- [ ] Create: record keputusan verifikasi ke `store_verifications`
- [ ] Read: detail data toko yang menunggu verifikasi
- [ ] List: antrian toko, daftar produk aktif, daftar pengguna (LazyColumn)
- [ ] ViewModel: AdminViewModel
