package com.rabin.backend.util;

import java.util.Set;

public class ContentScoreCalculator {
    public static double contentScore(Set<Long> userTagIds,Set<Long> eventTagIds){

        if(userTagIds == null || userTagIds.isEmpty()||
           eventTagIds == null || eventTagIds.isEmpty()){
            return 0.0;
        }

        int matches = 0;

        for (Long tag:userTagIds){
            if(eventTagIds.contains(tag)){
                matches++;
            }
        }

        if(matches==0){
            return 0.0;
        }

        int unionSize = userTagIds.size()+eventTagIds.size()-matches;

        return (double) matches/unionSize;
    }
}
