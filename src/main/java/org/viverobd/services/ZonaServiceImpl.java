package org.viverobd.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.viverobd.dao.ZonaDAO;
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
    public boolean agregarZona(Zona zona) {
        EntityManager em = emf.createEntityManager();
        ZonaDAO zonaDAO = new ZonaDAO(em, Zona.class);
        try {
            if (!datosValidos(zona) || zona.getZona_viv() == null) {
                zonaDAO.showOperationStatus(null, false, "Los datos de la zona no son válidos.");
                return false;
            }
            if (em.find(Zona.class, zona.getZona_nombre()) != null) {
                zonaDAO.showOperationStatus(null, false, "La zona ya existe en la base de datos.");
                return false;
            }
            Vivero vivero = em.find(Vivero.class, zona.getZona_viv().getViv_telefono());
            if (vivero == null) {
                zonaDAO.showOperationStatus(null, false, "El vivero seleccionado no existe.");
                return false;
            }
            zona.formZona_viv(vivero);
            em.getTransaction().begin();
            em.persist(zona);
            em.getTransaction().commit();
            return em.find(Zona.class, zona.getZona_nombre()) != null;
        } catch (RuntimeException e) {
            rollback(em);
            zonaDAO.showOperationStatus(null, false, "No se pudo agregar la zona: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean modificarZona(Zona zona) {
        EntityManager em = emf.createEntityManager();
        ZonaDAO zonaDAO = new ZonaDAO(em, Zona.class);
        try {
            if (!datosValidos(zona) || zona.getZona_viv() == null) {
                zonaDAO.showOperationStatus(null, false, "Los datos de la zona no son válidos.");
                return false;
            }
            Zona managed = em.find(Zona.class, zona.getZona_nombre());
            if (managed == null) {
                zonaDAO.showOperationStatus(null, false, "La zona no existe en la base de datos.");
                return false;
            }
            Vivero vivero = em.find(Vivero.class, zona.getZona_viv().getViv_telefono());
            if (vivero == null) {
                zonaDAO.showOperationStatus(null, false, "El vivero seleccionado no existe.");
                return false;
            }
            em.getTransaction().begin();
            managed.setZona_superficie(zona.getZona_superficie());
            managed.setZona_tipo(zona.getZona_tipo());
            managed.formZona_viv(vivero);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            rollback(em);
            zonaDAO.showOperationStatus(null, false, "No se pudo modificar la zona: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean eliminarZona(Zona zona) {
        EntityManager em = emf.createEntityManager();
        ZonaDAO zonaDAO = new ZonaDAO(em, Zona.class);
        try {
            if (zona == null || zona.getZona_nombre() == null || zona.getZona_nombre().trim().isEmpty()) {
                zonaDAO.showOperationStatus(null, false, "El nombre de la zona es obligatorio.");
                return false;
            }
            Zona managedZona = em.find(Zona.class, zona.getZona_nombre());
            if (managedZona == null) {
                zonaDAO.showOperationStatus(null, false, "La zona no existe en la base de datos.");
                return false;
            }
            long dependencias = em.createQuery(
                            "SELECT COUNT(z) FROM ZonaEmpleado z WHERE z.zonae_zona = :zona", Long.class)
                    .setParameter("zona", managedZona).getSingleResult()
                    + em.createQuery("SELECT COUNT(z) FROM ZonaPlanta z WHERE z.zonap_zona = :zona", Long.class)
                    .setParameter("zona", managedZona).getSingleResult()
                    + em.createQuery("SELECT COUNT(s) FROM Stock s WHERE s.stock_zona = :zona", Long.class)
                    .setParameter("zona", managedZona).getSingleResult();
            if (dependencias > 0) {
                zonaDAO.showOperationStatus(null, false, "No se puede eliminar una zona con relaciones de empleados, plantas o stock.");
                return false;
            }
            em.getTransaction().begin();
            em.remove(managedZona);
            em.getTransaction().commit();
            return em.find(Zona.class, zona.getZona_nombre()) == null;
        } catch (RuntimeException e) {
            rollback(em);
            zonaDAO.showOperationStatus(null, false, "No se pudo eliminar la zona: " + e.getMessage());
            return false;
        }
        finally {
            em.close();
        }
    }

    private boolean datosValidos(Zona zona) {
        return zona != null && zona.getZona_nombre() != null
                && !zona.getZona_nombre().trim().isEmpty()
                && Float.isFinite(zona.getZona_superficie())
                && zona.getZona_superficie() > 0
                && zona.getZona_tipo() != null;
    }

    private void rollback(EntityManager em) {
        if (em.getTransaction().isActive()) em.getTransaction().rollback();
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
            if (atributos == null || atributos.isEmpty()) {
                return em.createQuery("SELECT z FROM Zona z", Zona.class).getResultList();
            }
            if (atributos.containsKey("zona_superficie")) {
                return em.createQuery("SELECT z FROM Zona z WHERE z.zona_superficie = :superficie", Zona.class)
                        .setParameter("superficie", atributos.get("zona_superficie")).getResultList();
            }
            if (atributos.containsKey("zona_tipo")) {
                return em.createQuery("SELECT z FROM Zona z WHERE z.zona_tipo = :tipo", Zona.class)
                        .setParameter("tipo", atributos.get("zona_tipo")).getResultList();
            }
            if (atributos.containsKey("zona_nombre")) {
                return em.createQuery("SELECT z FROM Zona z WHERE LOWER(z.zona_nombre) LIKE :nombre", Zona.class)
                        .setParameter("nombre", "%" + atributos.get("zona_nombre").toString().toLowerCase() + "%")
                        .getResultList();
            }
            return List.of();
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
