# Bridal Cover CRM — Documentation

This documentation collection is a comprehensive, narrative-oriented manual for the **Bridal Cover CRM** project. It is written to explain the product, domain model, architectural choices, infrastructure, security and compliance, observability, testing strategy, integrations, data analytics, and contribution guidelines. The goal is to be a single-source-of-truth for the system — suitable for both development onboarding and as a demonstration of architecture knowledge in a portfolio.

Folder structure (this `docs/`):
- `01-overview.md` — detailed product overview and business context
- `02-domain-model.md` — domain model, aggregates, invariants, sample use-cases
- `03-domain-events.md` — domain events implementation, patterns and best practices
- `04-architecture.md` — architectural rationales, diagrams and patterns used
- `05-infrastructure.md` — local/dev/staging/production infra and IaC approach
- `06-security-lgpd.md` — security model, data protection, LGPD/GDPR practicality
- `07-devops-ci-cd.md` — CI/CD pipelines, branching and release flow
- `08-observability.md` — metrics, tracing, logs, SLO/SLI guidance
- `09-testing.md` — testing strategy and examples for each layer
- `10-data-analytics.md` — analytics architecture, read models, KPIs and BI
- `11-integrations.md` — external integrations and adapters
- `12-adr-001-monolith-modular.md` — ADR: modular monolith decision
- `12-adr-002-auth-keycloak.md` — ADR: Keycloak decision
- `12-adr-003-postgres.md` — ADR: Postgres decision
- `13-getting-started.md` — local dev quickstart + sample docker-compose
- `14-contributing.md` — contribution workflow and code quality checks
- `15-glossary.md` — domain and technical glossary
- `16-references.md` — recommended books, articles and tooling resources

Use these documents as living artifacts: update ADRs when decisions change, add new integration docs as you build them, and keep diagrams in sync with implementation.