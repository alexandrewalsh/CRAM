/*
 * Implementation for DatabaseImpl class
 *  Current public functions at a glance:
 *  test()
 *  addVideo()
 *  addClause()
 *  addClauses()
 *  addMetadata()
 *  getAllKeywords()
 *  getTimesForKeyword()
 *  videoInDb()
 * 
 */

package com.google.sps.storage;

import com.google.appengine.api.datastore.*;
import java.util.*;

public class CaptionStorage implements CaptionStorageInterface {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static final String COLUMN_VIDEO = "video";
    private static final String COLUMN_METADATA = "metadata";
    private static final String COLUMN_CAPTION = "caption";
    private static final String COLUMN_TIMES = "timestamps";

    //================================================================================
    // Public Interface Functions (see DatabaseInterface.java for official descriptions)
    //================================================================================    

    // add a video to the database
    public void addVideo(String videoID, String metadata) throws CaptionStorageException {
        Entity vidEnt, metaEnt;
        try {
            vidEnt = new Entity(COLUMN_VIDEO, videoID);
            datastore.put(vidEnt);
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.ADD_VIDEO_ERR, e.getMessage(), e.getCause());
        }

        try {
            metaEnt = new Entity(COLUMN_METADATA, metadata, vidEnt.getKey());
            datastore.put(metaEnt);
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.ADD_META_ERR, e.getMessage(), e.getCause());
        }
    }

    // add a keyphrase + timestamp pair to a particular video in the db
    public void addClause(String videoID, String keyword, List<Long> timestamps) throws CaptionStorageException {
        Entity vidEnt;
        
       // try {
            if ((vidEnt = getVideo(videoID)) == null) {
                throw new CaptionStorageException(Reason.NO_VIDEO_EXISTS, "Requested video does not exist");
            }
       // } catch (CaptionStorageException cse) {
       //     throw cse;
       // }

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
        } catch (CaptionStorageException cse) {
            throw cse;
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.ADD_KEYPHRASE_ERR, e.getMessage(), e.getCause());
        }
    }
    
    // add metadata information to a particular video in the db
    // overwite param will determine whether current metadata is replaced or just added on to
    public void addMetadata(String videoID, String metadata, boolean overwrite) throws CaptionStorageException {
        Entity vidEnt, metaEnt, currMeta;
        Key metaKey;
        String currData, newData;

        try {
            if ((vidEnt = getVideo(videoID)) == null) {
                throw new CaptionStorageException(Reason.NO_VIDEO_EXISTS, "Requested video does not exist");
            }
        } catch (CaptionStorageException cse) {
            throw cse;
        }
        
        // need to find metadata already in the database
        try {
            if ((currMeta = getMetadata(videoID)) == null) {
                throw new CaptionStorageException(Reason.NO_META_EXISTS, "Requested metadata does not exist");
            }
        } catch (CaptionStorageException cse) {
            throw cse;
        }

        metaKey = currMeta.getKey();

        if (overwrite) {
            try {
                datastore.delete(metaKey);
                metaEnt = new Entity(COLUMN_METADATA, metadata, vidEnt.getKey());
            } catch (Exception e) {
                throw new CaptionStorageException(Reason.OVERWRITE_META_ERR, e.getMessage(), e.getCause());
            }
        } else {
            try {
                currData = metaKey.getName();
                newData = currData + metadata;
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
        Key vidKey;
        Query query;
        PreparedQuery results;
        Map<String, List<Long>> clauseMap;

        try {
            if ((vidEnt = getVideo(videoID)) == null) {
                throw new CaptionStorageException(Reason.NO_VIDEO_EXISTS, "Requested video does not exist");
            }
        } catch (CaptionStorageException cse) {
            throw cse;
        }

        vidKey = vidEnt.getKey();

        query = new Query(COLUMN_CAPTION, vidKey);
        results = datastore.prepare(query);

        clauseMap = new HashMap<String, List<Long>>();

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
        } catch (CaptionStorageException cse) {
            throw cse;
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.GET_KEYPHRASE_ERR, e.getMessage(), e.getCause());
        }

        try {
            result = clauseMap.get(keyword);
        } catch (Exception e) {
            throw new CaptionStorageException(Reason.NO_KEYPHRASE_EXISTS, "Requested keyphrase does not exist in " + videoID);
        }
        if (result == null) {
            throw new CaptionStorageException(Reason.NO_KEYPHRASE_EXISTS, "Requested keyphrase does not exist in " + videoID);
        }

        return result;
    }

    // return true if specified video is in the database
    public boolean videoInDb(String videoID) throws CaptionStorageException {
        try {
            if ((getVideo(videoID)) == null) {
                return false;
            }
        } catch (CaptionStorageException cse) {
            throw cse;
        }

        return true;
    }

    // return true if specified meta is the metadata for videoID
    public boolean metaInDb(String videoID, String meta) throws CaptionStorageException {
        Entity data;
        try {
            data = getMetadata(videoID);
        } catch (CaptionStorageException cse) {
            throw cse;
        }
        if (data.getKey().getName().equals(meta)) {
            return true;
        } 
        return false;
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
