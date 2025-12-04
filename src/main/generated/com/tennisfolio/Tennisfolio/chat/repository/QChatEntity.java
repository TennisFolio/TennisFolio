package com.tennisfolio.Tennisfolio.chat.repository;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QChatEntity is a Querydsl query type for ChatEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatEntity extends EntityPathBase<ChatEntity> {

    private static final long serialVersionUID = -2109835338L;

    public static final QChatEntity chatEntity = new QChatEntity("chatEntity");

    public final com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity _super = new com.tennisfolio.Tennisfolio.common.Entity.QBaseTimeEntity(this);

    public final NumberPath<Long> chatId = createNumber("chatId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDt = _super.createDt;

    public final StringPath message = createString("message");

    public final StringPath nickName = createString("nickName");

    public final StringPath rapidMatchId = createString("rapidMatchId");

    public final StringPath type = createString("type");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDt = _super.updateDt;

    public final StringPath userId = createString("userId");

    public QChatEntity(String variable) {
        super(ChatEntity.class, forVariable(variable));
    }

    public QChatEntity(Path<? extends ChatEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChatEntity(PathMetadata metadata) {
        super(ChatEntity.class, metadata);
    }

}

