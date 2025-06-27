package com.tennisfolio.Tennisfolio.test.response;

import com.tennisfolio.Tennisfolio.test.domain.model.TestCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class TestCategoryResponse {
    private Long testCategoryId;
    private String testCategoryName;
    private String url;
    private String description;
    private String image;

    public TestCategoryResponse(TestCategory testCategory){
        this.testCategoryId = testCategory.getTestCategoryId();
        this.testCategoryName = testCategory.getTestCategoryName();
        this.url = testCategory.getUrl();
        this.description = testCategory.getDescription();
        this.image = testCategory.getImage();
    }
}
