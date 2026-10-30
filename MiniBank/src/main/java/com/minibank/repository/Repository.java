package com.minibank.repository;

import com.minibank.model.annotation.Id;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Repository<T> {
    private final HashMap<String, T> data = new HashMap<>();

    public T save(T entity) {
        data.put(readId(entity), entity);
        return entity;
    }

    @SafeVarargs
    public final void saveAll(T... items) {
        for (T item : items) save(item);
    }

    public T findById(String id) { return data.get(id); }
    public ArrayList<T> findAll() { return new ArrayList<>(data.values()); }
    public boolean delete(String id) { return data.remove(id) != null; }
    public int size() { return data.size(); }

    private String readId(T entity) {
        if (entity == null) throw new IllegalArgumentException("Entity cannot be null.");
        Class<?> type = entity.getClass();
        while (type != null) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(entity);
                        if (value == null) throw new IllegalArgumentException("@Id field cannot be null.");
                        return value.toString();
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Cannot read @Id field.", e);
                    }
                }
            }
            type = type.getSuperclass();
        }
        throw new IllegalArgumentException("No field marked with @Id found.");
    }
}
