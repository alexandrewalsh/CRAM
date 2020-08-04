

package com.google.sps.database;

public interface DatabaseInterface {

    /*
     * add a video to the database
     * @param videoID       Youtube ID of the video to be entered in the db
     * @param metadata      String containing any metadata to be stored about the video
     * @return              Error code (0 upon success)
     */
    int addVideo(String videoID, String metadata);

    /*
     * add a keyphrase + timestamp pair to a particular video in the db
     * @param videoID       Youtube ID of the video already in the db
     * @param keyword       Keyphrase to be added to the video's entry in the db
     * @param timestamps    List of timestamps corresponding to the entity's appearances in the video
     * @return              Error code (0 upon success, -1 if videoID not in db)
     */
    int addClause(String videoID, String keyword, List<long> timestamps);

    /*
     * add a multiple keyphrase + timestamp pair to a particular video's entry
     * @param videoID       Youtube ID of the video already in the db
     * @param clauses       map containing pairs of keys & corresponding timestamps
     * @return              Error code (0 upon success, -1 if videoID not in db)
     */
    int addClauses(String videoID, Map<String, List<long>> clauses);

    /*
     * add metadata information to a particular video in the db
     * @param videoID       Youtube ID of the video already in the db
     * @param metadata      String containing any metadata to be stored about the video
     * @param overwrite     if true, any current metadata will be overwritten (otherwise, append)
     * @return              Error code (0 upon success, -1 if videoID not in db)
     */
    int addMetadata(String videoID, String metadata, boolean overwrite);

    /*
     * retrieve specific timestamps for a specified keyword belonging to videoID
     * @param videoID       Youtube ID of the video already in the db
     * @param keyword       Keyphrase in the videoID's db entry
     * @return              List of timestamps representing the times that keyword appears in videoID (null if videoID not in db or keyword not in videoID)
     */
    List<long> getTimesForKeyword(String videoID, String keyword);

    /*
     * retrieve all entities + their timestamps in a specified videoID
     * @param videoID       Youtube ID of the video already in the db
     * @return              Map of keyword + timestamp pairs belonging to videoID (null if videoID not in db)
     */
    Map<String, List<long>> getAllEntities(String videoID);
}
  