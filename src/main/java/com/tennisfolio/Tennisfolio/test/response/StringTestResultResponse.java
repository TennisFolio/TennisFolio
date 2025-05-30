package com.tennisfolio.Tennisfolio.test.response;

import com.tennisfolio.Tennisfolio.test.domain.TestString;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StringTestResultResponse implements TestResultResponse{
    private Long stringId;
    private String stringName;
    private String stringType;
    private String description;
    private String query;
    private String image;

    public StringTestResultResponse(TestString testString){
        this.stringId = testString.getStringId();
        this.stringName = testString.getStringName();
        this.stringType = testString.getStringType();
        this.description = testString.getDescription();
        this.query = testString.getQuery();
        this.image = testString.getImage();
    }

    @Override
    public Long getResultId() {
        return stringId;
    }

    @Override
    public String getName() {
        return stringName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getQuery(){return query;}

    @Override
    public String getImage() {
        return image;
    }
}
