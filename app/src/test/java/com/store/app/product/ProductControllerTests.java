package com.store.app.product;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.store.app.product.entity.Manufacturer;
import com.store.app.product.entity.Product;
import com.store.app.product.entity.ProductCategory;
import com.store.app.product.entity.ProductOrigin;
import com.store.app.product.repository.ManufacturerRepository;
import com.store.app.product.repository.ProductCategoryRepository;
import com.store.app.product.repository.ProductOriginRepository;
import com.store.app.product.repository.ProductRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ManufacturerRepository manufacturerRepository;

	@Autowired
	private ProductCategoryRepository categoryRepository;

	@Autowired
	private ProductOriginRepository originRepository;

	private Manufacturer manufacturer;

	private ProductCategory category;

	@BeforeEach
	void setUp() {
		productRepository.deleteAll();
		manufacturerRepository.deleteAll();
		categoryRepository.deleteAll();
		originRepository.deleteAll();

		manufacturer = new Manufacturer();
		manufacturer.setName("Acme");
		manufacturer = manufacturerRepository.save(manufacturer);

		category = new ProductCategory();
		category.setName("Laptops");
		category = categoryRepository.save(category);
	}

	@Test
	void createsAndReadsProduct() throws Exception {
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(json("SKU-1", "19.99")))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andExpect(jsonPath("$.sku").value("SKU-1"))
			.andExpect(jsonPath("$.manufacturerName").value("Acme"))
			.andExpect(jsonPath("$.categoryName").value("Laptops"))
			.andExpect(jsonPath("$.active").value(true))
			.andExpect(jsonPath("$.createdAt").exists());

		Long id = productRepository.findAll().get(0).getId();
		mockMvc.perform(get("/api/products/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.price").value(19.99));
	}

	@Test
	void searchesByNameAndPriceRange() throws Exception {
		save("SKU-1", "Gaming Laptop", "1500.00");
		save("SKU-2", "Office Laptop", "700.00");
		save("SKU-3", "Mouse", "25.00");

		mockMvc.perform(get("/api/products")).andExpect(jsonPath("$", hasSize(3)));
		mockMvc.perform(get("/api/products").param("name", "laptop"))
			.andExpect(jsonPath("$[*].sku", contains("SKU-1", "SKU-2")));
		mockMvc.perform(get("/api/products").param("maxPrice", "700"))
			.andExpect(jsonPath("$[*].sku", contains("SKU-3", "SKU-2")));
		mockMvc.perform(get("/api/products").param("name", "laptop").param("minPrice", "1000"))
			.andExpect(jsonPath("$[*].sku", contains("SKU-1")));
	}

	@Test
	void sortsByRequestedFields() throws Exception {
		save("SKU-1", "Gaming Laptop", "1500.00");
		save("SKU-2", "Office Laptop", "700.00");
		save("SKU-3", "Mouse", "700.00");

		mockMvc.perform(get("/api/products").param("sort", "price,desc"))
			.andExpect(jsonPath("$[0].sku").value("SKU-1"));
		mockMvc.perform(get("/api/products").param("sort", "price").param("sort", "sku,desc"))
			.andExpect(jsonPath("$[*].sku", contains("SKU-3", "SKU-2", "SKU-1")));
		mockMvc.perform(get("/api/products").param("name", "laptop").param("sort", "sku,desc"))
			.andExpect(jsonPath("$[*].sku", contains("SKU-2", "SKU-1")));
		mockMvc.perform(get("/api/products").param("sort", "nope")).andExpect(status().isBadRequest());
	}

	@Test
	void searchesByEveryField() throws Exception {
		Manufacturer other = new Manufacturer();
		other.setName("Globex");
		other = manufacturerRepository.save(other);
		ProductOrigin origin = new ProductOrigin();
		origin.setCountry("Portugal");
		origin = originRepository.save(origin);

		Product laptop = save("LAP-1", "Gaming Laptop", "1500.00");
		laptop.setBarcode("560001");
		laptop.setShortDescription("Fast 100% of the time");
		laptop.setDescription("RGB keyboard included");
		laptop.setWeight(new BigDecimal("2.500"));
		laptop.setOrigin(origin);
		productRepository.save(laptop);

		Product mouse = save("MOU-1", "Mouse", "25.00");
		mouse.setBarcode("560002");
		mouse.setShortDescription("Wireless");
		mouse.setWeight(new BigDecimal("0.100"));
		mouse.setManufacturer(other);
		mouse.setActive(false);
		productRepository.save(mouse);

		assertSearch("sku", "lap", "LAP-1");
		assertSearch("barcode", "0002", "MOU-1");
		assertSearch("shortDescription", "WIRELESS", "MOU-1");
		assertSearch("description", "keyboard", "LAP-1");
		assertSearch("manufacturerId", other.getId().toString(), "MOU-1");
		assertSearch("categoryId", category.getId().toString(), "LAP-1", "MOU-1");
		assertSearch("originId", origin.getId().toString(), "LAP-1");
		assertSearch("minWeight", "1", "LAP-1");
		assertSearch("maxWeight", "1", "MOU-1");
		assertSearch("active", "false", "MOU-1");
		// wildcards in the search text are matched literally
		assertSearch("shortDescription", "100%", "LAP-1");
		mockMvc.perform(get("/api/products").param("sku", "_")).andExpect(jsonPath("$", hasSize(0)));

		mockMvc.perform(get("/api/products").param("sku", "1").param("active", "true").param("maxPrice", "2000"))
			.andExpect(jsonPath("$[*].sku", contains("LAP-1")));
		mockMvc.perform(get("/api/products").param("active", "maybe")).andExpect(status().isBadRequest());
	}

	@Test
	void updatesAndDeletesProduct() throws Exception {
		Long id = save("SKU-1", "Mouse", "25.00").getId();

		mockMvc
			.perform(put("/api/products/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(json("SKU-1", "30.00")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.price").value(30.00));

		mockMvc.perform(delete("/api/products/{id}", id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/products/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	void rejectsInvalidRequests() throws Exception {
		save("SKU-1", "Mouse", "25.00");

		// duplicate SKU
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(json("SKU-1", "10.00")))
			.andExpect(status().isConflict());
		// negative price
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(json("SKU-2", "-1")))
			.andExpect(status().isBadRequest());
		// unknown manufacturer
		mockMvc
			.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("""
					{"sku": "SKU-3", "name": "Keyboard", "price": 10, "manufacturerId": 999999, "categoryId": %d}
					""".formatted(category.getId())))
			.andExpect(status().isBadRequest());
	}

	private Product save(String sku, String name, String price) {
		Product product = new Product();
		product.setSku(sku);
		product.setName(name);
		product.setPrice(new BigDecimal(price));
		product.setManufacturer(manufacturer);
		product.setCategory(category);
		return productRepository.save(product);
	}

	private void assertSearch(String param, String value, String... expectedSkus) throws Exception {
		mockMvc.perform(get("/api/products").param(param, value))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].sku", contains(expectedSkus)));
	}

	private String json(String sku, String price) {
		return """
				{"sku": "%s", "name": "Keyboard", "price": %s, "manufacturerId": %d, "categoryId": %d}
				""".formatted(sku, price, manufacturer.getId(), category.getId());
	}

}
