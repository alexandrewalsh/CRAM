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

import com.google.sps.data.INaturalLanguage;
import com.google.sps.data.NaturalLanguageMock;
import com.google.sps.servlets.NaturalLanguageServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.IOException;
import java.io.PrintWriter;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Integration tests for NaturalLanguageServlet servlet */
@RunWith(JUnit4.class)
public final class NaturalLanguageServletTest {

    private static final String REQUEST_JSON_PARAM = "json";
    private static final String REQUEST_MOCK_PARAM = "mock";
    private static final String FAILED_TEST_MESSAGE = "Failed: Exception occurred when running servlet. Expected results potentially do not match actual output.";

    HttpServletRequest request = null;
    HttpServletResponse response = null;
    PrintWriter writer = null;
    NaturalLanguageServlet servlet = null;

    @Before
    public void Setup() {
        // Instantiates mock and real objeccts
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        writer = mock(PrintWriter.class);
        servlet = new NaturalLanguageServlet();

        // Adds stubbing that does not require specific parameters
        when(request.getParameter(REQUEST_MOCK_PARAM)).thenReturn(REQUEST_MOCK_PARAM);
        try { // Note: exception handling is needed here due to PrintWriter objects throwing IOExceptions
            when(response.getWriter()).thenReturn(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void SingleCaptionTest() {
        // Basic integration test using one caption with two entities
        // Captions Input : [{startTime: 0, endTime: 20, text: "Hello,World"}]
        // Expected Output: {"Hello":[0],"World":[0]} 

        // Defines mock input and expected output
        String mockCaptions = "{\"url\": \"mock\", \"captions\": [{\"startTime\": 0, \"endTime\": 20, \"text\": \"Hello,World\"}]}";
        String expectedResults = "{\"Hello\":[0],\"World\":[0]}";

        // Defines stubbing of inserting mock captions
        when(request.getParameter(REQUEST_JSON_PARAM)).thenReturn(mockCaptions);

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(eq(expectedResults)));

        // Runs doPost and fails if exceptions are thrown
        try { 
            servlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

    @Test
    public void NoCaptionsTest() {
        // Basic integration test using one caption with two entities
        // Captions Input : []
        // Expected Output: {} 

        // Defines mock input and expected output
        String mockCaptions = "{\"url\": \"mock\", \"captions\": []}";
        String expectedResults = "{}";

        // Defines stubbing of inserting mock captions
        when(request.getParameter(REQUEST_JSON_PARAM)).thenReturn(mockCaptions);

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(eq(expectedResults)));

        // Runs doPost and fails if exceptions are thrown
        try { 
            servlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

    @Test
    public void SameWordsTest() {
        // Basic integration test using one caption with two entities
        // Captions Input : []
        // Expected Output: {} 

        // Defines mock input and expected output
        String mockCaptions = "{\"url\": \"mock\", \"captions\": []}";
        String expectedResults = "{}";

        // Defines stubbing of inserting mock captions
        when(request.getParameter(REQUEST_JSON_PARAM)).thenReturn(mockCaptions);

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(eq(expectedResults)));

        // Runs doPost and fails if exceptions are thrown
        try { 
            servlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

}