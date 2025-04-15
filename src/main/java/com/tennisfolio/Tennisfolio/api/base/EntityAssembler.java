package com.tennisfolio.Tennisfolio.api.base;

public interface EntityAssembler<T,E>{
    E assemble(T dto);
}
