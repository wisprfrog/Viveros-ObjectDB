package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import javafx.scene.control.Alert;

import java.util.*;

public class GenericDAOImpl<T> implements GenericDAO<T> {
    protected final EntityManager em;
    protected final Class<T> entityClass;

    public GenericDAOImpl(EntityManager em, Class<T> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    @Override
    public void create(T entity) {
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();

        showOperationStatus(TipoOperacion.CREAR, true);
    }

    public void update(T entity) {}

    @Override
    public void delete(T entity) {
        em.getTransaction().begin();
        em.remove(em.contains(entity) ? entity : em.merge(entity));
        em.getTransaction().commit();

        showOperationStatus(TipoOperacion.ELIMINAR, !em.contains(entity)); //Si la bd contiene la entidad, entonces no se borro
    }

    @Override
    public List<T> readByAttributes(Map<String, Object> attributes) {
        StringBuilder queryString = new StringBuilder("SELECT e FROM ")
                .append(entityClass.getSimpleName())
                .append(" e WHERE 1=1");

        // Construcción dinámica de la cláusula WHERE
        for (String key : attributes.keySet()) {
            queryString.append(" AND e.").append(key).append(" LIKE :").append(key.replace(".", "_"));
        }

        TypedQuery<T> query = em.createQuery(queryString.toString(), entityClass);

        // Asignación de parámetros a la consulta
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                value = "%" + value + "%";
            }
            query.setParameter(entry.getKey().replace(".", "_"), value);
        }

        return query.getResultList();
    }

    @Override
    public List<T> readByNumberRange(String attribute, Double min, Double max) {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + attribute + " BETWEEN :min AND :max";

        TypedQuery<T> query = em.createQuery(jpql, entityClass);
        query.setParameter("min", min);
        query.setParameter("max", max);

        return query.getResultList();
    }

    @Override
    public void showOperationStatus(TipoOperacion operacion, boolean success){
            Alert alert;
        if(success){
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Informacion sobre operacion");
            alert.setHeaderText("Operacion realizada con exito");
            alert.setContentText("Se ha " + operacion.getOperacion() + " correctamente el objeto " + entityClass.getSimpleName());
            alert.showAndWait();
        }
        else{
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Informacion sobre operacion");
            alert.setHeaderText("Operacion NO realizada");
            alert.setContentText("No se ha " + operacion.getOperacion() + " el objeto " + entityClass.getSimpleName() + ", intentelo de nuevo");
            alert.showAndWait();
        }
    }
}
