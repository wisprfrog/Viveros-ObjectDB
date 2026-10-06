package org.viverobd.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.viverobd.dao.ZonaEmpleadoDAO;
import org.viverobd.models.ZonaEmpleado;
import java.util.List;
import java.util.Map;

public class ZonaEmpleadoServiceImpl implements ZonaEmpleadoService {
    private final EntityManagerFactory emf;

    public ZonaEmpleadoServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void agregarZonaEmpleado(ZonaEmpleado zonaEmpleado) {
        EntityManager em = emf.createEntityManager();
        try {
            ZonaEmpleadoDAO dao = new ZonaEmpleadoDAO(em, ZonaEmpleado.class);
            dao.create(zonaEmpleado);
        } finally {
            em.close();
        }
    }

    @Override
    public void modificarZonaEmpleado(ZonaEmpleado zonaEmpleado) {
        EntityManager em = emf.createEntityManager();
        try {
            ZonaEmpleadoDAO dao = new ZonaEmpleadoDAO(em, ZonaEmpleado.class);
            dao.update(zonaEmpleado);
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminarZonaEmpleado(ZonaEmpleado zonaEmpleado) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            ZonaEmpleado managedZE = em.find(ZonaEmpleado.class, zonaEmpleado.getZonae_codigo());
            if (managedZE != null) {
                if (managedZE.getZonae_emp() != null) {
                    managedZE.getZonae_emp().getEmp_zonae().remove(managedZE);
                }
                if (managedZE.getZonae_zona() != null) {
                    managedZE.getZonae_zona().getZona_zonae().remove(managedZE);
                }
                em.remove(managedZE);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public ZonaEmpleado buscarPorId(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(ZonaEmpleado.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<ZonaEmpleado> buscarPorAtributos(Map<String, Object> atributos) {
        EntityManager em = emf.createEntityManager();
        try {
            ZonaEmpleadoDAO dao = new ZonaEmpleadoDAO(em, ZonaEmpleado.class);
            return dao.readByAttributes(atributos);
        } finally {
            em.close();
        }
    }

    @Override
    public List<ZonaEmpleado> buscarTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT ze FROM ZonaEmpleado ze", ZonaEmpleado.class).getResultList();
        } finally {
            em.close();
        }
    }
}
