package com.store.app.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.store.app.store.repository.StoreRepository;

@SpringBootTest
@AutoConfigureMockMvc
class StoreControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StoreRepository storeRepository;

	// @SpringBootTest does not roll back, so start each test from an empty table
	@BeforeEach
	void setUp() {
		storeRepository.deleteAll();
	}

	@Test
	void createsStoreAndReturnsLocation() {
	}

	@Test
	void readsCreatedStoreById() {
	}

	@Test
	void returnsBadRequestForBlankName() {
	}

	@Test
	void returnsConflictForDuplicateNameInSameCity() {
	}

	@Test
	void returnsNotFoundForUnknownStore() {
	}

}
