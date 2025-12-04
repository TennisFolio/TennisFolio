package com.tennisfolio.Tennisfolio.player.repository;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCountryEntity is a Querydsl query type for CountryEntity
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QCountryEntity extends BeanPath<CountryEntity> {

    private static final long serialVersionUID = -2144666955L;

    public static final QCountryEntity countryEntity = new QCountryEntity("countryEntity");

    public final StringPath countryCode = createString("countryCode");

    public final StringPath countryName = createString("countryName");

    public QCountryEntity(String variable) {
        super(CountryEntity.class, forVariable(variable));
    }

    public QCountryEntity(Path<? extends CountryEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCountryEntity(PathMetadata metadata) {
        super(CountryEntity.class, metadata);
    }

}

