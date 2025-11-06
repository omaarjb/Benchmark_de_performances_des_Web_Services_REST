package com.omar.springmvc.repository;

import com.omar.entities.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findAll(Pageable pageable);

    Page<Item> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT i FROM Item i JOIN FETCH i.category WHERE i.category.id = :categoryId")
    Page<Item> findByCategoryIdWithJoinFetch(@Param("categoryId") Long categoryId, Pageable pageable);

    // Trouver par SKU
    Item findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("SELECT i FROM Item i WHERE i.category.id = :categoryId")
    Page<Item> findItemsByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT i FROM Item i " +
            "WHERE (:withJoin = true AND i.category.id = :categoryId) " +
            "OR (:withJoin = false AND i.category.id = :categoryId)")
    Page<Item> findByCategoryIdWithFlag(@Param("categoryId") Long categoryId,
                                        @Param("withJoin") boolean withJoin,
                                        Pageable pageable);
    @Query("SELECT COUNT(i) FROM Item i WHERE i.category.id = :categoryId")
    Long countByCategoryId(@Param("categoryId") Long categoryId);
}