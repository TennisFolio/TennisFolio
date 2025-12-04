package com.tennisfolio.Tennisfolio.infrastructure.batchlog;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBatchLogEntity is a Querydsl query type for BatchLogEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBatchLogEntity extends EntityPathBase<BatchLogEntity> {

    private static final long serialVersionUID = 1770095443L;

    public static final QBatchLogEntity batchLogEntity = new QBatchLogEntity("batchLogEntity");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final StringPath batchName = createString("batchName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final NumberPath<Long> durationMs = createNumber("durationMs", Long.class);

    public final DateTimePath<java.time.LocalDateTime> endTime = createDateTime("endTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath message = createString("message");

    public final DateTimePath<java.time.LocalDateTime> startTime = createDateTime("startTime", java.time.LocalDateTime.class);

    public final StringPath status = createString("status");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public QBatchLogEntity(String variable) {
        super(BatchLogEntity.class, forVariable(variable));
    }

    public QBatchLogEntity(Path<? extends BatchLogEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBatchLogEntity(PathMetadata metadata) {
        super(BatchLogEntity.class, metadata);
    }

}

