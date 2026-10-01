package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import org.viverobd.models.Producto;

public class ProductoDAO extends GenericDAOImpl<Producto>{
    public ProductoDAO(EntityManager em, Class<Producto> entityClass) {
        super(em, entityClass);
    }

    @Override
    public void update(Producto prodActualizado){
        em.getTransaction().begin();

        Producto prodAnterior = em.find(Producto.class, prodActualizado.getProd_nombre()); //Su id
        prodAnterior.setPro_descripcion(prodActualizado.getPro_descripcion());
        prodAnterior.setPro_precio(prodActualizado.getPro_precio());
        prodAnterior.setPro_tipo(prodActualizado.getPro_tipo());

        em.getTransaction().commit();

        super.showOperationStatus(TipoOperacion.ACTUALIZAR, true);
    }
}
