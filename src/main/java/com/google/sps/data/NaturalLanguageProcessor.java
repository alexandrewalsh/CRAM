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

import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.ClassificationCategory;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.language.v1.LanguageServiceClient;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.lang.Exception;

/** Picks entities from text using Google's NLP API */
public class NaturalLanguageProcessor {

    // Error strings for exceptions
    private static final String CATEGORY_EXCEPTION = "Exception caught when trying to find the category of the input.";
    private static final String ENTITIES_EXCEPTION = "Exception caught when trying to find the entities of the input.";

    private static final double DEFAULT_SALIENCE_THRESHOLD = 0.01;
    private static final double ACADEMIC_SALIENCE_THRESHOLD = 0.02;
    private static final double CATEGORY_THRESHOLD = 0.7;
    private static final String WORD_DELIMITER = " ";
    private static final String CATEGORY_DELIMITER = "/";
    private static final int MIN_TOKENS_FOR_CATEGORY_CLASSIFICATION = 20;
    private static final int CATEGORY_INDEX = 1;
    private static final String[] ACADEMIC_CATEGORIES = {
        "Arts & Entertainment", 
        "Books & Literature", 
        "Business & Industrial", 
        "Computers & Electronics", 
        "Health", 
        "Internet & Telecom", 
        "Jobs & Education", 
        "Law & Government", 
        "Reference", 
        "Science"
    };

    private double defaultThreshold;
    private double academicThreshold;
    private double categoryThreshold;

    /**
     * Constructor for NaturalLanguageProcessor with default thresholds
     */
    public NaturalLanguageProcessor() {
        this.defaultThreshold = DEFAULT_SALIENCE_THRESHOLD;
        this.academicThreshold = ACADEMIC_SALIENCE_THRESHOLD;
        this.categoryThreshold = CATEGORY_THRESHOLD;
    }

    /**
     * Constructor for NaturalLanguageProcessor with threshold parameters
     * @param defaultThreshold The minimum salience threshold without academic context
     * @param academicThreshold The minimum salience threshold for entities in an academic context
     * @param categoryThreshold The minimum category confidence for the category to apply
     */
    public NaturalLanguageProcessor(double defaultThreshold, double academicThreshold, double categoryThreshold) {
        this.defaultThreshold = defaultThreshold;
        this.academicThreshold = academicThreshold;
        this.categoryThreshold = categoryThreshold;
    }

    /**
     * Gets a list of entities using Google's NLP API
     * @param text The string that contains the text to pull entities from
     * @return The list of entities
     */
    public List<String> getEntities(String text) {
        Set<String> entities = new HashSet<String>();
        int tokens = text.split(WORD_DELIMITER).length;
        boolean hasAcademicCategory = false;

        // Calls the NLP API, but returns null if the API call fails
        try (LanguageServiceClient language = LanguageServiceClient.create()) {
            
            Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();

            // Attempts to classify the text if there are more than 20 words
            if (tokens >= MIN_TOKENS_FOR_CATEGORY_CLASSIFICATION) {
                hasAcademicCategory = checkAcademicCategory(language, doc);
            }
            
            double salienceThreshold = hasAcademicCategory ? academicThreshold : defaultThreshold;
            
            // Builds request to find all entities in the text
            AnalyzeEntitiesRequest entitiesRequest = 
                AnalyzeEntitiesRequest.newBuilder()
                .setDocument(doc)
                .setEncodingType(EncodingType.UTF16)
                .build();

            // Adds all entities that have a salience above the threshold
            AnalyzeEntitiesResponse entitiesResponse = language.analyzeEntities(entitiesRequest);
            for (Entity entity : entitiesResponse.getEntitiesList()) {
                if (entity.getSalience() >= salienceThreshold) {
                    entities.add(entity.getName().toLowerCase());
                }
            }

        } catch (Exception e) {
            System.out.println(ENTITIES_EXCEPTION);
            return null;
        }

        // Converts set to an unmodifiable list as the return object
        return Collections.unmodifiableList(new ArrayList<String>(entities));
    }

    /**
     * Checks if the text in the document is in an academic category
     * @param language The instance of the NLP API object
     * @param doc The document object that contains the the text to be analyzed
     * @return Whether the text is in an academic category
     */
    public boolean checkAcademicCategory(LanguageServiceClient language, Document doc) {
        // Calls the API and tries to match the found categories with the array of academic categories
        try {
            ClassifyTextRequest classifyRequest = ClassifyTextRequest.newBuilder().setDocument(doc).build();
            ClassifyTextResponse classifyResponse = language.classifyText(classifyRequest);
            for (ClassificationCategory category : classifyResponse.getCategoriesList()) {
                if (category.getConfidence() < this.categoryThreshold) {
                    continue;
                }
                String target = category.getName().split(CATEGORY_DELIMITER)[CATEGORY_INDEX];

                if (Arrays.stream(ACADEMIC_CATEGORIES).anyMatch(x -> target.equals(x))) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println(CATEGORY_EXCEPTION);
            return false;
        }
        return false;
    }

}