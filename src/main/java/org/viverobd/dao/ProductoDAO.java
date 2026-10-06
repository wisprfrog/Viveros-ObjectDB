package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import org.viverobd.models.Planta;
import org.viverobd.models.Producto;

import javax.swing.*;

public class ProductoDAO extends GenericDAOImpl<Producto>{
    public ProductoDAO(EntityManager em, Class<Producto> entityClass) {
        super(em, entityClass);
    }

    @Override
    public void update(Producto prodActualizado){
        try{
            Producto prodAnterior = em.find(Producto.class, prodActualizado.getProd_nombre()); //Buscamos la entidad por su Id
            if(prodAnterior == null){
                showOperationStatus(TipoOperacion.ACTUALIZAR, false, "No existe el objeto");
                return;
            }

            em.getTransaction().begin();

            prodAnterior.setPro_descripcion(prodActualizado.getPro_descripcion());
            prodAnterior.setPro_precio(prodActualizado.getPro_precio());
            prodAnterior.setPro_tipo(prodActualizado.getPro_tipo());
            
            // Si es planta, JPA se encarga de la actualización de la planta relacionada si se modifica
            if (prodActualizado.getPro_planta() != null) {
                Planta plantaActualizada = prodActualizado.getPro_planta();
                Planta plantaAnterior = prodAnterior.getPro_planta();
                if (plantaAnterior != null) {
                    plantaAnterior.setPla_nombre(plantaActualizada.getPla_nombre());
                    plantaAnterior.setPla_clima(plantaActualizada.getPla_clima());
                    plantaAnterior.setPla_humedad(plantaActualizada.getPla_humedad());
                    plantaAnterior.setPla_luz(plantaActualizada.getPla_luz());
                    plantaAnterior.setPla_cuidados(plantaActualizada.getPla_cuidados());
                    plantaAnterior.setPla_tipo(plantaActualizada.getPla_tipo());
                } else {
                    prodAnterior.formPro_planta(plantaActualizada);
                    plantaActualizada.formPla_prod(prodAnterior);
                }
            }

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
