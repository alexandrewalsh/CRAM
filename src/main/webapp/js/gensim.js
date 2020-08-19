
/**
 * Send YT captions to the python gensim server
 * @param url - the url of the python server
 * @param data - the youtube captions JSON string
 * @returns - a 200 response when the python server
 *          completes its POST processes, and 505 
 *          if some error occurs 
 */
async function postGensim(url = '', data={}) {

    const response = await fetch(url, {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });
    return response.json()
}