package com.tennisfolio.Tennisfolio.matching.service.fixed;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SameGenderScheduleTargetCalculator {

    public List<SameGenderScheduleTarget> calculate(int maleCount, int femaleCount, int totalGames) {
        int playerCount = maleCount + femaleCount;
        int totalSlots = totalGames * 4;
        int minGames = totalSlots / playerCount;
        int maxGames = (int) Math.ceil((double) totalSlots / playerCount);
        int extraSlots = totalSlots - playerCount * minGames;

        List<SameGenderScheduleTarget> targets = new ArrayList<>();
        for (int maleExtraSlots = 0; maleExtraSlots <= Math.min(maleCount, extraSlots); maleExtraSlots++) {
            int femaleExtraSlots = extraSlots - maleExtraSlots;
            if (femaleExtraSlots < 0 || femaleExtraSlots > femaleCount) {
                continue;
            }

            int maleSlots = maleCount * minGames + maleExtraSlots;
            int femaleSlots = femaleCount * minGames + femaleExtraSlots;
            if (maleSlots % 4 != 0 || femaleSlots % 4 != 0) {
                continue;
            }

            int maleGames = maleSlots / 4;
            int femaleGames = femaleSlots / 4;
            if (maleGames + femaleGames != totalGames) {
                continue;
            }
            if (maleGames > 0 && maleCount < 4) {
                continue;
            }
            if (femaleGames > 0 && femaleCount < 4) {
                continue;
            }

            targets.add(new SameGenderScheduleTarget(maleGames, femaleGames, minGames, maxGames));
        }

        targets.sort(Comparator
                .comparingInt((SameGenderScheduleTarget target) -> Math.abs(target.maleGames() - target.femaleGames()))
                .thenComparingInt(SameGenderScheduleTarget::maleGames));
        return targets;
    }
}
