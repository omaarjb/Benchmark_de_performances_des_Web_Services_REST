package com.omar.springmvc.repository;

import com.omar.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Trouver par code
    Category findByCode(String code);

    Page<Category> findAll(Pageable pageable);

    boolean existsByCode(String code);

    @Query("SELECT COUNT(i) > 0 FROM Item i WHERE i.category.id = :categoryId")
    boolean hasItems(@Param("categoryId") Long categoryId);
}