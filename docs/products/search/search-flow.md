# appsales

Spring Boot REST API for a store. The application lives in [app/](../../../app/).

## Product search

`GET /api/products` returns a JSON array of products. Every query parameter is optional: with none, it returns all products sorted by name.

```
GET /api/products?name=laptop&categoryId=3&active=true&maxPrice=2000&sort=price,desc
```

### Filters

Filters that are present are combined with AND. A filter that is missing or blank is ignored.

| Parameter | Matches products where |
|---|---|
| `sku` | SKU contains the text |
| `barcode` | barcode contains the text |
| `name` | name contains the text |
| `shortDescription` | short description contains the text |
| `description` | description contains the text |
| `manufacturerId` | manufacturer has this id |
| `categoryId` | category has this id |
| `originId` | origin has this id |
| `minPrice`, `maxPrice` | price is within the range (inclusive) |
| `minWeight`, `maxWeight` | weight is within the range (inclusive) |
| `active` | active flag equals `true` or `false` |

Text filters:

- They are case-insensitive and match anywhere in the field, so `sku=lap` finds `LAP-1`.
- Leading and trailing spaces are trimmed.
- `%` and `_` are matched literally, not as wildcards.

Range filters work on their own or as a pair: `minPrice=100` alone means "100 or more".

A product with no value in a field never matches a filter on that field. For example, `maxWeight=1` leaves out products that have no weight.

### Sorting

Use `sort=<field>` or `sort=<field>,<direction>`, where the direction is `asc` (default) or `desc`. Repeat the parameter to add tie-breakers:

```
GET /api/products?sort=price,desc
GET /api/products?sort=price&sort=sku,desc
```

Sortable fields: `id`, `sku`, `barcode`, `name`, `shortDescription`, `price`, `weight`, `active`, `createdAt`, `updatedAt`.

Without a `sort` parameter, results are sorted by `name` ascending.

### Errors

The API responds with `400 Bad Request` when:

- a value has the wrong type, such as `active=maybe` or `minPrice=abc`
- `sort` names a field that is not in the sortable list; the message lists the allowed fields

### Limits

- Results are not paginated; the response contains every match.
- Manufacturer, category and origin can be filtered by id only, not by name.
- There is no single parameter that searches across all text fields at once.

### How it is implemented

| Step | Where |
|---|---|
| Spring binds the query parameters to a `ProductSearchCriteria` record and the `sort` parameter to a `Sort` | [ProductController](../../../app/src/main/java/com/store/app/product/controller/ProductController.java), [ProductSearchCriteria](../../../app/src/main/java/com/store/app/product/dto/ProductSearchCriteria.java) |
| The service checks the sort fields against its whitelist | [ProductService](../../../app/src/main/java/com/store/app/product/service/ProductService.java) |
| The criteria are turned into a JPA `Specification`, one predicate per filter that is present | [ProductSpecifications](../../../app/src/main/java/com/store/app/product/repository/ProductSpecifications.java) |
| The repository runs it in a single query that also loads manufacturer, category and origin | [ProductRepository](../../../app/src/main/java/com/store/app/product/repository/ProductRepository.java) |

To add a filter, add a component to `ProductSearchCriteria` and a matching line in `ProductSpecifications.matching`. To make a field sortable, add it to `SORTABLE_FIELDS` in `ProductService`.

The search behaviour is covered by `searchesByNameAndPriceRange`, `searchesByEveryField` and `sortsByRequestedFields` in [ProductControllerTests](../../../app/src/test/java/com/store/app/product/ProductControllerTests.java).
