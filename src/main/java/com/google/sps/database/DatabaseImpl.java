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

package com.google.sps.database;

import com.google.appengine.api.datastore.*;
import java.util.*;

public class DatabaseImpl implements DatabaseInterface {

    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static final String V_KIND = "video";
    private static final String M_KIND = "metadata";
    private static final String C_KIND = "caption";
    private static final String T_KIND = "timestamps";

    //================================================================================
    // General Test Function (not used for official testing)
    //================================================================================
    
    public void test() {
        
        Map<String, List<Long>> hm = new HashMap<String, List<Long>>();

        String caption = "John Cena";
        List<Long> timestamps = new ArrayList<>();
        timestamps.add(42L);
        timestamps.add(365L);
        timestamps.add(31415L);
        hm.put(caption, timestamps);

        String caption2 = "Dwayne 'the Rock' Johnson";
        List<Long> timestamps2 = new ArrayList<>();
        timestamps2.add(0L);
        timestamps2.add(123L);
        timestamps2.add(404L);
        hm.put(caption2, timestamps2);

        Entity myEnt = new Entity(V_KIND,"ncbb5B85sd2");

        datastore.put(myEnt);

        List<Entity> le = new ArrayList<>();

        for (Map.Entry<String, List<Long>> me : hm.entrySet()) {
            Entity tempEnt = new Entity(C_KIND, me.getKey(), myEnt.getKey());
            tempEnt.setProperty(T_KIND, me.getValue());
            datastore.put(tempEnt);
            le.add(tempEnt);
        }
        
        for (Entity e : le) {
            System.out.println("Parent: " + e.getParent());
        }

    }

    //================================================================================
    // Public Interface Functions (see DatabaseInterface.java for official descriptions)
    //================================================================================    

    // add a video to the database
    public int addVideo(String videoID, String metadata) {
        Entity vidEnt, metaEnt;
        try {
            vidEnt = new Entity(V_KIND, videoID);
            datastore.put(vidEnt);
        } catch (Exception e) {
            System.out.println("Failed to add video to the database");
            return -1;
        }

        try {
            metaEnt = new Entity(M_KIND, metadata, vidEnt.getKey());
            datastore.put(metaEnt);
        } catch (Exception e) {
            System.out.println("Failed to add metadata to the database");
            return -2;
        }
        
        return 0;
    }

    // add a keyphrase + timestamp pair to a particular video in the db
    public int addClause(String videoID, String keyword, List<Long> timestamps) {
        Entity vidEnt;
        if ((vidEnt = getVideo(videoID)) == null) {
            System.out.println("Failed to retrieve " + videoID + " from database");
            return -1;
        }

        try {
            Entity keyEnt = new Entity(C_KIND, keyword, vidEnt.getKey());
            keyEnt.setProperty(T_KIND, timestamps);
            datastore.put(keyEnt);
        } catch (Exception e) {
            System.out.println("Failed to add " + keyword + " to database");
            return -2;
        }
        return 0;
    }
    
    // add multiple keyphrase + timestamp pairs to a particular video's entry
    public int addClauses(String videoID, Map<String, List<Long>> clauses) {
        for (Map.Entry<String, List<Long>> me : clauses.entrySet()) {
            if (addClause(videoID, me.getKey(), me.getValue()) < 0) {
                return -1;
            }
        }
        return 0;
    }
    
    // add metadata information to a particular video in the db
    // overwite param will determine whether current metadata is replaced or just added on to
    public int addMetadata(String videoID, String metadata, boolean overwrite) {
        Entity vidEnt, metaEnt, currMeta;
        Key metaKey;
        String currData, newData;

        if ((vidEnt = getVideo(videoID)) == null) {
            System.out.println("Failed to retrieve " + videoID + " from database");
            return -1;
        }
        
        // need to find metadata already in the database
        if ((currMeta = getMetadata(videoID)) == null) {
            System.out.println("Failed to retrieve current metadata for " + videoID + " in database");
            return -2;
        }

        metaKey = currMeta.getKey();

        if (overwrite) {
            try {
                datastore.delete(metaKey);
                metaEnt = new Entity(M_KIND, metadata, vidEnt.getKey());
            } catch (Exception e) {
                System.out.println("Failed to overwrite metadata");
                return -3;
            }
        } else {
            try {
                currData = metaKey.getName();
                newData = currData + metadata;
                datastore.delete(metaKey);
                metaEnt = new Entity(M_KIND, newData, vidEnt.getKey());
            } catch (Exception e) {
                System.out.println("Failed to update metadata");
                return -4;
            }
        }
        try {
            datastore.put(metaEnt);
        } catch (Exception e) {
            System.out.println("Failed to upload metadata to database");
            return -5;
        }

        return 0;
    }
    
    // retrieve all keywords + their timestamps in a specified videoID
    public Map<String, List<Long>> getAllKeywords(String videoID) {
        Entity vidEnt;
        Key vidKey;
        Query query;
        PreparedQuery results;
        Map<String, List<Long>> clauseMap;

        if ((vidEnt = getVideo(videoID)) == null) {
            System.out.println("Failed to retrieve " + videoID + " from database");
            return null;
        }

        vidKey = vidEnt.getKey();

        query = new Query(C_KIND, vidKey);
        results = datastore.prepare(query);

        clauseMap = new HashMap<String, List<Long>>();

        try {
            for (Entity e : results.asIterable()) {
                clauseMap.put(e.getKey().getName(), (List<Long>)e.getProperty(T_KIND));
            } 
        } catch (Exception exception) {
            System.out.println("Failed to add database entries to output map");
            return null;
        }

        return clauseMap;
    }
    
    // retrieve specific timestamps for a specified keyword belonging to videoID
    public List<Long> getTimesForKeyword(String videoID, String keyword) {
        Map<String, List<Long>> clauseMap;
        List<Long> result;
        
        if ((clauseMap = getAllKeywords(videoID)) == null) {
            System.out.println("Failed to retrieve video's clause list");
            return null;
        }

        try {
            result = clauseMap.get(keyword);
        } catch (Exception e) {
            System.out.println("Failed to find " + keyword + " under " + videoID);
            return null;
        }

        return result;
    }

    // return true if specified video is in the database
    public boolean videoInDb(String videoID) {
        if (getVideo(videoID) == null) return false;
        else return true;
    }

    //================================================================================
    // Private Helper Functions
    //================================================================================

    /*
     * helper function to query database for specific video entity
     * @param videoID       Youtube ID of the video already in the db
     * @return              Entity representing the video in the db
     */
    private Entity getVideo(String videoID) {
        Query query = new Query(V_KIND);
        PreparedQuery results = datastore.prepare(query);

        try {
            for (Entity entity : results.asIterable()) {
                if (entity.getKey().getName().equals(videoID)) {
                    return entity;
                }
            }
        } catch (Exception exception) {
            return null;
        }
        
        return null;
    }

    /*
     * helper function to query database for specific metadata entity whose parent is videoID
     * @param videoID       Youtube ID of the video already in the db
     * @return              Entity representing the metadata of the video in the db
     */
    private Entity getMetadata(String videoID) {
        Query query = new Query(M_KIND);
        PreparedQuery results = datastore.prepare(query);

        try {
            for (Entity entity : results.asIterable()) {
                if (entity.getKey().getParent().getName().equals(videoID)) {
                    return entity;
                }
            }
        } catch (Exception exception) {
            return null;
        }

        return null;
    }
}
