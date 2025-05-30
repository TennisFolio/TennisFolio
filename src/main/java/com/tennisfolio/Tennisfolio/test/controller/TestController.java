package com.tennisfolio.Tennisfolio.test.controller;

import com.tennisfolio.Tennisfolio.common.TestType;
import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.test.response.TestCategoryResponse;
import com.tennisfolio.Tennisfolio.test.response.TestQuestionResponse;
import com.tennisfolio.Tennisfolio.test.response.TestResultResponse;
import com.tennisfolio.Tennisfolio.test.service.TestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("")
    public ResponseEntity<ResponseDTO<List<TestCategoryResponse>>> getTestList() {
        List<TestCategoryResponse> testList = testService.getTestList();
        return new ResponseEntity<>(ResponseDTO.success(testList), HttpStatus.OK);
    }

    @GetMapping("/{testType}")
    public ResponseEntity<ResponseDTO<TestCategoryResponse>> getTest(@PathVariable("testType")String testType){
        TestType test = TestType.fromString(testType);
        TestCategoryResponse testCategory = testService.getTest(test);

        return new ResponseEntity<>(ResponseDTO.success(testCategory), HttpStatus.OK);
    }

    @GetMapping("/{testType}/questions")
    public ResponseEntity<ResponseDTO<List<TestQuestionResponse>>> getQuestionList(@PathVariable("testType")String testType){
        TestType test = TestType.fromString(testType);
        List<TestQuestionResponse> questionList = testService.getTestQuestion(test);

        return new ResponseEntity<>(ResponseDTO.success(questionList), HttpStatus.OK);
    }

    @PostMapping("/{testType}/result")
    public ResponseEntity<ResponseDTO<TestResultResponse>> getTestResult(
            @PathVariable("testType")String testType,
            @RequestBody List<Long> request
    ){
        TestType test = TestType.fromString(testType);
        TestResultResponse testResult = testService.getResult(request,test);

        return new ResponseEntity<>(ResponseDTO.success(testResult), HttpStatus.OK);
    }

    @GetMapping("/{testType}/result/{query}")
    public ResponseEntity<ResponseDTO<TestResultResponse>> getTestResultByQuery(
            @PathVariable("testType")String testType,
            @PathVariable("query")String query
    ){
        TestType test = TestType.fromString(testType);
        TestResultResponse testResult = testService.getTestResultByQuery(test,query);

        return new ResponseEntity<>(ResponseDTO.success(testResult), HttpStatus.OK);
    }
}
