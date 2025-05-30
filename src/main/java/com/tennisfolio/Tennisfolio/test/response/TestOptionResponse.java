package com.tennisfolio.Tennisfolio.test.response;

import com.tennisfolio.Tennisfolio.test.domain.TestOption;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestOptionResponse {
    private Long optionId;
    private String optionText;

    public TestOptionResponse(TestOption testOption){
        this.optionId = testOption.getOptionId();
        this.optionText = testOption.getOptionText();
    }
}
