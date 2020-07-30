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

import javax.annotation.*;
import com.google.gson.annotations.SerializedName;

/** The caption text in a defined time range */
public final class TimeRangedText {

    private static final String OPEN_CURLY = "{";
    private static final String CLOSE_CURLY = "}";
    private static final String DELIMITER = ", ";
    private static final String START = "Start Time: ";
    private static final String END = "End Time: ";
    private static final String TEXT = "Text: ";

    @SerializedName("startTime")
    private long startTime;
    @SerializedName("endTime")
    private long endTime;
    @SerializedName("text")
    private String text;

    /**
     * Constructor for a TimeRangedText object
     * @param startTime The start time of the text
     * @param endTime The end time of the text
     * @param text The text spoken in the given time range
     */
    public TimeRangedText(long startTime, long endTime, @Nonnull String text) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.text = text;
    }

    /**
     * Gets the start time of this TimeRangedText
     * @return The start time of this TimeRangedText
     */
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * Gets the end time of this TimeRangedText
     * @return The end time of this TimeRangedText
     */
    public long getEndTime() {
        return this.endTime;
    }

    /**
     * Gets the time elapsed of the text spoken
     * @return The time elapsed of this TimeRangedText
     */
    public long getTimeElapsed() {
        return this.startTime - this.endTime;
    }

    /**
     * Gets the text spoken 
     * @return The text spoken in the time range
     */
    public String getText() {
        return this.text;
    }

    /**
     * Gets the String representation of a TimeRangedText object
     */
    @Override
    public String toString() {
        return OPEN_CURLY + START + startTime + DELIMITER + END + endTime + DELIMITER + TEXT + text + CLOSE_CURLY;
    }
}