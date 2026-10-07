package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import org.viverobd.models.Empleado;

public class EmpleadoDAO extends GenericDAOImpl<Empleado> {
    public EmpleadoDAO(EntityManager em, Class<Empleado> entityClass) {
        super(em, entityClass);
    }

    @Override
    public void update(Empleado empActualizado) {
        try {
            Empleado empAnterior = em.find(Empleado.class, empActualizado.getEmp_telefono());
            if (empAnterior == null) {
                showOperationStatus(TipoOperacion.ACTUALIZAR, false, "No existe el objeto");
                return;
            }

            em.getTransaction().begin();

            empAnterior.setEmp_nombre(empActualizado.getEmp_nombre());
            empAnterior.setEmp_ine(empActualizado.getEmp_ine());
            empAnterior.formEmp_viv(empActualizado.getEmp_viv());

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
