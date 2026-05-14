-- =============================================================================
-- 1. KATEGORI UMKM AWAL
-- =============================================================================

INSERT INTO public.categories (name) VALUES
  ('Fashion'),
  ('Kerajinan Tangan'),
  ('Kecantikan & Perawatan'),
  ('Elektronik'),
  ('Rumah Tangga'),
  ('Pertanian & Tanaman'),
  ('Olahraga & Outdoor'),
  ('Buku & Alat Tulis'),
  ('Mainan & Hobi'),
  ('Otomotif'),
  ('Lain-lain')
ON CONFLICT (name) DO NOTHING;