package com.tennisfolio.Tennisfolio.test.service;

import com.tennisfolio.Tennisfolio.common.ExceptionCode;
import com.tennisfolio.Tennisfolio.common.TestType;
import com.tennisfolio.Tennisfolio.exception.TestNotFoundException;
import com.tennisfolio.Tennisfolio.test.domain.TestCategory;
import com.tennisfolio.Tennisfolio.test.domain.TestOption;
import com.tennisfolio.Tennisfolio.test.domain.TestOptionMapping;
import com.tennisfolio.Tennisfolio.test.provider.TestResultProvider;
import com.tennisfolio.Tennisfolio.test.repository.TestCategoryRepository;
import com.tennisfolio.Tennisfolio.test.repository.TestOptionMappingRepository;
import com.tennisfolio.Tennisfolio.test.repository.TestOptionRepository;
import com.tennisfolio.Tennisfolio.test.repository.TestQuestionRepository;
import com.tennisfolio.Tennisfolio.test.response.TestCategoryResponse;
import com.tennisfolio.Tennisfolio.test.response.TestQuestionResponse;
import com.tennisfolio.Tennisfolio.test.response.TestResultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TestServiceImpl implements TestService{
    private final TestCategoryRepository testCategoryRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestOptionRepository testOptionRepository;
    private final TestOptionMappingRepository testOptionMappingRepository;


    private final List<TestResultProvider> providers;

    public TestServiceImpl(TestCategoryRepository testCategoryRepository, TestQuestionRepository testQuestionRepository, TestOptionRepository testOptionRepository, TestOptionMappingRepository testOptionMappingRepository, List<TestResultProvider> providers) {
        this.testCategoryRepository = testCategoryRepository;
        this.testQuestionRepository = testQuestionRepository;
        this.testOptionRepository = testOptionRepository;
        this.testOptionMappingRepository = testOptionMappingRepository;
        this.providers = providers;
    }

    @Override
    public List<TestCategoryResponse> getTestList() {
        return testCategoryRepository.findAll().stream()
                .map( category -> new TestCategoryResponse(category))
                .collect(Collectors.toList());

    }

    @Override
    public TestCategoryResponse getTest(TestType testType) {
        TestCategory findTestCategory = testCategoryRepository.findByTestType(testType)
                .orElseThrow(() -> new TestNotFoundException(ExceptionCode.NOT_FOUND));

        return new TestCategoryResponse(findTestCategory);
    }

    @Override
    public List<TestQuestionResponse> getTestQuestion(TestType testType) {
        TestCategory findTestCategory = testCategoryRepository.findByTestType(testType)
                .orElseThrow(() -> new IllegalArgumentException(ExceptionCode.NOT_FOUND.getMessage()));

        return testQuestionRepository.findByTestCategory(findTestCategory).stream()
                .map(testQuestion -> new TestQuestionResponse(testQuestion))
                .collect(Collectors.toList());

    }

    @Override
    public TestResultResponse getResult(List<Long> answerList, TestType testType) {
        List<TestOption> optionList = testOptionRepository.findByOptionIdIn(answerList);

        List<TestOptionMapping> mappingList = testOptionMappingRepository.findByOptionIn(optionList);


        Map<Long, Integer> scoreMap = mappingList.stream()
                        .collect(Collectors.toMap(
                                TestOptionMapping::getTargetId,
                                TestOptionMapping::getScore,
                                Integer::sum
                        ));
        Long maxTarget = scoreMap.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(0L);

        return providers.stream()
                .filter(p -> p.supports(testType))
                .findFirst()
                .orElseThrow()
                .getResult(maxTarget);

    }
}
