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

package com.google.sps.storage;

/** Exception class to handle bookmark storage database exception */
public class BookmarkStorageException extends Exception {

    private String reason;

    /**
     * Constructor for a BookmarkStorageException
     * @param reason The reason for the exception
     * @param errorMessage The error message of the Exception superclass
     * @param err The Throwable object for this exception
     */
    public BookmarkStorageException(String reason, String errorMessage, Throwable err) {
        super(errorMessage, err);
        this.reason = reason;
    }

    /**
     * Gets the reason for this exception
     * @return The reason for this exception as a String
     */
    public String getReason() {
        return this.reason;
    }
}