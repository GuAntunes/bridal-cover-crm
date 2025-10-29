-- Create additional databases for different environments
CREATE DATABASE bridal_cover_crm_test;
CREATE DATABASE bridal_cover_crm_prod;

-- Create user for production (optional)
CREATE USER bridal_user WITH PASSWORD 'bridal_pass';
GRANT ALL PRIVILEGES ON DATABASE bridal_cover_crm_dev TO bridal_user;
GRANT ALL PRIVILEGES ON DATABASE bridal_cover_crm_test TO bridal_user;
GRANT ALL PRIVILEGES ON DATABASE bridal_cover_crm_prod TO bridal_user;
