package org.viverobd.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.viverobd.dao.GenericDAO;
import org.viverobd.dao.GenericDAOImpl;
import org.viverobd.dao.ViveroDAO;
import org.viverobd.models.Vivero;
import java.util.List;
import java.util.Map;

public class ViveroServiceImpl implements ViveroService {
    private final EntityManagerFactory emf;

    public ViveroServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void agregarVivero(Vivero vivero) {
        EntityManager em = emf.createEntityManager();
        try {
            ViveroDAO viveroDAO = new ViveroDAO(em, Vivero.class);
            viveroDAO.create(vivero);
        } finally {
            em.close();
        }
    }

    @Override
    public void modificarVivero(Vivero vivero) {
        EntityManager em = emf.createEntityManager();
        try {
            ViveroDAO viveroDAO = new ViveroDAO(em, Vivero.class);
            viveroDAO.update(vivero);
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminarVivero(Vivero vivero) {
        EntityManager em = emf.createEntityManager();
        ViveroDAO viveroDAO = new ViveroDAO(em, Vivero.class);
        try {
            Vivero managedVivero = em.find(Vivero.class, vivero.getViv_telefono());
            if (managedVivero != null) {
                if (!managedVivero.getViv_zona().isEmpty()) {
                    throw new RuntimeException("No se puede eliminar un vivero que tiene zonas asociadas.");
                }
                // Si tuviera empleado asociado y no pudiera borrarse, se añadiría aquí la lógica
            }
            viveroDAO.delete(vivero);
        }
        catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            viveroDAO.showOperationStatus(GenericDAO.TipoOperacion.ELIMINAR, false, e.getMessage());
        }
        finally {
            em.close();
        }
    }

    @Override
    public Vivero buscarPorId(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Vivero.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Vivero> buscarPorAtributos(Map<String, Object> atributos) {
        EntityManager em = emf.createEntityManager();
        try {
            ViveroDAO viveroDAO = new ViveroDAO(em, Vivero.class);
            return viveroDAO.readByAttributes(atributos);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Vivero> buscarTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT v FROM Vivero v", Vivero.class).getResultList();
        } finally {
            em.close();
        }
    }
}
