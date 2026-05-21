package com.tennisfolio.Tennisfolio.matching.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCompetitionEntry is a Querydsl query type for CompetitionEntry
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCompetitionEntry extends EntityPathBase<CompetitionEntry> {

    private static final long serialVersionUID = -1590040774L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCompetitionEntry competitionEntry = new QCompetitionEntry("competitionEntry");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final QCompetition competition;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final EnumPath<CompetitionEntry.Gender> gender = createEnum("gender", CompetitionEntry.Gender.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath playerName = createString("playerName");

    public final EnumPath<CompetitionEntry.EntryStatus> status = createEnum("status", CompetitionEntry.EntryStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public QCompetitionEntry(String variable) {
        this(CompetitionEntry.class, forVariable(variable), INITS);
    }

    public QCompetitionEntry(Path<? extends CompetitionEntry> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCompetitionEntry(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCompetitionEntry(PathMetadata metadata, PathInits inits) {
        this(CompetitionEntry.class, metadata, inits);
    }

    public QCompetitionEntry(Class<? extends CompetitionEntry> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.competition = inits.isInitialized("competition") ? new QCompetition(forProperty("competition")) : null;
    }

}

