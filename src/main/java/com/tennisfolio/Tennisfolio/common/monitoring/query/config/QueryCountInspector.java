package com.tennisfolio.Tennisfolio.common.monitoring.query.config;

import org.hibernate.resource.jdbc.spi.StatementInspector;

public class QueryCountInspector implements StatementInspector {
    @Override
    public String inspect(String sql) {
        RequestContext requestContext = RequestContextHolder.getContext();
        if (requestContext != null) {
            requestContext.incrementQueryCount(sql);
        }

        return sql;
    }
}
