package com.tennisfolio.Tennisfolio.infrastructure.worker;

import java.util.List;

public interface BatchSaver<T> {
    void saveBatch(List<T> items);
}
