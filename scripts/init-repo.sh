#!/bin/bash
# init-repo.sh
# Jalankan sekali oleh PIC repo untuk inisialisasi git.
# Setelah ini, anggota lain tinggal clone.

set -e

# Cek apakah sudah jadi git repo
if [ -d ".git" ]; then
    echo "❌ Folder ini sudah jadi git repo."
    echo "   Hapus folder .git/ dulu kalau mau mulai dari awal."
    exit 1
fi

echo "🚀 Inisialisasi git repo LokalMart..."
echo ""

# Init dengan branch utama bernama 'main'
git init -b main

# Pastikan local.properties tidak ke-track
if [ -f "local.properties" ]; then
    if ! git check-ignore -q local.properties; then
        echo "⚠️  local.properties tidak ada di .gitignore!"
        echo "   Tambahkan dulu sebelum lanjut."
        exit 1
    fi
fi

# Initial commit di main
echo "📦 Initial commit di branch 'main'..."
git add .
git commit -m "chore: initial project setup"

# Buat branch dev dari main
echo "🌿 Membuat branch 'dev'..."
git checkout -b dev

echo ""
echo "✅ Selesai! Repo lokal sudah siap."
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Langkah berikutnya:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. Buat repo BARU di GitHub (https://github.com/new)"
echo "   - Nama: lokalmart"
echo "   - Visibility: Private (atau Public, terserah)"
echo "   - JANGAN centang 'Add a README' / .gitignore / license"
echo "     (kita sudah punya semuanya)"
echo ""
echo "2. Hubungkan local ke remote:"
echo "   git remote add origin git@github.com:<owner>/lokalmart.git"
echo ""
echo "3. Push branch main dan dev:"
echo "   git push -u origin main"
echo "   git push -u origin dev"
echo ""
echo "4. Set 'dev' sebagai default branch:"
echo "   GitHub → Settings → Branches → Default branch → ganti ke 'dev'"
echo ""
echo "5. Set branch protection untuk 'main' DAN 'dev':"
echo "   GitHub → Settings → Branches → Add rule"
echo "   - Branch name pattern: main (lalu ulangi untuk dev)"
echo "   - ☑ Require a pull request before merging"
echo "     - ☑ Require approvals (1)"
echo "     - ☑ Require review from Code Owners"
echo "   - ☑ Do not allow bypassing the above settings"
echo ""
echo "6. Invite 5 anggota lain sebagai collaborator:"
echo "   GitHub → Settings → Collaborators → Add people"
echo "   - Ahsan, Putri, Mevya, Khoiriah, Muna, Febrian"
echo "   - Role: Write"
echo ""
echo "7. Update file .github/CODEOWNERS — ganti @ahsan-gh dst"
echo "   dengan username GitHub asli tiap anggota."
echo ""
echo "8. Share API key Supabase ke anggota lewat channel pribadi"
echo "   (JANGAN di-commit, JANGAN di grup publik)"
echo ""
