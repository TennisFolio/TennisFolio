package com.tennisfolio.Tennisfolio.test.response;

import com.tennisfolio.Tennisfolio.test.domain.model.TestRacket;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class RacketTestResultResponse implements TestResultResponse{
    private Long racketId;
    private String brand;
    private String modelName;
    private String description;
    private String query;
    private String image;

    public RacketTestResultResponse(TestRacket testRacket){
        this.racketId = testRacket.getRacketId();
        this.brand = testRacket.getBrand();
        this.modelName = testRacket.getModelName();
        this.description = testRacket.getDescription();
        this.query = testRacket.getQuery();
        this.image = testRacket.getImage();
    }

    @Override
    public Long getResultId() {
        return racketId;
    }

    @Override
    public String getName() {
        return modelName;
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
