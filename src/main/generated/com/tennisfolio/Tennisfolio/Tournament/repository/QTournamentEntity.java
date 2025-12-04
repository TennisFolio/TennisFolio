package com.tennisfolio.Tennisfolio.Tournament.repository;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTournamentEntity is a Querydsl query type for TournamentEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTournamentEntity extends EntityPathBase<TournamentEntity> {

    private static final long serialVersionUID = 789313208L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTournamentEntity tournamentEntity = new QTournamentEntity("tournamentEntity");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final com.tennisfolio.Tennisfolio.category.repository.QCategoryEntity categoryEntity;

    public final StringPath city = createString("city");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final StringPath groundType = createString("groundType");

    public final StringPath logo = createString("logo");

    public final StringPath matchType = createString("matchType");

    public final com.tennisfolio.Tennisfolio.player.repository.QPlayerEntity mostTitlePlayer;

    public final StringPath mostTitles = createString("mostTitles");

    public final NumberPath<Long> points = createNumber("points", Long.class);

    public final StringPath rapidTournamentId = createString("rapidTournamentId");

    public final com.tennisfolio.Tennisfolio.player.repository.QPlayerEntity titleHolder;

    public final NumberPath<Long> tournamentId = createNumber("tournamentId", Long.class);

    public final StringPath tournamentName = createString("tournamentName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public QTournamentEntity(String variable) {
        this(TournamentEntity.class, forVariable(variable), INITS);
    }

    public QTournamentEntity(Path<? extends TournamentEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTournamentEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTournamentEntity(PathMetadata metadata, PathInits inits) {
        this(TournamentEntity.class, metadata, inits);
    }

    public QTournamentEntity(Class<? extends TournamentEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.categoryEntity = inits.isInitialized("categoryEntity") ? new com.tennisfolio.Tennisfolio.category.repository.QCategoryEntity(forProperty("categoryEntity")) : null;
        this.mostTitlePlayer = inits.isInitialized("mostTitlePlayer") ? new com.tennisfolio.Tennisfolio.player.repository.QPlayerEntity(forProperty("mostTitlePlayer"), inits.get("mostTitlePlayer")) : null;
        this.titleHolder = inits.isInitialized("titleHolder") ? new com.tennisfolio.Tennisfolio.player.repository.QPlayerEntity(forProperty("titleHolder"), inits.get("titleHolder")) : null;
    }

}

