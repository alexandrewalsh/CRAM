/**
 * Render an error message for a given error code (Doesn't work yet)
 * @param error - an HTTP error code
 */
function renderError(error) {
    var msg;
    var longMsg;

    if (error == 403) {
        msg = "Video has private captions!";
        longMsg = "Ask the uploader to enable public captions or try another video";
    } else if (error == 404) {
        msg = "Something went wrong, try again.";
    }
    
    // clear search var text
    $('.search-input').val("");
    // change placeholder text to red
    $('.search-input').addClass('error-placeholder');
    // display error message
    $('.search-input')[0].placeholder = msg || error;

    // empty output if any exists
    $("#keywords-output").empty();
    $("#query-output").empty();
    $("#bookmarks-output").empty();
    $('#entity-search-form').hide();

    // display error message
    $("#resultsHeader").show();
    $("#resultsHeader").text((msg || error) + (longMsg ? "\n"+longMsg:""));
    alert(msg || error);
}