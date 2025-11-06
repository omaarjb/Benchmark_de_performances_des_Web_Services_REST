package com.omar.jersey;

import com.omar.entities.Category;
import com.omar.entities.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.Persistence;

import java.math.BigDecimal;
import java.util.Random;

public class DataSeeder {

    private static final int CATEGORY_COUNT = 2000;
    private static final int ITEMS_PER_CATEGORY = 50;
    private static final int BATCH_SIZE = 5000;

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("benchmark");
        EntityManager em = emf.createEntityManager();
        em.setFlushMode(FlushModeType.COMMIT);
        EntityTransaction tx = em.getTransaction();

        Random random = new Random();
        String lorem = generateLorem(5120);

        try {
            tx.begin();
            System.out.println("➡ Insertion des catégories...");
            for (int i = 1; i <= CATEGORY_COUNT; i++) {
                Category cat = new Category();
                cat.setCode(String.format("CAT%04d", i));
                cat.setName("Catégorie " + i);
                em.persist(cat);

                if (i % 100 == 0) {
                    System.out.println("   → " + i + " catégories insérées...");
                }

                if (i % 500 == 0) {
                    em.flush();
                    em.clear();
                }
            }
            tx.commit();
            em.clear();

            tx.begin();
            System.out.println("➡ Insertion des items...");
            long itemCounter = 0;

            for (long cid = 1; cid <= CATEGORY_COUNT; cid++) {
                Category cat = em.getReference(Category.class, cid);

                for (int j = 1; j <= ITEMS_PER_CATEGORY; j++) {
                    Item item = new Item();
                    item.setSku(String.format("ITEM%06d", ++itemCounter));
                    item.setName("Produit " + itemCounter);
                    item.setPrice(BigDecimal.valueOf(10 + random.nextDouble() * 990));
                    item.setStock(random.nextInt(100) + 1);
                    item.setDescription(lorem);
                    item.setCategory(cat);

                    em.persist(item);

                    if (itemCounter % BATCH_SIZE == 0) {
                        System.out.println("   → " + itemCounter + " items insérés...");
                        em.flush();
                        em.clear();

                        cat = em.getReference(Category.class, cid);
                    }
                }

                if (cid % 100 == 0) {
                    em.flush();
                    em.clear();
                }
            }

            tx.commit();
            System.out.println("Jeu de données inséré avec succès !");
            System.out.println("   - Catégories : " + CATEGORY_COUNT);
            System.out.println("   - Items : " + (CATEGORY_COUNT * ITEMS_PER_CATEGORY));

        } catch (Exception e) {
            e.printStackTrace();
            if (tx.isActive()) tx.rollback();
        } finally {
            em.close();
            emf.close();
        }
    }

    private static String generateLorem(int targetSize) {
        String base = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ";
        StringBuilder sb = new StringBuilder(targetSize);
        while (sb.length() < targetSize) {
            sb.append(base);
        }
        return sb.substring(0, targetSize);
    }
}
