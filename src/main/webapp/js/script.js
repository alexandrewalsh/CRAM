function authenticate() {
    return gapi.auth2.getAuthInstance()
        .signIn({scope: "https://www.googleapis.com/auth/youtube.force-ssl"})
        .then(function() { console.log("Sign-in successful"); },
              function(err) { console.error("Error signing in", err); });
}

function loadClient() {
    gapi.client.setApiKey(config.api_key);
    return gapi.client.load("https://www.googleapis.com/discovery/v1/apis/youtube/v3/rest")
        .then(function() { console.log("GAPI client loaded for API"); },
              function(err) { console.error("Error loading GAPI client for API", err); });
}

function epoch(timestamp) {
    const parts = timestamp.split(':');
    var hours = parseInt(parts[0]);
    var minutes = parseInt(parts[1]);
    var seconds = parseInt(parts[2]);
    var epochTime = hours*3600 + minutes*60 + seconds;
    return epochTime;
}

function parseCaptions(response){
    return new Promise((success, failure) => {
        var json = {
            url: 'dummy',
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
                    if (line === ""){ // likely eof
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
                } else if (lineType == 2) {
                    // do nothing 
                }

                lineType += 1;
                if (lineType > 2) {
                    lineType = 0;
                }
            });
                
            // send to backend
            console.log("stringy", JSON.stringify(json));
            success(JSON.stringify(json));
            // return JSON.stringify(json);
        };

        reader.readAsText(new Blob([response.body], {
         type: 'text/plain'
        }));

    });
}


// Make sure the client is loaded and sign-in is complete before calling this method.
function execute() {
    gapi.client.youtube.captions.download({
      "id": "VTGY74ABZGNwXQyjX50KfOz5Fm9xrJkzPbwNVQBbKVI=",
      "tlang": "en",
      "tfmt": "sbv"
    }).then(function(response){
        parseCaptions(response).then(json => {
            console.log('recieved json', json);    
        });
    }, function(err) { 
        console.error("Execute error", err); 
    });

    // return gapi.client.youtube.captions.list({
    //   "videoId": "ovJcsL7vyrk",
    //   "part": [
    //     "id"
    //   ]
    // }).then(function(response) {
    //     // Handle the results here (response.result has the parsed body).
    //     // console.log("Response", response);
    //     if (response.result != null & response.result.items != null &
    //         response.result.items.length > 0) {
    //         console.log("id: " + response.result.items[0].id);
    //     }
    // }, function(err) {
    //     console.error("Execute error", err); 
    // });
}
  
gapi.load("client:auth2", function() {
    gapi.auth2.init({client_id: config.client_id});
});