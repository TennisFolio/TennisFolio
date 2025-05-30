package com.tennisfolio.Tennisfolio.test.response;

import com.tennisfolio.Tennisfolio.test.domain.TestCategory;
import com.tennisfolio.Tennisfolio.test.domain.TestQuestion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class TestQuestionResponse {
    private Long questionId;
    private String question;
    private int order;
    private List<TestOptionResponse> testOption;

    public TestQuestionResponse( TestQuestion testQuestion){
        this.questionId = testQuestion.getQuestionId();
        this.question = testQuestion.getQuestionText();
        this.order = testQuestion.getQuestionOrder();
        this.testOption = testQuestion.getTestOptionList().stream().map(option -> (new TestOptionResponse(option))).collect(Collectors.toList());

    }
}
