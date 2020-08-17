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

package com.google.sps.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.*;

/** The object that represents a single Bookmark */
public final class Bookmark {

    private final String id;
    private final long timestamp;
    private final String title;
    private final String content;

    public Bookmark(String id, long timestamp, String title, String content) {
        this.id = id;
        this.timestamp = timestamp;
        this.title = title;
        this.content = content;
    }

    public String getBookmarkId() {
        return this.id;
    }

    public long getVideoTimestamp() {
        return this.timestamp;
    }

    public String getBookmarkTitle() {
        return this.title;
    }

    public String getBookmarkContent() {
        return this.content;
    }

}