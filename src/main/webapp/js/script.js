const MOCK_JSON_CAPTIONS = {
    url: 'mock',
    captions: [
        {'startTime': 0, 'endTime': 20, 'text': 'Mitochondria are membrane-bound cell organelles (mitochondrion, singular) that generate most of the chemical energy needed to power the cells biochemical reactions. Chemical energy produced by the mitochondria is stored in a small molecule called adenosine triphosphate (ATP).'}
    ]
}

/**
 * Convert a string timestamp to an epoch long
 * @param timestamp - a String in the format HH:MM:SS.FS
 * @returns the number of seconds since the video started
 */
function epoch(timestamp) {
    const parts = timestamp.split(':');
    var hours = parseInt(parts[0]);
    var minutes = parseInt(parts[1]);
    var seconds = parseInt(parts[2]);
    var epochTime = hours*3600 + minutes*60 + seconds;
    return epochTime;
}

/**
 * Parse and format the response to youtube.captions.download()
 * @param response - a String in SBV format representing the captions
 *                   and their respective timestamps
 * @param url - the url of the YouTube video
 * @returns a promise which upon success returns a JSON 
 *         string encoding the captions and timestamps 
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


            //lines = evt.target.result.split(/\r\n|\n|\r/);  

            /* Line types
                0: timestamp line
                1: text line
                2: empty line
            */
            /*
            var lineType = 0;
            var caption = {};

            lines.forEach(function(line) {
                if (lineType == 0) {    // timestamp
                    if (line === ""){  // likely EOF
                        return;
                    }
                    const timestamps = line.split(',');
                    if (timestamps.length != 2) {
                        console.error('timestamp line malformatted: ' + line);
                        failure('failed');
                        return;
                    }
                    caption["startTime"] = epoch(timestamps[0]);
                    caption["endTime"] = epoch(timestamps[1]);

                } else if (lineType == 1) {   // text
                    caption["text"] = line;
                    json.captions.push(caption);
                    caption = {};
                } 

                lineType += 1;
                if (lineType > 2) {
                    lineType = 0;
                }
            });
              */  
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
 * Render an error message for a given error code
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
 * Render a generic error with a `message`
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

// global variable holding the Youtube Video
var player;

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

    player = new YT.Player('player', {
        height: '390',
        width: '640',
        videoId: videoId,
        events: {'onReady': onPlayerReady, 'onStateChange': onPlayerStateChange}
    });

    if ($('#captionMockButton').text() == 'Mocking') {
        sendJsonForm(JSON.stringify(MOCK_JSON_CAPTIONS));
        return;
    }

    return gapi.client.youtube.captions.list({
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
                console.log("final destination", json);
                sendJsonForm(json);
            });
        }
    }, function(err) {
        // top level error handler
        console.error("Execute error", err); 
    });
}

/**
 * Create and send a form with the captions JSON. May need 
 * to authenticate users here too to prevent malicious requests.
 * @param json - the JSON string representing the 
 *               parsed captions response
 */
function sendJsonForm(json) {
    var params = new URLSearchParams();
    params.append('json', json);
    fetch('/caption', {
            method: 'POST',
            body: params,
        }).then((response) => response.json()).then((json) => {
            var output = '';
            var numCap = 0;
            var time = 0;
            for (var key in json) {
                output += '<div class="word">' + key + ':</div> ' + '<div class="timestamps">' + JSON.stringify(json[key]) + '</div><br>';
            }
            //$('#nlp-output').html(output);
            document.getElementById('output').innerHTML = output;

            // clickable timestamps
            var elements = document.getElementsByClassName("timestamps");

            for (var i = 0; i < elements.length; i++) {
                elements[i].addEventListener('click', onTimeClick, false);
            }

            numCap = json['METADATA'][0];
            time = json['METADATA'][1];
            //alert('Number of Captions: ' + numCap + '\nExecution Time: ' + time);
        });
}

/************************************************* */
/*        JS for Video Player Manipulation         */
/************************************************* */

// This code loads the IFrame Player API code asynchronously.
var tag = document.createElement('script');

tag.src = 'https://www.youtube.com/iframe_api';
var firstScriptTag = document.getElementsByTagName('script')[0];
firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

// The API will call this function when the video player is ready.
function onPlayerReady(event) {
  event.target.playVideo();
}

// The API calls this function when the player's state changes.
var done = false;
function onPlayerStateChange(event) {
  if (event.data == YT.PlayerState.PLAYING && !done) {
    done = true;
  }
}
function stopVideo() {
  player.stopVideo();
}
function seekVideo() {
    player.playVideo();
    player.seekTo(60, true);
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
    var numPattern = /\d+/g;
    var time = text.match(numPattern);
    player.seekTo(time[0]);
};