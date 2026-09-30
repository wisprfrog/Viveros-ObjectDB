package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import org.viverobd.models.Planta;

public class PlantaDAO extends GenericDAOImpl<Planta>{
    public PlantaDAO(EntityManager em, Class<Planta> entityClass) {
        super(em, entityClass);
    }

    @Override
    public void update(Planta planActualizada){
        em.getTransaction().begin();

        Planta planAnterior = em.find(Planta.class, planActualizada.getProd_nombre()); //Su id
        planAnterior.setPla_nombre(planActualizada.getPla_nombre());
        planAnterior.setPla_clima(planActualizada.getPla_clima());
        planAnterior.setPla_humedad(planActualizada.getPla_humedad());
        planAnterior.setPla_luz(planActualizada.getPla_luz());
        planAnterior.setPla_cuidados(planActualizada.getPla_cuidados());
        planAnterior.setPla_tipo(planActualizada.getPla_tipo());

        em.getTransaction().commit();

        super.showOperationStatus(TipoOperacion.ACTUALIZAR, true);
    }
}
