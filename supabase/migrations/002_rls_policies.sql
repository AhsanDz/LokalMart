CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS boolean
LANGUAGE sql
SECURITY DEFINER
SET search_path = public
STABLE
AS $$
  SELECT EXISTS (
    SELECT 1 FROM public.profiles
    WHERE id = auth.uid() AND role = 'admin'
  )
$$;

CREATE OR REPLACE FUNCTION public.is_store_owner(store_uuid UUID)
RETURNS boolean
LANGUAGE sql
SECURITY DEFINER
SET search_path = public
STABLE
AS $$
  SELECT EXISTS (
    SELECT 1 FROM public.stores
    WHERE id = store_uuid AND owner_id = auth.uid()
  )
$$;

-- =============================================================================
-- SECTION 1: PROFILES
-- =============================================================================

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "profiles_select_public"
  ON public.profiles FOR SELECT
  USING (true);

CREATE POLICY "profiles_update_own"
  ON public.profiles FOR UPDATE
  USING (auth.uid() = id)
  WITH CHECK (
    auth.uid() = id
    AND role = (SELECT role FROM public.profiles WHERE id = auth.uid())
  );

CREATE POLICY "profiles_update_admin"
  ON public.profiles FOR UPDATE
  USING (public.is_admin())
  WITH CHECK (public.is_admin());


-- =============================================================================
-- SECTION 2: CATEGORIES
-- =============================================================================

ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;

CREATE POLICY "categories_select_public"
  ON public.categories FOR SELECT
  USING (true);

CREATE POLICY "categories_all_admin"
  ON public.categories FOR ALL
  USING (public.is_admin())
  WITH CHECK (public.is_admin());

-- =============================================================================
-- SECTION 3: STORES
-- =============================================================================

ALTER TABLE public.stores ENABLE ROW LEVEL SECURITY;

CREATE POLICY "stores_select_active_public"
  ON public.stores FOR SELECT
  USING (status = 'active');

CREATE POLICY "stores_select_own"
  ON public.stores FOR SELECT
  USING (auth.uid() = owner_id);

CREATE POLICY "stores_select_admin"
  ON public.stores FOR SELECT
  USING (public.is_admin());

CREATE POLICY "stores_insert_authenticated"
  ON public.stores FOR INSERT
  TO authenticated
  WITH CHECK (auth.uid() = owner_id);

CREATE POLICY "stores_update_own"
  ON public.stores FOR UPDATE
  USING (auth.uid() = owner_id)
  WITH CHECK (auth.uid() = owner_id);

CREATE POLICY "stores_update_admin"
  ON public.stores FOR UPDATE
  USING (public.is_admin())
  WITH CHECK (public.is_admin());

CREATE POLICY "stores_delete_admin"
  ON public.stores FOR DELETE
  USING (public.is_admin());

-- =============================================================================
-- SECTION 4: STORE_VERIFICATIONS
-- =============================================================================

ALTER TABLE public.store_verifications ENABLE ROW LEVEL SECURITY;

CREATE POLICY "store_verifications_select_admin"
  ON public.store_verifications FOR SELECT
  USING (public.is_admin());

CREATE POLICY "store_verifications_select_store_owner"
  ON public.store_verifications FOR SELECT
  USING (public.is_store_owner(store_id));

-- Hanya admin yg boleh insert/update/delete record verifikasi
CREATE POLICY "store_verifications_all_admin"
  ON public.store_verifications FOR ALL
  USING (public.is_admin())
  WITH CHECK (public.is_admin() AND admin_id = auth.uid());

-- =============================================================================
-- SECTION 5: PRODUCTS
-- =============================================================================

ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;

CREATE POLICY "products_select_public"
  ON public.products FOR SELECT
  USING (
    is_active = true
    AND EXISTS (
      SELECT 1 FROM public.stores
      WHERE stores.id = products.store_id AND stores.status = 'active'
    )
  );

CREATE POLICY "products_select_store_owner"
  ON public.products FOR SELECT
  USING (public.is_store_owner(store_id));

CREATE POLICY "products_select_admin"
  ON public.products FOR SELECT
  USING (public.is_admin());

CREATE POLICY "products_insert_store_owner"
  ON public.products FOR INSERT
  WITH CHECK (public.is_store_owner(store_id));

CREATE POLICY "products_update_store_owner"
  ON public.products FOR UPDATE
  USING (public.is_store_owner(store_id))
  WITH CHECK (public.is_store_owner(store_id));

CREATE POLICY "products_delete_store_owner"
  ON public.products FOR DELETE
  USING (public.is_store_owner(store_id));

CREATE POLICY "products_update_admin"
  ON public.products FOR UPDATE
  USING (public.is_admin())
  WITH CHECK (public.is_admin());

-- =============================================================================
-- SECTION 6: PRODUCT_IMAGES
-- =============================================================================

ALTER TABLE public.product_images ENABLE ROW LEVEL SECURITY;

CREATE POLICY "product_images_select_public"
  ON public.product_images FOR SELECT
  USING (true);

CREATE POLICY "product_images_all_store_owner"
  ON public.product_images FOR ALL
  USING (
    EXISTS (
      SELECT 1 FROM public.products p
      WHERE p.id = product_images.product_id
        AND public.is_store_owner(p.store_id)
    )
  )
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM public.products p
      WHERE p.id = product_images.product_id
        AND public.is_store_owner(p.store_id)
    )
  );

-- =============================================================================
-- SECTION 7: CARTS
-- =============================================================================

ALTER TABLE public.carts ENABLE ROW LEVEL SECURITY;

CREATE POLICY "carts_all_own"
  ON public.carts FOR ALL
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

-- =============================================================================
-- SECTION 8: ORDERS
-- =============================================================================

ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;

CREATE POLICY "orders_select_buyer"
  ON public.orders FOR SELECT
  USING (auth.uid() = buyer_id);

CREATE POLICY "orders_select_store_owner"
  ON public.orders FOR SELECT
  USING (public.is_store_owner(store_id));

CREATE POLICY "orders_select_admin"
  ON public.orders FOR SELECT
  USING (public.is_admin());

CREATE POLICY "orders_insert_buyer"
  ON public.orders FOR INSERT
  WITH CHECK (auth.uid() = buyer_id);

CREATE POLICY "orders_update_participants"
  ON public.orders FOR UPDATE
  USING (
    auth.uid() = buyer_id
    OR public.is_store_owner(store_id)
  )
  WITH CHECK (
    auth.uid() = buyer_id
    OR public.is_store_owner(store_id)
  );

-- =============================================================================
-- SECTION 9: ORDER_ITEMS
-- =============================================================================

ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;

CREATE POLICY "order_items_select_via_orders"
  ON public.order_items FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.orders o
      WHERE o.id = order_items.order_id
        AND (o.buyer_id = auth.uid() OR public.is_store_owner(o.store_id))
    )
  );

CREATE POLICY "order_items_insert_buyer_pending"
  ON public.order_items FOR INSERT
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM public.orders o
      WHERE o.id = order_items.order_id
        AND o.buyer_id = auth.uid()
        AND o.status = 'pending'
    )
  );

-- =============================================================================
-- SECTION 10: PAYMENTS
-- =============================================================================

ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;

CREATE POLICY "payments_select_via_orders"
  ON public.payments FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.orders o
      WHERE o.id = payments.order_id
        AND (o.buyer_id = auth.uid() OR public.is_store_owner(o.store_id))
    )
  );

CREATE POLICY "payments_insert_buyer"
  ON public.payments FOR INSERT
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM public.orders o
      WHERE o.id = payments.order_id
        AND o.buyer_id = auth.uid()
    )
  );

CREATE POLICY "payments_update_buyer"
  ON public.payments FOR UPDATE
  USING (
    EXISTS (
      SELECT 1 FROM public.orders o
      WHERE o.id = payments.order_id
        AND o.buyer_id = auth.uid()
    )
  )
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM public.orders o
      WHERE o.id = payments.order_id
        AND o.buyer_id = auth.uid()
    )
  );

CREATE POLICY "payments_update_admin"
  ON public.payments FOR UPDATE
  USING (public.is_admin())
  WITH CHECK (public.is_admin());

-- =============================================================================
-- SECTION 11: REVIEWS
-- =============================================================================

ALTER TABLE public.reviews ENABLE ROW LEVEL SECURITY;

CREATE POLICY "reviews_select_public"
  ON public.reviews FOR SELECT
  USING (true);

CREATE POLICY "reviews_insert_buyer_delivered"
  ON public.reviews FOR INSERT
  WITH CHECK (
    auth.uid() = user_id
    AND EXISTS (
      SELECT 1 FROM public.orders o
      JOIN public.order_items oi ON oi.order_id = o.id
      WHERE o.id = reviews.order_id
        AND o.buyer_id = auth.uid()
        AND o.status = 'delivered'
        AND oi.product_id = reviews.product_id
    )
  );

CREATE POLICY "reviews_update_own"
  ON public.reviews FOR UPDATE
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY "reviews_delete_own_or_admin"
  ON public.reviews FOR DELETE
  USING (auth.uid() = user_id OR public.is_admin());

-- =============================================================================
-- SECTION 12: ANALYTICS_SUMMARY
-- =============================================================================

ALTER TABLE public.analytics_summary ENABLE ROW LEVEL SECURITY;

CREATE POLICY "analytics_select_store_owner"
  ON public.analytics_summary FOR SELECT
  USING (public.is_store_owner(store_id));

CREATE POLICY "analytics_select_admin"
  ON public.analytics_summary FOR SELECT
  USING (public.is_admin());

CREATE POLICY "analytics_upsert_store_owner"
  ON public.analytics_summary FOR ALL
  USING (public.is_store_owner(store_id))
  WITH CHECK (public.is_store_owner(store_id));

CREATE POLICY "analytics_all_admin"
  ON public.analytics_summary FOR ALL
  USING (public.is_admin())
  WITH CHECK (public.is_admin());
