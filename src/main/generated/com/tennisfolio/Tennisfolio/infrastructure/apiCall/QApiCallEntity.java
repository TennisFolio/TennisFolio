package com.tennisfolio.Tennisfolio.infrastructure.apiCall;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QApiCallEntity is a Querydsl query type for ApiCallEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QApiCallEntity extends EntityPathBase<ApiCallEntity> {

    private static final long serialVersionUID = -983891321L;

    public static final QApiCallEntity apiCallEntity = new QApiCallEntity("apiCallEntity");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final NumberPath<Long> apiCallId = createNumber("apiCallId", Long.class);

    public final NumberPath<Long> apiCount = createNumber("apiCount", Long.class);

    public final StringPath apiDate = createString("apiDate");

    public final StringPath apiName = createString("apiName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public QApiCallEntity(String variable) {
        super(ApiCallEntity.class, forVariable(variable));
    }

    public QApiCallEntity(Path<? extends ApiCallEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QApiCallEntity(PathMetadata metadata) {
        super(ApiCallEntity.class, metadata);
    }

}

