package com.tennisfolio.Tennisfolio.matching.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCompetition is a Querydsl query type for Competition
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCompetition extends EntityPathBase<Competition> {

    private static final long serialVersionUID = 1447723032L;

    public static final QCompetition competition = new QCompetition("competition");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final NumberPath<Integer> courtCount = createNumber("courtCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final StringPath adminPasswordHash = createString("adminPasswordHash");

    public final NumberPath<Integer> femaleCount = createNumber("femaleCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isModified = createBoolean("isModified");

    public final NumberPath<Integer> maleCount = createNumber("maleCount", Integer.class);

    public final EnumPath<Competition.CompetitionMode> mode = createEnum("mode", Competition.CompetitionMode.class);

    public final StringPath name = createString("name");

    public final StringPath publicId = createString("publicId");

    public final NumberPath<Integer> rounds = createNumber("rounds", Integer.class);

    public final NumberPath<Long> seed = createNumber("seed", Long.class);

    public final EnumPath<Competition.CompetitionStatus> status = createEnum("status", Competition.CompetitionStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public QCompetition(String variable) {
        super(Competition.class, forVariable(variable));
    }

    public QCompetition(Path<? extends Competition> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCompetition(PathMetadata metadata) {
        super(Competition.class, metadata);
    }

}

