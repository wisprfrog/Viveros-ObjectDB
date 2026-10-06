package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import org.viverobd.models.Stock;

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
            stockAnterior.formStock_zona(stockActualizado.getStock_zona());
            stockAnterior.formStock_producto(stockActualizado.getStock_producto());

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
