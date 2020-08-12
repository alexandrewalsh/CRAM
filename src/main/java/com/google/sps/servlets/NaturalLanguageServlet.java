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

import com.google.sps.storage.*;
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
    private static final String REQUEST_ID_PARAM = "id";
    private static final String REQUEST_NO_METADATA_PARAM = "no_metadata";
    private static final String RESPONSE_VIDEO_ID_NOT_IN_DB = "{}";
    private static final String METADATA_KEY = "METADATA";
    private static final String DB_NO_METADATA = "no_metadata";
    private static final String VIDEO_URL_ID_DELIMITER = "v=";
    private static final char URL_QUERY_DELIMITER = '&';

    private NaturalLanguageProcessor nlp;
    private CaptionStorageInterface db;


    /**
     * Gets database data for comments
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CaptionStorage dbi = new CaptionStorage();
        Gson gson = new Gson();
        String videoID = (String) request.getParameter(REQUEST_ID_PARAM);
        response.setContentType(RESPONSE_JSON_CONTENT);

        // Returns the entities mapping if the video id is in the database
        try {
            if (videoID != null && dbi.videoInDb(videoID)) {
                Map<String, List<Long>> resultMap = dbi.getAllKeywords(videoID);
                response.getWriter().println(gson.toJson(resultMap));
            } else {
                response.getWriter().println(RESPONSE_VIDEO_ID_NOT_IN_DB);
            }
        } catch (CaptionStorageException e) {
            // TODO: what to do if we get an exception from videoInDb() or getAllKeywords()?
        }
    }


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
        if (this.db == null) {
            this.db = new CaptionStorage();
        }

        // Builds the Java object from JSON and preprocesses the captions by redefining time ranges
        YoutubeCaptions youtubeCaptions = gson.fromJson(json, YoutubeCaptions.class);

        // Parses the video ID from the url
        boolean addToDatabase = true;
        String videoID = "";
        String[] urlParts = youtubeCaptions.getURL().split(VIDEO_URL_ID_DELIMITER);
        if (urlParts.length <= 1) {
            addToDatabase = false;
        }
        if (addToDatabase) {
            videoID = urlParts[1];
            int ampersandPosition = videoID.indexOf(URL_QUERY_DELIMITER);
            if (ampersandPosition != -1) {
                videoID = videoID.substring(0, ampersandPosition);
            }
            try {
                db.addVideo(videoID, DB_NO_METADATA);
            } catch (CaptionStorageException e) {
                // TODO: what to do if we get an exception from addVideo()?
            }
        }

        int numCaptions = youtubeCaptions.getCaptions().size();
        NaturalLanguagePreprocessor preprocessor = new NaturalLanguagePreprocessor();
        List<TimeRangedText> preprocessedResults = preprocessor.setTimeRanges(youtubeCaptions.getCaptions());
        
        // Sends the text of newly defined time ranges to the NLP API and organizes the results in the postprocessor
        NaturalLanguagePostprocessor postprocessor = new NaturalLanguagePostprocessor();
        for (TimeRangedText text : preprocessedResults) {
            postprocessor.addEntities(nlp.getEntities(text.getText()), text.getStartTime());
        }
        Map<String, List<Long>> resultMap = postprocessor.getEntitiesMap();

        // Adds clauses to database if a video id has been found
        if (addToDatabase) {
            try {
                db.addClauses(videoID, resultMap);
            } catch (CaptionStorageException e) {
                // TODO: what to do if we get an exception from addClauses()?
            }
        }
        
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

    /**
     * Sets the NaturalLanguageProcessor instance for the servlet to use
     * @param nlp The NaturalLanguageProcessor instance to use
     */
    public void setNaturalLanguageProcessor(NaturalLanguageProcessor nlp) {
        this.nlp = nlp;
    }

    /**
     * Sets the CaptionStorageInterface instance for the servlet to use
     * @param db The CaptionStorageInterface instance to use
     */
    public void setDatabase(CaptionStorageInterface db) {
        this.db = db;
    }


}