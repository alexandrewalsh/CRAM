/**
 * Render an error message for a given error code (Doesn't work yet)
 * @param error - an HTTP error code
 */
function renderError(error) {
    var errorMsg = document.getElementById('errorMsg'); // could make these consts
    if (!errorMsg) {
        errorMsg = document.createElement('p');
        errorMsg.setAttribute('id', 'errorMsg');
        errorMsg.classList += 'error';
        document.body.appendChild(errorMsg);
    }

    if (error == 403) {
        errMsg.innerText = "Video has private captions!";
    } else if (error == 404) {
        errMsg.innerText = "404 don't know how to deal with this";
    }
}

/**
 * Render a generic error with a `message`. (This also doesn't work yet)
 * @param message - the message to render
 */
function renderYtError(message) {
    var errorMsg = document.getElementById('ytError');
    if (!errorMsg) {
        errorMsg.setAttribute('id', 'ytError');
        errorMsg = document.createElement('p');
        errorMsg.classList += 'error';
        document.body.appendChild(errorMsg);
    }

    errorMsg.innerText = message;
}