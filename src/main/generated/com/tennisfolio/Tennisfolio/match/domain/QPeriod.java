package com.tennisfolio.Tennisfolio.match.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPeriod is a Querydsl query type for Period
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QPeriod extends BeanPath<Period> {

    private static final long serialVersionUID = 1483788117L;

    public static final QPeriod period = new QPeriod("period");

    public final StringPath set1 = createString("set1");

    public final StringPath set2 = createString("set2");

    public final StringPath set3 = createString("set3");

    public final StringPath set4 = createString("set4");

    public final StringPath set5 = createString("set5");

    public QPeriod(String variable) {
        super(Period.class, forVariable(variable));
    }

    public QPeriod(Path<? extends Period> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPeriod(PathMetadata metadata) {
        super(Period.class, metadata);
    }

}

