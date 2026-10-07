package org.viverobd.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.viverobd.dao.GenericDAO;
import org.viverobd.dao.ZonaDAO;
import org.viverobd.dao.GenericDAOImpl;
import org.viverobd.models.Zona;
import org.viverobd.models.Vivero;
import java.util.List;
import java.util.Map;

public class ZonaServiceImpl implements ZonaService {
    private final EntityManagerFactory emf;

    public ZonaServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void agregarZona(Zona zona) {
        EntityManager em = emf.createEntityManager();
        try {
            ZonaDAO zonaDAO = new ZonaDAO(em, Zona.class);
            zonaDAO.create(zona);
        } finally {
            em.close();
        }
    }

    @Override
    public void modificarZona(Zona zona) {
        EntityManager em = emf.createEntityManager();
        try {
            ZonaDAO zonaDAO = new ZonaDAO(em, Zona.class);
            zonaDAO.update(zona);
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminarZona(Zona zona) {
        EntityManager em = emf.createEntityManager();
        ZonaDAO zonaDAO = new ZonaDAO(em, Zona.class);
        try {
            // Comprobamos restricciones de integridad
            Zona managedZona = em.find(Zona.class, zona.getZona_nombre());
            if (managedZona != null && !managedZona.getZona_stock().isEmpty()) {
                throw new RuntimeException("La zona tiene productos en stock y no puede ser eliminada.");
            }
            zonaDAO.delete(zona);
        }
        catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            zonaDAO.showOperationStatus(GenericDAO.TipoOperacion.ELIMINAR, false, e.getMessage());
        }
        finally {
            em.close();
        }
    }

    @Override
    public Zona buscarPorId(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Zona.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Zona> buscarPorAtributos(Map<String, Object> atributos) {
        EntityManager em = emf.createEntityManager();
        try {
            ZonaDAO zonaDAO = new ZonaDAO(em, Zona.class);
            return zonaDAO.readByAttributes(atributos);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Zona> buscarTodas() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT z FROM Zona z", Zona.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Vivero> obtenerTodosViveros() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT v FROM Vivero v", Vivero.class).getResultList();
        } finally {
            em.close();
        }
    }
}
