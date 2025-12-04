package com.tennisfolio.Tennisfolio.match.repository;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMatchEntity is a Querydsl query type for MatchEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMatchEntity extends EntityPathBase<MatchEntity> {

    private static final long serialVersionUID = 186312570L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMatchEntity matchEntity = new QMatchEntity("matchEntity");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final com.tennisfolio.Tennisfolio.player.repository.QPlayerEntity awayPlayer;

    public final NumberPath<Long> awayScore = createNumber("awayScore", Long.class);

    public final StringPath awaySeed = createString("awaySeed");

    public final com.tennisfolio.Tennisfolio.match.domain.QScore awaySet;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final com.tennisfolio.Tennisfolio.player.repository.QPlayerEntity homePlayer;

    public final NumberPath<Long> homeScore = createNumber("homeScore", Long.class);

    public final StringPath homeSeed = createString("homeSeed");

    public final com.tennisfolio.Tennisfolio.match.domain.QScore homeSet;

    public final NumberPath<Long> matchId = createNumber("matchId", Long.class);

    public final com.tennisfolio.Tennisfolio.match.domain.QPeriod periodSet;

    public final StringPath rapidMatchId = createString("rapidMatchId");

    public final com.tennisfolio.Tennisfolio.round.repository.QRoundEntity roundEntity;

    public final StringPath startTimestamp = createString("startTimestamp");

    public final StringPath status = createString("status");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public final StringPath winner = createString("winner");

    public QMatchEntity(String variable) {
        this(MatchEntity.class, forVariable(variable), INITS);
    }

    public QMatchEntity(Path<? extends MatchEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMatchEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMatchEntity(PathMetadata metadata, PathInits inits) {
        this(MatchEntity.class, metadata, inits);
    }

    public QMatchEntity(Class<? extends MatchEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.awayPlayer = inits.isInitialized("awayPlayer") ? new com.tennisfolio.Tennisfolio.player.repository.QPlayerEntity(forProperty("awayPlayer"), inits.get("awayPlayer")) : null;
        this.awaySet = inits.isInitialized("awaySet") ? new com.tennisfolio.Tennisfolio.match.domain.QScore(forProperty("awaySet")) : null;
        this.homePlayer = inits.isInitialized("homePlayer") ? new com.tennisfolio.Tennisfolio.player.repository.QPlayerEntity(forProperty("homePlayer"), inits.get("homePlayer")) : null;
        this.homeSet = inits.isInitialized("homeSet") ? new com.tennisfolio.Tennisfolio.match.domain.QScore(forProperty("homeSet")) : null;
        this.periodSet = inits.isInitialized("periodSet") ? new com.tennisfolio.Tennisfolio.match.domain.QPeriod(forProperty("periodSet")) : null;
        this.roundEntity = inits.isInitialized("roundEntity") ? new com.tennisfolio.Tennisfolio.round.repository.QRoundEntity(forProperty("roundEntity"), inits.get("roundEntity")) : null;
    }

}

