package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import org.viverobd.models.Zona;

public class ZonaDAO extends GenericDAOImpl<Zona> {
    public ZonaDAO(EntityManager em, Class<Zona> entityClass) {
        super(em, entityClass);
    }

    @Override
    public void update(Zona zonaActualizada) {
        try {
            Zona zonaAnterior = em.find(Zona.class, zonaActualizada.getZona_nombre());
            if (zonaAnterior == null) {
                showOperationStatus(TipoOperacion.ACTUALIZAR, false, "No existe el objeto");
                return;
            }

            em.getTransaction().begin();

            zonaAnterior.setZona_superficie(zonaActualizada.getZona_superficie());
            zonaAnterior.setZona_tipo(zonaActualizada.getZona_tipo());
            zonaAnterior.formZona_viv(zonaActualizada.getZona_viv());

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
