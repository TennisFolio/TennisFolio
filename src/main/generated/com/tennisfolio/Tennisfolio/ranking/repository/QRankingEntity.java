package com.tennisfolio.Tennisfolio.ranking.repository;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRankingEntity is a Querydsl query type for RankingEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRankingEntity extends EntityPathBase<RankingEntity> {

    private static final long serialVersionUID = 712930330L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRankingEntity rankingEntity = new QRankingEntity("rankingEntity");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final NumberPath<Long> bestRank = createNumber("bestRank", Long.class);

    public final EnumPath<com.tennisfolio.Tennisfolio.common.RankingCategory> category = createEnum("category", com.tennisfolio.Tennisfolio.common.RankingCategory.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final NumberPath<Long> curPoints = createNumber("curPoints", Long.class);

    public final NumberPath<Long> curRank = createNumber("curRank", Long.class);

    public final StringPath lastUpdate = createString("lastUpdate");

    public final com.tennisfolio.Tennisfolio.player.repository.QPlayerEntity playerEntity;

    public final NumberPath<Long> prePoints = createNumber("prePoints", Long.class);

    public final NumberPath<Long> preRank = createNumber("preRank", Long.class);

    public final NumberPath<Long> rankingId = createNumber("rankingId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public QRankingEntity(String variable) {
        this(RankingEntity.class, forVariable(variable), INITS);
    }

    public QRankingEntity(Path<? extends RankingEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRankingEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRankingEntity(PathMetadata metadata, PathInits inits) {
        this(RankingEntity.class, metadata, inits);
    }

    public QRankingEntity(Class<? extends RankingEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.playerEntity = inits.isInitialized("playerEntity") ? new com.tennisfolio.Tennisfolio.player.repository.QPlayerEntity(forProperty("playerEntity"), inits.get("playerEntity")) : null;
    }

}

