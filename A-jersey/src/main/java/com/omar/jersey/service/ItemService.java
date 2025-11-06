package com.omar.jersey.service;

import com.omar.entities.Item;
import com.omar.jersey.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class ItemService {

    public List<Item> findAll(int page, int size) {
        EntityManager em = JpaUtil.getEntityManager();
        List<Item> items = em.createQuery("SELECT i FROM Item i ORDER BY i.id", Item.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
        em.close();
        return items;
    }

    public List<Item> findByCategory(Long categoryId, int page, int size) {
        EntityManager em = JpaUtil.getEntityManager();
        List<Item> items = em.createQuery(
                        "SELECT i FROM Item i WHERE i.category.id = :cid ORDER BY i.id", Item.class)
                .setParameter("cid", categoryId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
        em.close();
        return items;
    }

    public Item findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        Item item = em.find(Item.class, id);
        em.close();
        return item;
    }

    public Item create(Item item) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(item);
        tx.commit();
        em.close();
        return item;
    }

    public Item update(Long id, Item data) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Item item = em.find(Item.class, id);
        if (item != null) {
            item.setName(data.getName());
            item.setPrice(data.getPrice());
            item.setStock(data.getStock());
            item.setCategory(data.getCategory());
        }
        tx.commit();
        em.close();
        return item;
    }

    public boolean delete(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Item item = em.find(Item.class, id);
        if (item != null) em.remove(item);
        tx.commit();
        em.close();
        return item != null;
    }
}
