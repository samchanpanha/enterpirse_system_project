-- V1__Initialize_finance_schema.sql

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE chart_of_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    is_contra BOOLEAN DEFAULT false,
    parent_id UUID REFERENCES chart_of_accounts(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE journal_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    entry_number VARCHAR(50) NOT NULL,
    entry_date DATE NOT NULL,
    description TEXT,
    reference_type VARCHAR(50),
    reference_id UUID,
    is_posted BOOLEAN DEFAULT false,
    posted_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE journal_entry_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    journal_entry_id UUID NOT NULL REFERENCES journal_entries(id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES chart_of_accounts(id) ON DELETE CASCADE,
    debit NUMERIC(14,2) DEFAULT 0,
    credit NUMERIC(14,2) DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    invoice_type VARCHAR(20) NOT NULL,
    source_type VARCHAR(50),
    source_id UUID,
    customer_name VARCHAR(255),
    customer_tin VARCHAR(100),
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal NUMERIC(14,2) NOT NULL,
    discount NUMERIC(14,2) DEFAULT 0,
    tax_amount NUMERIC(14,2) DEFAULT 0,
    total NUMERIC(14,2) NOT NULL,
    amount_paid NUMERIC(14,2) DEFAULT 0,
    balance_due NUMERIC(14,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    currency VARCHAR(3) DEFAULT 'USD',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE invoice_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    quantity NUMERIC(12,2) DEFAULT 1,
    unit_price NUMERIC(14,2) NOT NULL,
    tax_rate NUMERIC(5,2) DEFAULT 10.00,
    tax_amount NUMERIC(14,2) DEFAULT 0,
    total NUMERIC(14,2) NOT NULL,
    account_id UUID REFERENCES chart_of_accounts(id) ON DELETE SET NULL
);

CREATE TABLE tax_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    tax_type VARCHAR(50) NOT NULL,
    period_month INTEGER NOT NULL,
    period_year INTEGER NOT NULL,
    taxable_amount NUMERIC(14,2) NOT NULL,
    tax_rate NUMERIC(5,2) NOT NULL,
    tax_amount NUMERIC(14,2) NOT NULL,
    source_type VARCHAR(50),
    source_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    UNIQUE (tenant_id, tax_type, period_month, period_year, source_id)
);

CREATE TABLE tax_filing_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    tax_type VARCHAR(50) NOT NULL,
    period_month INTEGER,
    period_year INTEGER NOT NULL,
    period_type VARCHAR(20) DEFAULT 'monthly',
    total_tax NUMERIC(14,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'draft',
    filed_date DATE,
    reference_number VARCHAR(100),
    export_format VARCHAR(20),
    export_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    code VARCHAR(50),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    khmer_name VARCHAR(255),
    gender VARCHAR(10),
    birth_date DATE,
    phone VARCHAR(50),
    email VARCHAR(255),
    id_type VARCHAR(50),
    id_number VARCHAR(100),
    position VARCHAR(255),
    department VARCHAR(255),
    hire_date DATE NOT NULL,
    termination_date DATE,
    status VARCHAR(20) DEFAULT 'active',
    base_salary NUMERIC(12,2) NOT NULL,
    bank_account VARCHAR(100),
    bank_name VARCHAR(100),
    nssf_number VARCHAR(100),
    tax_dependents INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE attendance_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    clock_in TIMESTAMP WITH TIME ZONE,
    clock_out TIMESTAMP WITH TIME ZONE,
    break_start TIMESTAMP WITH TIME ZONE,
    break_end TIMESTAMP WITH TIME ZONE,
    total_hours NUMERIC(5,2),
    overtime_hours NUMERIC(5,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'present',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    UNIQUE (employee_id, date)
);

CREATE TABLE payroll_periods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    period_month INTEGER NOT NULL,
    period_year INTEGER NOT NULL,
    period_type VARCHAR(20) DEFAULT 'monthly',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    payment_date DATE,
    status VARCHAR(20) DEFAULT 'open',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE payroll_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payroll_period_id UUID NOT NULL REFERENCES payroll_periods(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    base_salary NUMERIC(12,2) NOT NULL,
    allowances JSONB DEFAULT '[]',
    overtime_amount NUMERIC(12,2) DEFAULT 0,
    gross_salary NUMERIC(12,2) NOT NULL,
    tos_amount NUMERIC(12,2) DEFAULT 0,
    tofb_amount NUMERIC(12,2) DEFAULT 0,
    nssf_employee NUMERIC(12,2) DEFAULT 0,
    nssf_employer NUMERIC(12,2) DEFAULT 0,
    deductions JSONB DEFAULT '[]',
    net_salary NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_coa_tenant_type ON chart_of_accounts(tenant_id, type);
CREATE INDEX idx_coa_code ON chart_of_accounts(code);
CREATE INDEX idx_journal_date ON journal_entries(entry_date);
CREATE INDEX idx_journal_posted ON journal_entries(is_posted);
CREATE INDEX idx_jel_entry ON journal_entry_lines(journal_entry_id);
CREATE INDEX idx_invoices_tenant_status ON invoices(tenant_id, status);
CREATE INDEX idx_invoices_due ON invoices(due_date);
CREATE INDEX idx_invoices_source ON invoices(source_type, source_id);
CREATE INDEX idx_tax_records_period ON tax_records(tenant_id, tax_type, period_year, period_month);
CREATE INDEX idx_employees_tenant_status ON employees(tenant_id, status);
CREATE INDEX idx_attendance_employee_date ON attendance_records(employee_id, date);
CREATE INDEX idx_payroll_period ON payroll_items(payroll_period_id);
CREATE UNIQUE INDEX idx_payroll_items_employee_period ON payroll_items(payroll_period_id, employee_id);
