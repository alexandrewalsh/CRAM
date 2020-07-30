/**
 * Currently holds all the functionality of the site
 * Should be split into getting captions, video/player display, NLP fetching, and common utilities
 */

/**
 * Convert a string timestamp to an epoch long
 * @param timestamp - a String in the format HH:MM:SS.MS if
 *                    hours are included MM:SS.MS otherwise
 * @returns the number of seconds since the video started,
 *          returns null if the timestamp is improperly formatted
 */
function epoch(timestamp) {
    const parts = timestamp.split(':');

    if (parts.length == 3) {
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

/**
 * Event handler for search bar query, entry point
 * @param obj - the button invoking the click
 * @param evt - the click event
 */
function submitFn(obj, evt){
    $("#search-wrapper").addClass('search-wrapper-active');
    $('#resultsHeader').style = "display: unset;"
    value = $(obj).find('.search-input').val().trim();
    evt.preventDefault();
    execute(value);
}

/**
 * Excecutes a request to download captions for a YouTube 
 * video with a given `url`, and send data to the backend servlet
 * @param url - a url for a YouTube video
 * @requires - an authenticated user
 */
// Make sure the client is loaded and sign-in is complete before calling this method.
function execute(url) {
    // user inputs YouTube video URL
    var videoId = "";

    try {
        videoId = getIdFromUrl(url);
    } catch {
        renderYtError("Invalid youtube url!");
        return;
    }

    // build the youtube src url
    var youtubeSourceBuilder = "https://www.youtube.com/embed/"
    youtubeSourceBuilder += videoId
    youtubeSourceBuilder += "?enablejsapi=1"
    youtubeSourceBuilder += "&origin=" + location.origin;
    console.log(youtubeSourceBuilder);

    // set player source
    $('#player').attr('src', youtubeSourceBuilder);    
    player = new YT.Player('player', {
        events: {'onReady': onPlayerReady, 'onStateChange': onPlayerStateChange}
    });

    if ($('#captionMockButton').text() == 'Mocking') {
        sendJsonForm(JSON.stringify(MOCK_JSON_CAPTIONS));
        return;
    }

    gapi.client.youtube.captions.list({
      "videoId": videoId,
      "part": [
        "id"
      ]
    }).then(function(response) {
        if (response.result != null & response.result.items != null &
            response.result.items.length > 0) {

            const trackId = response.result.items[0].id;

            getCaptions(trackId, url).then(json => {
                // send to backend
                sendJsonForm(json);
            });
        }
    }, function(err) {
        // top level error handler
        console.error("Execute error", err); 
    });
}


/** 
 * Get the captions and timestamps for a video with a given `trackId`
 * @param trackId - a String representing the track id for a caption
 * @param url - the url of the YouTube video
 * @returns a promise which upon success returns a JSON 
 *         string encoding the captions and timestamps 
 */
function getCaptions(trackId, url) {
    return new Promise((success, failure) => {
        gapi.client.youtube.captions.download({
            "id": trackId,
            "tlang": "en",
            "tfmt": "sbv"
        }).then(function(response){
            parseCaptionsIntoJson(response, url).then(json => {
                success(json);  
            });
        }, function(err) { 
            console.error("errors getting captions", err); 
            renderError(err.status);
            failure(err);
        });
    }).catch(function(error) {
        // throw for top level handler to handle
        throw error;   
    });
}


/**
 * Parse and format the response to youtube.captions.download()
 * @param response - a String in SBV format representing the captions
 *                   and their respective timestamps
 * @param url - the url of the YouTube video
 * @returns a promise which upon success returns a JSON 
 *          string encoding the captions and timestamps 
 */
function parseCaptionsIntoJson(response, url){
    return new Promise((success, failure) => {
        var json = {
            url: url,
            captions: []    
        };

        let reader = new FileReader();

        // read response line by line
        reader.onloadend = function(evt) {
            
            // Removes newlines and splits by empty line, which signifies a new caption
            var result = evt.target.result.replace(/(\r\n|\n|\r)/gm,",");
            var lines = result.split(',,');

            // Loops through all captions, which should have the format:
            //      startTime, endTime, text
            for (var i = 0; i < lines.length; i++) {
                var caption = {}
                var data = lines[i].split(',');
                if (data.length < 3) {
                    continue;
                }
                caption['startTime'] = epoch(data[0]);
                caption['endTime'] = epoch(data[1]);

                // Builds text string with the consideration that commas could exist in text
                var textBuilder = '';
                for (var j = 2; j < data.length; j++) {
                    textBuilder += data[j];
                    if (j < data.length - 1) {
                        textBuilder += ', ';
                    }
                }
                caption['text'] = textBuilder;
                json.captions.push(caption);
            }   

            // successfully parsed response
            success(JSON.stringify(json));
        };

        reader.readAsText(new Blob([response.body], {
            type: 'text/plain'
        }));

    }).catch(function(error) {
        // stop processing lines, rethrow error
        throw error;
    });
}

/**
 * Render an error message for a given error code (Doesn't work yet)
 * @param error - an HTTP error code
 */
function renderError(error) {
    var errorMsg = document.getElementById('errorMsg'); // could make these consts
    if (!errorMsg) {
        errorMsg = document.createElement('p');
        errorMsg.setAttribute('id', 'errorMsg');
        errorMsg.classList += 'error';
        document.body.appendChild(errorMsg);
    }

    if (error == 403) {
        errMsg.innerText = "Video has private captions!";
    } else if (error == 404) {
        errMsg.innerText = "404 don't know how to deal with this";
    }
}

/**
 * Render a generic error with a `message`. (This also doesn't work yet)
 * @param message - the message to render
 */
function renderYtError(message) {
    var errorMsg = document.getElementById('ytError');
    if (!errorMsg) {
        errorMsg.setAttribute('id', 'ytError');
        errorMsg = document.createElement('p');
        errorMsg.classList += 'error';
        document.body.appendChild(errorMsg);
    }

    errorMsg.innerText = message;
}

/**
 * Gets the video id from a YouTube url
 * @param url - a YouTube video url
 * @returns - the `id` for a YouTube `url`
 */
function getIdFromUrl(url) {
    var video_id = url.split('v=')[1];
    var ampersandPosition = video_id.indexOf('&');
    if(ampersandPosition != -1) {
        video_id = video_id.substring(0, ampersandPosition);
    }

    return video_id;
}

/**
 * Toggle the search bar appearance based on event (To be moved to other file)
 * @param obj - the button invoking the click
 * @param evt - the click event
 */
function searchToggle(obj, evt){
    var container = $(obj).closest('.search-wrapper');

    if(!container.hasClass('active')){
        container.addClass('active');
        evt.preventDefault();
    }
    else if (container.hasClass('active') && $(obj).closest('.input-holder').length == 0){
        container.removeClass('active');
        // clear input
        container.find('.search-input').val('');
        // clear and hide result container when we press close
        container.find('.result-container').fadeOut(100, function(){$(this).empty();});
    }
}

/**
 * Create and send a form with the captions JSON. May need 
 * to authenticate users here too to prevent malicious requests. @enriqueavina
 * @param json - the JSON string representing the 
 *               parsed captions response
 * @return - HTML containing formatted captions and timestamps 
 */
function sendJsonForm(json) {
    var params = new URLSearchParams();
    params.append('json', json);
    $('#output').html('<p>Loading...</p>');

    fetch('/caption', {
            method: 'POST',
            body: params,
        }).then((response) => response.json()).then((json) => {
            // display "Results" header
            document.getElementById("resultsHeader").style.display = "inline";
            var output = '<table>';

            for (var key in json) {
                // METADATA line sent to log, all others are sent to Caption Results section.
                if (key == "METADATA") {
                    console.log('NLP Fetch Time: ' +  json[key][1]);
                    console.log('Total Youtube Captions: ' + json[key][0]);
                    console.log('Total Entities Found: ' + json[key][2]);
                }
                else {
                    output += '<tr><td><span class="word">' + key + ':</span></td> ' + '<td><span class="timestamps">' + epochToTimestamp(JSON.stringify(json[key][0])) + '</span></td></tr>';
                }
            }
            output += '</table>';
            document.getElementById('output').innerHTML = output;

            // clickable timestamps
            var elements = document.getElementsByClassName("timestamps");

            for (var i = 0; i < elements.length; i++) {
                elements[i].addEventListener('click', onTimeClick, false);
            }
        });
}

// The API will call this function when the video player is ready.
function onPlayerReady(event) {
  event.target.playVideo();
}

// The API calls this function when the player's state changes.
function onPlayerStateChange(event) {
  if (event.data == YT.PlayerState.PLAYING && !done) {
    done = true;
  }
}

// display that mocking captions are now active
$(document).ready(() => {
    $('#captionMockButton').click(() => {
        if ($('#captionMockButton').text() == 'Click Me to Mock Captions') {
            $('#captionMockButton').text('Mocking');
        } else {
            $('#captionMockButton').text('Click Me to Mock Captions');
        }
    });
});

// called when timestamp is clicked on
var onTimeClick = function() {
    var text = this.innerText;
    // convert the timestamp into seconds
    text = epoch(text).toString();
    var numPattern = /\d+/g;
    var time = text.match(numPattern);
    player.seekTo(time[0]);
};