package com.tennisfolio.Tennisfolio.mock;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiCaller;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;

import java.net.http.HttpResponse;

public class FakeApiCaller implements ApiCaller {
    @Override
    public String callApi(RapidApi endpoint, Object... params) {
        return "test";
    }
}
