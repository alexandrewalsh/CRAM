/*
 * Contains all the possible reasons a CaptionStorageException could be thrown.
 */

package com.google.sps.storage;

public enum Reason {
    ADD_VIDEO_ERR,
    ADD_META_ERR,
    GET_VIDEO_ERR,
    GET_META_ERR,
    ADD_KEYPHRASE_ERR,
    GET_KEYPHRASE_ERR,
    OVERWRITE_META_ERR,
    APPEND_META_ERR,
    NO_VIDEO_EXISTS,
    NO_META_EXISTS,
    NO_KEYPHRASE_EXISTS
}