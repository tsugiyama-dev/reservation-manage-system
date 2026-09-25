package com.example.demo;

import java.util.Optional;

public interface Repository<T, ID, R> {

	long insert(T entity);
	long update(ID entity);
	long delete(ID entity);
	Optional<R> findById(ID id);
}
