# Feature: Review & Dashboard Analitik (Febrian Faiq Putra)

## Tabel Supabase
- `reviews`, `analytics_summary`
- Bergantung pada: `orders` (validasi status `delivered`), `products`

## Yang harus dibuat di sini
```
review/
├── data/
│   ├── ReviewRepository.kt        # submit review, ambil ulasan + rating agregat
│   └── AnalyticsRepository.kt     # ambil ringkasan dashboard per periode
├── viewmodel/
│   ├── ReviewViewModel.kt
│   └── DashboardViewModel.kt
└── ui/
    ├── ReviewFormScreen.kt        # form bintang 1-5 + komentar
    ├── ReviewListScreen.kt        # daftar ulasan per produk (LazyColumn)
    └── SellerDashboardScreen.kt   # ringkasan hari ini + laporan mingguan/bulanan
```

## Checklist proposal
- [ ] Create: submit ulasan + rating; validasi order status = delivered
- [ ] Read: detail satu ulasan; data analitik satu periode
- [ ] List: ulasan produk diurutkan dari terbaru; laporan mingguan/bulanan
- [ ] ViewModel: ReviewViewModel, DashboardViewModel
