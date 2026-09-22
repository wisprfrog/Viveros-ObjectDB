package org.viverobd;

import jakarta.persistence.*;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("./db/viverobd.odb");
        EntityManager em = emf.createEntityManager();

        if (em.isOpen()) {
            System.out.println("Conectado a la base de datos");
        }

        em.close();
        emf.close();
    }
}
