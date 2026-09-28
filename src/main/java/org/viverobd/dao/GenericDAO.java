package org.viverobd.dao;

import java.util.*;

public interface GenericDAO<T, ID> {
    void create(T entity);
    List<T> readByAttributes(Map<String, Object> attributes);
    void update(T entity);
    void delete(T entity);

}
