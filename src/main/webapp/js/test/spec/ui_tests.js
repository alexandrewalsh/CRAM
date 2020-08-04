/**
 * Tests related to the YouTube player
 * 
 * Testing strategy: create a YouTube player
 */

function initPlayer(url) {
    try {
        videoId = getIdFromUrl(url);
    } catch {
        renderYtError("Invalid youtube url!");
        return;
    }

    // build the youtube src url
    var youtubeSourceBuilder = "https://www.youtube.com/embed/"
    youtubeSourceBuilder += videoId
    youtubeSourceBuilder += "?enablejsapi=1"
    youtubeSourceBuilder += "&origin=" + location.origin;
    console.log(youtubeSourceBuilder);

    // set player source
    $('#player').attr('src', youtubeSourceBuilder);    
    player = new YT.Player('player', {
        events: {'onReady': onPlayerReady, 'onStateChange': onPlayerStateChange}
    });
};

describe("youtube player test", ()=>{
    beforeEach(()=>{
        // set up player
        initPlayer("https://www.youtube.com/watch?v=ncbb5B85sd0");
        // set youtube video in URL
        $('#searchbar-div').find("input")[0].value = "https://www.youtube.com/watch?v=ncbb5B85sd0";
        // click the mock button
        $('#captionMockButton').click();
        // simulate form submission
        submitFn($('#searchbar-div').find('form')[0], new Event("none"));
    });

    var elements = document.getElementsByClassName("timestamps");

    it("simulate link clicks, verify video changes places", ()=> {
        elements[0].click();    // simulate a click 
        const time = timestampToEpoch(elements[0].textContent);
        const playerTime = player.getCurrentTime();

        expect(time).toEqual(playerTime);
     });
 });

/**
 * Simulate an event firing 
 * @param el: element to register an event
 */
function eventFire(el, etype){
  if (el.fireEvent) {
    el.fireEvent('on' + etype);
  } else {
    var evObj = document.createEvent('Events');
    evObj.initEvent(etype, true, false);
    el.dispatchEvent(evObj);
  }
}

function addScript(src) {
    const el = document.createElement('script');
    el.src= src;
    document.body.appendChild(el);
}

 describe("make sure API calls to changing the Youtube time works", function() {
     var player; 

     beforeEach(() => {
         $(document).ready(()=> {
            $.get("../../player.html", function (data) {
                const domparser = new DOMParser();
                const doc = domparser.parseFromString(data, "text/html");
                // console.log(doc);
                console.log(doc.body);

                // insert player page
                document.head.innerHtml = doc.head;
                document.body.innerHtml = doc.body;

                // add in Jasmine scripts and links
                const avi = document.createElement('link');
                avi.rel = "shortcut icon";
                avi.type = "image/png";
                avi.href = "https://avatars0.githubusercontent.com/u/4624349?s=400&v=4";
                document.body.appendChild(avi);

                const jasStyle = document.createElement('link');
                jasStyle.rel = "stylesheet";
                jasStyle.href = "https://cdnjs.cloudflare.com/ajax/libs/jasmine/3.6.0/jasmine.min.css";
                document.body.appendChild(jasStyle);

                addScript("https://cdnjs.cloudflare.com/ajax/libs/jasmine/3.6.0/jasmine.min.js");
                addScript("https://cdnjs.cloudflare.com/ajax/libs/jasmine/3.6.0/jasmine-html.min.js");
                addScript("https://cdnjs.cloudflare.com/ajax/libs/jasmine/3.6.0/boot.min.js");
                    // document.write(data);
            });
         });
     });

    it('first test', ()=> {
         expect(true).toBe(true);
    });
 });

            // var tag = document.createElement('script');
            // tag.src = 'https://www.youtube.com/iframe_api';
            // tag.id = "playerSrc";

            // if ($("#playerSrc").length === 0){  // element doesn't exists
            //     var firstScriptTag = document.getElementsByTagName('script')[0];
            //     firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
            // }

            // if ($("#player").length === 0){  // element doesn't exists
            //     var iFrame = document.createElement('iframe');
            //     iFrame.id = "player";
            //     iFrame.width = "640px";
            //     iFrame.height = "390px";
            //     document.body.appendChild(iFrame);
            // }


            // fetch("../../../header.html")
            //     .then(response => {
            // return response.text()
            // })
            // .then(data => {
            //     document.querySelector("body").innerHTML = data;
            // });

// fetch("./footer.html")
//   .then(response => {
//     return response.text()
//   })
//   .then(data => {
//     document.querySelector("footer").innerHTML = data;
//   });

        //     // build the youtube src url
        //     var videoId = "ncbb5B85sd0";
        //     var youtubeSourceBuilder = "https://www.youtube.com/embed/"
        //     youtubeSourceBuilder += videoId
        //     youtubeSourceBuilder += "?enablejsapi=1"
        //     youtubeSourceBuilder += "&origin=" + location.origin;
        //     console.log(youtubeSourceBuilder);

        //     // 3. This function creates an <iframe> (and YouTube player)
        //     //    after the API code downloads.
        //     var player;
        //     $('#player').attr('src', youtubeSourceBuilder);    
        //     // set player source
        //     player = new YT.Player('player', {
        //         events: {'onReady': onPlayerReady, 'onStateChange': onPlayerStateChange}
        //     });
        // });

    //  });

    //  it('first test', ()=> {
    //      expect(true).toBe(true);
//     //  });
//  });