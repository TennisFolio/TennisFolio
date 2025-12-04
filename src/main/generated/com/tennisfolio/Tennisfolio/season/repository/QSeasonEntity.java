package com.tennisfolio.Tennisfolio.season.repository;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSeasonEntity is a Querydsl query type for SeasonEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSeasonEntity extends EntityPathBase<SeasonEntity> {

    private static final long serialVersionUID = 145321868L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSeasonEntity seasonEntity = new QSeasonEntity("seasonEntity");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final NumberPath<Long> competitors = createNumber("competitors", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final StringPath endTimestamp = createString("endTimestamp");

    public final StringPath rapidSeasonId = createString("rapidSeasonId");

    public final NumberPath<Long> seasonId = createNumber("seasonId", Long.class);

    public final StringPath seasonName = createString("seasonName");

    public final StringPath startTimestamp = createString("startTimestamp");

    public final NumberPath<Long> totalPrize = createNumber("totalPrize", Long.class);

    public final StringPath totalPrizeCurrency = createString("totalPrizeCurrency");

    public final com.tennisfolio.Tennisfolio.Tournament.repository.QTournamentEntity tournamentEntity;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public final StringPath year = createString("year");

    public QSeasonEntity(String variable) {
        this(SeasonEntity.class, forVariable(variable), INITS);
    }

    public QSeasonEntity(Path<? extends SeasonEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSeasonEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSeasonEntity(PathMetadata metadata, PathInits inits) {
        this(SeasonEntity.class, metadata, inits);
    }

    public QSeasonEntity(Class<? extends SeasonEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.tournamentEntity = inits.isInitialized("tournamentEntity") ? new com.tennisfolio.Tennisfolio.Tournament.repository.QTournamentEntity(forProperty("tournamentEntity"), inits.get("tournamentEntity")) : null;
    }

}

