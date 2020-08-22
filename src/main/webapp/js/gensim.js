
/**
 * Send YT captions to the python gensim server
 * @param url - the url of the python server
 * @param data - the youtube captions JSON string
 * @returns - a 200 response when the python server
 *          completes its POST processes, and 505 
 *          if some error occurs 
 */
async function postGensim(url, data, query) {
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
        });

        return response;
    } catch(err) {
        console.log(err);
        return "{error: " + err + "}"
    }
}

/**
 * Callback for when gensim finishes POST operations
 * @param response - a 200 code if gensim operations were successful
 *                   500 otherwise
 */
function handleGensimResponse(response) {
    console.log("RESPONSE FROM GENSIM");
    console.log(response);
    // perhaps `successfulDisplay` should be set here
}