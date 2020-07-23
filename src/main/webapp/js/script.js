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

// Make sure the client is loaded and sign-in is complete before calling this method.
function execute() {
    gapi.client.youtube.captions.download({
      "id": "VTGY74ABZGNwXQyjX50KfOz5Fm9xrJkzPbwNVQBbKVI=",
      "tlang": "en",
      "tfmt": "sbv"
    }).then(function(response) {
        // Handle the results here (response.result has the parsed body).
        console.log("Response", response);

        var json = {
            url: 'dummy',
            captions: []
        };

        let reader = new FileReader();

        reader.onloadend = function(evt){
            lines = evt.target.result.split(/\r\n|\n|\r/);  

            var lineType = 0;
            var caption = {};

            lines.forEach(function(line) {
                console.log('line: ' + line);

                if (lineType == 0) {    // timestamp
                    if (line === ""){ // likely eof
                        return;
                    }
                    const timestamps = line.split(',');
                    if (timestamps.length != 2) {
                        console.error('timestamp line malformatted: ' + line);
                        return;
                    }
                    
                    caption["startTime"] = timestamps[0];
                    caption["endTime"] = timestamps[1];
                } else if (lineType == 1) {   // text
                    caption["text"] = line;

                    json.captions.push(caption);
                    caption = {};
                    // json += "\t\t\t'text': " + line + "\n\t\t},\n";
                } else if (lineType == 2) {
                    // do nothing 
                }

                lineType += 1;
                if (lineType > 2) {
                    lineType = 0;
                }
            });
            

            console.log("stringy", JSON.stringify(json));
        };

        reader.readAsText(new Blob([response.body], {
            type: 'text/plain'
        }));
    },
    function(err) { console.error("Execute error", err); });

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