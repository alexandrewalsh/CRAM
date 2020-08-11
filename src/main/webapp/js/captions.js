/**
 * Currently holds:
 * submitFn                 -- event handler for search bar query
 * execute                  -- execute a request to download captions from Youtube API
 * displayVideo
 * beginCaptionRequest
 * getCaptions
 * parseCaptionsIntoJson
 * getIdFromUrl
 * sendJsonForm
 * resizeIFrame
 * styleEntitiesFromJson
 * document.ready
 */

/** global variables holding the json response for timestamps*/
var timestamps;

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
        renderError("Invalid youtube url!");
        return;
    }

    // displays the video in the front end
    displayVideo(videoId);

    // checks to see if mock captions should be used
    const queryParams = new URLSearchParams(window.location.search)
    if (queryParams.has('mock')) {
        sendJsonForm(JSON.stringify(MOCK_JSON_CAPTIONS));
        return;
    }
    
    // checks if mock nlp should be used
    if (queryParams.has('mockall')) {
        // Sets the results table
        document.getElementById('output').innerHTML = styleEntitiesFromJson(MOCK_NLP_OUTPUT);

        // clickable entities and timestamps
        setClickableEntities();
        
        return;
    }

    // checks to see if captions already exist in the database
    fetch('/caption?id=' + videoId, {
        method: 'GET',
    }).then((response) => response.json()).then((json) => {
        if (Object.keys(json).length > 0) {
            // Sets the results table
            document.getElementById('output').innerHTML = styleEntitiesFromJson(json);

            // clickable entities and timestamps
            setClickableEntities();

            console.log("Fetching captions from database...");
        } else {
            // video id not found in db, fetching from Youtube API
            beginCaptionRequest(videoId, url);
        }
    });
}

/**
 * Renders the YouTube video in the iframe tag
 * @param videoId - the id of the YouTube video to display
 */
function displayVideo(videoId) {
    // build the youtube src url
    var youtubeSourceBuilder = "https://www.youtube.com/embed/"
    youtubeSourceBuilder += videoId
    youtubeSourceBuilder += "?enablejsapi=1"
    youtubeSourceBuilder += "&origin=" + location.origin;

    // display "Results" header
    document.getElementById("resultsHeader").style.display = "inline";

    // Displays entity searchbar
    $('#entity-search-form').css('display', 'flex');

    // set player source
    $('#player').attr('src', youtubeSourceBuilder);  
    resizeIFrame();  
    player = new YT.Player('player', {
        events: {'onReady': onPlayerReady, 'onStateChange': onPlayerStateChange}
    });
}

/**
 * Sets up the caption request call
 * @param videoId - the Youtube video id to find the captions of
 * @param url - the Youtube video url
 */
function beginCaptionRequest(videoId, url) {
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
            renderError(err.status);
            failure(err);
        });
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
            
            // Sets the results table
            document.getElementById('output').innerHTML = styleEntitiesFromJson(json);

            // clickable entities and timestamps
            setClickableEntities();
        });
}

/**
 * Resizes the embedded video based on window size, using either the 4:3 width to height ratio or the remaining screen
 */
function resizeIFrame() {
    // Saves necessary parameters as variables
    var width = $('#player').width();
    var windowHeight = $(window).height();
    var reservedHeight = $('#heading-div').outerHeight(true) + $('#searchbar-div').outerHeight(true) + windowHeight * 0.05;
    
    // The remaining avalable height for the video that avoids overflow
    var totalAvailableHeight = windowHeight - reservedHeight;

    // The height of the video to keep the 4:3 aspect ratio
    var videoHeightFromRatio = width / 1.33;

    // Defines and sets the best video height to ensure that overflow does not occur
    var playerHeight = (videoHeightFromRatio > totalAvailableHeight) ? totalAvailableHeight : videoHeightFromRatio;
    $('#player').height(playerHeight);
    $('#output').height(playerHeight - $('#resultsHeader').height());

    // change output size to match the player
    $("#flex-item-output").css("height", $("#player").height());
}

/**
 * Builds the entities table from the json response
 * @param json - The json response of entity data
 */
function styleEntitiesFromJson(json) {
    timestamps = json; // set global variable

    var output = '<table>';

    for (var key in json) {
        // METADATA line sent to log, all others are sent to Caption Results section.
        if (key == "METADATA") {
            console.log('NLP Fetch Time: ' +  json[key][1]);
            console.log('Total Youtube Captions: ' + json[key][0]);
            console.log('Total Entities Found: ' + json[key][2]);
        } else {
            output += '<tr><td><span class="word">' + key + '</span></td>';
            output += '</tr>';
        }
    }
    
    output += '</table>';
    return output;
}


/**
 * Sets the timestamp class objects to be clickable
 */
function setClickableTimestamps() {
    var elements = document.getElementsByClassName("timestamps");
    for (var i = 0; i < elements.length; i++) {
        elements[i].addEventListener('click', onTimeClick, false);
    }
}

/**
 * Adds click event listeners to word entities to show
 * timestamps. Also makes the timestamps clickable.
 */
function setClickableEntities() {
    $('.word').bind("click", function(){
        const entity = this.innerText;
            
        // delete all children
        $("#timestamp-timeline").empty();

        // query json
        for (var index in timestamps[entity]) {
            const timestamp = epochToTimestamp(timestamps[entity][index]);
            $("#timestamp-timeline").append("<span class='timestamps'>"+timestamp+"</span>");
            $("#timestamp-timeline").append("<p>,</p>");
        }

        $("#timestamp-timeline p:last-child").remove();
        
        // clickable timestamps
        setClickableTimestamps();
    });
}

$(document).ready(() => {

    // Resizes the video whenever the window resizes
    resizeIFrame();
    $(window).resize(() => {
        resizeIFrame();
    });
});