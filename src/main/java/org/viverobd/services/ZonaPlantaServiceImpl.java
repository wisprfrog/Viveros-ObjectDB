package org.viverobd.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.viverobd.dao.ZonaPlantaDAO;
import org.viverobd.models.ZonaPlanta;
import org.viverobd.models.Planta;
import org.viverobd.models.Zona;

import java.util.List;
import java.util.Map;

public class ZonaPlantaServiceImpl implements ZonaPlantaService {
    private final EntityManagerFactory emf;

    public ZonaPlantaServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public boolean registrarZonaPlanta(ZonaPlanta zp) {
        EntityManager em = emf.createEntityManager();
        ZonaPlantaDAO dao = new ZonaPlantaDAO(em, ZonaPlanta.class);
        try {
            if (!datosValidos(zp)) {
                dao.showOperationStatus(null, false, "Los datos del registro zona-planta no son válidos.");
                return false;
            }
            Zona zona = em.find(Zona.class, zp.getZonap_zona().getZona_nombre());
            Planta planta = buscarPlanta(em, zp.getZonap_planta().getPla_nombre());
            if (zona == null || planta == null) {
                dao.showOperationStatus(null, false, "La zona o la planta no existen.");
                return false;
            }
            zp.formZonap_zona(zona);
            zp.formZonap_planta(planta);
            if (zp.getZonap_id() <= 0) zp.setZonap_id(siguienteId(em));
            while (em.find(ZonaPlanta.class, zp.getZonap_id()) != null) {
                zp.setZonap_id(siguienteId(em));
            }
            em.getTransaction().begin();
            em.persist(zp);
            em.getTransaction().commit();
            return em.find(ZonaPlanta.class, zp.getZonap_id()) != null;
        } catch (RuntimeException e) {
            rollback(em);
            dao.showOperationStatus(null, false, "No se pudo registrar el dato: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean actualizarZonaPlanta(ZonaPlanta zp) {
        EntityManager em = emf.createEntityManager();
        ZonaPlantaDAO dao = new ZonaPlantaDAO(em, ZonaPlanta.class);
        try {
            if (!datosValidos(zp) || zp.getZonap_id() <= 0) {
                dao.showOperationStatus(null, false, "Los datos del registro zona-planta no son válidos.");
                return false;
            }
            ZonaPlanta managed = em.find(ZonaPlanta.class, zp.getZonap_id());
            if (managed == null) {
                dao.showOperationStatus(null, false, "El registro no existe en la base de datos.");
                return false;
            }
            Zona zona = em.find(Zona.class, zp.getZonap_zona().getZona_nombre());
            Planta planta = buscarPlanta(em, zp.getZonap_planta().getPla_nombre());
            if (zona == null || planta == null) {
                dao.showOperationStatus(null, false, "La zona o la planta no existen.");
                return false;
            }
            em.getTransaction().begin();
            managed.setZonap_temperatura(zp.getZonap_temperatura());
            managed.setZonap_humedad(zp.getZonap_humedad());
            managed.setZonap_fechaRegistro(zp.getZonap_fechaRegistro());
            managed.setZonap_horaRegistro(zp.getZonap_horaRegistro());
            managed.formZonap_zona(zona);
            managed.formZonap_planta(planta);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            rollback(em);
            dao.showOperationStatus(null, false, "No se pudo actualizar el dato: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean eliminarZonaPlanta(ZonaPlanta zp) {
        EntityManager em = emf.createEntityManager();
        ZonaPlantaDAO dao = new ZonaPlantaDAO(em, ZonaPlanta.class);
        try {
            if (zp == null || zp.getZonap_id() <= 0) {
                dao.showOperationStatus(null, false, "El registro seleccionado no es válido.");
                return false;
            }
            ZonaPlanta managed = em.find(ZonaPlanta.class, zp.getZonap_id());
            if (managed == null) {
                dao.showOperationStatus(null, false, "La relación zona-planta no existe en la base de datos.");
                return false;
            }
            em.getTransaction().begin();
            em.remove(managed);
            em.getTransaction().commit();
            return em.find(ZonaPlanta.class, zp.getZonap_id()) == null;
        } catch (RuntimeException e) {
            rollback(em);
            dao.showOperationStatus(null, false, "No se pudo eliminar el registro: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public List<ZonaPlanta> listarTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT zp FROM ZonaPlanta zp", ZonaPlanta.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<ZonaPlanta> buscarPorAtributos(Map<String, Object> atributos) {
        EntityManager em = emf.createEntityManager();
        try {
            if (atributos == null || atributos.isEmpty()) {
                return em.createQuery("SELECT zp FROM ZonaPlanta zp", ZonaPlanta.class).getResultList();
            }
            if (atributos.containsKey("zonap_zona.zona_nombre")) {
                return em.createQuery("SELECT zp FROM ZonaPlanta zp WHERE LOWER(zp.zonap_zona.zona_nombre) LIKE :valor", ZonaPlanta.class)
                        .setParameter("valor", "%" + atributos.get("zonap_zona.zona_nombre").toString().toLowerCase() + "%").getResultList();
            }
            if (atributos.containsKey("zonap_planta.pla_nombre")) {
                return em.createQuery("SELECT zp FROM ZonaPlanta zp WHERE LOWER(zp.zonap_planta.pla_nombre) LIKE :valor", ZonaPlanta.class)
                        .setParameter("valor", "%" + atributos.get("zonap_planta.pla_nombre").toString().toLowerCase() + "%").getResultList();
            }
            if (atributos.containsKey("zonap_temperatura")) {
                return em.createQuery("SELECT zp FROM ZonaPlanta zp WHERE zp.zonap_temperatura = :valor", ZonaPlanta.class)
                        .setParameter("valor", atributos.get("zonap_temperatura")).getResultList();
            }
            if (atributos.containsKey("zonap_humedad")) {
                return em.createQuery("SELECT zp FROM ZonaPlanta zp WHERE zp.zonap_humedad = :valor", ZonaPlanta.class)
                        .setParameter("valor", atributos.get("zonap_humedad")).getResultList();
            }
            return List.of();
        } finally {
            em.close();
        }
    }

    private Planta buscarPlanta(EntityManager em, String nombre) {
        return em.createQuery("SELECT p FROM Planta p WHERE p.pla_nombre = :nombre", Planta.class)
                .setParameter("nombre", nombre).getResultStream().findFirst().orElse(null);
    }

    private int siguienteId(EntityManager em) {
        Integer max = em.createQuery("SELECT MAX(zp.zonap_id) FROM ZonaPlanta zp", Integer.class).getSingleResult();
        return max == null ? 1 : max + 1;
    }

    private boolean datosValidos(ZonaPlanta zp) {
        return zp != null && zp.getZonap_zona() != null && zp.getZonap_planta() != null
                && Double.isFinite(zp.getZonap_temperatura())
                && Double.isFinite(zp.getZonap_humedad())
                && zp.getZonap_humedad() >= 0 && zp.getZonap_humedad() <= 100
                && zp.getZonap_fechaRegistro() != null && zp.getZonap_horaRegistro() != null;
    }

    private void rollback(EntityManager em) {
        if (em.getTransaction().isActive()) em.getTransaction().rollback();
    }
}
