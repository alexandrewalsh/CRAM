/**
 * All authentication related scripts
 */

const SIGN_IN_OPTIONS = {
    'client_id': config.client_id,
    'scope': 'https://www.googleapis.com/auth/youtube.force-ssl',
    'cookie_policy': 'single_host_origin'
}

$(document).ready(function() {
    // Performs login box dropdown animation if on the login screen
    if (window.location.pathname == '/') {
        loginBoxAnimation();
    }
    
    // event handler for clicking login link
    $("#loginBtn").click(function(e) {
        e.preventDefault();
        authenticate();
    });

    // event handler for logging out
    $("#signoutButton").click(function(e) {
        signOut();
    });
});


/**
 * Initialize authentication and attempt to sign user in
 */
function authenticate() {
    return gapi.auth2.getAuthInstance()
        .signIn(SIGN_IN_OPTIONS)
        .then(function() { 
            console.log("Sign-in successful");
            loadClient().then(function() {
                checkLogin();
            });
            },function(err) { 
                alert("Error signing in, please try again");
            });
}

/**
 * Initialize the gApi client to make API requests
 */
function loadClient() {
    gapi.client.setApiKey(config.api_key);
    return gapi.client.load(GAPI_CLIENT)
        .then(function() {
            console.log("GAPI client loaded for API");
            },
            function(err) {
                alert("Error loading Google APIs"); 
            });
}

/**
 * Get the Google Auth client, initialize one if it doesn't exist.
 * @returns a promise handling the auth instance
 */
function getAuth(){
    var GoogleAuth = gapi.auth2.getAuthInstance();

    if (GoogleAuth == null) {
        GoogleAuth = gapi.auth2.init(SIGN_IN_OPTIONS);
        loadClient();
    }
    
    return GoogleAuth;
}

/**
 * Redirect users back to the login page if they are
 * not authenticated.
 */
function checkLogin() {
    gapi.load('auth2', function() {
        var GoogleAuth = getAuth();

        GoogleAuth.then(function() {
            if (GoogleAuth.isSignedIn.get()) {
                if (window.location.href !== PLAYER_URL && window.location.href !== PLAYER_MOCK_URL && window.location.href !== PLAYER_MOCKALL_URL) {
                    window.location.replace(PLAYER_URL);    // redirect to player
                }
            } else {
                if (window.location.href !== LOGIN_URL_WITH_SLASH && window.location.href !== LOGIN_URL_WITHOUT_SLASH) {
                    window.location.replace(LOGIN_URL_WITH_SLASH);    // redirect back to login page
                }  
            }
        });
    });
}

/**
 * Sign out of current account and redirect back to home page
 */
function signOut() {
    const GoogleAuth = getAuth();

    GoogleAuth.then(function() {
        GoogleAuth.signOut();
        window.location.replace(LOGIN_URL_WITHOUT_SLASH);    // redirect back to login page
    });
}

/**
 * Render a generic error with a `message`. Currently does not work
 * @param message - the message to render
 */
function renderError(message) {
    var errorMsg = document.getElementById('error');
    if (!errorMsg) {
        errorMsg.setAttribute('id', 'error');
        errorMsg = document.createElement('p');
        errorMsg.classList += 'error';
        document.body.appendChild(errorMsg);
    }

    errorMsg.innerText = message;
}

/**
 * Animates the login box with a dropdown and opacity changes
 */
function loginBoxAnimation() {
    // Sets necessary objects and initial parameters
    var elem = document.getElementsByClassName("wrapper")[0];   
    var pos = 0; // Initial position of 0vh
    var targetPos = 30; // Final position is 30vh
    var opacity = 0; // Initial opacity of 0
    var opacityStep = 1 / targetPos; // The increase of opacity per frame to reach final opacity of 1

    // Changes frame animation every 25 milliseconds
    var id = setInterval(frame, 25);

    /**
     * Performs a single frame of the login box animation
     */
    function frame() {
        // Changes position and opacity until target position is reached
        if (pos >= targetPos) {
            clearInterval(id);
            elem.style.opacity = 1;
        } else {
            pos++; 
            elem.style.top = pos + 'vh'; 
            opacity += opacityStep;
            elem.style.opacity = opacity;
        }
    }
}
