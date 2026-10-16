package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import org.viverobd.models.Empleado;
import org.viverobd.models.Zona;
import org.viverobd.models.ZonaEmpleado;

public class ZonaEmpleadoDAO extends GenericDAOImpl<ZonaEmpleado> {
    public ZonaEmpleadoDAO(EntityManager em, Class<ZonaEmpleado> entityClass) {
        super(em, entityClass);
    }

    @Override
    public void update(ZonaEmpleado zeActualizado) {
        try {
            ZonaEmpleado zeAnterior = em.find(ZonaEmpleado.class, zeActualizado.getZonae_codigo());
            if (zeAnterior == null) {
                showOperationStatus(TipoOperacion.ACTUALIZAR, false, "No existe el objeto");
                return;
            }

            em.getTransaction().begin();

            zeAnterior.setZonae_fecha_asignacion(zeActualizado.getZonae_fecha_asignacion());
            zeAnterior.setZonae_fecha_salida(zeActualizado.getZonae_fecha_salida());
            zeAnterior.setZonae_hora_asignacion(zeActualizado.getZonae_hora_asignacion());
            if (zeActualizado.getZonae_emp() != null) {
                zeAnterior.formZonae_emp(em.find(Empleado.class,
                        zeActualizado.getZonae_emp().getEmp_telefono()));
            }
            if (zeActualizado.getZonae_zona() != null) {
                zeAnterior.formZonae_zona(em.find(Zona.class,
                        zeActualizado.getZonae_zona().getZona_nombre()));
            }

            em.getTransaction().commit();
            showOperationStatus(TipoOperacion.ACTUALIZAR, true);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            showOperationStatus(TipoOperacion.ACTUALIZAR, false, e.getMessage());
        }
    }
}
