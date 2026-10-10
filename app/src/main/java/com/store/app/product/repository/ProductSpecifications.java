package com.store.app.product.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.store.app.product.dto.ProductSearchCriteria;
import com.store.app.product.entity.Product;

public final class ProductSpecifications {

	private static final char ESCAPE = '\\';

	private ProductSpecifications() {
	}

	public static Specification<Product> matching(ProductSearchCriteria criteria) {
		List<Specification<Product>> filters = new ArrayList<>();
		addContains(filters, "sku", criteria.sku());
		addContains(filters, "barcode", criteria.barcode());
		addContains(filters, "name", criteria.name());
		addContains(filters, "shortDescription", criteria.shortDescription());
		addContains(filters, "description", criteria.description());
		addReference(filters, "manufacturer", criteria.manufacturerId());
		addReference(filters, "category", criteria.categoryId());
		addReference(filters, "origin", criteria.originId());
		addRange(filters, "price", criteria.minPrice(), criteria.maxPrice());
		addRange(filters, "weight", criteria.minWeight(), criteria.maxWeight());
		if (criteria.active() != null) {
			filters.add((root, query, cb) -> cb.equal(root.get("active"), criteria.active()));
		}
		return Specification.allOf(filters);
	}

	private static void addContains(List<Specification<Product>> filters, String field, String value) {
		if (value == null || value.isBlank()) {
			return;
		}
		String pattern = "%" + escape(value.trim().toLowerCase()) + "%";
		filters.add((root, query, cb) -> cb.like(cb.lower(root.get(field)), pattern, ESCAPE));
	}

	private static void addReference(List<Specification<Product>> filters, String association, Long id) {
		if (id != null) {
			filters.add((root, query, cb) -> cb.equal(root.get(association).get("id"), id));
		}
	}

	private static void addRange(List<Specification<Product>> filters, String field, BigDecimal min, BigDecimal max) {
		if (min != null) {
			filters.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get(field), min));
		}
		if (max != null) {
			filters.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get(field), max));
		}
	}

	// Keeps % and _ in the search text from acting as wildcards
	private static String escape(String value) {
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

}
