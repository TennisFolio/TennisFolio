package com.tennisfolio.Tennisfolio.infrastructure.api.base;

import java.net.http.HttpResponse;

public interface ApiCaller {
    String callApi(RapidApi endpoint, Object... params);
}
