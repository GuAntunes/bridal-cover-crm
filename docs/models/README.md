# 📊 Architecture Diagrams

**Ultra-simplified diagrams** - Everything you need, nothing you don't.

## 🚀 Quick Start

1. **Start here:** [`overview.puml`](overview.puml) - **Detailed project status** with all components
2. **Domain design:** [`domain-ddd.puml`](domain-ddd.puml) - **Pure DDD domain model** (Aggregates, Entities, VOs)
3. **Code structure:** [`domain-structure.puml`](domain-structure.puml) - **Package organization** following DDD
4. **Plan:** [`roadmap.puml`](roadmap.puml) - **Development timeline**
5. **Choose tech:** [`tech-stack.puml`](tech-stack.puml) - **Technology recommendations**

## 📋 Summary

| Diagram | Purpose | When to Use | Key Features |
|---------|---------|-------------|--------------|
| 🏢 **overview** | **Project status** | Daily standup, onboarding | **3 phases with components breakdown** |
| 🏗️ **domain-ddd** | **Pure DDD model** | Domain design, implementation | **Aggregates, Entities, Value Objects, Services** |
| 📦 **domain-structure** | **Package organization** | Code structure, file organization | **DDD-based folder structure with examples** |
| 🗺️ **roadmap** | **Development timeline** | Sprint planning, milestones | **Phase-by-phase development plan** |
| 🛠️ **tech-stack** | **Technology stack** | Architecture decisions, setup | **Complete stack with alternatives** |

## 🏗️ DDD Domain Model Features

The `domain-ddd.puml` includes:
- 🎯 **3 Aggregates** (Lead, Client, Territory) with clear boundaries
- 🏢 **Entities** (ContactAttempt, ContractInfo, LeadDensity) within aggregates
- 💎 **Value Objects** (Address, ContactInfo, CNPJ, Email, etc.) - immutable and shareable
- 🔧 **Domain Services** (LeadQualification, Conversion, TerritoryAnalysis, Distance)
- 📋 **Enumerations** with business behavior (LeadStatus, ContactChannel, etc.)
- 🎨 **Color coding** by DDD pattern type
- 📚 **Rich domain model** with business logic and behavior

## 📦 Domain Structure Features

The `domain-structure.puml` includes:
- 📁 **Package organization** following DDD tactical patterns
- 🏗️ **Aggregate folders** with root + entities structure
- 💎 **Value Object categorization** (identity, contact, geographic, common)
- 🔧 **Domain Services** in dedicated package
- ⚡ **Domain Events** organized by aggregate
- 📝 **Code examples** showing implementation patterns
- 🎯 **File naming conventions** and organization principles

## 🛠️ Technology Stack Features

The `tech-stack.puml` includes:
- ☕ **Backend Core** (Kotlin, Spring Boot, Testing)
- 💾 **Data Layer** (PostgreSQL, Redis, Kafka)
- 🌐 **Frontend** (React, TypeScript, Mobile)
- 🗺️ **External Services** (Google APIs, Communication)
- 🚀 **DevOps** (Docker, Cloud, CI/CD)
- 📊 **Monitoring** (Prometheus, Grafana, Logging)
- 📋 **Alternatives & Recommendations** with decision criteria

## 🗺️ Roadmap Features

The `roadmap.puml` includes:
- 📅 **Week-by-week breakdown** (20+ weeks total)
- 🎯 **Success metrics** for each phase
- ⚠️ **Risk mitigation** strategies
- 🔄 **Decision points** and quality gates
- 📊 **Progress tracking** and milestones

## 🎯 Current Status

- ✅ **Foundation Complete** - DDD, Architecture, Tests, Docs
- 🚧 **Next Phase** - Lead & Client aggregates (Weeks 1-4)
- 📋 **Phase 2** - REST API, Database, UI (Weeks 5-10)
- 🗺️ **Phase 3** - Google Maps, Mobile, Analytics (Weeks 11-20)

---

*Keep it simple. Keep it updated. Keep it useful.*
