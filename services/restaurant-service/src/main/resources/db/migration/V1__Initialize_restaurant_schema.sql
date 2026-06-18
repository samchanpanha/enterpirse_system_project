-- V1__Initialize_restaurant_schema.sql
-- Seed data: demo outlet + categories + menu items

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Outlets
CREATE TABLE outlets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    phone VARCHAR(50),
    email VARCHAR(255),
    tax_number VARCHAR(100),
    type VARCHAR(50),
    currency VARCHAR(3) DEFAULT 'KHR',
    settings JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_outlets_tenant ON outlets(tenant_id);

-- Tables
CREATE TABLE tables (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    outlet_id UUID NOT NULL REFERENCES outlets(id) ON DELETE CASCADE,
    label VARCHAR(50) NOT NULL,
    capacity INT DEFAULT 2,
    floor VARCHAR(50),
    section VARCHAR(100),
    pos_x DOUBLE PRECISION,
    pos_y DOUBLE PRECISION,
    status VARCHAR(20) DEFAULT 'available',
    qr_code_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_tables_outlet ON tables(outlet_id);

-- Categories
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    outlet_id UUID REFERENCES outlets(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
CREATE INDEX idx_categories_outlet ON categories(outlet_id);

-- Menu Items
CREATE TABLE menu_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    name_kh VARCHAR(255),
    description TEXT,
    description_kh TEXT,
    price NUMERIC(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'KHR',
    tax_rate NUMERIC(5,2) DEFAULT 10.00,
    image_url TEXT,
    options JSONB DEFAULT '[]',
    modifiers JSONB DEFAULT '[]',
    is_active BOOLEAN DEFAULT true,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_menu_items_category ON menu_items(category_id);
CREATE INDEX idx_menu_items_tenant_active ON menu_items(tenant_id, is_active);

-- Customers
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    outlet_id UUID REFERENCES outlets(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    birthday DATE,
    is_vip BOOLEAN DEFAULT false,
    notes TEXT,
    total_visits INT DEFAULT 0,
    total_spent NUMERIC(12,2) DEFAULT 0,
    last_visit_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_customers_tenant_phone ON customers(tenant_id, phone);

-- Orders
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    outlet_id UUID NOT NULL REFERENCES outlets(id) ON DELETE CASCADE,
    table_id UUID REFERENCES tables(id) ON DELETE SET NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    order_number VARCHAR(50) NOT NULL,
    type VARCHAR(20) DEFAULT 'dine_in',
    status VARCHAR(20) DEFAULT 'pending',
    subtotal NUMERIC(12,2) DEFAULT 0,
    discount NUMERIC(12,2) DEFAULT 0,
    tax_amount NUMERIC(12,2) DEFAULT 0,
    service_charge NUMERIC(12,2) DEFAULT 0,
    total NUMERIC(12,2) DEFAULT 0,
    payment_status VARCHAR(20) DEFAULT 'unpaid',
    notes TEXT,
    served_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    completed_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_orders_outlet_status ON orders(outlet_id, status);

-- Order Items
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id UUID NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
    quantity INT NOT NULL DEFAULT 1,
    unit_price NUMERIC(12,2) NOT NULL,
    modifiers JSONB DEFAULT '[]',
    subtotal NUMERIC(12,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'pending',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
CREATE INDEX idx_order_items_order ON order_items(order_id);

-- Reservations
CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    outlet_id UUID NOT NULL REFERENCES outlets(id) ON DELETE CASCADE,
    table_id UUID REFERENCES tables(id) ON DELETE SET NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    guest_name VARCHAR(255),
    guest_phone VARCHAR(50),
    guest_count INT NOT NULL DEFAULT 2,
    reservation_time TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INT DEFAULT 120,
    status VARCHAR(20) DEFAULT 'confirmed',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_reservations_outlet_time ON reservations(outlet_id, reservation_time);

-- Seed: demo outlet + basic categories + sample items
INSERT INTO outlets (tenant_id, name, address, phone, type, currency)
VALUES ('00000000-0000-0000-0000-000000000001', 'Demo Restaurant', '123 Main St, Phnom Penh', '+85512345678', 'restaurant', 'KHR');

INSERT INTO categories (tenant_id, outlet_id, name, sort_order)
VALUES
    ('00000000-0000-0000-0000-000000000001', (SELECT id FROM outlets WHERE name='Demo Restaurant'), 'Appetizers', 1),
    ('00000000-0000-0000-0000-000000000001', (SELECT id FROM outlets WHERE name='Demo Restaurant'), 'Main Course', 2),
    ('00000000-0000-0000-0000-000000000001', (SELECT id FROM outlets WHERE name='Demo Restaurant'), 'Beverages', 3);
