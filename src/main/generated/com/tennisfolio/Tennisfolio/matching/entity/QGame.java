package com.tennisfolio.Tennisfolio.matching.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGame is a Querydsl query type for Game
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGame extends EntityPathBase<Game> {

    private static final long serialVersionUID = -146153543L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGame game = new QGame("game");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final QCompetition competition;

    public final NumberPath<Integer> court = createNumber("court", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<Game.MatchType> matchType = createEnum("matchType", Game.MatchType.class);

    public final NumberPath<Integer> round = createNumber("round", Integer.class);

    public final NumberPath<Integer> teamAScore = createNumber("teamAScore", Integer.class);

    public final NumberPath<Integer> teamATiebreaKScore = createNumber("teamATiebreaKScore", Integer.class);

    public final NumberPath<Integer> teamBScore = createNumber("teamBScore", Integer.class);

    public final NumberPath<Integer> teamBTiebreaKScore = createNumber("teamBTiebreaKScore", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public QGame(String variable) {
        this(Game.class, forVariable(variable), INITS);
    }

    public QGame(Path<? extends Game> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGame(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGame(PathMetadata metadata, PathInits inits) {
        this(Game.class, metadata, inits);
    }

    public QGame(Class<? extends Game> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.competition = inits.isInitialized("competition") ? new QCompetition(forProperty("competition")) : null;
    }

}

