package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import org.viverobd.models.Planta;

import javax.swing.*;

public class PlantaDAO extends GenericDAOImpl<Planta>{
    public PlantaDAO(EntityManager em, Class<Planta> entityClass) {
        super(em, entityClass);
    }

    @Override
    public void update(Planta planActualizada){
        try{
            Planta planAnterior = em.find(Planta.class, planActualizada.getProd_nombre()); //Buscamos la entidad por su Id
            if(planAnterior == null){
                showOperationStatus(TipoOperacion.ACTUALIZAR, false, "No existe el objeto");
                return;
            }

            em.getTransaction().begin();

            planAnterior.setPla_nombre(planActualizada.getPla_nombre());
            planAnterior.setPla_clima(planActualizada.getPla_clima());
            planAnterior.setPla_humedad(planActualizada.getPla_humedad());
            planAnterior.setPla_luz(planActualizada.getPla_luz());
            planAnterior.setPla_cuidados(planActualizada.getPla_cuidados());
            planAnterior.setPla_tipo(planActualizada.getPla_tipo());

            em.getTransaction().commit();
            showOperationStatus(TipoOperacion.ACTUALIZAR, true);
        }
        catch(Exception e){
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            showOperationStatus(TipoOperacion.ACTUALIZAR, false, e.getMessage());
        }
    }
}
