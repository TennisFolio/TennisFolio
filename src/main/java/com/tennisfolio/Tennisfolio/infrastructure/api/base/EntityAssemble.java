package com.tennisfolio.Tennisfolio.infrastructure.api.base;

public interface EntityAssemble<T,E>{
    E assemble(T dto, Object... params);
}
