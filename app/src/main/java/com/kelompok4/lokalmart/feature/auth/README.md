# Feature: Auth & Profile (Ahsan Faqih)

## Tabel Supabase
- `profiles` — data profil pengguna
- Supabase Auth (built-in) untuk login/register/session

## Bucket Storage
- `avatars` — foto profil (`{user_id}/avatar.webp`)

## Yang harus dibuat di sini
```
auth/
├── data/
│   └── AuthRepository.kt          # signIn, signUp, signOut, currentUser, updateProfile
├── viewmodel/
│   ├── AuthViewModel.kt           # state login/register
│   └── ProfileViewModel.kt        # state profil + upload avatar
└── ui/
    ├── SplashScreen.kt
    ├── LoginScreen.kt
    ├── RegisterScreen.kt
    ├── ProfileScreen.kt
    └── EditProfileScreen.kt
```

## Checklist proposal
- [ ] Create: register akun via Supabase Auth
- [ ] Read: ambil data profil berdasarkan ID
- [ ] List: (di-handle di feature admin)
- [ ] ViewModel: AuthViewModel, ProfileViewModel
