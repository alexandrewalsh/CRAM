/**
 * Initialize authentication and attempt to sign user in
 */
function authenticate() {
    return gapi.auth2.getAuthInstance()
        .signIn({scope: "https://www.googleapis.com/auth/youtube.force-ssl"})
        .then(function() { console.log("Sign-in successful"); },
              function(err) { console.error("Error signing in", err); });
}

/**
 * Initialize the gApi client to make API requests
 */
function loadClient() {
    gapi.client.setApiKey(config.api_key);
    return gapi.client.load("https://www.googleapis.com/discovery/v1/apis/youtube/v3/rest")
        .then(function() { console.log("GAPI client loaded for API"); },
              function(err) { console.error("Error loading GAPI client for API", err); });
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
        reader.onloadend = function(evt){
            lines = evt.target.result.split(/\r\n|\n|\r/);  

            /* Line types
                0: timestamp line
                1: text line
                2: empty line
            */
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
function getCaptions(trackId, url){
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
            if (err.status == 403) {
                console.error("Video has private captions!");
            }
            failure(err);
        });
    }).catch(function(error) {
        // throw for top level handler to handle
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
 * Excecutes a request to download captions for a YouTube 
 * video with a given `url`, and send data to the backend servlet
 * @param url - a url for a YouTube video
 * @requires - an authenticated user
 */
// Make sure the client is loaded and sign-in is complete before calling this method.
function execute(url) {
    // user inputs YouTube video URL
    const videoId = getIdFromUrl(url);

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
    /** 
    var form = document.createElement('form');
    form.method = 'POST';
    form.action = '/caption';

    var jsonInput = document.createElement('input');
    jsonInput.type = 'hidden';
    jsonInput.name = 'json';
    jsonInput.value = json;
    form.appendChild(jsonInput);

    document.body.appendChild(form);
    form.submit();
    */
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
                output += key + ': ' + JSON.stringify(json[key]) + '<br>';
            }
            //$('#nlp-output').html(output);
            alert(output);
            numCap = json['METADATA'][0];
            time = json['METADATA'][1];
            //alert('Number of Captions: ' + numCap + '\nExecution Time: ' + time);
        });
}
  
// Initialize authentication client
gapi.load("client:auth2", function() {
    gapi.auth2.init({client_id: config.client_id});
});