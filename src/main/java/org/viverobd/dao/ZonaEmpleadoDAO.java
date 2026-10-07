package org.viverobd.dao;

import jakarta.persistence.EntityManager;
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
            zeAnterior.formZonae_emp(zeActualizado.getZonae_emp());
            zeAnterior.formZonae_zona(zeActualizado.getZonae_zona());

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
