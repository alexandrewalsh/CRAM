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
import com.google.sps.data.YoutubeCaptions;
import com.google.sps.data.TimeRangedText;
import java.util.*;


/** Servlet to retrieve the full captions of a video from datastore */
@WebServlet("/fullcaption")
public class CaptionRetrievalServlet extends HttpServlet {

    private static final String RESPONSE_JSON_CONTENT = "application/json;";
    private static final String REQUEST_JSON_PARAM = "json";
    private static final String REQUEST_ID_PARAM = "id";
    private static final String REQUEST_NO_METADATA_PARAM = "no_metadata";
    private static final String RESPONSE_VIDEO_ID_NOT_IN_DB = "{}";
    private static final String EXCEPTION_JSON_START = "{ \"ERROR\": ";
    private static final String EXCEPTION_JSON_END = "}";
    private static final String METADATA_KEY = "METADATA";
    private static final String DB_NO_METADATA = "no_metadata";
    private static final String VIDEO_URL_ID_DELIMITER = "v=";
    private static final char URL_QUERY_DELIMITER = '&';

    /**
     * Retrieves full captions from backend and serves them to frontend
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CaptionStorageManager dbi = new CaptionStorageManager();
        Gson gson = new Gson();
        String videoID = (String) request.getParameter(REQUEST_ID_PARAM);
        response.setContentType(RESPONSE_JSON_CONTENT);
        String captionText = "";

        try {
            if (videoID == null || !(dbi.videoInDb(videoID))) {
                response.getWriter().println(RESPONSE_VIDEO_ID_NOT_IN_DB);
                return;
            }
            List<TimeRangedText> full_captions = dbi.getFullCaptions(videoID);
            response.getWriter().println(gson.toJson(full_captions));
        } catch (CaptionStorageException e) {
            String exceptionString = EXCEPTION_JSON_START + e.getReason().toString() + EXCEPTION_JSON_END;
            response.getWriter().println(exceptionString);
        }
    }

}