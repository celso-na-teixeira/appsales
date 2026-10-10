package com.store.app.product.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Sort;

import com.store.app.product.dto.ProductSearchCriteria;
import com.store.app.product.entity.Manufacturer;
import com.store.app.product.entity.Product;
import com.store.app.product.entity.ProductCategory;

// Edge cases only; the filters themselves are covered end to end in ProductControllerTests
@DataJpaTest
class ProductSpecificationsTests {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ManufacturerRepository manufacturerRepository;

	@Autowired
	private ProductCategoryRepository categoryRepository;

	private Manufacturer manufacturer;

	private ProductCategory category;

	@BeforeEach
	void setUp() {
		manufacturer = new Manufacturer();
		manufacturer.setName("Acme");
		manufacturer = manufacturerRepository.save(manufacturer);

		category = new ProductCategory();
		category.setName("Laptops");
		category = categoryRepository.save(category);
	}

	@Test
	void blankTextFiltersAreIgnored() {
		save("SKU-1", "Laptop", null);
		save("SKU-2", "Mouse", null);

		assertThat(search(criteria("", "   ", null, null))).containsExactly("SKU-1", "SKU-2");
	}

	@Test
	void textFiltersAreTrimmed() {
		save("SKU-1", "Gaming Laptop", null);
		save("SKU-2", "Mouse", null);

		assertThat(search(criteria(null, "  laptop ", null, null))).containsExactly("SKU-1");
	}

	@Test
	void backslashInTextFilterIsMatchedLiterally() {
		save("A\\B", "Laptop", null);
		save("AB", "Mouse", null);

		assertThat(search(criteria("a\\b", null, null, null))).containsExactly("A\\B");
		assertThat(search(criteria("\\", null, null, null))).containsExactly("A\\B");
	}

	@Test
	void filterOnFieldExcludesProductsWithoutValue() {
		save("SKU-1", "Laptop", "2.500");
		save("SKU-2", "Mouse", null);

		assertThat(search(criteria(null, null, null, new BigDecimal("5")))).containsExactly("SKU-1");
		assertThat(search(criteria(null, null, new BigDecimal("1"), null))).containsExactly("SKU-1");
	}

	private List<String> search(ProductSearchCriteria criteria) {
		return productRepository.findAll(ProductSpecifications.matching(criteria), Sort.by("sku"))
			.stream()
			.map(Product::getSku)
			.toList();
	}

	private static ProductSearchCriteria criteria(String sku, String name, BigDecimal minWeight,
			BigDecimal maxWeight) {
		return new ProductSearchCriteria(sku, null, null, null, null, name, null, null, null, null, minWeight,
				maxWeight, null);
	}

	private void save(String sku, String name, String weight) {
		Product product = new Product();
		product.setSku(sku);
		product.setName(name);
		product.setPrice(new BigDecimal("10.00"));
		product.setWeight(weight != null ? new BigDecimal(weight) : null);
		product.setManufacturer(manufacturer);
		product.setCategory(category);
		productRepository.save(product);
	}

}
