package org.viverobd.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
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
    public boolean agregarVivero(Vivero vivero) {
        EntityManager em = emf.createEntityManager();
        ViveroDAO viveroDAO = new ViveroDAO(em, Vivero.class);
        try {
            if (vivero == null || !datosValidos(vivero)) {
                viveroDAO.showOperationStatus(null, false, "Los datos del vivero no son válidos.");
                return false;
            }
            if (em.find(Vivero.class, vivero.getViv_telefono()) != null) {
                viveroDAO.showOperationStatus(null, false, "Ya existe un vivero con ese teléfono.");
                return false;
            }
            em.getTransaction().begin();
            em.persist(vivero);
            em.getTransaction().commit();
            return em.find(Vivero.class, vivero.getViv_telefono()) != null;
        } catch (RuntimeException e) {
            rollback(em);
            viveroDAO.showOperationStatus(null, false, "No se pudo agregar el vivero: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean modificarVivero(Vivero vivero) {
        EntityManager em = emf.createEntityManager();
        ViveroDAO viveroDAO = new ViveroDAO(em, Vivero.class);
        try {
            if (vivero == null || !datosValidos(vivero)) {
                viveroDAO.showOperationStatus(null, false, "Los datos del vivero no son válidos.");
                return false;
            }
            Vivero managed = em.find(Vivero.class, vivero.getViv_telefono());
            if (managed == null) {
                viveroDAO.showOperationStatus(null, false, "El vivero no existe en la base de datos.");
                return false;
            }
            em.getTransaction().begin();
            managed.setViv_nombre(vivero.getViv_nombre());
            managed.setViv_direccion(vivero.getViv_direccion());
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            rollback(em);
            viveroDAO.showOperationStatus(null, false, "No se pudo modificar el vivero: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean eliminarVivero(Vivero vivero) {
        EntityManager em = emf.createEntityManager();
        ViveroDAO viveroDAO = new ViveroDAO(em, Vivero.class);
        try {
            if (vivero == null || vivero.getViv_telefono() == null
                    || vivero.getViv_telefono().trim().isEmpty()) {
                viveroDAO.showOperationStatus(null, false, "El teléfono del vivero es obligatorio.");
                return false;
            }
            Vivero managedVivero = em.find(Vivero.class, vivero.getViv_telefono());
            if (managedVivero == null) {
                viveroDAO.showOperationStatus(null, false, "El vivero no existe en la base de datos.");
                return false;
            }
            long zonas = em.createQuery(
                            "SELECT COUNT(z) FROM Zona z WHERE z.zona_viv = :vivero", Long.class)
                    .setParameter("vivero", managedVivero)
                    .getSingleResult();
            if (zonas > 0) {
                viveroDAO.showOperationStatus(null, false, "No se puede eliminar un vivero que tiene zonas asociadas.");
                return false;
            }
            long empleados = em.createQuery(
                            "SELECT COUNT(e) FROM Empleado e WHERE e.emp_viv = :vivero", Long.class)
                    .setParameter("vivero", managedVivero)
                    .getSingleResult();
            if (empleados > 0) {
                viveroDAO.showOperationStatus(null, false, "No se puede eliminar un vivero que tiene un empleado asociado.");
                return false;
            }
            em.getTransaction().begin();
            em.remove(managedVivero);
            em.getTransaction().commit();
            return em.find(Vivero.class, vivero.getViv_telefono()) == null;
        } catch (RuntimeException e) {
            rollback(em);
            viveroDAO.showOperationStatus(null, false, "No se pudo eliminar el vivero: " + e.getMessage());
            return false;
        }
        finally {
            em.close();
        }
    }

    private boolean datosValidos(Vivero vivero) {
        return vivero.getViv_telefono() != null
                && vivero.getViv_telefono().matches("\\d+")
                && vivero.getViv_nombre() != null
                && !vivero.getViv_nombre().trim().isEmpty()
                && vivero.getViv_direccion() != null
                && !vivero.getViv_direccion().trim().isEmpty();
    }

    private void rollback(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
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
