CREATE TABLE public.profiles (
  id          UUID         PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  full_name   VARCHAR(100) NOT NULL,
  email       VARCHAR(150) UNIQUE NOT NULL,
  phone       VARCHAR(20),
  bio         TEXT,
  avatar_url  TEXT,
  role        VARCHAR(20)  NOT NULL DEFAULT 'buyer'
                CHECK (role IN ('buyer', 'seller', 'admin')),
  is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.profiles IS 'Data profil pengguna; dibuat otomatis saat signup.';

-- =============================================================================
-- 2. CATEGORIES
-- =============================================================================

CREATE TABLE public.categories (
  id        SERIAL      PRIMARY KEY,
  name      VARCHAR(50) UNIQUE NOT NULL,
  icon_url  TEXT
);

COMMENT ON TABLE public.categories IS 'Master kategori UMKM (Makanan, Fashion, dst).';

-- =============================================================================
-- 3. STORES
-- =============================================================================

CREATE TABLE public.stores (
  id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  owner_id       UUID         NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  name           VARCHAR(100) NOT NULL,
  description    TEXT,
  address        TEXT         NOT NULL,
  contact_phone  VARCHAR(20),
  category       VARCHAR(50)  NOT NULL,
  status         VARCHAR(20)  NOT NULL DEFAULT 'pending'
                   CHECK (status IN ('pending', 'active', 'suspended', 'rejected')),
  logo_url       TEXT,
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stores_owner_id ON public.stores(owner_id);
CREATE INDEX idx_stores_status   ON public.stores(status);
CREATE INDEX idx_stores_category ON public.stores(category);

-- =============================================================================
-- 4. STORE_VERIFICATIONS
-- =============================================================================

CREATE TABLE public.store_verifications (
  id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  store_id     UUID        NOT NULL REFERENCES public.stores(id) ON DELETE CASCADE,
  admin_id     UUID        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  status       VARCHAR(20) NOT NULL CHECK (status IN ('approved', 'rejected')),
  notes        TEXT,
  verified_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_store_verifications_store_id ON public.store_verifications(store_id);
CREATE INDEX idx_store_verifications_admin_id ON public.store_verifications(admin_id);

-- =============================================================================
-- 5. PRODUCTS
-- =============================================================================

CREATE TABLE public.products (
  id           UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
  store_id     UUID            NOT NULL REFERENCES public.stores(id) ON DELETE CASCADE,
  category_id  INTEGER         REFERENCES public.categories(id) ON DELETE SET NULL,
  name         VARCHAR(150)    NOT NULL,
  description  TEXT,
  price        DECIMAL(12, 2)  NOT NULL CHECK (price >= 0),
  stock        INTEGER         NOT NULL DEFAULT 0 CHECK (stock >= 0),
  variant      TEXT,
  is_active    BOOLEAN         NOT NULL DEFAULT TRUE,
  created_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_store_id    ON public.products(store_id);
CREATE INDEX idx_products_category_id ON public.products(category_id);
CREATE INDEX idx_products_is_active   ON public.products(is_active);
CREATE INDEX idx_products_name_fts    ON public.products USING gin (to_tsvector('simple', name));

-- =============================================================================
-- 6. PRODUCT_IMAGES
-- =============================================================================

CREATE TABLE public.product_images (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  product_id  UUID        NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
  image_url   TEXT        NOT NULL,
  is_primary  BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_images_product_id ON public.product_images(product_id);
CREATE INDEX idx_product_images_primary    ON public.product_images(product_id)
  WHERE is_primary = TRUE;

-- =============================================================================
-- 7. CARTS
-- =============================================================================

CREATE TABLE public.carts (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  product_id  UUID        NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
  quantity    INTEGER     NOT NULL DEFAULT 1 CHECK (quantity > 0),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (user_id, product_id)
);

CREATE INDEX idx_carts_user_id ON public.carts(user_id);

-- =============================================================================
-- 8. ORDERS
-- =============================================================================

CREATE TABLE public.orders (
  id                UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
  buyer_id          UUID            NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  store_id          UUID            NOT NULL REFERENCES public.stores(id) ON DELETE CASCADE,
  total_price       DECIMAL(14, 2)  NOT NULL CHECK (total_price >= 0),
  shipping_address  TEXT            NOT NULL,
  status            VARCHAR(30)     NOT NULL DEFAULT 'pending'
                      CHECK (status IN ('pending', 'confirmed', 'shipped', 'delivered', 'cancelled')),
  created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_buyer_id   ON public.orders(buyer_id);
CREATE INDEX idx_orders_store_id   ON public.orders(store_id);
CREATE INDEX idx_orders_status     ON public.orders(status);
CREATE INDEX idx_orders_created_at ON public.orders(created_at DESC);

-- =============================================================================
-- 9. ORDER_ITEMS
-- =============================================================================

CREATE TABLE public.order_items (
  id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id        UUID            NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
  product_id      UUID            NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
  quantity        INTEGER         NOT NULL CHECK (quantity > 0),
  price_at_order  DECIMAL(12, 2)  NOT NULL CHECK (price_at_order >= 0)
);

CREATE INDEX idx_order_items_order_id   ON public.order_items(order_id);
CREATE INDEX idx_order_items_product_id ON public.order_items(product_id);

-- =============================================================================
-- 10. PAYMENTS
-- =============================================================================

CREATE TABLE public.payments (
  id        UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id  UUID            NOT NULL UNIQUE REFERENCES public.orders(id) ON DELETE CASCADE,
  method    VARCHAR(30)     NOT NULL CHECK (method IN ('bank_transfer', 'ewallet', 'qris')),
  amount    DECIMAL(14, 2)  NOT NULL CHECK (amount >= 0),
  status    VARCHAR(20)     NOT NULL DEFAULT 'pending'
              CHECK (status IN ('pending', 'paid', 'failed', 'refunded')),
  paid_at   TIMESTAMPTZ
);

CREATE INDEX idx_payments_status ON public.payments(status);

-- =============================================================================
-- 11. REVIEWS
-- =============================================================================

CREATE TABLE public.reviews (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  product_id  UUID        NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
  user_id     UUID        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  order_id    UUID        NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
  rating      SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment     TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (user_id, order_id, product_id)
);

CREATE INDEX idx_reviews_product_id  ON public.reviews(product_id);
CREATE INDEX idx_reviews_user_id     ON public.reviews(user_id);
CREATE INDEX idx_reviews_order_id    ON public.reviews(order_id);
CREATE INDEX idx_reviews_created_at  ON public.reviews(created_at DESC);

-- =============================================================================
-- 12. ANALYTICS_SUMMARY
-- =============================================================================

CREATE TABLE public.analytics_summary (
  id                UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
  store_id          UUID            NOT NULL REFERENCES public.stores(id) ON DELETE CASCADE,
  period            DATE            NOT NULL,
  total_orders      INTEGER         NOT NULL DEFAULT 0,
  total_revenue     DECIMAL(14, 2)  NOT NULL DEFAULT 0,
  total_items_sold  INTEGER         NOT NULL DEFAULT 0,
  -- 1 row per toko per hari
  UNIQUE (store_id, period)
);

CREATE INDEX idx_analytics_store_id ON public.analytics_summary(store_id);
CREATE INDEX idx_analytics_period   ON public.analytics_summary(period DESC);

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
SECURITY DEFINER         
SET search_path = public
AS $$
BEGIN
  INSERT INTO public.profiles (id, full_name, email)
  VALUES (
    NEW.id,
    COALESCE(NEW.raw_user_meta_data->>'full_name', split_part(NEW.email, '@', 1)),
    NEW.email
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();