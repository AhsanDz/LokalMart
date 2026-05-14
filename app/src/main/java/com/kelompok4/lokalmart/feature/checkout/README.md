# Feature: Checkout, Payment & Tracking (Muna Fatinah Atiqoh)

## Tabel Supabase
- `orders`, `order_items`, `payments`

## Yang harus dibuat di sini
```
checkout/
├── data/
│   ├── OrderRepository.kt         # buat order, ambil riwayat, update status
│   └── PaymentRepository.kt       # buat payment, update status pembayaran
├── viewmodel/
│   ├── CheckoutViewModel.kt
│   └── OrderViewModel.kt
└── ui/
    ├── CheckoutScreen.kt          # konfirmasi → alamat → metode → submit
    ├── PaymentMethodScreen.kt
    ├── OrderHistoryScreen.kt
    └── OrderDetailScreen.kt       # tracking pending → confirmed → shipped → delivered
```

## Checklist proposal
- [ ] Create: buat order ke `orders` + `order_items`, simpan payment ke `payments`
- [ ] Read: detail satu pesanan + status pembayaran
- [ ] List: riwayat pesanan user dalam LazyColumn
- [ ] ViewModel: CheckoutViewModel, OrderViewModel
