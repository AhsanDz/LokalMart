# Git Quickstart — LokalMart

Cheatsheet untuk **workflow tim ini specifically**, bukan tutorial git dasar.

## Skenario harian

### Mulai task baru
```bash
git checkout dev
git pull origin dev
git checkout -b feature/<nama>/<task>
```

### Sudah selesai, mau push & buat PR
```bash
git add .                                    # atau spesifik file
git commit -m "feat: tambah login screen"
git push -u origin feature/<nama>/<task>
# → buka GitHub, buat PR ke `dev`
```

### Setelah PR di-merge
```bash
git checkout dev
git pull origin dev
git branch -d feature/<nama>/<task>          # hapus branch lokal
git push origin --delete feature/<nama>/<task>   # hapus branch remote (atau pakai tombol GitHub)
```

---

## Skenario merepotkan (sering terjadi)

### "Kok branch `dev`-ku ketinggalan?"
```bash
git checkout dev
git pull origin dev
```
Selalu lakukan ini sebelum buat branch baru.

### "Aku lupa pull dev, sekarang conflict"
```bash
git checkout dev
git pull origin dev
git checkout feature/<nama>/<task>
git merge dev
# Resolve conflict di Android Studio → Save → commit
git push
```

### "Ada file di Screen.kt yang conflict sama punya anggota lain"
Ini paling umum terjadi. Solusinya:
1. Buka file di Android Studio
2. Klik tab dengan icon conflict
3. **Biasanya kamu cuma nambah route baru** — keep both changes
4. Save → build → kalau build lolos berarti aman
5. `git add Screen.kt && git commit && git push`

### "Aku salah commit di branch `dev` langsung, bukan di feature branch!"
```bash
# Asumsikan kamu belum push
git log --oneline                            # cek commit hash terakhir
git reset --soft HEAD~1                      # undo commit, file changes masih ada
git checkout -b feature/<nama>/<task>        # buat branch yang seharusnya
git commit -m "..."                          # commit ulang di sini
```

### "Aku commit `local.properties` (yang berisi API key)!"
**JANGAN PANIK, tapi cepat.**
```bash
# Kalau belum push:
git reset HEAD~1                             # undo commit
echo "local.properties" >> .gitignore        # cek sudah masuk gitignore (harusnya iya)
git add .gitignore && git commit -m "chore: ensure local.properties is gitignored"

# Kalau sudah push:
# 1. Kabari grup tim SEGERA
# 2. Regenerate Supabase anon key di Dashboard
# 3. Update local.properties semua anggota
```

### "Mau lihat siapa yang ngerjain file ini"
```bash
git blame app/src/main/java/com/kelompok4/lokalmart/feature/auth/ui/LoginScreen.kt
```

### "Aku mau revert satu file ke versi `dev`"
```bash
git checkout dev -- path/ke/file.kt
```

### "Branch-ku banyak commit kecil-kecil, mau di-squash"
Gak perlu di-squash manual — pas merge PR di GitHub, pilih opsi **"Squash and merge"** kalau commitnya berantakan.

---

## Perintah yang dilarang untuk branch shared (`dev` & `main`)

```bash
git push --force                # ❌ bisa hapus kerjaan orang
git push --force-with-lease     # ❌ tetap berbahaya di shared branch
git rebase dev (saat di dev)    # ❌
git reset --hard HEAD~N         # ❌ kalau sudah di-push
```

Aman dilakukan **hanya di branch feature kamu sendiri** yang belum di-merge.

---

## Tip umum

- **Commit kecil-kecil**, jangan sekali commit untuk seluruh hari kerja
- **Push minimal sekali sehari** biar kalau laptop rusak kerjaan gak ilang
- **Pull `dev` tiap pagi** sebelum mulai kerja
- **Baca diff sebelum commit**: `git diff --staged`
- Pakai Android Studio's built-in git UI kalau lebih nyaman (View → Tool Windows → Commit)
