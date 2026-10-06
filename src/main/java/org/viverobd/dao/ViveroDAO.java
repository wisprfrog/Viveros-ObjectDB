package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import org.viverobd.models.Vivero;

public class ViveroDAO extends GenericDAOImpl<Vivero> {
    public ViveroDAO(EntityManager em, Class<Vivero> entityClass) {
        super(em, entityClass);
    }

    @Override
    public void update(Vivero vivActualizado) {
        try {
            Vivero vivAnterior = em.find(Vivero.class, vivActualizado.getViv_telefono());
            if (vivAnterior == null) {
                showOperationStatus(TipoOperacion.ACTUALIZAR, false, "No existe el objeto");
                return;
            }

            em.getTransaction().begin();

            vivAnterior.setViv_nombre(vivActualizado.getViv_nombre());
            vivAnterior.setViv_direccion(vivActualizado.getViv_direccion());
            vivAnterior.formViv_emp(vivActualizado.getViv_emp());

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
