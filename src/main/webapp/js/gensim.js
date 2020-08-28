
/**
 * Send YT captions to the python gensim server
 * @param url - the url of the python server
 * @param data - the youtube captions JSON string
 * @returns - a 200 response when the python server
 *          completes its POST processes, and 505 
 *          if some error occurs 
 */
async function postGensim(url, data, query, callback=handleGensimResponse) {
    console.log("POST gesim request to " + url + "...")
    try {
        const response = await fetch(url, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({'query': query,
                                'ytCaptions': data})
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