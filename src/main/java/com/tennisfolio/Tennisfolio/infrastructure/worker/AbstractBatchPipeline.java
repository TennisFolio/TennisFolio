package com.tennisfolio.Tennisfolio.infrastructure.worker;

import java.util.List;

public abstract class AbstractBatchPipeline<T> {

    public final void runBatch(List<T> batch) {
        if (batch == null || batch.isEmpty()) return;

        List<T> enriched = enrich(batch);

        save(enriched);

    }

    protected abstract List<T> enrich(List<T> batch);

    protected abstract void save(List<T> batchEntities);

}
