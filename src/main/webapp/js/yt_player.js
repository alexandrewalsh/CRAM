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
    });

});
