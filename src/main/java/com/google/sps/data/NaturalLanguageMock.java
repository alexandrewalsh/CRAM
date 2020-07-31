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

package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/** A mock object for Natural Language processing to avoid external API calls */
public class NaturalLanguageMock implements INaturalLanguage {

    // Delimiter that defines seperated entities from text input
    private static final String MOCK_TEXT_DELIMITER = ",";

    /**
     * Gets a list of entities by splitting them by the comma delimiter
     * @param text The String text to extract entities from
     * @return The list of entities from the text
     */
    public List<String> getEntities(String text) {
        // Define objects to be used
        String[] components = text.split(MOCK_TEXT_DELIMITER);
        Set<String> uniqueComponenets = new HashSet<>();

        // Trims strings and uses set to remove duplicates
        for (String component : components) {
            uniqueComponenets.add(component.trim());
        }
        return Collections.unmodifiableList(new ArrayList<String>(uniqueComponenets));
    }

}