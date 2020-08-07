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

import com.google.sps.database.DatabaseImpl;
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
    private static final String METADATA_KEY = "METADATA";


    /**
     * Gets database data for comments
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DatabaseImpl dbi = new DatabaseImpl();
        Gson gson = new Gson();
        String videoID = (String) request.getParameter(REQUEST_ID_PARAM);

        response.setContentType(RESPONSE_JSON_CONTENT);

        if (dbi.videoInDb(videoID)) {
            Map<String, List<Long>> resultMap = dbi.getAllKeywords(videoID);
            response.getWriter().println(gson.toJson(resultMap));
        } else {
            response.getWriter().println("{}");
        }
    }


    /**
     * Gets database data for comments
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        DatabaseImpl dbi = new DatabaseImpl();

        long startTime = System.nanoTime();

        String json = (String) request.getParameter(REQUEST_JSON_PARAM);
        List<String> entities = new ArrayList<String>();
        Gson gson = new Gson();

        // Builds the Java object from JSON and preprocesses the captions by redefining time ranges
        YoutubeCaptions youtubeCaptions = gson.fromJson(json, YoutubeCaptions.class);

        String url = youtubeCaptions.getURL();
        String video_id = url.split("v=")[1];
        int ampersandPosition = video_id.indexOf('&');
        if(ampersandPosition != -1) {
            video_id = video_id.substring(0, ampersandPosition);
        }
        dbi.addVideo(video_id, "no metadata");

        int numCaptions = youtubeCaptions.getCaptions().size();
        NaturalLanguagePreprocessor preprocessor = new NaturalLanguagePreprocessor();
        List<TimeRangedText> preprocessedResults = preprocessor.setTimeRanges(youtubeCaptions.getCaptions());
        
        // Sends the text of newly defined time ranges to the NLP API and organizes the results in the postprocessor
        NaturalLanguageProcessor nlp = new NaturalLanguageProcessor();
        NaturalLanguagePostprocessor postprocessor = new NaturalLanguagePostprocessor();
        for (TimeRangedText text : preprocessedResults) {
            postprocessor.addEntities(nlp.getEntities(text.getText()), text.getStartTime());
        }
        Map<String, List<Long>> resultMap = postprocessor.getEntitiesMap();
        dbi.addClauses(video_id, resultMap);
        
        long endTime = System.nanoTime();
        
        // Adds metadata to the result
        List<Long> metadataList = new ArrayList<>();
        metadataList.add((long)numCaptions); // Number of captions passed in by the request
        metadataList.add((long)(endTime - startTime)); // Amount of time the NLP process takes (in ns)
        metadataList.add((long)resultMap.size() - 1); // Number of entities found
        resultMap.put(METADATA_KEY, metadataList);

        // Converts Java object to JSON and sends it back to the front end
        response.setContentType(RESPONSE_JSON_CONTENT);
        response.getWriter().println(gson.toJson(resultMap));
    }


}