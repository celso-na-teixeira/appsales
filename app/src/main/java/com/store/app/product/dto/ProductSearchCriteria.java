package com.store.app.product.dto;

import java.math.BigDecimal;

// Every filter is optional; the ones that are present are combined with AND
public record ProductSearchCriteria(

		String sku,

		String barcode,

		Long manufacturerId,

		Long categoryId,

		Long originId,

		String name,

		String shortDescription,

		String description,

		BigDecimal minPrice,

		BigDecimal maxPrice,

		BigDecimal minWeight,

		BigDecimal maxWeight,

		Boolean active) {

}
