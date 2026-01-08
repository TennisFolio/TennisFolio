package com.tennisfolio.Tennisfolio.ranking.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tennisfolio.Tennisfolio.common.RankingCategory;
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
    public Page<RankingEntity> search(Pageable pageable, RankingCategory category, String country, String name) {
        QRankingEntity ranking = QRankingEntity.rankingEntity;
        QPlayerEntity player = QPlayerEntity.playerEntity;

        QRankingEntity ranking2 = new QRankingEntity("ranking2");

        BooleanExpression predicate = buildPredicate(category, country, name);

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

    private BooleanExpression buildPredicate(RankingCategory category, String country, String name){
        BooleanExpression expression = QRankingEntity.rankingEntity.category.eq(category);

        if(StringUtils.hasText(name)){
            expression = QPlayerEntity.playerEntity.playerName.containsIgnoreCase(name)
                    .or(QPlayerEntity.playerEntity.playerNameKr.containsIgnoreCase(name));
        }

        if(StringUtils.hasText(country)){
            BooleanExpression countryExpression = QPlayerEntity.playerEntity.countryEntity.countryCode.eq(country);
            expression = expression == null ? countryExpression : expression.and(countryExpression);
        }

        return expression;
    }
}
