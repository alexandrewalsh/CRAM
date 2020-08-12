/**
 * Render an error message for a given error code (Doesn't work yet)
 * @param error - an HTTP error code
 */
function renderError(error) {
    var msg;

    if (error == 403) {
        msg = "Video has private captions!";
    } else if (error == 404) {
        msg = "Something went wrong, try again.";
    }
    
    // clear search var text
    $('.search-input').val("");
    // change placeholder text to red
    $('.search-input').addClass('error-placeholder');
    // display error message
    $('.search-input')[0].placeholder = msg || error;

    alert(msg || error);
}