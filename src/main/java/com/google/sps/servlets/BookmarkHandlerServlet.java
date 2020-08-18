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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.*;
import com.google.sps.data.Bookmark;
import com.google.sps.storage.BookmarkStorageInterface;
import com.google.sps.storage.BookmarkStorageManager;
import com.google.sps.storage.BookmarkStorageException;


/** Servlet that interacts with Google's NLP API */
@WebServlet("/bookmark")
public class BookmarkHandlerServlet extends HttpServlet {

    private static final String REQUEST_EMAIL_PARAM = "email";
    private static final String REQUEST_VIDEO_ID_PARAM = "videoId";
    private static final String REQUEST_TIMESTAMP_PARAM = "timestamp";
    private static final String REQUEST_TITLE_PARAM = "title";
    private static final String REQUEST_CONTENT_PARAM = "content";
    private static final String REQUEST_BOOKMARK_ID_PARAM = "bookmarkId";
    private static final String REQUEST_FUNCTION_PARAM = "function";
    private static final String REQUEST_ADD_FUNCTION = "add";
    private static final String REQUEST_REMOVE_FUNCTION = "remove";
    private static final String RESPONSE_JSON_CONTENT = "json";

    /**
     * Gets the bookmarks of the current video and user
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = (String) request.getParameter(REQUEST_EMAIL_PARAM);
        String videoId = (String) request.getParameter(REQUEST_VIDEO_ID_PARAM);

        BookmarkStorageInterface storage = new BookmarkStorageManager();
        List<Bookmark> bookmarks = new ArrayList<>();
        try {
            bookmarks = storage.getAllBookmarks(email, videoId);
        } catch (BookmarkStorageException e) {
            // TODO: Handle exception
        }

        Gson gson = new Gson();
        response.setContentType(RESPONSE_JSON_CONTENT);
        response.getWriter().println(gson.toJson(bookmarks));
    }

    /**
     * Adds or removes a bookmark for the current user and video
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        String function = (String) request.getParameter(REQUEST_FUNCTION_PARAM);
        String email = (String) request.getParameter(REQUEST_EMAIL_PARAM);
        String videoId = (String) request.getParameter(REQUEST_VIDEO_ID_PARAM);
        BookmarkStorageInterface storage = new BookmarkStorageManager();

        if (function.equals(REQUEST_ADD_FUNCTION)) {
            long timestamp = Long.parseLong((String) request.getParameter(REQUEST_TIMESTAMP_PARAM));
            String title = (String) request.getParameter(REQUEST_TITLE_PARAM);
            String content = (String) request.getParameter(REQUEST_CONTENT_PARAM);
            try {
                storage.addBookmark(email, videoId, timestamp, title, content);
            } catch (BookmarkStorageException e) {
                // TODO: Handle exception
            }
        }

        if (function.equals(REQUEST_REMOVE_FUNCTION)) {
            String bookmarkId = (String) request.getParameter(REQUEST_BOOKMARK_ID_PARAM);
            try {
                storage.removeBookmark(bookmarkId);
            } catch (BookmarkStorageException e) {
                // TODO: Handle exception
            }
        }

        List<Bookmark> bookmarks = new ArrayList<>();
        try {
            bookmarks = storage.getAllBookmarks(email, videoId);
        } catch (BookmarkStorageException e) {
            // TODO: Handle exception
        }

        Gson gson = new Gson();
        response.setContentType(RESPONSE_JSON_CONTENT);
        response.getWriter().println(gson.toJson(bookmarks));

    }

}