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

import com.google.sps.servlets.NaturalLanguageServlet;
import com.google.sps.data.NaturalLanguageProcessor;
import com.google.sps.storage.CaptionStorage;
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
public final class NaturalLanguageServletTest {

    private static final String REQUEST_JSON_PARAM = "json";
    private static final String REQUEST_NO_METADATA_PARAM = "no_metadata";
    private static final String FAILED_TEST_MESSAGE = "Failed: Exception occurred when running servlet. Expected results potentially do not match actual output.";
    private static final String URL_MOCK = "\"url\": \"mock\"";
    private static final String CAPTIONS = "\"captions\":";
    private static final String START_TIME = "\"startTime\":";
    private static final String END_TIME = "\"endTime\":";
    private static final String TEXT = "\"text\":";

    HttpServletRequest request = null;
    HttpServletResponse response = null;
    NaturalLanguageProcessor nlp = null;
    CaptionStorage db = null;
    PrintWriter writer = null;
    NaturalLanguageServlet servlet = null;

    @Before
    public void Setup() throws IOException {
        // Instantiates mock and real objeccts
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        writer = mock(PrintWriter.class);
        nlp = mock(NaturalLanguageProcessor.class);
        db = mock(CaptionStorage.class);
        servlet = new NaturalLanguageServlet();

        // Adds stubbing that does not require specific parameters
        when(request.getParameter(REQUEST_NO_METADATA_PARAM)).thenReturn(REQUEST_NO_METADATA_PARAM);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    public void SingleCaptionTest() {
        // Basic integration test using one caption with two entities
        // Captions Input : [{startTime: 0, endTime: 20, text: "Hello,World"}]
        // Expected Output: {"Hello":[0], "World":[0]} 

        // Defines mock input and expected output
        String mockCaptions = "{" + URL_MOCK + "," + CAPTIONS + "[{" + START_TIME + "0," + END_TIME + "20," + TEXT + "\"Hello,World\"}]}";
        String mockText = "Hello,World";
        List<String> mockEntities = new ArrayList<>(Arrays.asList("Hello", "World"));
        // Note multiple expected result combinations due to unpredictability of hashset ordering
        String expectedResults1 = "{\"Hello\":[0],\"World\":[0]}";
        String expectedResults2 = "{\"World\":[0],\"Hello\":[0]}";

        // Defines stubbing of inserting mock captions 
        when(request.getParameter(REQUEST_JSON_PARAM)).thenReturn(mockCaptions);
        // Defines stubbing of NLP API calls
        when(nlp.getEntities(contains(mockText))).thenReturn(mockEntities);

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(or(eq(expectedResults1), eq(expectedResults2))));

        // Runs doPost and fails if exceptions are thrown
        try { 
            servlet.setNaturalLanguageProcessor(nlp);
            servlet.setDatabase(db);
            servlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

    @Test
    public void NoCaptionsTest() {
        // Integration test with empty input
        // Captions Input : []
        // Expected Output: {} 

        // Defines mock input and expected output
        String mockCaptions = "{" + URL_MOCK + "," + CAPTIONS + "[]}";
        String expectedResults = "{}";

        // Defines stubbing of inserting mock captions
        when(request.getParameter(REQUEST_JSON_PARAM)).thenReturn(mockCaptions);

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(eq(expectedResults)));

        // Runs doPost and fails if exceptions are thrown
        try { 
            servlet.setNaturalLanguageProcessor(nlp);
            servlet.setDatabase(db);
            servlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

    @Test
    public void CombiningCaptionsTest() {
        // Integration test with combining captions to meet time duration threshold
        // Captions Input : [{startTime:  0, endTime: 10, text: "Hello World"}, 
        //                   {startTime: 10, endTime: 20, text: "Goodbye"},
        //                   {startTime: 20, endTime: 50, text: "Return"}]
        // Expected Output: {"Hello World Goodbye":[0], "Return": [20]} 

        // Defines mock input and expected output
        String mockCaptions = "{" + URL_MOCK + "," + CAPTIONS + "[";
        mockCaptions += "{" + START_TIME + "0," + END_TIME + "10," + TEXT + "\"Hello World\"},";
        mockCaptions += "{" + START_TIME + "10," + END_TIME + "20," + TEXT + "\"Goodbye\"},";
        mockCaptions += "{" + START_TIME + "20," + END_TIME + "50," + TEXT + "\"Return\"}]}";
        String mockText1 = "Hello World Goodbye";
        String mockText2 = "Return";
        List<String> mockEntities1 = new ArrayList<>(Arrays.asList("Hello World Goodbye"));
        List<String> mockEntities2 = new ArrayList<>(Arrays.asList("Return"));
        // Note multiple expected result combinations due to unpredictability of hashset ordering
        String expectedResults1 = "{\"Hello World Goodbye\":[0],\"Return\":[20]}";
        String expectedResults2 = "{\"Return\":[20],\"Hello World Goodbye\":[0]}";

        // Defines stubbing of inserting mock captions
        when(request.getParameter(REQUEST_JSON_PARAM)).thenReturn(mockCaptions);
        // Defines stubbing of NLP API calls
        when(nlp.getEntities(contains(mockText1))).thenReturn(mockEntities1);
        when(nlp.getEntities(contains(mockText2))).thenReturn(mockEntities2);

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(or(eq(expectedResults1), eq(expectedResults2))));

        // Runs doPost and fails if exceptions are thrown
        try { 
            servlet.setNaturalLanguageProcessor(nlp);
            servlet.setDatabase(db);
            servlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

    @Test
    public void SingleWordWithMultipleOccurancesTest() {
        // Integration test where one word is seen multiple times in captions
        // Captions Input : [{startTime:  0, endTime: 20, text: "Hello,Hello,Hello"}, 
        //                   {startTime: 20, endTime: 50, text: "Hello"},
        //                   {startTime: 30, endTime: 50, text: "Hello, Hello"}]
        // Expected Output: {"Hello":[0,20,30]} 

        // Defines mock input and expected output
        String mockCaptions = "{" + URL_MOCK + "," + CAPTIONS + "[";
        mockCaptions += "{" + START_TIME + "0," + END_TIME + "20," + TEXT + "\"Hello,Hello,Hello\"},";
        mockCaptions += "{" + START_TIME + "20," + END_TIME + "50," + TEXT + "\"Hello \"},";
        mockCaptions += "{" + START_TIME + "30," + END_TIME + "50," + TEXT + "\"Hello, Hello\"}]}";
        String mockText1 = "Hello,Hello,Hello";
        String mockText2 = "Hello ";
        String mockText3 = "Hello, Hello";
        List<String> mockEntities = new ArrayList<>(Arrays.asList("Hello"));
        String expectedResults = "{\"Hello\":[0,20,30]}";

        // Defines stubbing of inserting mock captions
        when(request.getParameter(REQUEST_JSON_PARAM)).thenReturn(mockCaptions);
        // Defines stubbing of NLP API calls
        when(nlp.getEntities(contains(mockText1))).thenReturn(mockEntities);
        when(nlp.getEntities(contains(mockText2))).thenReturn(mockEntities);
        when(nlp.getEntities(contains(mockText3))).thenReturn(mockEntities);

        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(eq(expectedResults)));

        // Runs doPost and fails if exceptions are thrown
        try { 
            servlet.setNaturalLanguageProcessor(nlp);
            servlet.setDatabase(db);
            servlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

    @Test
    public void MultipleWordsWithMultipleOccurancesTest() {
        // Integration test where two words are seen multiple times in captions
        // Captions Input : [{startTime:  0, endTime: 20, text: "Hello,Hello,World,World"}, 
        //                   {startTime: 20, endTime: 50, text: "Hello"},
        //                   {startTime: 30, endTime: 50, text: "Hello,World"},
        //                   {startTIme: 40, endTime: 50, text: "World,World"}]
        // Expected Output: {"Hello":[0,20,30], "World":[0,30,40]} 

        // Defines mock input and expected output
        String mockCaptions = "{" + URL_MOCK + "," + CAPTIONS + "[";
        mockCaptions += "{" + START_TIME + "0," + END_TIME + "20," + TEXT + "\"Hello,Hello,World,World\"},";
        mockCaptions += "{" + START_TIME + "20," + END_TIME + "50," + TEXT + "\"Hello \"},";
        mockCaptions += "{" + START_TIME + "30," + END_TIME + "50," + TEXT + "\"Hello, World\"},";
        mockCaptions += "{" + START_TIME + "40," + END_TIME + "50," + TEXT + "\"World, World\"}]}";
        String mockText1 = "Hello,Hello,World,World";
        String mockText2 = "Hello ";
        String mockText3 = "Hello, World";
        String mockText4 = "World, World";
        List<String> mockEntities1 = new ArrayList<>(Arrays.asList("Hello", "World"));
        List<String> mockEntities2 = new ArrayList<>(Arrays.asList("Hello"));
        List<String> mockEntities3 = new ArrayList<>(Arrays.asList("Hello", "World"));
        List<String> mockEntities4 = new ArrayList<>(Arrays.asList("World"));

        // Note multiple expected result combinations due to unpredictability of hashset ordering
        String expectedResults1 = "{\"Hello\":[0,20,30],\"World\":[0,30,40]}";
        String expectedResults2 = "{\"World\":[0,30,40],\"Hello\":[0,20,30]}";

        // Defines stubbing of inserting mock captions
        when(request.getParameter(REQUEST_JSON_PARAM)).thenReturn(mockCaptions);
        // Defines stubbing of NLP API calls
        when(nlp.getEntities(contains(mockText1))).thenReturn(mockEntities1);
        when(nlp.getEntities(contains(mockText2))).thenReturn(mockEntities2);
        when(nlp.getEntities(contains(mockText3))).thenReturn(mockEntities3);
        when(nlp.getEntities(contains(mockText4))).thenReturn(mockEntities4);
        
        // Sets stubbing to throw exception when string written to PrintWriter is not the expected results
        doThrow(new RuntimeException()).when(writer).println(not(or(eq(expectedResults1), eq(expectedResults2))));

        // Runs doPost and fails if exceptions are thrown
        try { 
            servlet.setNaturalLanguageProcessor(nlp);
            servlet.setDatabase(db);
            servlet.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(FAILED_TEST_MESSAGE);
        }
    }

}