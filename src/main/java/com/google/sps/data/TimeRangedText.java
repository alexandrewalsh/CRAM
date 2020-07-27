// Copyright 2019 Google LLC
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

/** The caption text in a defined time range */
public final class TimeRangedText {

    private static final String OPEN_CURLY = "{";
    private static final String CLOSE_CURLY = "}";
    private static final String DELIMITER = ", ";
    private static final String START = "Start Time: ";
    private static final String END = "End Time: ";
    private static final String TEXT = "Text: ";

    private long startTime;
    private long endTime;
    private String text;

    /**
     * Constructor for a TimeRangedText object
     * @param startTime The start time of the text
     * @param endTime The end time of the text
     * @param text The text spoken in the given time range
     */
    public TimeRangedText(long startTime, long endTime, String text) {
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

    /**
     * Tests if two TimeRangedText objects are equal
     * @param other The other object to check equality with this object
     * @return Whether the input object has the same parameter values as this object
     */
    @Override
    public boolean equals(Object other) {
        // Tries to cast Object to a TimeRangedText, and returns false if there is a casting error
        try {
            TimeRangedText that = (TimeRangedText) other;
            return (this.getStartTime() == that.getStartTime()) 
                    && (this.getEndTime() == that.getEndTime()) 
                    && (this.getText().equals(that.getText()));
        } catch (Exception e) {
            return false;
        }
    }
    
}