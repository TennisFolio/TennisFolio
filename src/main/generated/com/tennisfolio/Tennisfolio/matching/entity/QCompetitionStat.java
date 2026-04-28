package com.tennisfolio.Tennisfolio.matching.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCompetitionStat is a Querydsl query type for CompetitionStat
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCompetitionStat extends EntityPathBase<CompetitionStat> {

    private static final long serialVersionUID = 780414604L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCompetitionStat competitionStat = new QCompetitionStat("competitionStat");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final QCompetition competition;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final NumberPath<Integer> femaleCount = createNumber("femaleCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> maleCount = createNumber("maleCount", Integer.class);

    public final NumberPath<Integer> maxGames = createNumber("maxGames", Integer.class);

    public final NumberPath<Integer> minGames = createNumber("minGames", Integer.class);

    public final NumberPath<Integer> mixedCount = createNumber("mixedCount", Integer.class);

    public final NumberPath<Integer> randomM1F3Count = createNumber("randomM1F3Count", Integer.class);

    public final NumberPath<Integer> randomM3F1Count = createNumber("randomM3F1Count", Integer.class);

    public final NumberPath<Integer> totalGames = createNumber("totalGames", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public QCompetitionStat(String variable) {
        this(CompetitionStat.class, forVariable(variable), INITS);
    }

    public QCompetitionStat(Path<? extends CompetitionStat> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCompetitionStat(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCompetitionStat(PathMetadata metadata, PathInits inits) {
        this(CompetitionStat.class, metadata, inits);
    }

    public QCompetitionStat(Class<? extends CompetitionStat> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.competition = inits.isInitialized("competition") ? new QCompetition(forProperty("competition")) : null;
    }

}

