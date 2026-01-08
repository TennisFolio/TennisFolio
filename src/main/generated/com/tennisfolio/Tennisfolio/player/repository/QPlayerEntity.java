package com.tennisfolio.Tennisfolio.player.repository;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPlayerEntity is a Querydsl query type for PlayerEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPlayerEntity extends EntityPathBase<PlayerEntity> {

    private static final long serialVersionUID = -1215250680L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPlayerEntity playerEntity = new QPlayerEntity("playerEntity");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final StringPath birth = createString("birth");

    public final QCountryEntity countryEntity;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final StringPath gender = createString("gender");

    public final StringPath height = createString("height");

    public final StringPath image = createString("image");

    public final NumberPath<Long> playerId = createNumber("playerId", Long.class);

    public final StringPath playerName = createString("playerName");

    public final StringPath playerNameKr = createString("playerNameKr");

    public final StringPath plays = createString("plays");

    public final StringPath rapidPlayerId = createString("rapidPlayerId");

    public final StringPath turnedPro = createString("turnedPro");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public final StringPath weight = createString("weight");

    public QPlayerEntity(String variable) {
        this(PlayerEntity.class, forVariable(variable), INITS);
    }

    public QPlayerEntity(Path<? extends PlayerEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPlayerEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPlayerEntity(PathMetadata metadata, PathInits inits) {
        this(PlayerEntity.class, metadata, inits);
    }

    public QPlayerEntity(Class<? extends PlayerEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.countryEntity = inits.isInitialized("countryEntity") ? new QCountryEntity(forProperty("countryEntity")) : null;
    }

}

