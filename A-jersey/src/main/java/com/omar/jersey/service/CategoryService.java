package com.omar.jersey.service;

import com.omar.entities.Category;
import com.omar.entities.Item;
import com.omar.jersey.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class CategoryService {

    public List<Category> findAll(int page, int size) {
        EntityManager em = JpaUtil.getEntityManager();
        List<Category> list = em.createQuery("SELECT c FROM Category c ORDER BY c.id", Category.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
        em.close();
        return list;
    }

    public Category findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        Category cat = em.find(Category.class, id);
        em.close();
        return cat;
    }

    public Category create(Category c) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(c);
        tx.commit();
        em.close();
        return c;
    }

    public Category update(Long id, Category data) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Category c = em.find(Category.class, id);
        if (c != null) {
            c.setCode(data.getCode());
            c.setName(data.getName());
        }
        tx.commit();
        em.close();
        return c;
    }

    public boolean delete(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Category c = em.find(Category.class, id);
        if (c != null) em.remove(c);
        tx.commit();
        em.close();
        return c != null;
    }

    public List<Item> findItemsByCategory(Long categoryId, int page, int size) {
        EntityManager em = JpaUtil.getEntityManager();
        List<Item> list = em.createQuery(
                        "SELECT i FROM Item i WHERE i.category.id = :cid ORDER BY i.id", Item.class)
                .setParameter("cid", categoryId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
        em.close();
        return list;
    }
}
