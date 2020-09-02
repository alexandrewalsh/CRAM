
/**
 * Send YT captions to the python gensim server
 * @param url - the url of the python server
 * @param v_id - the video id of the YouTube video
 * @param data - the youtube captions JSON string
 * @returns - a 200 response when the python server
 *          completes its POST processes, and 500 
 *          if some error occurs 
 */
async function postGensim(url, v_id, data, callback=handleGensimResponse) {
    console.log("POST gesim request to " + url + "...")
    try {
        const response = await fetch(url, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({'data': data,
                                'v_id': v_id})
        }).catch(error => { throw error });

        callback(response);

    } catch(err) {
        console.log(err);
        $('#query-output table').empty();
        $('#query-output table').append("<tr><td>Server error encountered</tr></td>");
        $('#query-loading-text').hide();
        return new Response("Error: " + err.toString(), {'status': 500}); 
    }
}

/**
 * Get the closest matches in the transcript to the query
 * @param url - the url of the python server
 * @param query - the search query
 * @param v_id - the youtube video ID
 * @returns - JSON encoding the lines of trascript which 
 *              match closest to the input query
 */
async function getGensim(url, query, v_id, callback=handleGensimResponse) {
    console.log("GET gesim request to " + url + "...")
    try {
        const response = await fetch(url, {
            method: 'GET',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'query': query,
                'vid': v_id
            }
        }).catch(error => { throw error });

        callback(response);

    } catch(err) {
        console.log(err);
        $('#query-output table').empty();
        $('#query-output table').append("<tr><td>Server error encountered</tr></td>");
        $('#query-loading-text').hide();
        return new Response("Error: " + err.toString(), {'status': 500}); 
    }
}

/**
 * default callback for when gensim finishes POST operations
 * @param response - json in the form '{"indices": [2, 1, ..]}'
 */
function handleGensimResponse(response) {
    console.log("RESPONSE FROM GENSIM");
    console.log(response);
    response.json().then(data => {
        console.log(data);
    });
    // perhaps `successfulDisplay` should be set here
}