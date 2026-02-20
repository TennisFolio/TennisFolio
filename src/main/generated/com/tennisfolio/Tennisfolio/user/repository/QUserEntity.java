package com.tennisfolio.Tennisfolio.user.repository;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserEntity is a Querydsl query type for UserEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserEntity extends EntityPathBase<UserEntity> {

    private static final long serialVersionUID = 1863294492L;

    public static final QUserEntity userEntity = new QUserEntity("userEntity");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final StringPath email = createString("email");

    public final StringPath nickName = createString("nickName");

    public final EnumPath<com.tennisfolio.Tennisfolio.common.UserStatus> status = createEnum("status", com.tennisfolio.Tennisfolio.common.UserStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QUserEntity(String variable) {
        super(UserEntity.class, forVariable(variable));
    }

    public QUserEntity(Path<? extends UserEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserEntity(PathMetadata metadata) {
        super(UserEntity.class, metadata);
    }

}

