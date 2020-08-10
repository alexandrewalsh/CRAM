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
 * 
 */

package com.google.sps.database;

import com.google.appengine.api.datastore.*;
import java.util.*;

public class DatabaseImpl implements DatabaseInterface {

    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static final String vKind = "video";
    private static final String mKind = "metadata";
    private static final String cKind = "caption";
    private static final String tKind = "timestamps";

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

        Entity myEnt = new Entity(vKind,"ncbb5B85sd2");

        datastore.put(myEnt);

        List<Entity> le = new ArrayList<>();

        for (Map.Entry<String, List<Long>> me : hm.entrySet()) {
            Entity tempEnt = new Entity(cKind, me.getKey(), myEnt.getKey());
            tempEnt.setProperty(tKind, me.getValue());
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
        Entity vidEnt = new Entity(vKind, videoID); //error possibility
        datastore.put(vidEnt);  //error possibility

        Entity metaEnt = new Entity(mKind, metadata, vidEnt.getKey()); //error possibility
        datastore.put(metaEnt); //error possibility

        return 0;
    }

    // add a keyphrase + timestamp pair to a particular video in the db
    public int addClause(String videoID, String keyword, List<Long> timestamps) {
        Entity vidEnt = getVideo(videoID);  //error possibility
        Entity keyEnt = new Entity(cKind, keyword, vidEnt.getKey()); //error possibility
        keyEnt.setProperty(tKind, timestamps); //error possibility
        datastore.put(keyEnt); //error possibility

        return 0;
    }
    
    // add multiple keyphrase + timestamp pairs to a particular video's entry
    public int addClauses(String videoID, Map<String, List<Long>> clauses) {
        for (Map.Entry<String, List<Long>> me : clauses.entrySet()) {
            addClause(videoID, me.getKey(), me.getValue());  //error possibility
        }

        return 0;
    }
    
    // add metadata information to a particular video in the db
    public int addMetadata(String videoID, String metadata, boolean overwrite) {
        Entity vidEnt = getVideo(videoID); //error possibility
        Entity metaEnt;
        
        // need to find metadata already in the database
        Entity currMeta = getMetadata(videoID);  //error possibility
        Key metaKey = currMeta.getKey();  //error possibility

        if (overwrite) {
            datastore.delete(metaKey); //error possibility
            metaEnt = new Entity(mKind, metadata, vidEnt.getKey()); //error possibility
            datastore.put(metaEnt); //error possibility
        } else {
// TO BE IMPLEMENTED (do not delete)
            /*
            try {
                metaEnt = datastore.get(metaKey); //error possibility
            } catch (Exception e) {
                return -1;
            }
            String current = metaEnt.getProperty("ID/Name").toString(); //error possibility
            datastore.delete(metaKey); //error possibility
            metaEnt = new Entity(m, current + metadata, vidEnt.getKey()); //error possibility
            */
            
        }
        //datastore.put(metaEnt); //error possibility

        return 0;
    }
    
    // retrieve all keywords + their timestamps in a specified videoID
    public Map<String, List<Long>> getAllKeywords(String videoID) {
        Entity vidEnt = getVideo(videoID);  //error possibility
        Key vidKey = vidEnt.getKey();  //error possibility

        Query query = new Query(cKind, vidKey); //error possibility
        PreparedQuery results = datastore.prepare(query); //error possibility

        Map<String, List<Long>> hm = new HashMap<String, List<Long>>();

        for (Entity e : results.asIterable()) {
            hm.put(e.getKey().getName(), (List<Long>)e.getProperty(t)); //error possibility
        }

        return hm;
    }
    
    // retrieve specific timestamps for a specified keyword belonging to videoID
    public List<Long> getTimesForKeyword(String videoID, String keyword) {
        Map<String, List<Long>> hm = getAllKeywords(videoID); //error possibility

        return hm.get(keyword); //error possibility
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
        Query query = new Query(vKind);
        PreparedQuery results = datastore.prepare(query); //error possibility

        for (Entity entity : results.asIterable()) {
            if (entity.getKey().getName().equals(videoID)) { //error possibility
                return entity;
            }
        }
        return null;
    }

    /*
     * helper function to query database for specific metadata entity whose parent is videoID
     * @param videoID       Youtube ID of the video already in the db
     * @return              Entity representing the metadata of the video in the db
     */
    private Entity getMetadata(String videoID) {
        Query query = new Query(mKind);
        PreparedQuery results = datastore.prepare(query); //error possibility

        for (Entity entity : results.asIterable()) {
            if (entity.getKey().getParent().getName().equals(videoID)) { //error possibility
                return entity;
            }
        }
        return null;
    }
}
