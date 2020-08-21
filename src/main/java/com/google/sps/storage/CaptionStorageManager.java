/*
 * Implementation for DatabaseImpl class
 *  Current public functions at a glance:
 *  addVideo()
 *  addClause()
 *  addClauses()
 *  addMetadata()
 *  getAllKeywords()
 *  getTimesForKeyword()
 *  videoInDb()
 *  metaInDb()
 *  deleteVideo()
 *  deleteClause()
 *  deleteMetadata()
 */

package com.google.sps.storage;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.util.*;
import com.google.sps.data.TimeRangedText;

public class CaptionStorageManager implements CaptionStorageInterface {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static final String COLUMN_VIDEO = "video";
    private static final String COLUMN_METADATA = "metadata";
    private static final String COLUMN_CAPTION = "caption";
    private static final String COLUMN_FULL_CAPTIONS = "full_captions";
    private static final String COLUMN_TIMES = "timestamps";
    private static final String NO_VID_ERR = "Requested video does not exist";
    private static final String NO_META_ERR = "Requested metadata does not exist";
    private static final String NO_PHRASE_ERR = "Requested keyphrase does not exist in ";
    private static final String COLUMN_START = "start_time";
    private static final String COLUMN_END = "end_time";

    //================================================================================
    // Public Interface Functions (see DatabaseInterface.java for official descriptions)
    //================================================================================    

    // add a video, its metadata, and its full captions to the database
    public void addVideo(String videoID, String metadata, List<TimeRangedText> fullCaptions) throws CaptionStorageException {
        // add video to the database
        Entity vidEnt;
        try {
            vidEnt = new Entity(COLUMN_VIDEO, videoID);
            datastore.put(vidEnt);
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.ADD_VIDEO_ERR, e.getMessage(), e.getCause());
        }

        // add metadata to the database
        Entity metaEnt;
        try {
            metaEnt = new Entity(COLUMN_METADATA, metadata, vidEnt.getKey());
            datastore.put(metaEnt);
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.ADD_META_ERR, e.getMessage(), e.getCause());
        }

        // add full captions to the database
        int i = 0;
        try {
            for (TimeRangedText single_line : fullCaptions) {
                String caption = single_line.getText();
                Entity capEnt = new Entity(COLUMN_FULL_CAPTIONS, Integer.toString(i++), vidEnt.getKey());
                capEnt.setProperty(COLUMN_CAPTION, caption);
                capEnt.setProperty(COLUMN_START, single_line.getStartTime());
                capEnt.setProperty(COLUMN_END, single_line.getEndTime());
                datastore.put(capEnt);
            }
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.ADD_FULL_CAPTIONS_ERR, e.getMessage(), e.getCause());
        }
    }

    // add a keyphrase + timestamp pair to a particular video in the db
    public void addClause(String videoID, String keyword, List<Long> timestamps) throws CaptionStorageException {
        Entity vidEnt;
        if ((vidEnt = getVideo(videoID)) == null) {
            throw new CaptionStorageException(Reason.NO_VIDEO_EXISTS, NO_VID_ERR);
        }

        try {
            Entity keyEnt = new Entity(COLUMN_CAPTION, keyword, vidEnt.getKey());
            keyEnt.setProperty(COLUMN_TIMES, timestamps);
            datastore.put(keyEnt);
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.ADD_KEYPHRASE_ERR, e.getMessage(), e.getCause());
        }
    }
    
    // add multiple keyphrase + timestamp pairs to a particular video's entry
    public void addClauses(String videoID, Map<String, List<Long>> clauses) throws CaptionStorageException {
        try {
            for (Map.Entry<String, List<Long>> me : clauses.entrySet()) {
                addClause(videoID, me.getKey(), me.getValue());
            }
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.ADD_KEYPHRASE_ERR, e.getMessage(), e.getCause());
        }
    }
    
    // add metadata information to a particular video in the db
    // overwite param will determine whether current metadata is replaced or just added on to
    public void addMetadata(String videoID, String metadata, boolean overwrite) throws CaptionStorageException {
        Entity vidEnt;
        if ((vidEnt = getVideo(videoID)) == null) {
            throw new CaptionStorageException(Reason.NO_VIDEO_EXISTS, NO_VID_ERR);
        }
        
        // need to find metadata already in the database
        Entity currMeta;
        if ((currMeta = getMetadata(videoID)) == null) {
            throw new CaptionStorageException(Reason.NO_META_EXISTS, NO_META_ERR);
        }

        Key metaKey = currMeta.getKey();

        Entity metaEnt;
        if (overwrite) {
            try {
                datastore.delete(metaKey);
                metaEnt = new Entity(COLUMN_METADATA, metadata, vidEnt.getKey());
            } catch (Exception e) {
                throw new CaptionStorageException(Reason.OVERWRITE_META_ERR, e.getMessage(), e.getCause());
            }
        } else {
            try {
                String currData = metaKey.getName();
                String newData = currData + metadata;
                datastore.delete(metaKey);
                metaEnt = new Entity(COLUMN_METADATA, newData, vidEnt.getKey());
            } catch (Exception e) {
                throw new CaptionStorageException(Reason.APPEND_META_ERR, e.getMessage(), e.getCause());
            }
        }
        try {
            datastore.put(metaEnt);
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.ADD_META_ERR, e.getMessage(), e.getCause());
        }
    }
    
    // retrieve all keywords + their timestamps in a specified videoID
    public Map<String, List<Long>> getAllKeywords(String videoID) throws CaptionStorageException {
        Entity vidEnt;
        try {
            if ((vidEnt = getVideo(videoID)) == null) {
                throw new CaptionStorageException(Reason.NO_VIDEO_EXISTS, NO_VID_ERR);
            }
        } catch (CaptionStorageException cse) {
            throw cse;
        }

        Key vidKey = vidEnt.getKey();

        Query query = new Query(COLUMN_CAPTION, vidKey);
        PreparedQuery results = datastore.prepare(query);

        Map<String, List<Long>> clauseMap = new HashMap<String, List<Long>>();

        try {
            for (Entity entity : results.asIterable()) {
                clauseMap.put(entity.getKey().getName(), (List<Long>)entity.getProperty(COLUMN_TIMES));
            } 
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.GET_KEYPHRASE_ERR, e.getMessage(), e.getCause());
        }

        return clauseMap;
    }
    
    // retrieve specific timestamps for a specified keyword belonging to videoID
    public List<Long> getTimesForKeyword(String videoID, String keyword) throws CaptionStorageException {
        Map<String, List<Long>> clauseMap;
        List<Long> result;
        
        try {
            clauseMap = getAllKeywords(videoID);
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.GET_KEYPHRASE_ERR, e.getMessage(), e.getCause());
        }

        try {
            result = clauseMap.get(keyword);
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.NO_KEYPHRASE_EXISTS, NO_PHRASE_ERR + videoID);
        }
        if (result == null) {
            throw new CaptionStorageException(Reason.NO_KEYPHRASE_EXISTS, NO_PHRASE_ERR + videoID);
        }

        return result;
    }

    // retrieve full captions of videoID and return them as a list of TimeRangedText objects
    public List<TimeRangedText> getFullCaptions(String videoID) {
        Query query = new Query(COLUMN_FULL_CAPTIONS).addSort(COLUMN_START, SortDirection.ASCENDING);
        PreparedQuery results = datastore.prepare(query);

        List<TimeRangedText> full_captions = new ArrayList<TimeRangedText>();

        for (Entity entity : results.asIterable()) {
            if (entity.getParent().getName().equals(videoID)) {
                TimeRangedText single_line = new TimeRangedText((Long)entity.getProperty(COLUMN_START), (Long)entity.getProperty(COLUMN_END), (String)entity.getProperty(COLUMN_CAPTION));
                full_captions.add(single_line);
            }
        }

        return full_captions;
    }

    // return true if specified video is in the database
    public boolean videoInDb(String videoID) throws CaptionStorageException {
        if ((getVideo(videoID)) == null) {
            return false;
        }
        return true;
    }

    // return true if specified meta is the metadata for videoID
    public boolean metaInDb(String videoID, String meta) throws CaptionStorageException {
        Entity data = getMetadata(videoID);
        if (data.getKey().getName().equals(meta)) {
            return true;
        }
        return false;
    }

    // delete a video & all its children (metadata & captions) from the database
    public void deleteVideo(String videoID) throws CaptionStorageException {
        Entity vidEnt;
        if ((vidEnt = getVideo(videoID)) == null) {
            throw new CaptionStorageException(Reason.NO_VIDEO_EXISTS, NO_VID_ERR);
        }
        Key vidKey = vidEnt.getKey();
        try {
            datastore.delete(vidKey);
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.DELETE_VIDEO_ERR, e.getMessage(), e.getCause());
        }
    }

    // delete a specific keyword from a video in the database
    public void deleteClause(String videoID, String keyword) throws CaptionStorageException {
        Query query = new Query(COLUMN_CAPTION);
        PreparedQuery results = datastore.prepare(query);

        Entity keywordEnt = null;
        try {
            for (Entity entity : results.asIterable()) {
                if (entity.getKey().getName().equals(keyword)) {
                    keywordEnt = entity;
                }
            }
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.GET_KEYPHRASE_ERR, e.getMessage(), e.getCause());
        }

        Key keywordKey = keywordEnt.getKey();
        try {
            datastore.delete(keywordKey);
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.DELETE_KEYPHRASE_ERR, e.getMessage(), e.getCause());
        }
        
    }

    // delete the metadata belonging to a specific video in the database
    public void deleteMetadata(String metadata) throws CaptionStorageException {
        Query query = new Query(COLUMN_METADATA);
        PreparedQuery results = datastore.prepare(query);

        Entity metaEnt = null;
        try {
            for (Entity entity : results.asIterable()) {
                if (entity.getKey().getName().equals(metadata)) {
                    metaEnt = entity;
                }
            }
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.GET_META_ERR, e.getMessage(), e.getCause());
        }

        Key metaKey = metaEnt.getKey();
        try {
            datastore.delete(metaKey);
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.DELETE_META_ERR, e.getMessage(), e.getCause());
        }
        
    }

    //================================================================================
    // Private Helper Functions
    //================================================================================

    /*
     * helper function to query database for specific video entity
     * @param videoID       Youtube ID of the video already in the db
     * @return              Entity representing the video in the db,
     *                          null if not matching entity is found
     */
    private Entity getVideo(String videoID) throws CaptionStorageException {
        Query query = new Query(COLUMN_VIDEO);
        PreparedQuery results = datastore.prepare(query);

        try {
            for (Entity entity : results.asIterable()) {
                if (entity.getKey().getName().equals(videoID)) {
                    return entity;
                }
            }
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.GET_VIDEO_ERR, e.getMessage(), e.getCause());
        }
        
        return null;    // no entity matched videoID in db video list
    }

    /*
     * helper function to query database for specific metadata entity whose parent is videoID
     * @param videoID       Youtube ID of the video already in the db
     * @return              Entity representing the metadata of the video in the db,
     *                          null if no matching entity is found
     */
    private Entity getMetadata(String videoID) throws CaptionStorageException {
        Query query = new Query(COLUMN_METADATA);
        PreparedQuery results = datastore.prepare(query);

        try {
            for (Entity entity : results.asIterable()) {
                if (entity.getKey().getParent().getName().equals(videoID)) {
                    return entity;
                }
            }
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.GET_META_ERR, e.getMessage(), e.getCause());
        }

        return null;    // no entity had videoID as its parent in db metadata list
    }
}
