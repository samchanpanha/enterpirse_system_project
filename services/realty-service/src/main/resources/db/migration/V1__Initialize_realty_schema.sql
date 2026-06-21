-- V1__Initialize_realty_schema.sql

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Listings
CREATE TABLE listings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    branch_id UUID,
    agent_id UUID,
    title VARCHAR(255) NOT NULL,
    title_kh VARCHAR(255),
    description TEXT,
    description_kh TEXT,
    property_type VARCHAR(50),
    listing_type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'draft',
    price NUMERIC(15,2),
    currency VARCHAR(3) DEFAULT 'USD',
    area_sqm NUMERIC(12,2),
    bedrooms INTEGER,
    bathrooms INTEGER,
    floors INTEGER,
    year_built INTEGER,
    address TEXT,
    city VARCHAR(100),
    district VARCHAR(100),
    province VARCHAR(100),
    lat DECIMAL(10,7),
    lng DECIMAL(10,7),
    features JSONB DEFAULT '[]',
    is_featured BOOLEAN DEFAULT false,
    is_published BOOLEAN DEFAULT false,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_listings_tenant ON listings(tenant_id);
CREATE INDEX idx_listings_status ON listings(status);
CREATE INDEX idx_listings_property_type ON listings(property_type);
CREATE INDEX idx_listings_tenant_status ON listings(tenant_id, status);
CREATE INDEX idx_listings_city ON listings(city);
CREATE INDEX idx_listings_agent ON listings(agent_id);

-- Listing Images
CREATE TABLE listing_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    listing_id UUID NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    is_primary BOOLEAN DEFAULT false,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
CREATE INDEX idx_listing_images_listing ON listing_images(listing_id);

-- Agents
CREATE TABLE agents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    branch_id UUID,
    user_id UUID,
    name VARCHAR(255) NOT NULL,
    name_kh VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255),
    license_number VARCHAR(100),
    bio TEXT,
    avatar_url TEXT,
    rating DECIMAL(3,2),
    total_sales INTEGER DEFAULT 0,
    total_listings INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_agents_tenant ON agents(tenant_id);
CREATE INDEX idx_agents_email ON agents(email);

-- Leads
CREATE TABLE leads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    branch_id UUID,
    listing_id UUID REFERENCES listings(id) ON DELETE SET NULL,
    agent_id UUID,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    message TEXT,
    source VARCHAR(50),
    status VARCHAR(20) DEFAULT 'new',
    budget_min NUMERIC(15,2),
    budget_max NUMERIC(15,2),
    property_type_preference VARCHAR(50),
    preferred_district VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_leads_tenant ON leads(tenant_id);
CREATE INDEX idx_leads_listing ON leads(listing_id);
CREATE INDEX idx_leads_status ON leads(status);

-- Tours
CREATE TABLE tours (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    branch_id UUID,
    listing_id UUID REFERENCES listings(id) ON DELETE CASCADE,
    agent_id UUID,
    lead_id UUID,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) DEFAULT 'scheduled',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_tours_listing ON tours(listing_id);
CREATE INDEX idx_tours_agent ON tours(agent_id);
CREATE INDEX idx_tours_scheduled_at ON tours(scheduled_at);

-- Offers
CREATE TABLE offers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    branch_id UUID,
    listing_id UUID NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    buyer_id UUID,
    agent_id UUID,
    amount NUMERIC(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    terms TEXT,
    status VARCHAR(20) DEFAULT 'pending',
    expiry_date DATE,
    counter_amount NUMERIC(15,2),
    rejected_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_offers_listing ON offers(listing_id);
CREATE INDEX idx_offers_status ON offers(status);

-- Commissions
CREATE TABLE commissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    branch_id UUID,
    offer_id UUID NOT NULL REFERENCES offers(id) ON DELETE CASCADE,
    agent_id UUID,
    amount NUMERIC(15,2) NOT NULL,
    percentage DECIMAL(5,2),
    status VARCHAR(20) DEFAULT 'pending',
    paid_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
CREATE INDEX idx_commissions_offer ON commissions(offer_id);
CREATE INDEX idx_commissions_agent ON commissions(agent_id);

-- Property Valuations
CREATE TABLE property_valuations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    branch_id UUID,
    property_id UUID,
    estimated_value NUMERIC(15,2),
    low_range NUMERIC(15,2),
    high_range NUMERIC(15,2),
    confidence_score DECIMAL(5,2),
    valuation_date DATE,
    factors JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
CREATE INDEX idx_valuations_property ON property_valuations(property_id);
CREATE INDEX idx_valuations_tenant ON property_valuations(tenant_id);

-- HOA Fees
CREATE TABLE hoa_fees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    branch_id UUID,
    property_id UUID,
    resident_id UUID,
    amount NUMERIC(12,2) NOT NULL,
    due_date DATE NOT NULL,
    period VARCHAR(20),
    status VARCHAR(20) DEFAULT 'pending',
    paid_at TIMESTAMP WITH TIME ZONE,
    payment_ref VARCHAR(100),
    late_fee NUMERIC(12,2) DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_hoa_fees_property ON hoa_fees(property_id);
CREATE INDEX idx_hoa_fees_resident ON hoa_fees(resident_id);
CREATE INDEX idx_hoa_fees_status ON hoa_fees(status);

-- Residents
CREATE TABLE residents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    branch_id UUID,
    property_id UUID,
    name VARCHAR(255) NOT NULL,
    name_kh VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255),
    id_number VARCHAR(100),
    move_in_date DATE,
    move_out_date DATE,
    status VARCHAR(20) DEFAULT 'active',
    emergency_contact VARCHAR(255),
    emergency_phone VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_residents_tenant ON residents(tenant_id);
CREATE INDEX idx_residents_property ON residents(property_id);
CREATE INDEX idx_residents_status ON residents(status);

-- Amenity Bookings
CREATE TABLE amenity_bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    branch_id UUID,
    resident_id UUID,
    amenity_type VARCHAR(50) NOT NULL,
    booked_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    guests INTEGER DEFAULT 1,
    status VARCHAR(20) DEFAULT 'confirmed',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_amenity_bookings_resident ON amenity_bookings(resident_id);
CREATE INDEX idx_amenity_bookings_date ON amenity_bookings(booked_date);
CREATE INDEX idx_amenity_bookings_type ON amenity_bookings(amenity_type);

-- Visitors
CREATE TABLE visitors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    branch_id UUID,
    property_id UUID,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    id_number VARCHAR(100),
    vehicle_plate VARCHAR(20),
    purpose VARCHAR(100),
    check_in TIMESTAMP WITH TIME ZONE,
    check_out TIMESTAMP WITH TIME ZONE,
    qr_code TEXT,
    status VARCHAR(20) DEFAULT 'checked_in',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
CREATE INDEX idx_visitors_property ON visitors(property_id);
CREATE INDEX idx_visitors_status ON visitors(status);
CREATE INDEX idx_visitors_check_in ON visitors(check_in);

-- Packages
CREATE TABLE packages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    branch_id UUID,
    resident_id UUID,
    carrier VARCHAR(100),
    tracking_number VARCHAR(100),
    description TEXT,
    status VARCHAR(20) DEFAULT 'received',
    received_at TIMESTAMP WITH TIME ZONE,
    picked_up_at TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
CREATE INDEX idx_packages_resident ON packages(resident_id);
CREATE INDEX idx_packages_status ON packages(status);
CREATE INDEX idx_packages_tracking ON packages(tracking_number);
