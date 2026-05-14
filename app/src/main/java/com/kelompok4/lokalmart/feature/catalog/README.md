# Feature: Katalog Produk (Putri Anastasya)

## Tabel Supabase
- `products`, `product_images`, `categories`

## Bucket Storage
- `product-images` — foto produk (`{store_id}/{product_id}/main.webp`)

## Yang harus dibuat di sini
```
catalog/
├── data/
│   └── ProductRepository.kt       # CRUD produk + upload foto
├── viewmodel/
│   └── ProductViewModel.kt
└── ui/
    ├── HomeScreen.kt              # katalog di beranda (LazyVerticalGrid)
    ├── ProductDetailScreen.kt     # detail + galeri foto
    ├── AddProductScreen.kt        # form input + upload foto
    └── EditProductScreen.kt
```

## Checklist proposal
- [ ] Create: tambah produk + upload satu/lebih foto ke `product-images`
- [ ] Read: detail produk berdasarkan ID beserta seluruh foto
- [ ] List: katalog produk aktif dalam LazyVerticalGrid
- [ ] ViewModel: ProductViewModel

## Catatan implementasi fitur media
Foto produk WAJIB:
- Disimpan di bucket `product-images`
- URL hasil upload disimpan ke tabel `product_images`
- Satu foto ditandai sebagai `is_primary = true`
