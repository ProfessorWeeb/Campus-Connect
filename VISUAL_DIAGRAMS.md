# Campus Connect - Visual Diagrams

This document contains detailed visual diagrams for the Requirements Analysis and Design Document.

---

## 1. Use-Case Diagram

### 1.1 Complete Use-Case Diagram with All Actors

```
                            CAMPUS CONNECT SYSTEM

                            ─────────────────────

                                    

                                    │

                    ┌───────────────┼───────────────┐

                    │               │               │

            ┌───────▼──────┐ ┌─────▼─────┐ ┌──────▼──────┐

            │   Student     │ │    TA      │ │   Faculty   │

            └───────┬───────┘ └─────┬─────┘ └──────┬──────┘

                    │               │               │

                    └───────────────┼───────────────┘

                                    │

                    ┌───────────────▼───────────────┐

                    │      CAMPUS CONNECT           │

                    │      Use Cases                │

                    ├───────────────────────────────┤

                    │                               │

                    │  ┌─────────────────────────┐  │

                    │  │ Register & Create      │  │

                    │  │ Profile                 │  │

                    │  └─────────────────────────┘  │

                    │                               │

                    │  ┌─────────────────────────┐  │

                    │  │ Discover Groups         │  │

                    │  │ Join Group              │  │

                    │  │ Create Group            │  │

                    │  │ Manage Groups           │  │

                    │  └─────────────────────────┘  │

                    │                               │

                    │  ┌─────────────────────────┐  │

                    │  │ Send Direct Message     │  │

                    │  │ Group Messaging         │  │

                    │  │ Discussion Boards       │  │

                    │  └─────────────────────────┘  │

                    │                               │

                    │  ┌─────────────────────────┐  │

                    │  │ Schedule Meetings        │  │

                    │  │ Share Resources          │  │

                    │  │ View Calendar            │  │

                    │  └─────────────────────────┘  │

                    │                               │

                    │  ┌─────────────────────────┐  │

                    │  │ Find Tutor               │  │

                    │  │ Offer Tutoring           │  │

                    │  │ Request Mentorship       │  │

                    │  └─────────────────────────┘  │

                    │                               │

                    │  ┌─────────────────────────┐  │

                    │  │ Receive Notifications    │  │

                    │  │ Manage Preferences       │  │

                    │  └─────────────────────────┘  │

                    │                               │

                    └───────────────────────────────┘

                                    │

                                    │

                            ┌───────▼────────┐

                            │   System       │

                            │   Backend      │

                            └────────────────┘
```

### 1.2 Use-Case Flow Diagram

```
                    ┌─────────────────────────────┐

                    │   User Registration         │

                    │   ─────────────────────     │

                    │   1. Enter credentials      │

                    │   2. Verify email           │

                    │   3. Complete profile       │

                    └──────────────┬──────────────┘

                                   │

                                   ▼

                    ┌─────────────────────────────┐

                    │   Group Discovery           │

                    │   ─────────────────────     │

                    │   1. Search/Filter groups    │

                    │   2. View group details      │

                    │   3. Join group              │

                    └──────────────┬──────────────┘

                                   │

                                   ▼

                    ┌─────────────────────────────┐

                    │   Group Management          │

                    │   ─────────────────────     │

                    │   1. Create group            │

                    │   2. Invite members          │

                    │   3. Manage settings         │

                    └──────────────┬──────────────┘

                                   │

                                   ▼

                    ┌─────────────────────────────┐

                    │   Communication             │

                    │   ─────────────────────     │

                    │   1. Send messages           │

                    │   2. Share resources         │

                    │   3. Schedule meetings      │

                    └─────────────────────────────┘
```

---

## 2. System Architecture Diagrams

### 2.1 Three-Tier Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    TIER 1: PRESENTATION LAYER                           │
│                                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    React.js Frontend Application                 │  │
│  │                                                                   │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │  │
│  │  │   Desktop    │  │   Tablet     │  │   Mobile     │          │  │
│  │  │   Browser    │  │   Browser    │  │   Browser    │          │  │
│  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │  │
│  │         │                 │                  │                     │  │
│  │         └─────────────────┴──────────────────┘                     │  │
│  │                            │                                          │  │
│  │                    ┌───────┴────────┐                                │  │
│  │                    │  API Client    │                                │  │
│  │                    │  (Axios/Fetch) │                                │  │
│  │                    │  HTTP/HTTPS    │                                │  │
│  │                    └───────┬────────┘                                │  │
│  └────────────────────────────┼──────────────────────────────────────┘  │
└────────────────────────────────┼──────────────────────────────────────────┘
                                 │
                                 │ HTTPS REST API
                                 │ JSON Data Format
                                 │
┌────────────────────────────────┼──────────────────────────────────────────┐
│                    TIER 2: APPLICATION LAYER                             │
│                                 │                                         │
│                    ┌─────────────▼─────────────┐                         │
│                    │   Spring Boot Server      │                         │
│                    │   Port: 8080              │                         │
│                    └─────────────┬─────────────┘                         │
│                                  │                                         │
│         ┌────────────────────────┼────────────────────────┐                │
│         │                        │                        │                │
│  ┌──────▼──────┐        ┌───────▼──────┐        ┌───────▼──────┐         │
│  │   User      │        │   Group      │        │  Messaging   │         │
│  │ Management  │        │ Management   │        │   Service    │         │
│  │  Service    │        │   Service    │        │              │         │
│  │             │        │              │        │              │         │
│  │ • Register  │        │ • Create     │        │ • Send Msg   │         │
│  │ • Login     │        │ • Search     │        │ • Get Thread │         │
│  │ • Profile   │        │ • Join/Leave │        │ • Group Chat │         │
│  └─────────────┘        └──────────────┘        └──────────────┘         │
│                                                                           │
│  ┌──────────────┐        ┌──────────────┐        ┌──────────────┐       │
│  │ Scheduling   │        │  Resource    │        │ Notification │       │
│  │   Service    │        │ Management   │        │   Service    │       │
│  │              │        │   Service    │        │              │       │
│  │ • Create Mtg │        │ • Upload     │        │ • Send Alert │       │
│  │ • View Cal   │        │ • Download   │        │ • Preferences│       │
│  │ • RSVP       │        │ • Organize   │        │ • Manage     │       │
│  └──────────────┘        └──────────────┘        └──────────────┘       │
│                                                                           │
│  ┌──────────────────────────────────────────────────────────┐            │
│  │   Authentication & Authorization Layer                    │            │
│  │   ────────────────────────────────────────────────────   │            │
│  │   • JWT Token Generation & Validation                    │            │
│  │   • Role-Based Access Control (RBAC)                     │            │
│  │   • Session Management                                   │            │
│  │   • Password Encryption (bcrypt)                         │            │
│  └──────────────────────────────────────────────────────────┘            │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │
                                     │ JDBC/JPA
                                     │ Spring Data JPA
                                     │
┌────────────────────────────────────┴─────────────────────────────────────┐
│                    TIER 3: DATA LAYER                                    │
│                                                                           │
│                    ┌─────────────────────┐                              │
│                    │   PostgreSQL         │                              │
│                    │   Database Server    │                              │
│                    │   Port: 5432         │                              │
│                    └──────────┬──────────┘                              │
│                               │                                           │
│         ┌─────────────────────┼─────────────────────┐                   │
│         │                     │                       │                   │
│  ┌──────▼──────┐     ┌───────▼──────┐     ┌─────────▼──────┐            │
│  │   users     │     │   groups     │     │   messages     │            │
│  │   table     │     │   table      │     │   table        │            │
│  └─────────────┘     └──────────────┘     └────────────────┘            │
│                                                                           │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐             │
│  │  meetings    │     │  resources   │     │group_members│             │
│  │   table      │     │   table      │     │   ships      │             │
│  └──────────────┘     └──────────────┘     └──────────────┘             │
│                                                                           │
│  ┌──────────────────────────────────────────────────────────┐            │
│  │   Indexes:                                                │            │
│  │   • users.email (UNIQUE)                                 │            │
│  │   • groups.course_code                                    │            │
│  │   • messages.sent_at                                      │            │
│  │   • group_memberships (user_id, group_id) UNIQUE         │            │
│  └──────────────────────────────────────────────────────────┘            │
└──────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Component Interaction Diagram

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   React     │         │   React     │         │   React     │
│ Component   │         │ Component   │         │ Component   │
│  (Frontend) │         │  (Frontend) │         │  (Frontend) │
└──────┬──────┘         └──────┬──────┘         └──────┬──────┘
       │                       │                       │
       │ HTTP Request           │ HTTP Request           │ HTTP Request
       │                       │                       │
       └───────────────────────┴───────────────────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │  API Gateway         │
                    │  (Spring Boot)       │
                    └──────────┬───────────┘
                               │
                ┌───────────────┼───────────────┐
                │               │               │
        ┌───────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
        │   User       │ │   Group     │ │  Messaging  │
        │   Service    │ │   Service   │ │   Service   │
        └───────┬──────┘ └──────┬──────┘ └──────┬──────┘
                │               │               │
                └───────────────┼───────────────┘
                                │
                                ▼
                    ┌──────────────────────┐
                    │   Data Access Layer  │
                    │   (Spring Data JPA)  │
                    └──────────┬───────────┘
                                │
                                ▼
                    ┌──────────────────────┐
                    │   PostgreSQL          │
                    │   Database            │
                    └──────────────────────┘
```

### 2.3 Data Flow Diagram

```
USER INPUT
    │
    ▼
┌──────────────┐
│  React UI    │
│  Component   │
└──────┬───────┘
       │
       │ User Action
       │
       ▼
┌──────────────┐
│  API Client  │
│  (HTTP/HTTPS)│
└──────┬───────┘
       │
       │ JSON Request
       │
       ▼
┌──────────────┐
│  Spring Boot │
│  Controller  │
└──────┬───────┘
       │
       │ Business Logic
       │
       ▼
┌──────────────┐
│   Service    │
│   Layer      │
└──────┬───────┘
       │
       │ Data Access
       │
       ▼
┌──────────────┐
│  Repository  │
│  (JPA)       │
└──────┬───────┘
       │
       │ SQL Query
       │
       ▼
┌──────────────┐
│  PostgreSQL  │
│  Database    │
└──────┬───────┘
       │
       │ Result Set
       │
       ▼
┌──────────────┐
│  Entity      │
│  Objects     │
└──────┬───────┘
       │
       │ Return Data
       │
       ▼
┌──────────────┐
│  JSON        │
│  Response    │
└──────┬───────┘
       │
       │ HTTP Response
       │
       ▼
┌──────────────┐
│  React UI    │
│  Update      │
└──────────────┘
```

---

## 3. Entity Relationship Diagram (ERD)

### 3.1 Complete ERD with All Relationships

```
                    ┌─────────────────────────────┐
                    │          USER                │
                    │  ────────────────────────   │
                    │  PK: id                      │
                    │      email (UNIQUE)          │
                    │      password_hash           │
                    │      first_name              │
                    │      last_name               │
                    │      role                    │
                    │      created_at              │
                    │      is_active               │
                    └──────────┬──────────────────┘
                               │
                ┌───────────────┼───────────────┐
                │               │               │
                │ 1             │ 1             │ 1
                │ creates       │ sends         │ receives
                │               │               │
                │ M             │ M             │ M
    ┌───────────▼───┐   ┌───────▼──────┐   ┌───▼────────────┐
    │    GROUP      │   │   MESSAGE    │   │   MESSAGE      │
    │───────────────│   │───────────────│   │───────────────│
    │ PK: id        │   │ PK: id        │   │ (recipient)   │
    │ FK: creator_id│   │ FK: sender_id │   │               │
    │ name          │   │ FK: group_id  │   │               │
    │ description   │   │ content       │   │               │
    │ course_code   │   │ sent_at       │   │               │
    │ max_members   │   │ is_read       │   │               │
    │ is_public     │   └───────────────┘   └───────────────┘
    └───────┬───────┘
            │
            │ 1
            │ has
            │
            │ M
    ┌───────▼──────────────────────┐
    │   GROUP_MEMBERSHIP            │
    │───────────────────────────────│
    │ PK: id                        │
    │ FK: user_id ───────────────┐  │
    │ FK: group_id              │  │
    │ role                       │  │
    │ status                     │  │
    │ joined_at                  │  │
    └────────────────────────────┘  │
                                    │
            ┌───────────────────────┘
            │
            │ 1
            │ belongs to
            │
            │ M
    ┌───────▼──────────┐
    │    USER          │
    │ (as member)      │
    └──────────────────┘

    ┌──────────────┐
    │    GROUP     │
    │ (from above) │
    └──────┬───────┘
           │
           │ 1
           │
           │ M
    ┌──────┴───────────────┐
    │                      │
    │ 1                    │ 1
    │                      │
    ▼ M                    ▼ M
┌──────────────┐    ┌──────────────┐
│   MEETING    │    │   RESOURCE   │
│──────────────│    │──────────────│
│ PK: id       │    │ PK: id       │
│ FK: group_id │    │ FK: group_id │
│ FK: organizer│    │ FK: uploader │
│ title        │    │ title        │
│ scheduled_at │    │ file_url     │
│ location     │    │ file_type    │
│ duration     │    │ uploaded_at  │
│ status       │    │ tags[]       │
└──────────────┘    └──────────────┘
```

### 3.2 Relationship Cardinality Diagram

```
USER ──────<creates>────── GROUP
 (1)                      (M)

 │                         │
 │                         │
 │ ──────<member of>───────┘
 │         │
 │         │
 │         ▼
 │    MEMBERSHIP
 │    (join table)
 │         │
 │         │
 │         │
 │    ┌────┴────┐
 │    │         │
 │  (M)       (M)
 │    │         │
 │    ▼         ▼
 │  USER      GROUP
 │
 │
 │ ──────<sends>─────── MESSAGE
 │ (1)                  (M)
 │
 │ ──────<receives>──── MESSAGE
 │ (1)                  (M)
 │
GROUP ──────<contains>────── MESSAGE
 (1)                         (M)

GROUP ──────<has>─────── MEETING
 (1)                     (M)

GROUP ──────<contains>────── RESOURCE
 (1)                        (M)
```

---

## 4. Deployment Architecture Diagram

```
                    ┌─────────────────────────┐
                    │      Internet Users     │
                    └────────────┬────────────┘
                                 │
                                 │ HTTPS
                                 │
                    ┌────────────▼────────────┐
                    │  AWS CloudFront CDN      │
                    │  (Static Assets)         │
                    └────────────┬────────────┘
                                 │
                                 │
                    ┌────────────▼────────────┐
                    │  AWS S3 Bucket          │
                    │  (React Frontend)        │
                    └────────────┬────────────┘
                                 │
                                 │
                    ┌────────────▼────────────┐
                    │  AWS Application        │
                    │  Load Balancer          │
                    └────────────┬────────────┘
                                 │
                ┌────────────────┼────────────────┐
                │                │                │
    ┌───────────▼────┐  ┌────────▼──────┐  ┌──────▼────────┐
    │  EC2 Instance  │  │  EC2 Instance │  │ EC2 Instance │
    │  (Spring Boot) │  │  (Spring Boot)│  │ (Spring Boot)│
    │  Auto-Scaling  │  │  Auto-Scaling │  │ Auto-Scaling │
    └───────────┬────┘  └────────┬──────┘  └──────┬────────┘
                │                │                │
                └────────────────┼────────────────┘
                                 │
                                 │ JDBC Connection
                                 │
                    ┌────────────▼────────────┐
                    │  AWS RDS PostgreSQL     │
                    │  (Primary Database)      │
                    └────────────┬────────────┘
                                 │
                                 │ Replication
                                 │
                    ┌────────────▼────────────┐
                    │  AWS RDS Read Replica    │
                    │  (For Read Operations)   │
                    └──────────────────────────┘

    ┌──────────────────────────────────────────┐
    │  AWS Services:                             │
    │  • S3: Static file storage                 │
    │  • CloudFront: CDN for faster delivery    │
    │  • EC2: Application servers (auto-scaling)│
    │  • RDS: PostgreSQL database                │
    │  • ELB: Load balancing                     │
    │  • IAM: Security & access control          │
    └──────────────────────────────────────────┘
```

---

## 5. Database Schema Diagram

### 5.1 Table Structure Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        USERS TABLE                                   │
├─────────────────────────────────────────────────────────────────────┤
│ PK │ id            │ BIGSERIAL      │ Auto-increment                │
│    │ email         │ VARCHAR(255)   │ UNIQUE, NOT NULL              │
│    │ password_hash │ VARCHAR(255)   │ NOT NULL                      │
│    │ first_name    │ VARCHAR(100)   │                               │
│    │ last_name     │ VARCHAR(100)   │                               │
│    │ role          │ VARCHAR(20)    │ CHECK (STUDENT/TA/FACULTY)   │
│    │ created_at    │ TIMESTAMP      │ DEFAULT NOW()                 │
│    │ last_login    │ TIMESTAMP      │                               │
│    │ is_active     │ BOOLEAN        │ DEFAULT TRUE                  │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              │ FK: creator_id
                              │
┌─────────────────────────────▼─────────────────────────────────────┐
│                        GROUPS TABLE                                 │
├─────────────────────────────────────────────────────────────────────┤
│ PK │ id              │ BIGSERIAL      │ Auto-increment              │
│ FK │ creator_id      │ BIGINT         │ REFERENCES users(id)         │
│    │ name            │ VARCHAR(255)   │ NOT NULL                    │
│    │ description     │ TEXT           │                             │
│    │ course_code     │ VARCHAR(50)    │                             │
│    │ max_members     │ INTEGER        │ DEFAULT 10                  │
│    │ is_public       │ BOOLEAN        │ DEFAULT TRUE                 │
│    │ requires_approval│ BOOLEAN      │ DEFAULT FALSE                │
│    │ created_at     │ TIMESTAMP      │ DEFAULT NOW()                │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              │ FK: group_id
                              │
┌─────────────────────────────▼─────────────────────────────────────┐
│                  GROUP_MEMBERSHIPS TABLE                           │
├─────────────────────────────────────────────────────────────────────┤
│ PK │ id            │ BIGSERIAL      │ Auto-increment                │
│ FK │ user_id       │ BIGINT         │ REFERENCES users(id)          │
│ FK │ group_id      │ BIGINT         │ REFERENCES groups(id)         │
│    │ role          │ VARCHAR(20)    │ MEMBER/ADMIN/MODERATOR        │
│    │ status        │ VARCHAR(20)    │ ACTIVE/PENDING/LEFT          │
│    │ joined_at     │ TIMESTAMP      │ DEFAULT NOW()                │
│    │ UNIQUE(user_id, group_id)                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 6. Sequence Diagrams

### 6.1 User Registration Sequence

```
User          Browser          Frontend        Backend         Database
 │               │                 │              │                │
 │──Register─────>│                 │              │                │
 │               │──POST /api/auth/register─────>│                │
 │               │                 │              │                │
 │               │                 │              │──Validate──────>│
 │               │                 │              │                │
 │               │                 │              │<──Check Email───│
 │               │                 │              │                │
 │               │                 │              │──Insert User───>│
 │               │                 │              │                │
 │               │                 │              │<──User Created──│
 │               │                 │              │                │
 │               │                 │              │──Send Email────>│
 │               │<──201 Created───│              │                │
 │<──Success─────│                 │              │                │
 │               │                 │              │                │
 │──Verify Email─>│                 │              │                │
 │               │──GET /api/auth/verify?token───>│                │
 │               │                 │              │──Update Status─>│
 │               │                 │              │                │
 │               │<──200 OK────────│              │                │
 │<──Verified────│                 │              │                │
```

### 6.2 Group Creation Sequence

```
User          Frontend        Group Service    User Service    Database
 │               │                 │              │                │
 │──Create Grp───>│                 │              │                │
 │               │──POST /api/groups─────────────>│                │
 │               │                 │              │                │
 │               │                 │──Validate───>│                │
 │               │                 │              │                │
 │               │                 │──Check Auth─>│                │
 │               │                 │              │──Verify User───>│
 │               │                 │              │                │
 │               │                 │              │<──User Valid───│
 │               │                 │              │                │
 │               │                 │──Create Grp─────────────────>│
 │               │                 │              │                │
 │               │                 │              │<──Group Created│
 │               │                 │              │                │
 │               │                 │──Create Membership───────────>│
 │               │                 │              │                │
 │               │                 │              │<──Membership OK │
 │               │<──201 Created───│              │                │
 │<──Group Ready─│                 │              │                │
```

### 6.3 Message Sending Sequence

```
User A        Frontend        Messaging       Notification    Database
 │               │              Service         Service           │
 │──Send Msg─────>│                 │              │                │
 │               │──POST /api/messages───────────>│                │
 │               │                 │              │                │
 │               │                 │──Validate───>│                │
 │               │                 │              │                │
 │               │                 │──Save Msg────────────────────>│
 │               │                 │              │                │
 │               │                 │              │<──Message Saved │
 │               │                 │              │                │
 │               │                 │──Notify──────────────────────>│
 │               │                 │              │                │
 │               │                 │              │──Send Alert───>│
 │               │                 │              │                │
 │               │<──200 OK────────│              │                │
 │<──Sent────────│                 │              │                │
 │               │                 │              │                │
User B          │                 │              │                │
 │<──Notification│                 │              │                │
 │               │                 │              │                │
 │──Fetch Msg───>│                 │              │                │
 │               │──GET /api/messages/conversations──────────────>│
 │               │                 │              │                │
 │               │                 │              │<──Messages─────│
 │               │<──200 OK────────│              │                │
 │<──Messages────│                 │              │                │
```

---

## 7. Class Diagram (UML Style)

```
┌─────────────────────────────────────────────────────────┐
│                    User                                  │
├─────────────────────────────────────────────────────────┤
│ - id: Long                                               │
│ - email: String                                          │
│ - passwordHash: String                                    │
│ - firstName: String                                      │
│ - lastName: String                                       │
│ - role: Role                                             │
│ - createdAt: Timestamp                                   │
│ - isActive: Boolean                                      │
├─────────────────────────────────────────────────────────┤
│ + register()                                             │
│ + login()                                                │
│ + updateProfile()                                        │
│ + getProfile()                                           │
└───────────────┬─────────────────────────────────────────┘
                │
                │ 1
                │
                │ creates
                │
                │ M
┌───────────────▼─────────────────────────────────────────┐
│                    Group                                 │
├─────────────────────────────────────────────────────────┤
│ - id: Long                                               │
│ - name: String                                          │
│ - description: String                                    │
│ - courseCode: String                                     │
│ - creatorId: Long                                       │
│ - maxMembers: Integer                                   │
│ - isPublic: Boolean                                     │
├─────────────────────────────────────────────────────────┤
│ + create()                                              │
│ + update()                                              │
│ + addMember()                                           │
│ + removeMember()                                        │
│ + search()                                              │
└───────────────┬─────────────────────────────────────────┘
                │
                │ 1
                │
                │ has
                │
                │ M
┌───────────────▼─────────────────────────────────────────┐
│              GroupMembership                              │
├─────────────────────────────────────────────────────────┤
│ - id: Long                                               │
│ - userId: Long                                           │
│ - groupId: Long                                          │
│ - role: MembershipRole                                   │
│ - status: MembershipStatus                               │
│ - joinedAt: Timestamp                                   │
├─────────────────────────────────────────────────────────┤
│ + join()                                                 │
│ + leave()                                                │
│ + changeRole()                                           │
└─────────────────────────────────────────────────────────┘
```

---

## 8. State Diagram

### 8.1 Group Membership State Diagram

```
                    [Initial State]
                          │
                          ▼
                    ┌──────────────┐
                    │   PENDING    │
                    │  (Requested) │
                    └──────┬───────┘
                           │
                           │ Approval
                           │
                           ▼
                    ┌──────────────┐
                    │   ACTIVE     │
                    │  (Member)    │
                    └──────┬───────┘
                           │
                           │ Leave/Remove
                           │
                           ▼
                    ┌──────────────┐
                    │    LEFT      │
                    │  (Inactive)  │
                    └──────────────┘
```

### 8.2 Meeting State Diagram

```
                    [Create Meeting]
                          │
                          ▼
                    ┌──────────────┐
                    │  SCHEDULED   │
                    └──────┬───────┘
                           │
                ┌──────────┼──────────┐
                │                    │
                │ Cancel             │ Complete
                │                    │
                ▼                    ▼
        ┌──────────────┐    ┌──────────────┐
        │  CANCELLED   │    │  COMPLETED   │
        └──────────────┘    └──────────────┘
```

---

## 9. Activity Diagram

### 9.1 Group Discovery and Join Flow

```
                    [Start]
                      │
                      ▼
            ┌─────────────────┐
            │ Login Required  │
            └────────┬────────┘
                     │
                     ▼
            ┌─────────────────┐
            │ Navigate to      │
            │ Discovery Page   │
            └────────┬────────┘
                     │
                     ▼
            ┌─────────────────┐
            │ Apply Filters    │
            │ (Course, Time)   │
            └────────┬────────┘
                     │
                     ▼
            ┌─────────────────┐
            │ View Results    │
            └────────┬────────┘
                     │
                     ▼
            ┌─────────────────┐
            │ Select Group     │
            └────────┬────────┘
                     │
                     ▼
            ┌─────────────────┐
            │ View Details     │
            └────────┬────────┘
                     │
                     ▼
            ┌─────────────────┐
            │ Click Join       │
            └────────┬────────┘
                     │
          ┌──────────┴──────────┐
          │                      │
    Requires          Public/No
    Approval?        Approval?
          │                      │
          ▼                      ▼
    ┌──────────┐          ┌──────────┐
    │ PENDING  │          │  ACTIVE  │
    └──────────┘          └──────────┘
          │                      │
          └──────────┬───────────┘
                     │
                     ▼
            ┌─────────────────┐
            │   Notification   │
            │   Sent           │
            └─────────────────┘
                     │
                     ▼
                   [End]
```

---

## 10. Network Architecture Diagram

```
                    ┌─────────────────────────┐
                    │   Client Browser        │
                    │   (React App)           │
                    └──────────┬──────────────┘
                               │
                               │ HTTPS (Port 443)
                               │
                    ┌──────────▼──────────────┐
                    │   AWS CloudFront        │
                    │   (CDN)                 │
                    └──────────┬──────────────┘
                               │
                    ┌──────────▼──────────────┐
                    │   AWS S3                 │
                    │   (Static Files)         │
                    └──────────┬──────────────┘
                               │
                    ┌──────────▼──────────────┐
                    │   Application           │
                    │   Load Balancer         │
                    │   (Port 80/443)         │
                    └──────────┬──────────────┘
                               │
                ┌──────────────┼──────────────┐
                │              │              │
        ┌───────▼──────┐ ┌─────▼──────┐ ┌─────▼──────┐
        │  EC2 Server  │ │ EC2 Server │ │ EC2 Server │
        │  Instance 1  │ │ Instance 2 │ │ Instance 3 │
        │  Port: 8080  │ │ Port: 8080 │ │ Port: 8080 │
        └───────┬──────┘ └─────┬──────┘ └─────┬──────┘
                │              │              │
                └──────────────┼──────────────┘
                               │
                    ┌──────────▼──────────────┐
                    │   RDS PostgreSQL        │
                    │   (Port 5432)            │
                    │   Primary Instance       │
                    └──────────┬──────────────┘
                               │
                    ┌──────────▼──────────────┐
                    │   RDS Read Replica      │
                    │   (Read Operations)     │
                    └─────────────────────────┘
```

