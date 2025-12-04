package com.tennisfolio.Tennisfolio.statistic.repository;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStatisticEntity is a Querydsl query type for StatisticEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStatisticEntity extends EntityPathBase<StatisticEntity> {

    private static final long serialVersionUID = -291705638L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStatisticEntity statisticEntity = new QStatisticEntity("statisticEntity");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final NumberPath<Long> awayTotal = createNumber("awayTotal", Long.class);

    public final NumberPath<Long> awayValue = createNumber("awayValue", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final StringPath groupName = createString("groupName");

    public final NumberPath<Long> homeTotal = createNumber("homeTotal", Long.class);

    public final NumberPath<Long> homeValue = createNumber("homeValue", Long.class);

    public final com.tennisfolio.Tennisfolio.match.repository.QMatchEntity matchEntity;

    public final StringPath metric = createString("metric");

    public final StringPath period = createString("period");

    public final StringPath statDirection = createString("statDirection");

    public final NumberPath<Long> statId = createNumber("statId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public QStatisticEntity(String variable) {
        this(StatisticEntity.class, forVariable(variable), INITS);
    }

    public QStatisticEntity(Path<? extends StatisticEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStatisticEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStatisticEntity(PathMetadata metadata, PathInits inits) {
        this(StatisticEntity.class, metadata, inits);
    }

    public QStatisticEntity(Class<? extends StatisticEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.matchEntity = inits.isInitialized("matchEntity") ? new com.tennisfolio.Tennisfolio.match.repository.QMatchEntity(forProperty("matchEntity"), inits.get("matchEntity")) : null;
    }

}

