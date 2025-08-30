# Bridal Cover CRM – Project Overview

## 1. Project Context

The **Bridal Cover CRM** is designed for **manufacturers of garment covers**, such as bridal covers, suit covers, and other protective apparel.

**Primary clients:**
- Rental stores, boutiques, and businesses that lease bridal or formal attire.
- These clients require frequent orders, timely deliveries, and accurate stock tracking.

**Challenges for manufacturers:**
- Tracking orders and production schedules across multiple clients.
- Maintaining strong B2B relationships with rental stores and boutiques.
- Ensuring fast communication for quotes, delivery updates, and follow-ups.

**Objective of the CRM:**
- Centralize client and lead management.
- Track orders and inventory.
- Manage sales pipeline and activities.
- Provide analytics for performance monitoring.
- Serve as a technical playground for modern software practices.

---

## 2. Primary Goals

1. **Client & Lead Management** – Handle potential and established clients effectively.
2. **Order & Production Tracking** – Link client requests to production schedules and stock.
3. **Sales Pipeline Management** – Visualize the sales process from inquiry to confirmed order.
4. **Activity Logging** – Record calls, emails, WhatsApp messages, and visits.
5. **Analytics & Reporting** – Monitor conversions, order fulfillment, and top clients.
6. **Integration Ready** – Connect with email, WhatsApp, calendar, and shipping services.
7. **Learning Platform** – Apply DDD, Hexagonal Architecture, CQRS, event sourcing, CI/CD, and modern backend/frontend practices.

---

## 3. Flow of Project Usage

### Step 1 – Register Client / Lead
- Leads are potential rental stores or boutiques.
- Clients are stores with ongoing orders.
- Validation rules: unique CNPJ/email, mandatory contact info, optional billing details.

### Step 2 – Manage Interactions
- Log communication: phone calls, emails, WhatsApp, or visits.
- Activities can trigger follow-up reminders.

### Step 3 – Lead Qualification
- Evaluate client potential: order volume, interest, and reliability.
- Status progression: `New` → `Contacted` → `Qualified`.
- Only qualified leads can be converted to **Accounts** (formal clients).

### Step 4 – Order Management
- Create orders linked to client accounts.
- Track status: `Pending`, `In Production`, `Shipped`, `Delivered`.
- Alerts for delayed orders or insufficient stock.

### Step 5 – Reporting & Analytics
- Monitor client conversion rates, order volumes, and delivery performance.
- Identify top clients and high-performing sales channels.

---

## 4. Business Rules (B2B Focus)

| Area       | Rule                       | Description |
|----------- |--------------------------- |------------ |
| Lead       | Unique contact             | Email, phone, or CNPJ must be unique across leads and clients |
| Lead       | Status progression         | `New` → `Contacted` → `Qualified` → `Account` |
| Account    | Orders                     | Must be linked to a valid client account |
| Order      | Inventory check            | Order cannot be confirmed if stock is insufficient |
| Activity   | Traceability               | All interactions must reference a lead, client, or order |
| Opportunity| Stage validation           | Only assigned sales representatives can move stages |
| Reporting  | KPIs                       | Orders, conversion rates, lead qualification, and client retention must be tracked |

---
