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

package com.google.sps.data;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/** Postprocessing for outputs of the NLP API that maps a keyphrase to all mention times */
public class NaturalLanguagePostprocessor {
    
    // Mapping of String keyphrases to a list of Long timestamps where the keyphrase is detected
    private Map<String, List<Long>> entitiesMap;

    /** 
     * Constructor for the postprocessor object
     */
    public NaturalLanguagePostprocessor() {
        entitiesMap = new HashMap<>();
    }

    /**
     * Adds a list of entities with the time range for that list to the compiled map
     * @param entities The list of keyphrases of the current iteration of the NLP output
     * @param startTime The start time of the list of entities mentions
     */
    public void addEntities(List<String> entities, long startTime) {
        for (String entity : entities) {
            if (!entitiesMap.containsKey(entity)) {
                entitiesMap.put(entity, new ArrayList<Long>());
            } 
            entitiesMap.get(entity).add(startTime);
        }
    }

    /**
     * Gets the entities mapping that is currently built
     * @return The currently built entities mapping
     */
    public Map<String, List<Long>> getEntitiesMap() {
        return entitiesMap;
    }
}
