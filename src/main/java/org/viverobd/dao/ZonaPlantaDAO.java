package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import org.viverobd.models.Planta;
import org.viverobd.models.Zona;
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
            if (zpActualizada.getZonap_zona() != null) {
                Zona zona = em.find(Zona.class, zpActualizada.getZonap_zona().getZona_nombre());
                if (zona == null) {
                    throw new IllegalArgumentException("La zona no existe.");
                }
                zpAnterior.formZonap_zona(zona);
            }
            if (zpActualizada.getZonap_planta() != null) {
                Planta planta = em.createQuery(
                                "SELECT p FROM Planta p WHERE p.pla_nombre = :nombre", Planta.class)
                        .setParameter("nombre", zpActualizada.getZonap_planta().getPla_nombre())
                        .getResultStream().findFirst().orElse(null);
                if (planta == null) {
                    throw new IllegalArgumentException("La planta no existe.");
                }
                zpAnterior.formZonap_planta(planta);
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
