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
}

// The API calls this function when the player's state changes.
function onPlayerStateChange(event) {
  if (event.data == YT.PlayerState.PLAYING && !done) {
    done = true;
  }
}