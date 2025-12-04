package com.tennisfolio.Tennisfolio.match.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QScore is a Querydsl query type for Score
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QScore extends BeanPath<Score> {

    private static final long serialVersionUID = 1574593150L;

    public static final QScore score = new QScore("score");

    public final NumberPath<Long> set1 = createNumber("set1", Long.class);

    public final NumberPath<Long> set1Tie = createNumber("set1Tie", Long.class);

    public final NumberPath<Long> set2 = createNumber("set2", Long.class);

    public final NumberPath<Long> set2Tie = createNumber("set2Tie", Long.class);

    public final NumberPath<Long> set3 = createNumber("set3", Long.class);

    public final NumberPath<Long> set3Tie = createNumber("set3Tie", Long.class);

    public final NumberPath<Long> set4 = createNumber("set4", Long.class);

    public final NumberPath<Long> set4Tie = createNumber("set4Tie", Long.class);

    public final NumberPath<Long> set5 = createNumber("set5", Long.class);

    public final NumberPath<Long> set5Tie = createNumber("set5Tie", Long.class);

    public QScore(String variable) {
        super(Score.class, forVariable(variable));
    }

    public QScore(Path<? extends Score> path) {
        super(path.getType(), path.getMetadata());
    }

    public QScore(PathMetadata metadata) {
        super(Score.class, metadata);
    }

}

