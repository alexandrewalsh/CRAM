/*
 * Interface for DatabaseImpl class. Use as reference and official description of public functions.
 */

package com.google.sps.storage;

import java.util.*;
import com.google.sps.data.TimeRangedText;

public interface CaptionStorageInterface {

    /*
     * add a video, its metadata, and its full captions to the database 
     * @param videoID       Youtube ID of the video to be entered in the db
     * @param metadata      String containing any metadata to be stored about the video
     * @exception           throws ADD_VIDEO_ERR or ADD_META_ERR
     */
    public void addVideo(String videoID, String metadata, List<TimeRangedText> fullCaptions) throws CaptionStorageException;

    /*
     * add a keyphrase + timestamp pair to a particular video in the db
     * @param videoID       Youtube ID of the video already in the db
     * @param keyword       Keyphrase to be added to the video's entry in the db
     * @param timestamps    List of timestamps corresponding to the entity's appearances in the video
     * @exception           throws NO_VIDEO_EXISTS or GET_VIDEO_ERR or ADD_KEYPHRASE_ERR
     */
    public void addClause(String videoID, String keyword, List<Long> timestamps) throws CaptionStorageException;

    /*
     * add multiple keyphrase + timestamp pairs to a particular video's entry
     * @param videoID       Youtube ID of the video already in the db
     * @param clauses       map containing pairs of keys & corresponding timestamps
     * @exception           throws NO_VIDEO_EXISTS or GET_VIDEO_ERR or ADD_KEYPHRASE_ERR
     */
    public void addClauses(String videoID, Map<String, List<Long>> clauses) throws CaptionStorageException;

    /*
     * add metadata information to a particular video in the db
     * @param videoID       Youtube ID of the video already in the db
     * @param metadata      String containing any metadata to be stored about the video
     * @param overwrite     if true, any current metadata will be overwritten (otherwise, append)
     * @exception           throws NO_VIDEO_EXISTS or GET_VIDEO_ERR or ADD_KEYPHRASE_ERR or NO_META_EXISTS
     */
    public void addMetadata(String videoID, String metadata, boolean overwrite) throws CaptionStorageException;

    /*
     * retrieve all keywords + their timestamps in a specified videoID
     * @param videoID       Youtube ID of the video already in the db
     * @exception           throws GET_VIDEO_ERR or NO_VIDEO_EXISTS or GET_KEYPHRASE_ERR
     * @return              Map of keyword + timestamp pairs belonging to videoID (null if videoID not in db)
     */
    public Map<String, List<Long>> getAllKeywords(String videoID) throws CaptionStorageException;

    /*
     * retrieve specific timestamps for a specified keyword belonging to videoID
     * @param videoID       Youtube ID of the video already in the db
     * @param keyword       Keyphrase in the videoID's db entry
     * @exception           throws GET_VIDEO_ERR or NO_VIDEO_EXISTS or GET_KEYPHRASE_ERR or NO_KEYPHRASE_EXISTS
     * @return              List of timestamps representing the times that keyword appears in videoID (null if videoID not in db or keyword not in videoID)
     */
    public List<Long> getTimesForKeyword(String videoID, String keyword) throws CaptionStorageException;

    /*
     * retrieve full captions of videoID and return them as a list of TimeRangedText objects
     * @param videoID       Youtube ID of video already in the db
     * @return              List of TimeRangedText objects, which each represent a single line of the full YT captions
     */
    public List<TimeRangedText> getFullCaptions(String videoID);

    /*
     * return true if specified video is in the database
     * @param videoID       Youtube ID of video we are looking for
     * @exception           throws GET_VIDEO_ERR
     * @return              true if ID is stored in db, false if video ID not found
     */
    public boolean videoInDb(String videoID) throws CaptionStorageException;

    /*
     * return true if specified meta is the metadata for videoID
     * @param videoID       Youtube ID of video whose metadata we are looking for
     * @param meta          metadata we are looking for
     * @exception           throws GET_VIDEO_ERR
     * @return              true if meta is the metadata for videoID, false otherwise
     */
    public boolean metaInDb(String videoID, String meta) throws CaptionStorageException;

    /*
     * delete a video & all its children (metadata & captions) from the database
     * @param videoID       Youtube ID of video in database to be deleted
     */
    public void deleteVideo(String videoID) throws CaptionStorageException;

    /*
     * delete a specific keyword from a video in the database
     * @param videoID       Youtube ID of video in database
     * @param keyword       clause belonging to videoID in the db to be deleted
     */
    public void deleteClause(String videoID, String keyword) throws CaptionStorageException;

    /*
     * delete the metadata belonging to a specific video in the database
     * @param metadata      Metadata in the database to be deleted
     */
    public void deleteMetadata(String metadata) throws CaptionStorageException;
}
