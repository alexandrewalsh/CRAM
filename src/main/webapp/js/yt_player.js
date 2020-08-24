/**
 * Holds all functions and variables required to set up & operate the embedded youtube player
**/

// global variable holding the Youtube Video
var player;


// This code loads the IFrame Player API code asynchronously.
var tag = document.createElement('script');

tag.src = IFRAME_API_URL;
var firstScriptTag = document.getElementsByTagName('script')[0];
firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

var done = false;

// The API will call this function when the video player is ready.
function onPlayerReady(event) {
  event.target.playVideo();
  document.getElementById("theater-button").style.display = "inline-block";
}

// The API calls this function when the player's state changes.
function onPlayerStateChange(event) {
  if (event.data == YT.PlayerState.PLAYING && !done) {
    done = true;
  }
}


/**
 * Resizes the embedded video based on window size, using either the 4:3 width to height ratio or the remaining screen
 */
function resizeIFrame() {
    // Saves necessary parameters as variables
    var width = $('#player').width();
    var windowHeight = $(window).height();
    var reservedHeight = $('#heading-div').outerHeight(true) + $('#searchbar-div').outerHeight(true) + windowHeight * 0.05;
    
    // The remaining avalable height for the video that avoids overflow
    var totalAvailableHeight = windowHeight - reservedHeight;

    // The height of the video to keep the 4:3 aspect ratio
    var videoHeightFromRatio = width / 1.33;

    // Defines and sets the best video height to ensure that overflow does not occur
    var playerHeight = (videoHeightFromRatio > totalAvailableHeight) ? totalAvailableHeight : videoHeightFromRatio;
    $('#player').height(playerHeight);
    $('#output').height(playerHeight - $('#resultsHeader').height());

    // change output size to match the player
    if ($('#flex-item-video').hasClass('theater')) {
        $("#flex-item-output").css("width", $("#player").width());
    } else {
        $("#flex-item-output").css("height", $("#player").height());
    }
    
}


$(document).ready(function() {

    // Toggles theater mode with button click
    $('.theater-toggle').click(function() {
        var video = $('#flex-item-video');
        var results = $('#flex-item-output');

        if (video.hasClass('theater')) {

            // remove table wrapper 
            video.removeClass('theater');
            results.append($("#resultsHeader"));
            results.append($("#entity-search-form"));
            results.append($("#output"));
            $("#table-wrapper").remove();

            // put timestamp timeline under player
            $("#player-pane").append($("#timestamp-timeline"));

            results.removeClass('container-row');
            results.addClass('container-column');
            $('#output-container').removeClass('container-column');
            $('#output-container').addClass('container-row');
        } else {
            video.addClass('theater');

            // package results header and table in a column
            results.prepend($("<div id='table-wrapper' class='container-column'></div>"));
            $("#table-wrapper").append($("#resultsHeader"));
            $("#table-wrapper").append($("#entity-search-form"));
            $("#table-wrapper").append($("#output"));

            // put timestamps into flex-output
            results.append($("#timestamp-timeline"));
            results.removeClass('container-column');
            results.addClass('container-row');
            $('#output-container').removeClass('container-row');
            $('#output-container').addClass('container-column');

            // change output container size to video size
            $("#flex-item-output").css("width", $("#player").width());
        }

        resizeIFrame();
    });

});