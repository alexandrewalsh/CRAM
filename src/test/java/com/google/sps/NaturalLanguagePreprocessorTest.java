// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.sps.data.TimeRangedText;
import com.google.sps.data.NaturalLanguagePreprocessor;


/** Tests for the NaturalLanguagePreprocessor class methods */
@RunWith(JUnit4.class)
public final class NaturalLanguagePreprocessorTest {

    private static final long TIME_THRESHOLD_2 = 2;
    private static final long TIME_THRESHOLD_5 = 5;

    private static final String TEXT_DELIMITER = " ";
    private static final String TEXT_A = "A";
    private static final String TEXT_B = "B";
    private static final String TEXT_C = "C";
    private static final String TEXT_D = "D";
    private static final String TEXT_A_AND_B = "A B ";
    private static final String TEXT_C_AND_D = "C D ";
    
    @Test
    public void TimeRangedTextsPerfectlyFit() {
        // The TimeRangedTexts perfectly align with the thresholds
        // Time Threshold  : 2

        // TimeRangedTexts : |--A--|     |--C--| 
        //                         |--B--|     |--D--|
        
        // Expected Results: |-----------|
        //                               |------------|

        NaturalLanguagePreprocessor nlp = new NaturalLanguagePreprocessor(TIME_THRESHOLD_2);
        List<TimeRangedText> initialList = new ArrayList<>();
        long currentTime = 0;
        initialList.add(new TimeRangedText(currentTime, ++currentTime, TEXT_A));
        initialList.add(new TimeRangedText(currentTime, ++currentTime, TEXT_B));
        initialList.add(new TimeRangedText(currentTime, ++currentTime, TEXT_C));
        initialList.add(new TimeRangedText(currentTime, ++currentTime, TEXT_D));

        List<TimeRangedText> expected = new ArrayList<>();
        expected.add(new TimeRangedText(0, 2, TEXT_A_AND_B));
        expected.add(new TimeRangedText(2, 4, TEXT_C_AND_D));

        List<TimeRangedText> actual = nlp.setTimeRanges(initialList);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void TimeOverlappingWithinDuration() {
        // The TimeRangedTexts overlap within the specified duration
        // Time Threshold  : 5
       
        // TimeRangedTexts : |--A--|   |---C---| 
        //                       |--B--|  |---D---|
 
        // Expected Results: |---------|
        //                             |----------|
        
        NaturalLanguagePreprocessor nlp = new NaturalLanguagePreprocessor(TIME_THRESHOLD_5);
        List<TimeRangedText> initialList = new ArrayList<>();
        initialList.add(new TimeRangedText(0, 3, TEXT_A));
        initialList.add(new TimeRangedText(2, 5, TEXT_B));
        initialList.add(new TimeRangedText(5, 9, TEXT_C));
        initialList.add(new TimeRangedText(6, 10, TEXT_D));

        List<TimeRangedText> expected = new ArrayList<>();
        expected.add(new TimeRangedText(0, 5, TEXT_A_AND_B));
        expected.add(new TimeRangedText(5, 10, TEXT_C_AND_D));

        List<TimeRangedText> actual = nlp.setTimeRanges(initialList);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void TimeOverlappingThroughDuration() {
        // The TimeRangedTexts overlap through the duration
        // Time Threshold  : 5
       
        // TimeRangedTexts : |--A--| |---C---| 
        //                       |--B--|  |--D--|
 
        // Expected Results: |---------|
        //                           |----------|
        
        NaturalLanguagePreprocessor nlp = new NaturalLanguagePreprocessor(TIME_THRESHOLD_5);
        List<TimeRangedText> initialList = new ArrayList<>();
        initialList.add(new TimeRangedText(0, 3, TEXT_A));
        initialList.add(new TimeRangedText(2, 5, TEXT_B));
        initialList.add(new TimeRangedText(4, 8, TEXT_C));
        initialList.add(new TimeRangedText(6, 9, TEXT_D));

        List<TimeRangedText> expected = new ArrayList<>();
        expected.add(new TimeRangedText(0, 5, TEXT_A_AND_B));
        expected.add(new TimeRangedText(4, 9, TEXT_C_AND_D));

        List<TimeRangedText> actual = nlp.setTimeRanges(initialList);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void LastTimeRangedTextAlwaysIncluded() {
        // The last TimeRangedText does not meet the threshold duration, but is still included
        // Time Threshold  : 5
       
        // TimeRangedTexts : |--A--|     |-C-| 
        //                         |--B--|
 
        // Expected Results: |-----------|
        //                               |---|
        
        NaturalLanguagePreprocessor nlp = new NaturalLanguagePreprocessor(TIME_THRESHOLD_5);
        List<TimeRangedText> initialList = new ArrayList<>();
        initialList.add(new TimeRangedText(0, 3, TEXT_A));
        initialList.add(new TimeRangedText(3, 5, TEXT_B));
        initialList.add(new TimeRangedText(5, 6, TEXT_C));

        List<TimeRangedText> expected = new ArrayList<>();
        expected.add(new TimeRangedText(0, 5, TEXT_A_AND_B));
        expected.add(new TimeRangedText(5, 6, TEXT_C + TEXT_DELIMITER));

        List<TimeRangedText> actual = nlp.setTimeRanges(initialList);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void LongTimeRangeIncluded() {
        // Longer TimeRangedText are included to meet the threshold
        // Time Threshold  : 5
       
        // TimeRangedTexts : |-A-|      
        //                       |-----B-----|
 
        // Expected Results: |---------------|
        
        NaturalLanguagePreprocessor nlp = new NaturalLanguagePreprocessor(TIME_THRESHOLD_5);
        List<TimeRangedText> initialList = new ArrayList<>();
        initialList.add(new TimeRangedText(0, 3, TEXT_A));
        initialList.add(new TimeRangedText(3, 100, TEXT_B));

        List<TimeRangedText> expected = new ArrayList<>();
        expected.add(new TimeRangedText(0, 100, TEXT_A_AND_B));

        List<TimeRangedText> actual = nlp.setTimeRanges(initialList);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void OneRangeInsideOfAnother() {
        // One TimeRangedText is completely included in the range of another
        // Time Threshold  : 2
       
        // TimeRangedTexts : |------A------|      
        //                         |--B--|
 
        // Expected Results: |-------------|
        //                         |-----|
        
        NaturalLanguagePreprocessor nlp = new NaturalLanguagePreprocessor(TIME_THRESHOLD_2);
        List<TimeRangedText> initialList = new ArrayList<>();
        initialList.add(new TimeRangedText(0, 10, TEXT_A));
        initialList.add(new TimeRangedText(5, 7, TEXT_B));

        List<TimeRangedText> expected = new ArrayList<>();
        expected.add(new TimeRangedText(0, 10, TEXT_A + TEXT_DELIMITER));
        expected.add(new TimeRangedText(5, 7, TEXT_B + TEXT_DELIMITER));

        List<TimeRangedText> actual = nlp.setTimeRanges(initialList);

        Assert.assertEquals(expected, actual);
    }
    
}