-- Create leads table
CREATE TABLE leads (
    id VARCHAR(36) PRIMARY KEY,
    company_name VARCHAR(200) NOT NULL,
    cnpj VARCHAR(14),
    contact_info JSONB NOT NULL,
    status VARCHAR(20) NOT NULL,
    source VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create indexes for better performance
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_source ON leads(source);
CREATE INDEX idx_leads_created_at ON leads(created_at);
CREATE INDEX idx_leads_updated_at ON leads(updated_at);
CREATE INDEX idx_leads_cnpj ON leads(cnpj) WHERE cnpj IS NOT NULL;
CREATE INDEX idx_leads_company_name ON leads USING gin(to_tsvector('portuguese', company_name));

-- Create partial indexes for common queries
CREATE INDEX idx_leads_active ON leads(status, updated_at) WHERE status NOT IN ('CONVERTED', 'LOST');
CREATE INDEX idx_leads_follow_up ON leads(status, created_at) WHERE status IN ('NEW', 'CONTACTED', 'QUALIFIED');

-- Add constraints
ALTER TABLE leads ADD CONSTRAINT chk_leads_status 
    CHECK (status IN ('NEW', 'CONTACTED', 'QUALIFIED', 'PROPOSAL_SENT', 'NEGOTIATING', 'CONVERTED', 'LOST'));

ALTER TABLE leads ADD CONSTRAINT chk_leads_source 
    CHECK (source IN ('MANUAL_ENTRY', 'GOOGLE_PLACES', 'REFERRAL', 'WEBSITE', 'COLD_CALL', 'SOCIAL_MEDIA'));

ALTER TABLE leads ADD CONSTRAINT chk_leads_dates 
    CHECK (updated_at >= created_at);

-- Add comments
COMMENT ON TABLE leads IS 'Stores lead information for the CRM system';
COMMENT ON COLUMN leads.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN leads.company_name IS 'Company name of the lead';
COMMENT ON COLUMN leads.cnpj IS 'Brazilian company registration number (CNPJ)';
COMMENT ON COLUMN leads.contact_info IS 'Contact information stored as JSON (email, phone, website, social media)';
COMMENT ON COLUMN leads.status IS 'Current status of the lead in the sales process';
COMMENT ON COLUMN leads.source IS 'Source where the lead was acquired';
COMMENT ON COLUMN leads.created_at IS 'Timestamp when the lead was created';
COMMENT ON COLUMN leads.updated_at IS 'Timestamp when the lead was last updated';
