package com.tennisfolio.Tennisfolio.api.base;

public interface Mapper<T,E>{
    E map(T dto, Object... params);
}
