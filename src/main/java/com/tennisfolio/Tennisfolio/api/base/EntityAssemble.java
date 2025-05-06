package com.tennisfolio.Tennisfolio.api.base;

public interface EntityAssemble<T,E>{
    E assemble(T dto);
}
