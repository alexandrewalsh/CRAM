/**
 * Tests related to the YouTube player
 * 
 * Testing strategy: create a YouTube player, simulate site actions
 *                   and verify player behaves correctly.
 */

function initPlayer(url) {
    return new Promise((resolve, reject) => {
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
            events: {'onReady': ()=> {
                resolve();
            }, 'onStateChange': onPlayerStateChange}
        });
    });

};

describe("youtube player tests", () => {
    var elements;

    beforeAll(async () => {
         $('.jasmine_html-reporter').css({position: "absolute", bottom: "0", margin: "0"});
        // set Jasmine's default async timeout interval
        jasmine.DEFAULT_TIMEOUT_INTERVAL = 999999;
        // set up player
        await initPlayer("https://www.youtube.com/watch?v=ncbb5B85sd0");
        // set youtube video in URL
        $('.search-input')[0].value = "https://www.youtube.com/watch?v=ncbb5B85sd0";
        // wait until authentication client loaded
        await getAuth();
        await loadClient();
    });

    it("simulate link click, verify video changes places with mocked captions", async () => {
        // click the mock button
        if ($('#captionMockButton').text() != "Mocking") {
            $('#captionMockButton').click();
        }

        // simulate form submission and wait until elements are rendered
        await submitFn($('form')[0], new Event("none"));
        // sometimes player isn't ready here
        elements = document.getElementsByClassName("timestamps");
        elements[0].click();    // simulate a click 
        const time = timestampToEpoch(elements[0].textContent);
        const playerTime = player.getCurrentTime();

        expect(time).toEqual(playerTime);
     });

    xit("simulate repeated link clicks", async () => {
        // click the mock button
        if ($('#captionMockButton').text() != "Mocking") {
            $('#captionMockButton').click();
        }

        // simulate form submission and wait until elements are rendered
        await submitFn($('form')[0], new Event("none"));
        var time;
        var playerTime;
        elements = document.querySelectorAll(".timestamps").forEach(el => {
            el.click(); // simulate click
            time = timestampToEpoch(el.textContent);
            playerTime = player.getCurrentTime();
            expect(time).toBe(playerTime);
        });
     });

     it("simulate link clicks for real captions", async ()=> {
        // simulate form submission and wait until elements are rendered
        if ($('#captionMockButton').text() == "Mocking") {
            $('#captionMockButton').click();
        };

        await submitFn($('form')[0], new Event("none"));

        elements = document.getElementsByClassName("timestamps");
        elements[0].click();    // simulate a click 
        const time = timestampToEpoch(elements[0].textContent);
        const playerTime = player.getCurrentTime();

        expect(time).toEqual(playerTime);
     });
 });

