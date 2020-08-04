/**
 * Currently holds:
 * submitFn                 -- event handler for search bar query
 * execute                  -- execute a request to download captions from Youtube API
 * getCaptions
 * parseCaptionsIntoJson
 * getIdFromUrl
 * sendJsonForm
 * document.ready
 */

/**
 * Event handler for search bar query, entry point
 * @param obj - the button invoking the click
 * @param evt - the click event
 */
function submitFn(obj, evt){
    return new Promise((resolve, reject) => {
        $("#search-wrapper").addClass('search-wrapper-active');
        $('#resultsHeader').style = "display: unset;"
        value = $(obj).find('.search-input').val().trim();
        evt.preventDefault();
        execute(value).then(()=>{
            resolve();
        });
    });
}

/**
 * Excecutes a request to download captions for a YouTube 
 * video with a given `url`, and send data to the backend servlet
 * @param url - a url for a YouTube video
 * @requires - an authenticated user
 */
// Make sure the client is loaded and sign-in is complete before calling this method.
function execute(url) {
    return new Promise((resolve, reject) => {
        // user inputs YouTube video URL
        var videoId = "";

        try {
            videoId = getIdFromUrl(url);
        } catch {
            renderYtError("Invalid youtube url!");
            reject("Invalid YT URL");
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
            sendJsonForm(JSON.stringify(MOCK_JSON_CAPTIONS)).then(()=>{
                resolve();
            });
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
                    sendJsonForm(json).then(() => {
                        resolve();   // register when elements have been rendered
                    });
                });
            }
        }, function(err) {
            // top level error handler
            console.error("Execute error", err); 
            reject("Execute error: " + err);
        });
    });
}

/**
 * Create and send a form with the captions JSON. May need 
 * to authenticate users here too to prevent malicious requests. @enriqueavina
 * @param json - the JSON string representing the 
 *               parsed captions response
 * @return - HTML containing formatted captions and timestamps 
 */
function sendJsonForm(json) {
    return new Promise((resolve, reject) => {
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

                resolve("Captions rendered");
        });
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
                caption['startTime'] = timestampToEpoch(data[0]);
                caption['endTime'] = timestampToEpoch(data[1]);

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