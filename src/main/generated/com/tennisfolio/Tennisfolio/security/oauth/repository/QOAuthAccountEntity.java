package com.tennisfolio.Tennisfolio.security.oauth.repository;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOAuthAccountEntity is a Querydsl query type for OAuthAccountEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOAuthAccountEntity extends EntityPathBase<OAuthAccountEntity> {

    private static final long serialVersionUID = -140368539L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOAuthAccountEntity oAuthAccountEntity = new QOAuthAccountEntity("oAuthAccountEntity");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final StringPath email = createString("email");

    public final NumberPath<Long> oAuthId = createNumber("oAuthId", Long.class);

    public final EnumPath<com.tennisfolio.Tennisfolio.common.OAuthProvider> provider = createEnum("provider", com.tennisfolio.Tennisfolio.common.OAuthProvider.class);

    public final StringPath providerId = createString("providerId");

    public final EnumPath<com.tennisfolio.Tennisfolio.common.OAuthStatus> status = createEnum("status", com.tennisfolio.Tennisfolio.common.OAuthStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public final com.tennisfolio.Tennisfolio.user.repository.QUserEntity user;

    public QOAuthAccountEntity(String variable) {
        this(OAuthAccountEntity.class, forVariable(variable), INITS);
    }

    public QOAuthAccountEntity(Path<? extends OAuthAccountEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOAuthAccountEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOAuthAccountEntity(PathMetadata metadata, PathInits inits) {
        this(OAuthAccountEntity.class, metadata, inits);
    }

    public QOAuthAccountEntity(Class<? extends OAuthAccountEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.tennisfolio.Tennisfolio.user.repository.QUserEntity(forProperty("user")) : null;
    }

}

