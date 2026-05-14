# Panduan Kerja Tim — LokalMart

Dokumen ini wajib dibaca oleh **semua anggota Kelompok 4** sebelum mulai koding.

---

## 1. Setup awal (sekali per anggota)

### Pertama kali clone repo
```bash
git clone https://github.com/<owner>/lokalmart.git
cd lokalmart
git config user.name "Nama Lengkap"
git config user.email "email@kampus.ac.id"
```

### Buat `local.properties`
1. Copy `local.properties.example` → `local.properties`
2. Isi `SUPABASE_URL` dan `SUPABASE_ANON_KEY` (tanya di grup tim)
3. **JANGAN commit file ini** (sudah di-gitignore)

### Sinkron `dev` ke local
```bash
git checkout dev
git pull origin dev
```

---

## 2. Branch strategy

| Branch | Fungsi | Yang boleh push |
|---|---|---|
| `main` | Versi stabil untuk demo | Hanya merge dari `dev` saat milestone |
| `dev` | Integrasi semua fitur | Hanya via Pull Request (PR) |
| `feature/<nama>/<task>` | Tempat kerja anggota | Anggota itu sendiri |

### Konvensi nama branch
```
feature/<nama-anggota>/<task-singkat>
```

**Contoh:**
- `feature/ahsan/auth-login`
- `feature/ahsan/admin-verification`
- `feature/putri/product-catalog`
- `feature/mevya/store-register`
- `feature/khoiriah/cart-screen`
- `feature/muna/checkout-flow`
- `feature/febrian/review-form`

---

## 3. Workflow harian

```bash
# 1. Mulai hari: pull dev terbaru
git checkout dev
git pull origin dev

# 2. Buat branch baru untuk task hari ini
git checkout -b feature/ahsan/auth-login

# 3. Kerja, commit kecil-kecil
git add app/src/main/java/com/kelompok4/lokalmart/feature/auth/
git commit -m "feat: add login screen UI"

# 4. Push ke remote
git push -u origin feature/ahsan/auth-login

# 5. Buka GitHub → buat Pull Request ke branch `dev`
# 6. Tunggu approval dari minimal 1 anggota lain
# 7. Merge via tombol GitHub
# 8. Hapus branch lokal & remote setelah merge
git checkout dev
git pull origin dev
git branch -d feature/ahsan/auth-login
```

---

## 4. Commit message convention

Format: `<tipe>: <deskripsi pendek>`

| Tipe | Kapan dipakai | Contoh |
|---|---|---|
| `feat` | Fitur baru | `feat: add product detail screen` |
| `fix` | Bug fix | `fix: cart total calculation wrong` |
| `refactor` | Refactor tanpa ubah behavior | `refactor: extract ProductCard composable` |
| `docs` | Ubah dokumentasi | `docs: update auth README` |
| `chore` | Build, config, dependency | `chore: bump compose bom to 2024.12.01` |
| `style` | Formatting, tidak ubah logic | `style: format AuthViewModel.kt` |
| `test` | Tambah/ubah test | `test: add login validation test` |

**Tulis dalam bahasa Inggris** biar konsisten dan singkat. Deskripsi pakai imperative ("add login" bukan "added login").

---

## 5. File yang dilindungi

File-file ini **butuh diskusi di grup tim** sebelum diubah, karena dipakai banyak anggota:

- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/kelompok4/lokalmart/core/**`
- `app/src/main/java/com/kelompok4/lokalmart/data/model/**`
- `app/src/main/java/com/kelompok4/lokalmart/navigation/Screen.kt`
- `app/src/main/java/com/kelompok4/lokalmart/navigation/AppNavHost.kt`

**Aturan saat menambah route di `Screen.kt` atau `AppNavHost.kt`:**
- Boleh menambah, jangan menghapus/mengubah punya anggota lain
- Commit terpisah (jangan dicampur dengan commit fitur)
- Tag anggota terkait di PR description

---

## 6. Aturan Pull Request

### Sebelum buat PR
- [ ] Pull `dev` terbaru, rebase atau merge ke branch kamu
- [ ] Pastikan build lolos di local (jalanin Run di Android Studio)
- [ ] Cek file yang berubah — kalau ada file di luar folder fitur kamu, kasih tau di description
- [ ] Commit messages sudah ikut convention

### Isi PR
- **Title**: pakai format conventional commit (`feat: add login screen`)
- **Description**: pakai template otomatis yang muncul
- **Reviewer**: minimal 1 anggota lain
- **Label**: tambahkan label yang sesuai (kalau ada)

### Review etiquette
- Review dalam **24 jam** kalau bisa, jangan biar PR menumpuk
- Komen yang konstruktif, jelaskan **kenapa** kalau minta perubahan
- Approve hanya kalau benar-benar sudah dibaca

### Setelah merge
- Reviewer atau author boleh klik tombol "Delete branch" di GitHub
- Author hapus branch lokal: `git branch -d feature/...`

---

## 7. Menangani konflik

### Konflik dengan `dev` (saat mau merge PR)
```bash
git checkout dev
git pull origin dev
git checkout feature/ahsan/auth-login
git merge dev
# Resolve conflict di Android Studio → Save → commit
git push
```

### Konflik di `Screen.kt` atau `AppNavHost.kt`
1. Pull `dev` ke branch kamu
2. Buka file yang konflik
3. **Keep both changes** — biasanya kamu cuma nambah route baru, anggota lain juga nambah route baru
4. Save, build, commit, push

---

## 8. Milestone & merge ke `main`

`dev` di-merge ke `main` hanya saat:
- Akhir setiap minggu (Sabtu malam) **atau**
- Setelah milestone tercapai (auth selesai, katalog selesai, dst)

Yang merge: PIC repo (Ahsan), setelah:
- Semua PR untuk milestone sudah merge
- App berjalan tanpa crash di emulator
- README status implementasi diupdate

---

## 9. Kalau bingung

1. Tanya di grup tim WhatsApp/Discord dulu
2. Cek `docs/GIT_QUICKSTART.md` untuk perintah git dasar
3. Cek README masing-masing folder fitur untuk apa yang harus dibuat
