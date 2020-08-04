
/**
 * Convert a string timestamp to an epoch long
 * @param timestamp - a String in the format HH:MM:SS.MS if
 *                    hours are included MM:SS.MS otherwise
 * @returns the number of seconds since the video started,
 *          returns null if the timestamp is improperly formatted
 */
function timestampToEpoch(timestamp) {
    const parts = timestamp.split(':');

    if (parts.length == 3) {
        // contains h:m:s
        const hours = parseInt(parts[0]);
        const minutes = parseInt(parts[1]);
        const seconds = parseInt(parts[2]);
        return hours*3600 + minutes*60 + seconds;
    } else if (parts.length == 2) {
        // only m:s
        const minutes = parseInt(parts[0]);
        const seconds = parseInt(parts[1]);
        return minutes*60 + seconds;
    }
    return null;
}

/**
 * Convert epoch seconds to a timestamp
 * in the format H:M:S.MS
 * @param secs - seconds
 * @return - a String timestamp 
 */
function epochToTimestamp(secs) {
    var sec_num = parseInt(secs, 10);
    var hours   = Math.floor(sec_num / 3600);
    var minutes = Math.floor(sec_num / 60) % 60;
    var seconds = sec_num % 60;

    return [hours,minutes,seconds]
        .map(v => v < 10 ? "0" + v : v)
        .filter((v,i) => v !== "00" || i > 0)
        .join(":");
}

// called when timestamp is clicked on
var onTimeClick = function() {
    var text = this.innerText;
    // convert the timestamp into seconds
    text = timestampToEpoch(text).toString();
    var numPattern = /\d+/g;
    var time = text.match(numPattern);
    player.seekTo(time[0], false);
};
