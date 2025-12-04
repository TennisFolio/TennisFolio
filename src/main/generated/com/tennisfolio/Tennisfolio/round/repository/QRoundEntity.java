package com.tennisfolio.Tennisfolio.round.repository;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoundEntity is a Querydsl query type for RoundEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoundEntity extends EntityPathBase<RoundEntity> {

    private static final long serialVersionUID = 492403354L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoundEntity roundEntity = new QRoundEntity("roundEntity");

    public final StringPath name = createString("name");

    public final NumberPath<Long> round = createNumber("round", Long.class);

    public final NumberPath<Long> roundId = createNumber("roundId", Long.class);

    public final com.tennisfolio.Tennisfolio.season.repository.QSeasonEntity seasonEntity;

    public final StringPath slug = createString("slug");

    public QRoundEntity(String variable) {
        this(RoundEntity.class, forVariable(variable), INITS);
    }

    public QRoundEntity(Path<? extends RoundEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoundEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoundEntity(PathMetadata metadata, PathInits inits) {
        this(RoundEntity.class, metadata, inits);
    }

    public QRoundEntity(Class<? extends RoundEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.seasonEntity = inits.isInitialized("seasonEntity") ? new com.tennisfolio.Tennisfolio.season.repository.QSeasonEntity(forProperty("seasonEntity"), inits.get("seasonEntity")) : null;
    }

}

