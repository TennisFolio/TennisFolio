package com.tennisfolio.Tennisfolio.matching.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGameEntry is a Querydsl query type for GameEntry
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGameEntry extends EntityPathBase<GameEntry> {

    private static final long serialVersionUID = -155573191L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGameEntry gameEntry = new QGameEntry("gameEntry");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final QCompetitionEntry competitionEntry;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final QGame game;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> position = createNumber("position", Integer.class);

    public final EnumPath<GameEntry.Team> team = createEnum("team", GameEntry.Team.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public QGameEntry(String variable) {
        this(GameEntry.class, forVariable(variable), INITS);
    }

    public QGameEntry(Path<? extends GameEntry> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGameEntry(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGameEntry(PathMetadata metadata, PathInits inits) {
        this(GameEntry.class, metadata, inits);
    }

    public QGameEntry(Class<? extends GameEntry> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.competitionEntry = inits.isInitialized("competitionEntry") ? new QCompetitionEntry(forProperty("competitionEntry"), inits.get("competitionEntry")) : null;
        this.game = inits.isInitialized("game") ? new QGame(forProperty("game"), inits.get("game")) : null;
    }

}

