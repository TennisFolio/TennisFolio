package com.tennisfolio.Tennisfolio.ranking.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tennisfolio.Tennisfolio.common.RankingSearchCondition;
import com.tennisfolio.Tennisfolio.player.repository.QPlayerEntity;
import com.tennisfolio.Tennisfolio.ranking.domain.Ranking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;


public class RankingQueryRepositoryImpl implements RankingQueryRepository{
    private final JPAQueryFactory queryFactory;

    public RankingQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<RankingEntity> search(Pageable pageable, RankingSearchCondition condition, String keyword) {
        QRankingEntity ranking = QRankingEntity.rankingEntity;
        QPlayerEntity player = QPlayerEntity.playerEntity;

        QRankingEntity ranking2 = new QRankingEntity("ranking2");

        BooleanExpression predicate = buildPredicate(condition, keyword);

        BooleanExpression latestUpdateCondition =
                ranking.lastUpdate.eq(
                        JPAExpressions.select(ranking2.lastUpdate.max())
                                .from(ranking2)
                );

        List<RankingEntity> rankingEntityList = queryFactory
                .selectFrom(ranking)
                .leftJoin(ranking.playerEntity, player).fetchJoin()
                .where(predicate, latestUpdateCondition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(ranking.curRank.asc())
                .fetch();

        Long total = queryFactory
                .select(ranking.count())
                .from(ranking)
                .where(predicate, latestUpdateCondition)
                .fetchOne();

        return new PageImpl<>(rankingEntityList, pageable, total == null ? 0 : total);
    }

    private BooleanExpression buildPredicate(RankingSearchCondition condition, String keyword){
        if(!StringUtils.hasText(keyword)){
            return null;
        }

        return switch(condition){
            case NAME -> QPlayerEntity.playerEntity.playerName.containsIgnoreCase(keyword).or(QPlayerEntity.playerEntity.playerNameKr.containsIgnoreCase(keyword));
            case COUNTRY -> QPlayerEntity.playerEntity.countryEntity.countryCode.eq(keyword);
        };
    }
}
