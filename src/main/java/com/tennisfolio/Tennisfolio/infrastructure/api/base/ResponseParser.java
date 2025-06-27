package com.tennisfolio.Tennisfolio.infrastructure.api.base;

public interface ResponseParser<T> {
    T parse(String response);
}
