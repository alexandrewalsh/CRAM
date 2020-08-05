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

package com.google.sps.servlets;

import java.io.IOException;
import java.lang.InterruptedException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.sps.data.NaturalLanguagePreprocessor;
import com.google.sps.data.NaturalLanguageProcessor;
import com.google.sps.data.NaturalLanguagePostprocessor;
import com.google.sps.data.YoutubeCaptions;
import com.google.sps.data.TimeRangedText;
import java.util.*;


/** Servlet that interacts with Google's NLP API */
@WebServlet("/caption")
public class NaturalLanguageServlet extends HttpServlet {

    private static final String RESPONSE_JSON_CONTENT = "application/json;";
    private static final String REQUEST_JSON_PARAM = "json";
    private static final String REQUEST_NO_METADATA_PARAM = "no_metadata";
    private static final String METADATA_KEY = "METADATA";

    private NaturalLanguageProcessor nlp;

    /**
     * Gets database data for comments
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        boolean includeMetadata = true;
        long startTime = System.nanoTime();

        String json = (String) request.getParameter(REQUEST_JSON_PARAM);
        String noMetadata = (String) request.getParameter(REQUEST_NO_METADATA_PARAM);
        List<String> entities = new ArrayList<String>();
        Gson gson = new Gson();

        if (noMetadata != null) {
            includeMetadata = false;
        }

        if (this.nlp == null) {
            this.nlp = new NaturalLanguageProcessor();
        }

        // Builds the Java object from JSON and preprocesses the captions by redefining time ranges
        YoutubeCaptions youtubeCaptions = gson.fromJson(json, YoutubeCaptions.class);
        int numCaptions = youtubeCaptions.getCaptions().size();
        NaturalLanguagePreprocessor preprocessor = new NaturalLanguagePreprocessor();
        List<TimeRangedText> preprocessedResults = preprocessor.setTimeRanges(youtubeCaptions.getCaptions());
        
        // Sends the text of newly defined time ranges to the NLP API and organizes the results in the postprocessor
        NaturalLanguagePostprocessor postprocessor = new NaturalLanguagePostprocessor();
        for (TimeRangedText text : preprocessedResults) {
            postprocessor.addEntities(nlp.getEntities(text.getText()), text.getStartTime());
        }
        Map<String, List<Long>> resultMap = postprocessor.getEntitiesMap();
        
        long endTime = System.nanoTime();
        
        // Adds metadata to the result
        if (includeMetadata) {
            List<Long> metadataList = new ArrayList<>();
            metadataList.add((long)numCaptions); // Number of captions passed in by the request
            metadataList.add((long)(endTime - startTime)); // Amount of time the NLP process takes (in ns)
            metadataList.add((long)resultMap.size()); // Number of entities found
            resultMap.put(METADATA_KEY, metadataList);
        }

        // Converts Java object to JSON and sends it back to the front end
        response.setContentType(RESPONSE_JSON_CONTENT);
        String result = gson.toJson(resultMap);
        response.getWriter().println(result);
    }

    public void setNaturalLanguageProcessor(NaturalLanguageProcessor nlp) {
        this.nlp = nlp;
    }
}