package com.tennisfolio.Tennisfolio.api.base;

public interface SaveStrategy <E>{
    E save (E entity);
}
