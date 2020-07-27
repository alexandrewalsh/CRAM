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
import com.google.sps.data.TimeRangedText;

/** The object that is parsed from JSON as the entire YouTube captions */
public final class YoutubeCaptions {

    private String url;
    private List<TimeRangedText> captions;

    /**
     * Gets the list of TimeRangedText objects
     * @return The list of TimeRangedText
     */
    public List<TimeRangedText> getCaptions() {
        return this.captions;
    }

    /**
     * Gets the String representation of the captions
     * @return The String representation of captions list
     */
    @Override
    public String toString() {
        return captions.toString();
    }

}