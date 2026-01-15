package com.tennisfolio.Tennisfolio.mock;

import com.tennisfolio.Tennisfolio.infrastructure.api.base.ApiCaller;
import com.tennisfolio.Tennisfolio.infrastructure.api.base.RapidApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;

@Component
@Profile("test")
public class FakeApiCaller implements ApiCaller {
    @Override
    public String callApi(RapidApi endpoint, Object... params) {
        return "test";
    }
}
