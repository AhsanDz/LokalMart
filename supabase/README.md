# Supabase Migrations

Folder ini berisi file SQL untuk setup database LokalMart di Supabase.

## Cara menjalankan

1. Buka project di [Supabase Dashboard](https://supabase.com/dashboard)
2. Sidebar kiri → **SQL Editor** → klik **New query**
3. Buka file `.sql` di folder ini, copy seluruh isinya
4. Paste ke SQL Editor → klik **Run** (atau `Ctrl/Cmd + Enter`)
5. Cek hasilnya di **Table Editor** atau jalankan query verifikasi yang ada di akhir file

## Urutan menjalankan

Jalankan secara berurutan (yang belum dibuat akan diisi di step berikutnya):

| File | Isi | Status |
|---|---|---|
| `001_initial_schema.sql` | 12 tabel + index + trigger profile | ✅ Ada |
| `002_rls_policies.sql` | RLS + policy semua tabel | ⏳ Step 3 |
| `003_storage_buckets.sql` | 4 bucket + policy storage | ⏳ Step 4 |
| `004_seed_data.sql` | Insert kategori awal | ⏳ Step 5 |

## Kalau perlu reset (DEV ONLY)

Untuk drop semua tabel dan mulai dari nol:
```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;
```

**JANGAN PERNAH** jalankan ini di project produksi.

## Catatan

- Supabase punya schema khusus seperti `auth`, `storage`, `realtime` yang tidak boleh diubah manual
- Semua tabel app kita ada di schema `public`
- Trigger `on_auth_user_created` mendengarkan INSERT di `auth.users`, ini cara standar Supabase
