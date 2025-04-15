package com.tennisfolio.Tennisfolio.api.base;

public interface ResponseParser<T> {
    T parse(String response);
}
