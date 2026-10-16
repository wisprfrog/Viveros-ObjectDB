package org.viverobd.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.viverobd.dao.ZonaEmpleadoDAO;
import org.viverobd.models.Empleado;
import org.viverobd.models.Zona;
import org.viverobd.models.ZonaEmpleado;
import java.util.List;
import java.util.Map;

public class ZonaEmpleadoServiceImpl implements ZonaEmpleadoService {
    private final EntityManagerFactory emf;

    public ZonaEmpleadoServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public boolean agregarZonaEmpleado(ZonaEmpleado zonaEmpleado) {
        EntityManager em = emf.createEntityManager();
        ZonaEmpleadoDAO dao = new ZonaEmpleadoDAO(em, ZonaEmpleado.class);
        try {
            if (!datosValidos(zonaEmpleado)) {
                dao.showOperationStatus(null, false, "Los datos de la asignación no son válidos.");
                return false;
            }
            if (em.find(ZonaEmpleado.class, zonaEmpleado.getZonae_codigo()) != null) {
                dao.showOperationStatus(null, false, "El código de asignación ya existe.");
                return false;
            }
            Empleado empleado = em.find(Empleado.class, zonaEmpleado.getZonae_emp().getEmp_telefono());
            Zona zona = em.find(Zona.class, zonaEmpleado.getZonae_zona().getZona_nombre());
            if (empleado == null || zona == null) {
                dao.showOperationStatus(null, false, "El empleado o la zona no existen.");
                return false;
            }
            if (existeRelacion(em, empleado, zona, null)) {
                dao.showOperationStatus(null, false, "El empleado ya está asignado a esta zona.");
                return false;
            }
            zonaEmpleado.formZonae_emp(empleado);
            zonaEmpleado.formZonae_zona(zona);
            em.getTransaction().begin();
            em.persist(zonaEmpleado);
            em.getTransaction().commit();
            return em.find(ZonaEmpleado.class, zonaEmpleado.getZonae_codigo()) != null;
        } catch (RuntimeException e) {
            rollback(em);
            dao.showOperationStatus(null, false, "No se pudo agregar la asignación: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean modificarZonaEmpleado(ZonaEmpleado zonaEmpleado) {
        EntityManager em = emf.createEntityManager();
        ZonaEmpleadoDAO dao = new ZonaEmpleadoDAO(em, ZonaEmpleado.class);
        try {
            if (!datosValidos(zonaEmpleado)) {
                dao.showOperationStatus(null, false, "Los datos de la asignación no son válidos.");
                return false;
            }
            ZonaEmpleado managed = em.find(ZonaEmpleado.class, zonaEmpleado.getZonae_codigo());
            if (managed == null) {
                dao.showOperationStatus(null, false, "La asignación no existe en la base de datos.");
                return false;
            }
            Empleado empleado = em.find(Empleado.class, zonaEmpleado.getZonae_emp().getEmp_telefono());
            Zona zona = em.find(Zona.class, zonaEmpleado.getZonae_zona().getZona_nombre());
            if (empleado == null || zona == null || existeRelacion(em, empleado, zona, zonaEmpleado.getZonae_codigo())) {
                dao.showOperationStatus(null, false, "El empleado o la zona no existen, o la relación ya está registrada.");
                return false;
            }
            em.getTransaction().begin();
            managed.setZonae_fecha_asignacion(zonaEmpleado.getZonae_fecha_asignacion());
            managed.setZonae_fecha_salida(zonaEmpleado.getZonae_fecha_salida());
            managed.setZonae_hora_asignacion(zonaEmpleado.getZonae_hora_asignacion());
            managed.formZonae_emp(empleado);
            managed.formZonae_zona(zona);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            rollback(em);
            dao.showOperationStatus(null, false, "No se pudo modificar la asignación: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean eliminarZonaEmpleado(ZonaEmpleado zonaEmpleado) {
        EntityManager em = emf.createEntityManager();
        ZonaEmpleadoDAO dao = new ZonaEmpleadoDAO(em, ZonaEmpleado.class);
        try {
            if (zonaEmpleado == null || zonaEmpleado.getZonae_codigo() == null
                    || zonaEmpleado.getZonae_codigo().trim().isEmpty()) {
                dao.showOperationStatus(null, false, "El código de asignación es obligatorio.");
                return false;
            }
            ZonaEmpleado managedZE = em.find(ZonaEmpleado.class, zonaEmpleado.getZonae_codigo());
            if (managedZE == null) {
                dao.showOperationStatus(null, false, "La asignación no existe en la base de datos.");
                return false;
            }
            em.getTransaction().begin();
            em.remove(managedZE);
            em.getTransaction().commit();
            return em.find(ZonaEmpleado.class, zonaEmpleado.getZonae_codigo()) == null;
        } catch (Exception e) {
            rollback(em);
            dao.showOperationStatus(null, false, "No se pudo eliminar la asignación: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    private boolean datosValidos(ZonaEmpleado ze) {
        return ze != null && ze.getZonae_codigo() != null && !ze.getZonae_codigo().trim().isEmpty()
                && ze.getZonae_emp() != null && ze.getZonae_zona() != null
                && ze.getZonae_fecha_asignacion() != null
                && ze.getZonae_hora_asignacion() != null
                && ze.getZonae_hora_asignacion().matches("([01]\\d|2[0-3]):[0-5]\\d")
                && (ze.getZonae_fecha_salida() == null
                    || !ze.getZonae_fecha_salida().before(ze.getZonae_fecha_asignacion()));
    }

    private boolean existeRelacion(EntityManager em, Empleado empleado, Zona zona, String codigoExcluir) {
        String jpql = "SELECT COUNT(ze) FROM ZonaEmpleado ze " +
                "WHERE ze.zonae_emp = :empleado AND ze.zonae_zona = :zona";
        var query = em.createQuery(jpql, Long.class)
                .setParameter("empleado", empleado).setParameter("zona", zona);
        if (codigoExcluir != null) {
            query = em.createQuery(jpql + " AND ze.zonae_codigo <> :codigo", Long.class)
                    .setParameter("empleado", empleado).setParameter("zona", zona)
                    .setParameter("codigo", codigoExcluir);
        }
        return query.getSingleResult() > 0;
    }

    private void rollback(EntityManager em) {
        if (em.getTransaction().isActive()) em.getTransaction().rollback();
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
            if (atributos == null || atributos.isEmpty()) {
                return em.createQuery("SELECT ze FROM ZonaEmpleado ze", ZonaEmpleado.class).getResultList();
            }
            if (atributos.containsKey("zonae_codigo")) {
                return em.createQuery("SELECT ze FROM ZonaEmpleado ze WHERE LOWER(ze.zonae_codigo) LIKE :codigo", ZonaEmpleado.class)
                        .setParameter("codigo", "%" + atributos.get("zonae_codigo").toString().toLowerCase() + "%").getResultList();
            }
            if (atributos.containsKey("zonae_zona.zona_nombre")) {
                return em.createQuery("SELECT ze FROM ZonaEmpleado ze WHERE LOWER(ze.zonae_zona.zona_nombre) LIKE :nombre", ZonaEmpleado.class)
                        .setParameter("nombre", "%" + atributos.get("zonae_zona.zona_nombre").toString().toLowerCase() + "%").getResultList();
            }
            if (atributos.containsKey("zonae_emp.emp_telefono")) {
                return em.createQuery("SELECT ze FROM ZonaEmpleado ze WHERE LOWER(ze.zonae_emp.emp_telefono) LIKE :telefono", ZonaEmpleado.class)
                        .setParameter("telefono", "%" + atributos.get("zonae_emp.emp_telefono").toString().toLowerCase() + "%").getResultList();
            }
            return List.of();
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
