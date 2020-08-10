// Copyright 2020 Google LLC
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
import java.util.Map;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.sps.data.TimeRangedText;
import com.google.sps.data.NaturalLanguagePostprocessor;


/** Tests for the NaturalLanguagePostprocessor class methods */
@RunWith(JUnit4.class)
public final class NaturalLanguagePostprocessorTest {

    private static final String ENTITY_A = "A";
    private static final String ENTITY_B = "B";
    private static final String ENTITY_C = "C";

    @Test
    public void DifferentEntitiesInDifferentTimes() {
        // Different entities will be found in different times, with all entities and times being unique

        // Time 1: [A]
        // Time 2: [B]
        // Time 3: [C]

        // Expected: {A: [1], B:[2], C:[3]}
        
        NaturalLanguagePostprocessor nlp = new NaturalLanguagePostprocessor();
        List<String> time1 = Arrays.asList(new String[] {ENTITY_A}); 
        List<String> time2 = Arrays.asList(new String[] {ENTITY_B}); 
        List<String> time3 = Arrays.asList(new String[] {ENTITY_C}); 

        nlp.addEntities(time1, 1);
        nlp.addEntities(time2, 2);
        nlp.addEntities(time3, 3);

        Map<String, List<Long>> expected = new HashMap<>();
        expected.put(ENTITY_A, Arrays.asList(new Long[] {1L}));
        expected.put(ENTITY_B, Arrays.asList(new Long[] {2L}));
        expected.put(ENTITY_C, Arrays.asList(new Long[] {3L}));

        Map<String, List<Long>> actual = nlp.getEntitiesMap();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void SameEntitiesInDifferentTimes() {
        // Same entities will be found in different times

        // Time 1: [A]
        // Time 2: [B]
        // Time 3: [A]
        // Time 4: [B]

        // Expected: {A: [1, 3], B: [2, 4]}
        
        NaturalLanguagePostprocessor nlp = new NaturalLanguagePostprocessor();
        List<String> time1 = Arrays.asList(new String[] {ENTITY_A}); 
        List<String> time2 = Arrays.asList(new String[] {ENTITY_B}); 
        List<String> time3 = Arrays.asList(new String[] {ENTITY_A}); 
        List<String> time4 = Arrays.asList(new String[] {ENTITY_B}); 

        nlp.addEntities(time1, 1);
        nlp.addEntities(time2, 2);
        nlp.addEntities(time3, 3);
        nlp.addEntities(time4, 4);

        Map<String, List<Long>> expected = new HashMap<>();
        expected.put(ENTITY_A, Arrays.asList(new Long[] {1L, 3L}));
        expected.put(ENTITY_B, Arrays.asList(new Long[] {2L, 4L}));

        Map<String, List<Long>> actual = nlp.getEntitiesMap();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void DifferentEntitiesInSameTimes() {
        // Different entities will be found in the same times

        // Time 1: [A, B, C]
        // Time 2: [A, C]
        // Time 3: [B, C]
        // Time 4: [A, B]

        // Expected: {A: [1, 2, 4], B: [1, 3, 4], C: [1, 2, 3]}
        
        NaturalLanguagePostprocessor nlp = new NaturalLanguagePostprocessor();
        List<String> time1 = Arrays.asList(new String[] {ENTITY_A, ENTITY_B, ENTITY_C}); 
        List<String> time2 = Arrays.asList(new String[] {ENTITY_A, ENTITY_C}); 
        List<String> time3 = Arrays.asList(new String[] {ENTITY_B, ENTITY_C}); 
        List<String> time4 = Arrays.asList(new String[] {ENTITY_A, ENTITY_B}); 

        nlp.addEntities(time1, 1);
        nlp.addEntities(time2, 2);
        nlp.addEntities(time3, 3);
        nlp.addEntities(time4, 4);

        Map<String, List<Long>> expected = new HashMap<>();
        expected.put(ENTITY_A, Arrays.asList(new Long[] {1L, 2L, 4L}));
        expected.put(ENTITY_B, Arrays.asList(new Long[] {1L, 3L, 4L}));
        expected.put(ENTITY_C, Arrays.asList(new Long[] {1L, 2L, 3L}));

        Map<String, List<Long>> actual = nlp.getEntitiesMap();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void SameEntitiesInSameTimes() {
        // The same entities will be found in the same times

        // Time 1: [A, A, A]
        // Time 2: [B, B]
        // Time 3: [C]
        
        // Expected: {A: [1, 1, 1], B: [2, 2], C: [3]}
       
        // NOTE: This test is only done to show functionality. 
        //       In our NLP pipeline, we ensure that the same entities in the same time never occurs

        NaturalLanguagePostprocessor nlp = new NaturalLanguagePostprocessor();
        List<String> time1 = Arrays.asList(new String[] {ENTITY_A, ENTITY_A, ENTITY_A}); 
        List<String> time2 = Arrays.asList(new String[] {ENTITY_B, ENTITY_B}); 
        List<String> time3 = Arrays.asList(new String[] {ENTITY_C}); 

        nlp.addEntities(time1, 1);
        nlp.addEntities(time2, 2);
        nlp.addEntities(time3, 3);

        Map<String, List<Long>> expected = new HashMap<>();
        expected.put(ENTITY_A, Arrays.asList(new Long[] {1L, 1L, 1L}));
        expected.put(ENTITY_B, Arrays.asList(new Long[] {2L, 2L}));
        expected.put(ENTITY_C, Arrays.asList(new Long[] {3L}));

        Map<String, List<Long>> actual = nlp.getEntitiesMap();

        Assert.assertEquals(expected, actual);
    }
    
}