-- Create additional databases for different environments
CREATE DATABASE bridal_cover_crm;
CREATE DATABASE bridal_cover_crm_test;
CREATE DATABASE bridal_cover_crm_prod;

-- Create user for production (optional)
CREATE USER bridal_user WITH PASSWORD 'bridal_pass';
GRANT ALL PRIVILEGES ON DATABASE bridal_cover_crm TO bridal_user;
GRANT ALL PRIVILEGES ON DATABASE bridal_cover_crm_dev TO bridal_user;
GRANT ALL PRIVILEGES ON DATABASE bridal_cover_crm_test TO bridal_user;
GRANT ALL PRIVILEGES ON DATABASE bridal_cover_crm_prod TO bridal_user;

-- Grant schema permissions (required for PostgreSQL 15+)
\c bridal_cover_crm
GRANT ALL ON SCHEMA public TO bridal_user;
GRANT CREATE ON SCHEMA public TO bridal_user;

\c bridal_cover_crm_dev
GRANT ALL ON SCHEMA public TO bridal_user;
GRANT CREATE ON SCHEMA public TO bridal_user;

\c bridal_cover_crm_test
GRANT ALL ON SCHEMA public TO bridal_user;
GRANT CREATE ON SCHEMA public TO bridal_user;

\c bridal_cover_crm_prod
GRANT ALL ON SCHEMA public TO bridal_user;
GRANT CREATE ON SCHEMA public TO bridal_user;
