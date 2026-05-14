-- 1. avatars - foto profil pengguna (max 2 MB)
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
  'avatars',
  'avatars',
  true,
  2097152,  -- 2 MB dalam bytes (2 * 1024 * 1024)
  ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/svg+xml']
)
ON CONFLICT (id) DO UPDATE SET
  public = EXCLUDED.public,
  file_size_limit = EXCLUDED.file_size_limit,
  allowed_mime_types = EXCLUDED.allowed_mime_types;

-- 2. store-assets - logo toko + dokumen verifikasi (max 5 MB)
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
  'store-assets',
  'store-assets',
  true,
  5242880,  -- 5 MB
  ARRAY['image/jpeg', 'image/png', 'image/webp', 'application/pdf']
)
ON CONFLICT (id) DO UPDATE SET
  public = EXCLUDED.public,
  file_size_limit = EXCLUDED.file_size_limit,
  allowed_mime_types = EXCLUDED.allowed_mime_types;

-- 3. product-images - foto produk (max 5 MB)
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
  'product-images',
  'product-images',
  true,
  5242880,  -- 5 MB
  ARRAY['image/jpeg', 'image/png', 'image/webp']
)
ON CONFLICT (id) DO UPDATE SET
  public = EXCLUDED.public,
  file_size_limit = EXCLUDED.file_size_limit,
  allowed_mime_types = EXCLUDED.allowed_mime_types;

-- 4. category-icons - ikon kategori (admin only, max 1 MB)
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
  'category-icons',
  'category-icons',
  true,
  1048576,  -- 1 MB
  ARRAY['image/svg+xml', 'image/png']
)
ON CONFLICT (id) DO UPDATE SET
  public = EXCLUDED.public,
  file_size_limit = EXCLUDED.file_size_limit,
  allowed_mime_types = EXCLUDED.allowed_mime_types;

-- =====================================================
-- POLICIES
-- =====================================================

CREATE POLICY "avatars_select_public"
  ON storage.objects FOR SELECT
  USING (bucket_id = 'avatars');

CREATE POLICY "avatars_insert_own"
  ON storage.objects FOR INSERT
  TO authenticated
  WITH CHECK (
    bucket_id = 'avatars'
    AND auth.uid()::text = (storage.foldername(name))[1]
  );

CREATE POLICY "avatars_update_own"
  ON storage.objects FOR UPDATE
  TO authenticated
  USING (
    bucket_id = 'avatars'
    AND auth.uid()::text = (storage.foldername(name))[1]
  )
  WITH CHECK (
    bucket_id = 'avatars'
    AND auth.uid()::text = (storage.foldername(name))[1]
  );

CREATE POLICY "avatars_delete_own"
  ON storage.objects FOR DELETE
  TO authenticated
  USING (
    bucket_id = 'avatars'
    AND auth.uid()::text = (storage.foldername(name))[1]
  );

-- =====================================================
-- STORE-ASSETS
-- =====================================================

-- Logos folder: public bisa baca
CREATE POLICY "store_assets_select_logos_public"
  ON storage.objects FOR SELECT
  USING (
    bucket_id = 'store-assets'
    AND (storage.foldername(name))[1] = 'logos'
  );

-- Docs folder: hanya admin atau owner toko
CREATE POLICY "store_assets_select_docs_restricted"
  ON storage.objects FOR SELECT
  USING (
    bucket_id = 'store-assets'
    AND (storage.foldername(name))[1] = 'docs'
    AND (
      public.is_admin()
      OR public.is_store_owner(((storage.foldername(name))[2])::uuid)
    )
  );

-- Write: hanya owner toko (untuk kedua folder logos/ maupun docs/)
-- folder[2] adalah store_id di kedua kasus
CREATE POLICY "store_assets_insert_store_owner"
  ON storage.objects FOR INSERT
  TO authenticated
  WITH CHECK (
    bucket_id = 'store-assets'
    AND public.is_store_owner(((storage.foldername(name))[2])::uuid)
  );

CREATE POLICY "store_assets_update_store_owner"
  ON storage.objects FOR UPDATE
  TO authenticated
  USING (
    bucket_id = 'store-assets'
    AND public.is_store_owner(((storage.foldername(name))[2])::uuid)
  )
  WITH CHECK (
    bucket_id = 'store-assets'
    AND public.is_store_owner(((storage.foldername(name))[2])::uuid)
  );

CREATE POLICY "store_assets_delete_store_owner"
  ON storage.objects FOR DELETE
  TO authenticated
  USING (
    bucket_id = 'store-assets'
    AND public.is_store_owner(((storage.foldername(name))[2])::uuid)
  );

-- =====================================================
-- PRODUCT-IMAGES
-- =====================================================

CREATE POLICY "product_images_select_public"
  ON storage.objects FOR SELECT
  USING (bucket_id = 'product-images');

CREATE POLICY "product_images_insert_store_owner"
  ON storage.objects FOR INSERT
  TO authenticated
  WITH CHECK (
    bucket_id = 'product-images'
    AND public.is_store_owner(((storage.foldername(name))[1])::uuid)
  );

CREATE POLICY "product_images_update_store_owner"
  ON storage.objects FOR UPDATE
  TO authenticated
  USING (
    bucket_id = 'product-images'
    AND public.is_store_owner(((storage.foldername(name))[1])::uuid)
  )
  WITH CHECK (
    bucket_id = 'product-images'
    AND public.is_store_owner(((storage.foldername(name))[1])::uuid)
  );

CREATE POLICY "product_images_delete_store_owner"
  ON storage.objects FOR DELETE
  TO authenticated
  USING (
    bucket_id = 'product-images'
    AND public.is_store_owner(((storage.foldername(name))[1])::uuid)
  );

-- =====================================================
-- CATEGORY-ICONS
-- =====================================================

CREATE POLICY "category_icons_select_public"
  ON storage.objects FOR SELECT
  USING (bucket_id = 'category-icons');

CREATE POLICY "category_icons_insert_admin"
  ON storage.objects FOR INSERT
  TO authenticated
  WITH CHECK (
    bucket_id = 'category-icons'
    AND public.is_admin()
  );

CREATE POLICY "category_icons_update_admin"
  ON storage.objects FOR UPDATE
  TO authenticated
  USING (
    bucket_id = 'category-icons'
    AND public.is_admin()
  )
  WITH CHECK (
    bucket_id = 'category-icons'
    AND public.is_admin()
  );

CREATE POLICY "category_icons_delete_admin"
  ON storage.objects FOR DELETE
  TO authenticated
  USING (
    bucket_id = 'category-icons'
    AND public.is_admin()
  );
