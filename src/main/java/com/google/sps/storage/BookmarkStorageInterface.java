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

import com.google.sps.data.Bookmark;
import java.util.List;

/** The database interface for managing bookmarks */
public interface BookmarkStorageInterface {

    /**
     * Gets all bookmarks for the current email and videoId
     * @param email The email to fetch the bookmarks of
     * @param videoId The Youtube video id to fetch the bookmarks of
     * @return The list of Bookmark objects for the given email and videoId 
     */
    public List<Bookmark> getAllBookmarks(String email, String videoId) throws BookmarkStorageException;

    /**
     * Adds a bookmark with the defined parameters to the database
     * @param userEmail The user email for the bookmark
     * @param videoId The Youtube video id for the bookmark
     * @param videoTimestamp The Youtube timestamp for the bookmark
     * @param title The title of the bookmark
     * @param content The content of the bookmark
     */
    public void addBookmark(String userEmail, String videoId, long videoTimestamp, String title, String content) throws BookmarkStorageException;

    /**
     * Removes a bookmark from the database
     * @param id The globally unique id of the bookmark to remove
     */
    public void removeBookmark(String id) throws BookmarkStorageException;

}