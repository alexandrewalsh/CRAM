/**
 * Currently holds:
 * submitFn                 -- event handler for search bar query
 * execute                  -- execute a request to download captions from Youtube API
 * successfulDisplay
 * displayVideo
 * beginCaptionRequest
 * getCaptions
 * parseCaptionsIntoJson
 * getIdFromUrl
 * sendJsonForm
 * styleEntitiesFromJson
 * styleEntitiesFromList
 * setClickableTimestamps
 * setClickableEntities
 * fetchBookmarks
 * displayBookmarks
 * clearBookmarkForm
 * setBookmarkButton
 * document.ready
 */

/** global variables holding the json response for timestamps */
var currentVideoID;
var timestamps;
var bookmarks;

/**
 * Event handler for search bar query, entry point
 * @param obj - the button invoking the click
 * @param evt - the click event
 */
function submitFn(obj, evt){
    $("#search-wrapper").addClass('search-wrapper-active');
    // hide results and search form
    $('#entity-search-form').hide();
    value = $(obj).find('.search-input').val().trim();
    evt.preventDefault();

    // delete all children
    $("#timestamp-timeline").empty();
    $("#keywords-output").empty();
    $("#query-output").empty();
    $("#bookmarks-output").empty();

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

    // verify youtube regex 
    if (!/(?:https?:\/\/)?(?:youtu\.be\/|(?:www\.|m\.)?youtube\.com\/(?:watch|v|embed)(?:\.php)?(?:\?.*v=|\/))([a-zA-Z0-9\-_]+)/.test(url)) {
        renderError("Invalid youtube url!");
        return;
    }

    var videoId = "";

    try {
        videoId = getIdFromUrl(url);
        currentVideoID = videoId;
    } catch {
        renderError("Invalid youtube url!");
        return;
    }

    // hides the bookmarks for the previous video
    $('#bookmark-display-div').html('');

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
        successfulDisplay(MOCK_NLP_OUTPUT);
        return;
    }

    // checks to see if captions already exist in the database
    fetch('/caption?id=' + videoId, {
        method: 'GET',
    }).then((response) => response.json()).then((json) => {
        if (Object.keys(json).length > 0) {
            // Sets the results table
            successfulDisplay(json);
            console.log("Fetching captions from database...");
        } else {
            // video id not found in db, fetching from Youtube API
            beginCaptionRequest(videoId, url);
        }
    });
}

/**
 * Renders the entities of the video after a successful json fetch
 * @param json The json of the entities fetched
 */
function successfulDisplay(json) {
    // display results
    $('#entity-search-form').show();
    $('#entity-search-form').css('display', 'flex');
    $('#entity-search-form').css('justify-content', 'center');


    // valid YT Url, clear error status if one exists
    $('.search-input').removeClass("error-placeholder");

    document.getElementById('keywords-output').innerHTML = styleEntitiesFromJson(json);
    $('#keywords-toggle-button').trigger('click');
    $("#output").show();
    $('.btn-group').css('display', 'block');
    // clickable entities and timestamps
    setClickableEntities();
    sortEntities();

    const queryParams = new URLSearchParams(window.location.search);
    var email = '';
    if (queryParams.has('mockall')) {
        email = 'MOCK';
    } else {
        email = getAuth().currentUser.get().getBasicProfile().getEmail();
    }

    fetchBookmarks(email, currentVideoID);
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

    // set player source
    $('#player').attr('src', youtubeSourceBuilder);  
    resizeIFrame();  

    if (player == null) {
        player = new YT.Player('player', {
            events: {'onReady': onPlayerReady, 'onStateChange': onPlayerStateChange}
        });
    } else {
        player.loadVideoByUrl("http://www.youtube.com/v/"+videoId+"?version=3");
    }

    // append bookmark button
    setBookmarkButton();
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
        "snippet"
      ]
    }).then(function(response) {
        if (response.result != null & response.result.items != null &
            response.result.items.length > 0) {
            
            var i;
            var trackId = "";
            for (i = 0; i < response.result.items.length; i++) {
                if (response.result.items[i].snippet.language === "en") {
                    trackId = response.result.items[i].id;
                    break;
                }
            }
            
            if (trackId === "") { // no english track found
                renderError("No English Captions Track for this Video");
                return;
            }
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

    fetch('/caption', {
            method: 'POST',
            body: params,
        }).then((response) => response.json()).then((json) => {     
            // Sets the results table            
            successfulDisplay(json);
        });
}


/**
 * Builds the entities table from the json response
 * @param json - The json response of entity data
 * @return The HTML string that creates the table of entities
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
 * Builds the entities table from a list of entities
 * @param list - The list containing entity names
 * @return The HTML string that creates the table of entities
 */
function styleEntitiesFromList(list) {
    var output = '<table>';
    for (entity of list) {
        output += '<tr><td><span class="word">' + entity + '</span></td></tr>';
    }
    output += '</table>'
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
    $('.word').bind("click", function() {
        const entity = this.innerText;
            
        // delete all children
        $("#timestamp-timeline").empty();

        // append bookmark button
        setBookmarkButton();

        // query json
        $("#timestamp-timeline").append("<p>"+entity+" appears at </p>");

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

/**
 * Fetches and displays the bookmarks for the current user and video
 * @param email - The email of the current user
 * @param videoId - The videoId of the current video
 */
function fetchBookmarks(email, videoId) {
    var fetchUrlBuilder = '/bookmark?email=' + email + '&videoId=' + videoId;

    fetch(fetchUrlBuilder).then(response => response.json()).then(json => {
        $('#bookmarks-output').html('');
        if (Array.isArray(json)) {
            displayBookmarks(json);
        } else if (typeof json === 'object' && json !== null) {
            console.log(json.ERROR);
            alert(json.ERROR);
        }
    });

}

/**
 * Sets listeners for deleting bookmarks on clicks
 */
function addRemoveBookmarkListeners() {
    // Removes click listeners from buttons to remove bookmarks to redefine click functionality
    // Uses a fetch POST request to remove the current bookmark from the database
    $('.remove-bookmark').off('click');
    $('.remove-bookmark').click(function() {
        const queryParams = new URLSearchParams(window.location.search)
        var id = $(this).val();
        var params = new URLSearchParams();
        if (queryParams.has('mockall')) {
            params.append('email', 'MOCK');
        } else {
            params.append('email', getAuth().currentUser.get().getBasicProfile().getEmail());
        }
        params.append('videoId', currentVideoID);
        params.append('bookmarkId', id);
        params.append('function', 'remove');
        fetch('/bookmark', {
            method: 'POST',
            body: params,
        }).then((response) => response.json()).then(json => {
            displayBookmarks(json);
        });
    });
}


/**
 * Sets listeners for viewing content on clicks
 */
function addContentBookmarkListeners() {
    // Removes click listeners from buttons to show bookmark content to redefine click functionality
    // Toggles between showing and hiding the bookmark content
    $('.bookmark').off('click');
    $('.bookmark').click(function() {
        var contentDiv = this.parentElement.nextSibling;
        if (contentDiv.style.maxHeight && contentDiv.style.maxHeight != '0px') {
            contentDiv.style.maxHeight = null;
        } else {
            $('.content').css('maxHeight', '0px');
            contentDiv.style.maxHeight = contentDiv.scrollHeight + "px";
            var bookmarkId = $(this).next().val();
            player.seekTo(bookmarks[bookmarkId].timestamp, true);
        } 
    });
}

/**
 * Renders bookmarks in HTML from a list of Bookmark objects
 * @param list - The list of Bookmark objects
 */
function displayBookmarks(list) {
    // Resets the global bookmarks variable to only store current bookmarks
    bookmarks = {};

    // Builds the HTML text to display on page
    var output = '<ul>';
    for (bookmark of list) {
        bookmarks[bookmark.id] = {'title': bookmark.title, 'timestamp': bookmark.timestamp, 'content': bookmark.content};
        output += '<li><span  class="bookmark collapsible">' + bookmark.title + '</span>';
        output += '<button class="remove-bookmark" value="' + bookmark.id + '">&times;</button></li>'; 
        output += '<div class="content"><pre>' + bookmark.content + '</pre></div>'
    }
    output += '</ul>';
    
    // Inserts the HTML text to the page
    $('#bookmarks-output').html(output);
    sortBookmarks();
    addRemoveBookmarkListeners();
    addContentBookmarkListeners();

}

/**
 * Builds the unordered list of bookmarks from the list of bookmark ids
 * @param list The list of bookmark uuids 
 * @return The HTML of an unordered list in the order of the list
 */
function styleBookmarksFromList(list) {
    var output = '<ul>';
    for (bookmark of list) {
        output += '<li><span  class="bookmark collapsible">' + bookmarks[bookmark].title + '</span>';
        output += '<button class="remove-bookmark" value="' + bookmark + '">&times;</button></li>'; 
        output += '<div class="content"><pre>' + bookmarks[bookmark].content + '</pre></div>'
    }
    output += '</ul>';
    return output;
}


/**
 * Clears the contents of the bookmarks form
 */
function clearBookmarkForm() {
    $('#bookmark-title').val('');
    $('#bookmark-content').val('');
    player.playVideo();
}

/**
 * Adds button to add bookmarks and listeners
 */
function setBookmarkButton() {
    // append bookmark button
    $("#timestamp-timeline").append('<button id="add-bookmark-button"><i style="font-size:24px" class="fa">&#xf097;</i></button>');

    // Displays modal for adding bookmark
    $('#add-bookmark-button').click(() => {
        $('#myModal').css('display', 'block');
        $('.modal-body form').css('display', 'block');
        player.pauseVideo();
    });

    // Closes modal when clicking close button
    $('.modal-close').click(() => {
        $('#myModal').css('display', 'none');
        $('.modal-body form').css('display', 'none');
        clearBookmarkForm();
    });

    // Closes modal when clicking outside the modal
    $(window).click(function(event) {
        // Must check if the modal is open before closing the modal
        if (event.target == document.getElementById('myModal')) {
            $('#myModal').css('display', 'none');
            $('.modal-body form').css('display', 'none');
            clearBookmarkForm();
        }
    });
}


/**
 * Adds bookmark to database based on modal input
 */
function addBookmarkToDatabase() {
    // Creates the request parameters
    const queryParams = new URLSearchParams(window.location.search)
    var params = new URLSearchParams();
    if (queryParams.has('mockall')) {
        params.append('email', 'MOCK');
    } else {
        params.append('email', getAuth().currentUser.get().getBasicProfile().getEmail());
    }
    params.append('videoId', currentVideoID);
    params.append('timestamp', Math.floor(player.getCurrentTime()));
    params.append('title', ESCAPE_HTML($('#bookmark-title').val()));
    params.append('content', ESCAPE_HTML($('#bookmark-content').val()));
    params.append('function', 'add');

    // Sends the bookmark parameters to the servlet to process
    fetch('/bookmark', {
        method: 'POST',
        body: params,
    }).then((response) => response.json()).then(json => {
        displayBookmarks(json);
    });

    // Hides the modal
    $('#myModal').css('display', 'none');
    $('.modal-body form').css('display', 'none');
    clearBookmarkForm();
}


$(document).ready(() => {

    // Resizes the video whenever the window resizes
    resizeIFrame();
    $(window).resize(() => {
        resizeIFrame();
    });

    // Adds a bookmark when clicking the 'add bookmark' button
    $('#bookmark-add-button').click(() => {addBookmarkToDatabase()});

});