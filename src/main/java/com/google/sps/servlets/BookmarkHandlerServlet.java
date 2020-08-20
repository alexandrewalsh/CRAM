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


/** Servlet that interacts with setting and fetching bookmarks */
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

    private BookmarkStorageInterface storage;

    /**
     * Gets the bookmarks of the current video and user
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = (String) request.getParameter(REQUEST_EMAIL_PARAM);
        String videoId = (String) request.getParameter(REQUEST_VIDEO_ID_PARAM);
        response.setContentType(RESPONSE_JSON_CONTENT);

        // Tries to get all the bookmarks for the current user and video
        if (storage == null) {
            storage = new BookmarkStorageManager();
        }
        List<Bookmark> bookmarks = new ArrayList<>();
        try {
            bookmarks = storage.getAllBookmarks(email, videoId);
        } catch (BookmarkStorageException e) {
            response.getWriter().println(e.getReasonAsJsonString());
            return;
        }

        // Converts the fetched database data and passes it to the front end
        Gson gson = new Gson();
        String res = gson.toJson(bookmarks);
        response.getWriter().println(res);
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
        if (storage == null) {
            storage = new BookmarkStorageManager();
        }        
        response.setContentType(RESPONSE_JSON_CONTENT);

        // Case 1: Post request is used to add a bookmark
        if (function.equals(REQUEST_ADD_FUNCTION)) {
            long timestamp = Long.parseLong((String) request.getParameter(REQUEST_TIMESTAMP_PARAM));
            String title = (String) request.getParameter(REQUEST_TITLE_PARAM);
            String content = (String) request.getParameter(REQUEST_CONTENT_PARAM);
            try {
                storage.addBookmark(email, videoId, timestamp, title, content);
            } catch (BookmarkStorageException e) {
                response.getWriter().println(e.getReasonAsJsonString());
                return;
            }
        }

        // Case 2: Post request is used to remove a bookmark
        if (function.equals(REQUEST_REMOVE_FUNCTION)) {
            String bookmarkId = (String) request.getParameter(REQUEST_BOOKMARK_ID_PARAM);
            try {
                storage.removeBookmark(bookmarkId);
            } catch (BookmarkStorageException e) {
                response.getWriter().println(e.getReasonAsJsonString());
                return;
            }
        }

        // After performing database writes, fetch all bookmarks to update front-end
        List<Bookmark> bookmarks = new ArrayList<>();
        try {
            bookmarks = storage.getAllBookmarks(email, videoId);
        } catch (BookmarkStorageException e) {
            response.getWriter().println(e.getReasonAsJsonString());
            return;
        }

        // Converts the fetched database results to JSON
        Gson gson = new Gson();
        response.getWriter().println(gson.toJson(bookmarks));

    }

    /**
     * Sets the BookmarkStorageInterface object to be used my the servlet, used for testing purposes
     * @param storage The BookmarkStorageInterface implementation to set
     */
    public void setBookmarkStorageInterface(BookmarkStorageInterface storage) {
        this.storage = storage;
    }

}
