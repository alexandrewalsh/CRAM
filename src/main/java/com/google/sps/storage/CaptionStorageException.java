/*
 * Wrapper exception class to contain all our exceptions thrown in CaptionStorage.java
 */

package com.google.sps.storage;

public class CaptionStorageException extends Exception {

    private Reason m_reason;

    public CaptionStorageException(Reason r, String errorMessage, Throwable err) {
        super(errorMessage, err);
        this.setReason(r);
    }

    public CaptionStorageException(Reason r, String errorMessage) {
        super(errorMessage);
        this.setReason(r);
    }

    public Reason getReason() {
        return m_reason;
    }

    public void setReason(Reason r) {
        m_reason = r;
    }
}