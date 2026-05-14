# Feature: Search, Filter & Cart (Khoiriah Azizah Nur L)

## Tabel Supabase
- `products`, `stores` (untuk search)
- `carts` (untuk keranjang)

## Yang harus dibuat di sini
```
search/
├── data/
│   ├── SearchRepository.kt        # query produk/toko + filter
│   └── CartRepository.kt          # CRUD item keranjang
├── viewmodel/
│   ├── SearchViewModel.kt
│   └── CartViewModel.kt
└── ui/
    ├── SearchScreen.kt            # input keyword + hasil
    ├── FilterScreen.kt            # kategori / harga / lokasi
    └── CartScreen.kt              # daftar item + total harga
```

## Checklist proposal
- [ ] Create: tambah item ke keranjang (insert ke `carts`)
- [ ] Read: ambil item keranjang user yg login + total harga
- [ ] List: hasil pencarian dalam LazyColumn; daftar isi keranjang
- [ ] ViewModel: SearchViewModel, CartViewModel
