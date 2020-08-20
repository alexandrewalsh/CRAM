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
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;
import java.util.List;
import java.util.ArrayList;

/** The implemented Bookmark database manager that communicates with Google Datastore */
public class BookmarkStorageManager implements BookmarkStorageInterface {

    private static final String ENTITY_BOOKMARK = "Bookmark";
    private static final String BOOKMARK_EMAIL_PROPERTY = "email";
    private static final String BOOKMARK_VIDEOID_PROPERTY = "videoId";
    private static final String BOOKMARK_TIMESTAMP_PROPERTY = "timestamp";
    private static final String BOOKMARK_TITLE_PROPERTY = "title";
    private static final String BOOKMARK_CONTENT_PROPERTY = "content";
    private static final String EXCEPTION_ADD_BOOKMARK = "AddBookmarkException";
    private static final String EXCEPTION_REMOVE_BOOKMARK = "RemoveBookmarkException";
    private static final String EXCEPTION_GET_BOOKMARKS = "GetBookmarksException";

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    /**
     * Gets all bookmarks for the current email and videoId from the Datastore database
     * @param email The email to fetch the bookmarks of
     * @param videoId The Youtube video id to fetch the bookmarks of
     * @return The list of Bookmark objects for the given email and videoId 
     */
    public List<Bookmark> getAllBookmarks(String email, String videoId) throws BookmarkStorageException {

        // Defines filters for the query and gets the prepared query results
        Filter userFilter = new FilterPredicate(BOOKMARK_EMAIL_PROPERTY, FilterOperator.EQUAL, email);
        Filter videoIdFilter = new FilterPredicate(BOOKMARK_VIDEOID_PROPERTY, FilterOperator.EQUAL, videoId);
        Filter bookmarkFilter = CompositeFilterOperator.and(userFilter, videoIdFilter);
        Query query = new Query(ENTITY_BOOKMARK).setFilter(bookmarkFilter);
        
        try {
            PreparedQuery results = datastore.prepare(query);
            List<Bookmark> bookmarks = new ArrayList<>();

            // Creates a list of Bookmarks from the perpared query results
            for (Entity entity : results.asIterable()) {
                String id = KeyFactory.keyToString(entity.getKey());
                long timestamp = (long) entity.getProperty(BOOKMARK_TIMESTAMP_PROPERTY);
                String title = (String) entity.getProperty(BOOKMARK_TITLE_PROPERTY);
                String content = (String) entity.getProperty(BOOKMARK_CONTENT_PROPERTY);

                Bookmark bookmark = new Bookmark(id, timestamp, title, content);
                bookmarks.add(bookmark);
            }
            return bookmarks;
        } catch (Exception e) {
            throw new BookmarkStorageException(EXCEPTION_GET_BOOKMARKS, e.getMessage(), e.getCause());
        }

    }

    /**
     * Adds a bookmark with the defined parameters to the Datastore database
     * @param userEmail The user email for the bookmark
     * @param videoId The Youtube video id for the bookmark
     * @param videoTimestamp The Youtube timestamp for the bookmark
     * @param title The title of the bookmark
     * @param content The content of the bookmark
     */
    public void addBookmark(String userEmail, String videoId, long videoTimestamp, String title, String content) throws BookmarkStorageException {
        // Tries to create an Entity object and store it in Datastore
        Entity bookmark;
        try {
            bookmark = new Entity(ENTITY_BOOKMARK);
            bookmark.setProperty(BOOKMARK_EMAIL_PROPERTY, userEmail);
            bookmark.setProperty(BOOKMARK_VIDEOID_PROPERTY, videoId);
            bookmark.setProperty(BOOKMARK_TIMESTAMP_PROPERTY, videoTimestamp);
            bookmark.setProperty(BOOKMARK_TITLE_PROPERTY, title);
            bookmark.setProperty(BOOKMARK_CONTENT_PROPERTY, content);

            datastore.put(bookmark);
        } catch (Exception e) {
            throw new BookmarkStorageException(EXCEPTION_ADD_BOOKMARK, e.getMessage(), e.getCause());
        }
    }

    /**
     * Removes a bookmark from the database
     * @param id The globally unique id of the bookmark to remove
     */
    public void removeBookmark(String id) throws BookmarkStorageException {
        // Tries to parse the Entity Key from the string to remove the Entity from Datastore
        Key key;
        try {
            key = KeyFactory.stringToKey(id);
            datastore.delete(key);
        } catch (Exception e) {
            throw new BookmarkStorageException(EXCEPTION_REMOVE_BOOKMARK, e.getMessage(), e.getCause()); 
        }
    }
}