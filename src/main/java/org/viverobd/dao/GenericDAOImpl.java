package org.viverobd.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.*;

public class GenericDAOImpl<T, ID> implements GenericDAO<T, ID> {
    protected final EntityManager em;
    protected final Class<T> entityClass;

    public GenericDAOImpl(EntityManager em, Class<T> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    @Override
    public void create(T entity) {
        em.persist(entity);
    }

    @Override
    public void update(T entity) {
        em.merge(entity);
    }

    @Override
    public void delete(T entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    @Override
    public List<T> readByAttributes(Map<String, Object> attributes) {
        StringBuilder queryString = new StringBuilder("SELECT e FROM ")
                .append(entityClass.getSimpleName())
                .append(" e WHERE 1=1");

        // Construcción dinámica de la cláusula WHERE
        for (String key : attributes.keySet()) {
            queryString.append(" AND e.").append(key).append(" = :").append(key.replace(".", "_"));
        }

        TypedQuery<T> query = em.createQuery(queryString.toString(), entityClass);

        // Asignación de parámetros a la consulta
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            query.setParameter(entry.getKey().replace(".", "_"), entry.getValue());
        }

        return query.getResultList();
    }
}
