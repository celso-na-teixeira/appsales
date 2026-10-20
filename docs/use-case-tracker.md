# Use Case Implementation Tracker

Tracks progress on the use cases defined in [Use cases and learinh path.md](../Use%20cases%20and%20learinh%20path.md).

**Legend:** ⬜ Not started · 🟡 In progress · ✅ Done

**Test types used below**

| Type | What it covers | Typical tooling |
|------|----------------|-----------------|
| Unit | Service and mapper logic in isolation | JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`) |
| Integration – Repository | Query methods, JPQL, specifications against a real DB | `@DataJpaTest` |
| Integration – API | Endpoint → service → DB, validation, status codes | `@SpringBootTest` + `MockMvc` |
| Manual | Quick smoke check of the running app | curl / HTTP client |

---

## Overview

| # | Use Case | Concept | Impl | Unit | Repo IT | API IT | Status |
|---|----------|---------|------|------|---------|--------|--------|
| 1 | Register Customer | Entity, Repository, `save()` | ✅ | ⬜ | ⬜ | ✅ | 🟡 |
| 2 | Search Customers by City | Derived query | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 3 | Product Catalogue | Basic CRUD | ✅ | ✅ | ⬜ | ✅ | 🟡 |
| 4 | Search Products | Derived queries | 🟡 | ✅ | ✅ | ✅ | 🟡 |
| 5 | Warehouse Inventory | `@ManyToOne` | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 6 | Register Store | Simple entity | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 7 | Create Order | `@ManyToOne` (Customer, Store) | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 8 | Add Order Items | `@OneToMany` | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 9 | Create Shipment | `@OneToOne` | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 10 | Orders By Customer | Navigating relationships | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 11 | Orders By Date Range | Date queries | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 12 | Top Customers | JPQL, aggregation, GROUP BY | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 13 | Best Selling Products | Reporting queries | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 14 | Pagination | `Page`, `Pageable`, `Sort` | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 15 | Dynamic Search | `Specification<Order>` | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 16 | N+1 Problem | `@EntityGraph`, `JOIN FETCH` | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |

---

## UC1 – Register Customer

`POST /api/customers` · Week 1

**Implementation**
- [x] `Customer` entity
- [x] `CustomerRepository`
- [x] Request / response DTOs with validation
- [x] `CustomerMapper`
- [x] `CustomerService.create()` (duplicate code / email → 409)
- [x] `CustomerController` POST, GET all, GET by id

**Unit tests** (`CustomerServiceTest`)
- [ ] Create saves and returns mapped customer
- [ ] Create rejects duplicate customer code (409)
- [ ] Create rejects duplicate email (409)
- [ ] `findById` throws 404 for unknown id

**Integration – Repository**
- [ ] `existsByEmail` / `existsByCustomerCode` return true/false correctly
- [ ] Unique constraint on email enforced by DB

**Integration – API** (`CustomerControllerTest`)
- [x] Create returns 201 + `Location`, readable via GET
- [x] Invalid payload returns 400
- [x] Unknown id returns 404
- [ ] Duplicate email returns 409

**Manual**
- [ ] `curl -X POST /api/customers` with valid body

---

## UC2 – Search Customers by City

`GET /api/customers?city=Lisbon` · Week 1

**Implementation**
- [ ] `List<Customer> findByCity(String city)` in `CustomerRepository`
- [ ] Service method `findByCity`
- [ ] Controller: optional `city` request param (fallback to `findAll` when absent)

**Unit tests**
- [ ] Service delegates to `findByCity` when city given
- [ ] Service falls back to `findAll` when city missing

**Integration – Repository**
- [ ] Returns only customers from the given city
- [ ] Returns empty list for unknown city
- [ ] Decide & test case sensitivity (`findByCityIgnoreCase`?)

**Integration – API**
- [ ] `?city=Lisbon` returns only Lisbon customers
- [ ] No param returns all customers

---

## UC3 – Product Catalogue

`GET /api/products`, `GET /api/products/{id}` · Week 1

**Implementation**
- [x] `Product` entity (+ `Manufacturer`, `ProductCategory`, `ProductOrigin`)
- [x] `ProductRepository`
- [x] DTOs, `ProductMapper`
- [x] `ProductService` CRUD
- [x] `ProductController` GET, GET by id, POST, PUT, DELETE

**Unit tests** (`ProductServiceTests`, `ProductMapperTests`)
- [x] `findById` not found → 404
- [x] Create resolves associations, rejects duplicate SKU / unknown refs
- [x] Update applies changes, rejects SKU clash, 404 on unknown
- [x] Mapper flattens associations
- [ ] Delete of unknown product → 404

**Integration – Repository**
- [ ] `findById` entity graph loads associations in one query

**Integration – API** (`ProductControllerTests`)
- [x] Create and read product
- [x] Update and delete product
- [x] Invalid requests rejected

---

## UC4 – Search Products

`GET /api/products?name=...&minPrice=...&maxPrice=...` · Week 5

> Implemented with `ProductSpecifications` (dynamic). The derived queries from the use case are still worth adding as an exercise.

**Implementation**
- [x] Search by name / price range via Specifications
- [x] Configurable sorting
- [ ] Derived query `findByNameContaining(String text)`
- [ ] Derived query `findByPriceLessThan(BigDecimal price)`
- [ ] Derived query `findByPriceGreaterThan(BigDecimal price)`

**Unit tests**
- [x] Search maps results in requested order
- [x] Unknown sort field rejected

**Integration – Repository** (`ProductSpecificationsTests`)
- [x] Blank filters ignored, text trimmed, special chars escaped
- [ ] Each derived query returns the expected subset

**Integration – API**
- [x] Search by name and price range
- [x] Sort by requested fields

---

## UC5 – Warehouse Inventory

`GET /api/stock?productId=` · Week 3

**Implementation**
- [ ] `Warehouse` entity + repository
- [ ] `WarehouseStock` entity (`@ManyToOne` Warehouse, `@ManyToOne` Product, `quantity`)
- [ ] Unique constraint (warehouse, product)
- [ ] `WarehouseStockRepository.findByProductId(Long productId)`
- [ ] DTOs, mapper, service, controller

**Unit tests**
- [ ] Service returns stock rows mapped to DTO
- [ ] Quantity cannot be negative

**Integration – Repository**
- [ ] `findByProductId` returns stock across all warehouses
- [ ] Duplicate (warehouse, product) rejected

**Integration – API**
- [ ] `GET /api/stock?productId=` returns stock per warehouse
- [ ] Unknown product → empty list or 404 (decide)

---

## UC6 – Register Store

`POST /api/stores` · Week 1

**Implementation**
- [ ] `Store` entity (`name`, `city`)
- [ ] `StoreRepository`
- [ ] DTOs with validation, mapper, service, controller

**Unit tests**
- [ ] Create saves and maps store
- [ ] Duplicate name rejected (if enforced)

**Integration – Repository**
- [ ] Save and find by id

**Integration – API**
- [ ] Create returns 201 + `Location`
- [ ] Invalid payload returns 400

---

## UC7 – Create Order

`POST /api/orders` · Week 2

**Implementation**
- [ ] `Order` entity (`createdAt`, `@ManyToOne` Customer, `@ManyToOne` Store) — table name `orders` (`order` is reserved)
- [ ] `OrderRepository`
- [ ] DTOs (customerId, storeId), mapper
- [ ] Service resolves customer and store, sets `createdAt`
- [ ] Controller

**Unit tests**
- [ ] Create resolves customer and store
- [ ] Unknown customer → 404 / 400
- [ ] Unknown store → 404 / 400

**Integration – Repository**
- [ ] Order persisted with FK to customer and store

**Integration – API**
- [ ] Create returns 201 with customer and store info
- [ ] Unknown references rejected

---

## UC8 – Add Order Items

`POST /api/orders/{id}/items` · Week 2

**Implementation**
- [ ] `OrderItem` entity (`@ManyToOne` Order, `@ManyToOne` Product, `quantity`, `unitPrice`)
- [ ] `Order.items` `@OneToMany(mappedBy = "order", cascade = ALL, orphanRemoval = true)`
- [ ] Helper `Order.addItem()` keeping both sides in sync
- [ ] `unitPrice` copied from product price at time of order
- [ ] Service, controller

**Unit tests**
- [ ] `addItem` sets back-reference
- [ ] Unit price snapshot taken from product
- [ ] Quantity ≤ 0 rejected
- [ ] Unknown order / product → 404

**Integration – Repository**
- [ ] Items cascade-saved with order
- [ ] Removing an item deletes it (orphan removal)

**Integration – API**
- [ ] Add item returns updated order with items
- [ ] Unknown order returns 404

---

## UC9 – Create Shipment

`POST /api/shipments` · Week 4

**Implementation**
- [ ] `Carrier` entity (`name`, `trackingUrl`) + repository
- [ ] `Shipment` entity (`trackingNumber`, `@ManyToOne` Carrier, `@OneToOne` Order)
- [ ] Unique constraint on `order_id` (one shipment per order)
- [ ] DTOs, mapper, service, controller

**Unit tests**
- [ ] Create resolves order and carrier
- [ ] Second shipment for same order rejected (409)
- [ ] Order without items cannot be shipped (if enforced)

**Integration – Repository**
- [ ] One-to-one uniqueness enforced by DB

**Integration – API**
- [ ] Create returns 201 with tracking info
- [ ] Duplicate shipment returns 409

---

## UC10 – Orders By Customer

`GET /api/customers/{id}/orders` (or `/api/orders?customerId=`) · Week 5

**Implementation**
- [ ] `OrderRepository.findByCustomerId(Long customerId)`
- [ ] Service, controller

**Unit tests**
- [ ] Unknown customer → 404

**Integration – Repository**
- [ ] Returns only that customer's orders
- [ ] Inspect generated SQL (`spring.jpa.show-sql`)

**Integration – API**
- [ ] Returns orders for customer, empty list when none

---

## UC11 – Orders By Date Range

`GET /api/orders?from=...&to=...` · Week 5

**Implementation**
- [ ] `findByCreatedAtBetween(LocalDateTime start, LocalDateTime end)`
- [ ] Parse dates with `@DateTimeFormat(iso = ISO.DATE_TIME)`
- [ ] Validate `from <= to`

**Unit tests**
- [ ] `from > to` rejected (400)

**Integration – Repository**
- [ ] Boundaries are inclusive
- [ ] Orders outside range excluded

**Integration – API**
- [ ] Valid range returns matching orders
- [ ] Malformed date returns 400

---

## UC12 – Top Customers

`GET /api/reports/top-customers?limit=10` · Week 5

**Implementation**
- [ ] JPQL `@Query` with `SUM(i.quantity * i.unitPrice)` and `GROUP BY`
- [ ] Projection (interface or record DTO via `SELECT new ...`)
- [ ] Limit via `Pageable`

**Unit tests**
- [ ] Service maps projection to response

**Integration – Repository**
- [ ] Ordering by total descending is correct
- [ ] Customers without orders excluded
- [ ] Totals calculated correctly across multiple orders

**Integration – API**
- [ ] Returns ranked list with totals

---

## UC13 – Best Selling Products

`GET /api/reports/best-selling-products?limit=10` · Week 5

**Implementation**
- [ ] JPQL `SUM(i.quantity)` grouped by product
- [ ] Projection DTO
- [ ] Limit via `Pageable`

**Unit tests**
- [ ] Service maps projection to response

**Integration – Repository**
- [ ] Quantities summed across orders
- [ ] Ordering and limit correct

**Integration – API**
- [ ] Returns ranked products

---

## UC14 – Pagination

`GET /api/orders?page=0&size=20&sort=createdAt,desc` · Week 5

**Implementation**
- [ ] Controller accepts `Pageable` (`@PageableDefault(size = 20, sort = "createdAt")`)
- [ ] Return page DTO (content, page, size, totalElements, totalPages)
- [ ] Whitelist sortable fields
- [ ] Cap max page size

**Unit tests**
- [ ] Unknown sort field rejected

**Integration – Repository**
- [ ] Page size, total count and sort order correct

**Integration – API**
- [ ] Default page/size applied
- [ ] Out-of-range page returns empty content

---

## UC15 – Dynamic Search

`GET /api/orders/search?customerId=&storeId=&carrierId=&from=&to=&productId=` · Week 6

**Implementation**
- [ ] `OrderRepository extends JpaSpecificationExecutor<Order>`
- [ ] `OrderSpecifications` (customer, store, carrier, date range, product)
- [ ] `OrderSearchCriteria` DTO
- [ ] `query.distinct(true)` when joining items (product filter)
- [ ] Combine with pagination

**Unit tests**
- [ ] Null criteria produce no predicate

**Integration – Repository**
- [ ] Each filter alone
- [ ] Filters combined (AND)
- [ ] Product filter does not return duplicate orders

**Integration – API**
- [ ] Combined filters + paging

---

## UC16 – N+1 Problem

`GET /api/orders/{id}` with customer and items · Week 6

**Implementation**
- [ ] Reproduce N+1 with `findAll()` + accessing `customer` / `items`
- [ ] Fix with `@EntityGraph(attributePaths = {"customer", "items", "items.product"})`
- [ ] Alternative fix with `JOIN FETCH` query
- [ ] Note about paging + collection fetch (`HHH90003004`) and how to avoid it

**Unit tests**
- [ ] n/a

**Integration – Repository**
- [ ] Count statements with Hibernate `Statistics` (before vs. after fix)
- [ ] Lazy associations initialised without extra queries

**Integration – API**
- [ ] Order details endpoint returns customer and items

---

## Advanced Challenges

| # | Challenge | Impl | Repo IT | API IT |
|---|-----------|------|---------|--------|
| 1 | Customers who never placed an order | ⬜ | ⬜ | ⬜ |
| 2 | Products with stock below 10 units | ⬜ | ⬜ | ⬜ |
| 3 | Orders not shipped yet | ⬜ | ⬜ | ⬜ |
| 4 | Total revenue per store | ⬜ | ⬜ | ⬜ |
| 5 | Total revenue per month | ⬜ | ⬜ | ⬜ |
| 6 | Top 10 products sold | ⬜ | ⬜ | ⬜ |
| 7 | Dashboard endpoint (orders, revenue, customers, products sold) | ⬜ | ⬜ | ⬜ |

---

## Repositories

- [x] `CustomerRepository`
- [x] `ProductRepository`
- [ ] `StoreRepository`
- [ ] `WarehouseRepository`
- [ ] `WarehouseStockRepository`
- [ ] `OrderRepository`
- [ ] `OrderItemRepository`
- [ ] `CarrierRepository`
- [ ] `ShipmentRepository`

---

## Definition of Done (per use case)

- [ ] Entity, repository, DTOs, mapper, service, controller implemented
- [ ] Validation on request DTOs
- [ ] Errors return correct status (400 / 404 / 409)
- [ ] Unit tests for service logic
- [ ] Repository integration tests for every custom query
- [ ] API integration tests for happy path and main error cases
- [ ] `./mvnw test` passes
- [ ] Tracker updated
