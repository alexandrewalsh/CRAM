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

import com.google.sps.storage.*;
import java.io.IOException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public final class CaptionStorageTests {
    
    private static final String dummyId = "dummyId";
    private static final String keyword = "keyword";

    private CaptionStorage db = null;

    @Before
    public void Setup() throws IOException {

    }

    @Test
    public void testSameVideo() {
        // Test the same video being added, now one has metadata

        db = mock(CaptionStorage.class);
        try {
            db.addVideo(dummyId, "");
            db.addVideo(dummyId, "");

            Assert.assertTrue(db.videoInDb(dummyId));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testVideoNotInDb() {
        // test grabbing a video that isn't in the db

        db = mock(CaptionStorage.class);
        
        try {
            Assert.assertFalse(db.videoInDb(dummyId));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSameVideoMetadata() {
        // Test the same video being added, now one has metadata

        db = mock(CaptionStorage.class);
        try {
            db.addVideo(dummyId, "");
            db.addVideo(dummyId, "{'length': 2}");

            // this test is for the future, when metadata is used
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }  
    }

    @Test
    public void testEmptyTimestamps() {
        // Test an empty list as the timestamps

        db = mock(CaptionStorage.class);
        try {
            List<Long> empty = new ArrayList<>();

            db.addVideo(dummyId, "");
            db.addClause(dummyId, keyword, empty);

            Map<String, List<Long>> computed = db.getAllKeywords(dummyId);
            Map<String, List<Long>> expected = new HashMap();
            expected.put(dummyId, empty);

            Assert.assertEquals(computed, expected);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }  
    }

    @Test
    public void testKeywordsForMissingVideo() {
        // Try to retrieve keywords for a video that 
        // was never added

        db = mock(CaptionStorage.class);
        
        try {
            assertThrows(CaptionStorageException.class, () -> {
                db.addClause(dummyId, keyword, Collections.emptyList());
            });
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }  
    }

}