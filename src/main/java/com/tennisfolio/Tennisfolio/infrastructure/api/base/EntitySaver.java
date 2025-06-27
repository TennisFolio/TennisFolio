package com.tennisfolio.Tennisfolio.infrastructure.api.base;

public interface EntitySaver<E>{
    E save (E entity);
}
