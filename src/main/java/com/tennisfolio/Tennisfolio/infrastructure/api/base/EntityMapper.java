package com.tennisfolio.Tennisfolio.infrastructure.api.base;

public interface EntityMapper<T,E>{
    E map(T dto, Object... params);
}
