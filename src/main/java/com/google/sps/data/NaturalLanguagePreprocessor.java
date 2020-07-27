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

import java.util.List;
import java.util.ArrayList;
import com.google.sps.data.TimeRangedText;

/** Preprocesses captions by concatenating TimeRangedTexts to reach a time threshold */
public class NaturalLanguagePreprocessor {

    // The default time threshold in seconds to contatenate TimeRangedText objects
    private static final long DEFAULT_TIME_THRESHOLD = 20;

    private long timeThreshold;

    /**
     * Default constructor that uses the default time threshold
     */
    public NaturalLanguagePreprocessor() {
        this.timeThreshold = DEFAULT_TIME_THRESHOLD;
    }

    /**
     * Constructor that uses a specified time threshold
     * @param timeThreshold The time threshold in seconds to use in concatenating TimeRangedTexts
     */
    public NaturalLanguagePreprocessor(long timeThreshold) {
        this.timeThreshold = timeThreshold;
    }

    /**
     * Creates a list of concatenated TimeRangedTexts so that each ranged text has a duration at least as long as the time threshold
     * @param texts The list of TimeRangedTexts with short durations
     * @return The list of TimeRangedTexts with durations at least as long as the time threshold
     */
    public List<TimeRangedText> setTimeRanges(List<TimeRangedText> texts) {
        List<TimeRangedText> mergedTexts = new ArrayList<>();
        int size = texts.size();
        long startTime = 0;
        long timeElapsed = -1;
        String textBuild = "";

        // Loops through all captions to determine where to build and split increments
        for (int i = 0; i < size; i++) {
            TimeRangedText currentTimeRangedText = texts.get(i);

            // Sets start time when previous TimeRangedText added to the merged list (so timeElapsed is -1)
            if (timeElapsed == -1) {
                startTime = currentTimeRangedText.getStartTime();
            }

            // Builds the text from the current caption
            textBuild += currentTimeRangedText.getText() + " ";
            timeElapsed = currentTimeRangedText.getEndTime() - startTime;

            // Splits the text when the elapsed time for the current TimeRangedText meets the timeThreshold
            if (timeElapsed >= timeThreshold) {
                TimeRangedText builtTimeRangedText = new TimeRangedText(startTime, currentTimeRangedText.getEndTime(), textBuild);
                mergedTexts.add(builtTimeRangedText);
                textBuild = "";
                timeElapsed = -1;
            } 
        }

        // Adds the last TimeRangedTexts to the list, even if the threshold is not reached
        if (timeElapsed != -1) {
            TimeRangedText builtTimeRangedText = new TimeRangedText(startTime, texts.get(size - 1).getEndTime(), textBuild);
            mergedTexts.add(builtTimeRangedText);
        }

        return mergedTexts;
    }
}