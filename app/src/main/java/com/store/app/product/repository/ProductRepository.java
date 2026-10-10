package com.store.app.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.store.app.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

	// The entity graphs fetch the lazy associations in the same query, avoiding N+1 when mapping to DTOs.

	@Override
	@EntityGraph(attributePaths = { "manufacturer", "category", "origin" })
	Optional<Product> findById(Long id);

	@Override
	@EntityGraph(attributePaths = { "manufacturer", "category", "origin" })
	List<Product> findAll(Specification<Product> spec, Sort sort);

	boolean existsBySku(String sku);

	boolean existsBySkuAndIdNot(String sku, Long id);

}
