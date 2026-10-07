package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import org.viverobd.models.ZonaPlanta;

public class ZonaPlantaDAO extends GenericDAOImpl<ZonaPlanta> {
    public ZonaPlantaDAO(EntityManager em, Class<ZonaPlanta> entityClass) {
        super(em, entityClass);
    }

    @Override
    public void update(ZonaPlanta zpActualizada) {
        try {
            ZonaPlanta zpAnterior = em.find(ZonaPlanta.class, zpActualizada.getZonap_id());
            if (zpAnterior == null) {
                showOperationStatus(TipoOperacion.ACTUALIZAR, false, "No existe el objeto");
                return;
            }

            em.getTransaction().begin();

            zpAnterior.setZonap_temperatura(zpActualizada.getZonap_temperatura());
            zpAnterior.setZonap_humedad(zpActualizada.getZonap_humedad());
            zpAnterior.setZonap_fechaRegistro(zpActualizada.getZonap_fechaRegistro());
            zpAnterior.setZonap_horaRegistro(zpActualizada.getZonap_horaRegistro());
            zpAnterior.formZonap_zona(zpActualizada.getZonap_zona());
            zpAnterior.formZonap_planta(zpActualizada.getZonap_planta());

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
