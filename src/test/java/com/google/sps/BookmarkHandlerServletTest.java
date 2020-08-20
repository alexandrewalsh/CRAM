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

import com.google.sps.servlets.BookmarkHandlerServlet;
import com.google.sps.storage.BookmarkStorageManager;
import com.google.sps.storage.BookmarkStorageInterface;
import com.google.sps.storage.BookmarkStorageException;
import com.google.sps.data.Bookmark;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.lang.Throwable;
import java.io.IOException;
import java.io.PrintWriter;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.Mockito.*;

/** Integration tests for NaturalLanguageServlet servlet */
@RunWith(JUnit4.class)
public final class BookmarkHandlerServletTest {

    private static final String FAILED_TEST_MESSAGE = "Failed: Exception occurred when running servlet. Expected results potentially do not match actual output.";
    private static final String REQUEST_EMAIL_PARAM = "email";
    private static final String REQUEST_VIDEO_ID_PARAM = "videoId";
    private static final String REQUEST_TIMESTAMP_PARAM = "timestamp";
    private static final String REQUEST_TITLE_PARAM = "title";
    private static final String REQUEST_CONTENT_PARAM = "content";
    private static final String REQUEST_BOOKMARK_ID_PARAM = "bookmarkId";
    private static final String REQUEST_FUNCTION_PARAM = "function";
    private static final String REQUEST_ADD_FUNCTION = "add";
    private static final String REQUEST_REMOVE_FUNCTION = "remove";
    private static final String MOCK_VALUE = "MOCK";
    private static final String MOCK_ZERO = "0";

    HttpServletRequest request = null;
    HttpServletResponse response = null;
    BookmarkHandlerServlet servlet = null;
    PrintWriter writer = null;
    BookmarkStorageInterface db = null;

    @Before
    public void Setup() throws IOException {
        // Instantiates mock and real objeccts
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        writer = mock(PrintWriter.class);
        db = mock(BookmarkStorageManager.class);
        servlet = new BookmarkHandlerServlet();

        // Adds stubbing that does not require specific parameters
        when(request.getParameter(REQUEST_EMAIL_PARAM)).thenReturn(MOCK_VALUE);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    public void GetVideoBookmarks() {
        // Basic integration test for the doGet method with successful output
        // Input : {email: MOCK, videoId: MOCK}
        // Output: List of Bookmarks as JSON String

        // Defines the mock outputs
        List<Bookmark> expectedList = new ArrayList<>();
        expectedList.add(new Bookmark(MOCK_VALUE, 0, MOCK_VALUE, MOCK_VALUE));
        String expectedResults = "[{\"id\":\"MOCK\",\"timestamp\":0,\"title\":\"MOCK\",\"content\":\"MOCK\"}]";

        // Defines stubbing of the Youtube video id 
        when(request.getParameter(REQUEST_VIDEO_ID_PARAM)).thenReturn(MOCK_VALUE);
        // Defines stubbing of database calls
        try {
            when(db.getAllBookmarks(MOCK_VALUE, MOCK_VALUE)).thenReturn(expectedList);
        } catch(BookmarkStorageException e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(eq(expectedResults)));

        // Runs doGet and fails if exceptions are thrown
        try { 
            servlet.setBookmarkStorageInterface(db);
            servlet.doGet(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

    @Test
    public void GetVideoBookmarksException() {
        // Basic integration test for the doGet method with thrown exception
        // Input : {email: MOCK, videoId: MOCK}
        // Output: Exception data as JSON object

        // Defines the mock outputs
        String expectedResults = "{\"ERROR\":\"GetBookmarksException\"}";

        // Defines stubbing of the Youtube video id 
        when(request.getParameter(REQUEST_VIDEO_ID_PARAM)).thenReturn(MOCK_VALUE);
        // Defines stubbing of database calls
        try {
            when(db.getAllBookmarks(MOCK_VALUE, MOCK_VALUE)).thenThrow(new BookmarkStorageException("GetBookmarksException", MOCK_VALUE, new Throwable()));
        } catch(BookmarkStorageException e) {}

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(eq(expectedResults)));

        // Runs doGet and fails if exceptions are thrown
        try { 
            servlet.setBookmarkStorageInterface(db);
            servlet.doGet(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

    @Test
    public void AddBookmark() {
        // Basic integration test for the doPost method with adding bookmarks
        // Input : {function: add, email: MOCK, videoId: MOCK, timestamp: 0, title: MOCK, content: MOCK}
        // Output: List of Bookmarks as JSON String

        // Defines the mock outputs
        List<Bookmark> expectedList = new ArrayList<>();
        expectedList.add(new Bookmark(MOCK_VALUE, 0, MOCK_VALUE, MOCK_VALUE));
        String expectedResults = "[{\"id\":\"MOCK\",\"timestamp\":0,\"title\":\"MOCK\",\"content\":\"MOCK\"}]";

        // Defines stubbing of the request params 
        when(request.getParameter(REQUEST_VIDEO_ID_PARAM)).thenReturn(MOCK_VALUE);
        when(request.getParameter(REQUEST_TITLE_PARAM)).thenReturn(MOCK_VALUE);
        when(request.getParameter(REQUEST_CONTENT_PARAM)).thenReturn(MOCK_VALUE);
        when(request.getParameter(REQUEST_TIMESTAMP_PARAM)).thenReturn(MOCK_ZERO);
        when(request.getParameter(REQUEST_FUNCTION_PARAM)).thenReturn(REQUEST_ADD_FUNCTION);

        // Defines stubbing of database calls
        try {
            doThrow(new RuntimeException()).when(db).addBookmark(not(eq(MOCK_VALUE)), not(eq(MOCK_VALUE)), not(eq(0)), not(eq(MOCK_VALUE)), not(eq(MOCK_VALUE)));
            when(db.getAllBookmarks(MOCK_VALUE, MOCK_VALUE)).thenReturn(expectedList);
        } catch(Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(eq(expectedResults)));

        // Runs doPost and fails if exceptions are thrown
        try { 
            servlet.setBookmarkStorageInterface(db);
            servlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

    @Test
    public void RemoveBookmark() {
        // Basic integration test for the doPost method with removing bookmarks
        // Input : {function: remove, email: MOCK, videoId: MOCK, bookmarkId: MOCK}
        // Output: List of Bookmarks as JSON String

        // Defines the mock outputs
        List<Bookmark> expectedList = new ArrayList<>();
        String expectedResults = "[]";

        // Defines stubbing of the request params 
        when(request.getParameter(REQUEST_VIDEO_ID_PARAM)).thenReturn(MOCK_VALUE);
        when(request.getParameter(REQUEST_BOOKMARK_ID_PARAM)).thenReturn(MOCK_VALUE);
        when(request.getParameter(REQUEST_FUNCTION_PARAM)).thenReturn(REQUEST_REMOVE_FUNCTION);

        // Defines stubbing of database calls
        try {
            doThrow(new RuntimeException()).when(db).removeBookmark(not(eq(MOCK_VALUE)));
            when(db.getAllBookmarks(MOCK_VALUE, MOCK_VALUE)).thenReturn(expectedList);
        } catch(BookmarkStorageException e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(eq(expectedResults)));

        // Runs doPost and fails if exceptions are thrown
        try { 
            servlet.setBookmarkStorageInterface(db);
            servlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

    @Test
    public void AddBookmarkException() {
        // Basic integration test for the doPost method with adding bookmarks with thrown exception
        // Input : {function: add, email: MOCK, videoId: MOCK, timestamp: 0, title: MOCK, content: MOCK}
        // Output: Exception data as JSON object

        // Defines the mock outputs
        String expectedResults = "{\"ERROR\":\"AddBookmarkException\"}";

        // Defines stubbing of the request params 
        when(request.getParameter(REQUEST_VIDEO_ID_PARAM)).thenReturn(MOCK_VALUE);
        when(request.getParameter(REQUEST_TITLE_PARAM)).thenReturn(MOCK_VALUE);
        when(request.getParameter(REQUEST_CONTENT_PARAM)).thenReturn(MOCK_VALUE);
        when(request.getParameter(REQUEST_TIMESTAMP_PARAM)).thenReturn(MOCK_ZERO);
        when(request.getParameter(REQUEST_FUNCTION_PARAM)).thenReturn(REQUEST_ADD_FUNCTION);

        // Defines stubbing of database calls
        try {
            doThrow(new BookmarkStorageException("AddBookmarkException", MOCK_VALUE, new Throwable())).when(db).addBookmark(MOCK_VALUE, MOCK_VALUE, 0, MOCK_VALUE, MOCK_VALUE);
        } catch(Exception e) {}

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(eq(expectedResults)));

        // Runs doPost and fails if exceptions are thrown
        try { 
            servlet.setBookmarkStorageInterface(db);
            servlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

    @Test
    public void RemoveBookmarkException() {
        // Basic integration test for the doPost method with removing bookmarks with thrown exception
        // Input : {function: remove, email: MOCK, videoId: MOCK, bookmarkId: MOCK}
        // Output: Exception data as JSON object

        // Defines the mock outputs
        String expectedResults = "{\"ERROR\":\"RemoveBookmarkException\"}";

        // Defines stubbing of the request params 
        when(request.getParameter(REQUEST_VIDEO_ID_PARAM)).thenReturn(MOCK_VALUE);
        when(request.getParameter(REQUEST_BOOKMARK_ID_PARAM)).thenReturn(MOCK_VALUE);
        when(request.getParameter(REQUEST_FUNCTION_PARAM)).thenReturn(REQUEST_REMOVE_FUNCTION);

        // Defines stubbing of database calls
        try {
            doThrow(new BookmarkStorageException("RemoveBookmarkException", MOCK_VALUE, new Throwable())).when(db).removeBookmark(MOCK_VALUE);

        } catch(BookmarkStorageException e) {}

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(eq(expectedResults)));

        // Runs doPost and fails if exceptions are thrown
        try { 
            servlet.setBookmarkStorageInterface(db);
            servlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

    @Test
    public void GetBookmarksAfterPostException() {
        // Basic integration test for the doPost method with thrown exception after post
        // Input : {function: remove, email: MOCK, videoId: MOCK, bookmarkId: MOCK}
        // Output: Exception data as JSON object

        // Defines the mock outputs
        String expectedResults = "{\"ERROR\":\"GetBookmarksException\"}";

        // Defines stubbing of the request params 
        when(request.getParameter(REQUEST_VIDEO_ID_PARAM)).thenReturn(MOCK_VALUE);
        when(request.getParameter(REQUEST_BOOKMARK_ID_PARAM)).thenReturn(MOCK_VALUE);
        when(request.getParameter(REQUEST_FUNCTION_PARAM)).thenReturn(REQUEST_REMOVE_FUNCTION);

        // Defines stubbing of database calls
        try {
            doThrow(new BookmarkStorageException("GetBookmarksException", MOCK_VALUE, new Throwable())).when(db).getAllBookmarks(MOCK_VALUE, MOCK_VALUE);
        } catch(BookmarkStorageException e) {}

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(eq(expectedResults)));

        // Runs doPost and fails if exceptions are thrown
        try { 
            servlet.setBookmarkStorageInterface(db);
            servlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

}