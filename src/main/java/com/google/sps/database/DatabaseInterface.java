/*
 * Interface for DatabaseImpl class. Use as reference and official description of public functions.
 */

package com.google.sps.database;

import java.util.*;

public interface DatabaseInterface {

    /*
     * add a video to the database
     * @param videoID       Youtube ID of the video to be entered in the db
     * @param metadata      String containing any metadata to be stored about the video
     * @return              Error code (0 upon success)
     */
    public int addVideo(String videoID, String metadata);

    /*
     * add a keyphrase + timestamp pair to a particular video in the db
     * @param videoID       Youtube ID of the video already in the db
     * @param keyword       Keyphrase to be added to the video's entry in the db
     * @param timestamps    List of timestamps corresponding to the entity's appearances in the video
     * @return              Error code (0 upon success, -1 if videoID not in db)
     */
    public int addClause(String videoID, String keyword, List<Long> timestamps);

    /*
     * add multiple keyphrase + timestamp pairs to a particular video's entry
     * @param videoID       Youtube ID of the video already in the db
     * @param clauses       map containing pairs of keys & corresponding timestamps
     * @return              Error code (0 upon success, -1 if videoID not in db)
     */
    public int addClauses(String videoID, Map<String, List<Long>> clauses);

    /*
     * add metadata information to a particular video in the db
     * @param videoID       Youtube ID of the video already in the db
     * @param metadata      String containing any metadata to be stored about the video
     * @param overwrite     if true, any current metadata will be overwritten (otherwise, append)
     * @return              Error code (0 upon success, -1 if videoID not in db)
     */
    public int addMetadata(String videoID, String metadata, boolean overwrite);

    /*
     * retrieve all keywords + their timestamps in a specified videoID
     * @param videoID       Youtube ID of the video already in the db
     * @return              Map of keyword + timestamp pairs belonging to videoID (null if videoID not in db)
     */
    public Map<String, List<Long>> getAllKeywords(String videoID);

    /*
     * retrieve specific timestamps for a specified keyword belonging to videoID
     * @param videoID       Youtube ID of the video already in the db
     * @param keyword       Keyphrase in the videoID's db entry
     * @return              List of timestamps representing the times that keyword appears in videoID (null if videoID not in db or keyword not in videoID)
     */
    public List<Long> getTimesForKeyword(String videoID, String keyword);

    /*
     * return true if specified video is in the database
     * @param videoID       Youtube ID of video we are looking for
     * @return              true if ID is stored in db, false if video ID not found
     */
    public boolean videoInDb(String videoID);

}
  