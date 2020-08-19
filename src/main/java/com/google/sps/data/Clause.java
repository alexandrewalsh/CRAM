

package com.google.sps.data;

import java.util.List;


public final class Clause {

    private String keyphrase;
    private List<Long> timestamps;
    private long caption_id;

    public Clause(String keyphrase, List<Long> timestamps, long caption_id) {
        this.keyphrase = keyphrase;
        this.timestamps = timestamps;
        this.caption_id = caption_id;
    }

    public String getKeyphrase() {
        return this.keyphrase;
    }

    public List<Long> getTimestamps() {
        return this.timestamps;
    }

    public long getCaptionID(){
        return this.caption_id;
    }
    
}