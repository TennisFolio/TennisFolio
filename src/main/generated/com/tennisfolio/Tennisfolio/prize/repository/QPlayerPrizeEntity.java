package com.tennisfolio.Tennisfolio.prize.repository;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPlayerPrizeEntity is a Querydsl query type for PlayerPrizeEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPlayerPrizeEntity extends EntityPathBase<PlayerPrizeEntity> {

    private static final long serialVersionUID = 972349881L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPlayerPrizeEntity playerPrizeEntity = new QPlayerPrizeEntity("playerPrizeEntity");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final com.tennisfolio.Tennisfolio.player.repository.QPlayerEntity playerEntity;

    public final NumberPath<Long> prizeCurrentAmount = createNumber("prizeCurrentAmount", Long.class);

    public final StringPath prizeCurrentCurrency = createString("prizeCurrentCurrency");

    public final NumberPath<Long> prizeId = createNumber("prizeId", Long.class);

    public final NumberPath<Long> prizeTotalAmount = createNumber("prizeTotalAmount", Long.class);

    public final StringPath prizeTotalCurrency = createString("prizeTotalCurrency");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public QPlayerPrizeEntity(String variable) {
        this(PlayerPrizeEntity.class, forVariable(variable), INITS);
    }

    public QPlayerPrizeEntity(Path<? extends PlayerPrizeEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPlayerPrizeEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPlayerPrizeEntity(PathMetadata metadata, PathInits inits) {
        this(PlayerPrizeEntity.class, metadata, inits);
    }

    public QPlayerPrizeEntity(Class<? extends PlayerPrizeEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.playerEntity = inits.isInitialized("playerEntity") ? new com.tennisfolio.Tennisfolio.player.repository.QPlayerEntity(forProperty("playerEntity"), inits.get("playerEntity")) : null;
    }

}

