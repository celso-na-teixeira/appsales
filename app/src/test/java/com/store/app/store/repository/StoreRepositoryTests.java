package com.store.app.store.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class StoreRepositoryTests {

	@Autowired
	private StoreRepository storeRepository;

	@Test
	void savesAndFindsStoreById() {
	}

	@Test
	void existsByNameAndCityMatchesOnlySameNameAndCity() {
	}

	@Test
	void allowsSameNameInDifferentCities() {
	}

	@Test
	void rejectsDuplicateNameAndCity() {
	}

}
