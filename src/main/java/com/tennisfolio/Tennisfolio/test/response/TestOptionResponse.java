package com.tennisfolio.Tennisfolio.test.response;

import com.tennisfolio.Tennisfolio.test.domain.model.TestOption;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class TestOptionResponse {
    private Long optionId;
    private String optionText;

    public TestOptionResponse(TestOption testOption){
        this.optionId = testOption.getOptionId();
        this.optionText = testOption.getOptionText();
    }
}
