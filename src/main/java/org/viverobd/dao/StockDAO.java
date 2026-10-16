package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import org.viverobd.models.Producto;
import org.viverobd.models.Stock;
import org.viverobd.models.Zona;

public class StockDAO extends GenericDAOImpl<Stock> {
    public StockDAO(EntityManager em, Class<Stock> entityClass) {
        super(em, entityClass);
    }

    @Override
    public void update(Stock stockActualizado) {
        try {
            Stock stockAnterior = em.find(Stock.class, stockActualizado.getStock_id());
            if (stockAnterior == null) {
                showOperationStatus(TipoOperacion.ACTUALIZAR, false, "No existe el objeto");
                return;
            }

            em.getTransaction().begin();

            stockAnterior.setStock_cantidad(stockActualizado.getStock_cantidad());
            if (stockActualizado.getStock_zona() != null) {
                Zona zona = em.find(Zona.class, stockActualizado.getStock_zona().getZona_nombre());
                if (zona == null) {
                    em.getTransaction().rollback();
                    showOperationStatus(TipoOperacion.ACTUALIZAR, false, "La zona no existe.");
                    return;
                }
                stockAnterior.formStock_zona(zona);
            }
            if (stockActualizado.getStock_producto() != null) {
                Producto producto = em.find(Producto.class, stockActualizado.getStock_producto().getProd_nombre());
                if (producto == null) {
                    em.getTransaction().rollback();
                    showOperationStatus(TipoOperacion.ACTUALIZAR, false, "El producto no existe.");
                    return;
                }
                stockAnterior.formStock_producto(producto);
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
