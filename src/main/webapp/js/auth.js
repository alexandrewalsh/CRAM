const signInOptions = {
    'client_id': config.client_id,
    'scope': 'https://www.googleapis.com/auth/youtube.force-ssl',
    'cookie_policy': 'single_host_origin'
}

/**
 * Initialize authentication and attempt to sign user in
 */
function authenticate() {
    return gapi.auth2.getAuthInstance()
        .signIn(signInOptions)
        .then(function() { 
            console.log("Sign-in successful");
            loadClient().then(function() {
                checkLogin();
            });
            },function(err) { 
                console.error("Error signing in", err); 
                renderError("Error signing in, please ty again.");
                // render error elements
            });
}

/**
 * Initialize the gApi client to make API requests
 */
function loadClient() {
    gapi.client.setApiKey(config.api_key);
    return gapi.client.load("https://www.googleapis.com/discovery/v1/apis/youtube/v3/rest")
        .then(function() {
            console.log("GAPI client loaded for API");
            // checkLogin(); 
            },
            function(err) { 
                renderError("Error loading GAPI client");
                console.error("Error loading GAPI client for API", err);
                // render error elements
            });
}

/**
 * Get the Google Auth client, initialize one if it doesn't exist.
 * @returns a promise handling the auth instance
 */
function getAuth(){
    var GoogleAuth = gapi.auth2.getAuthInstance();

    if (GoogleAuth == null) {
        GoogleAuth = gapi.auth2.init(signInOptions);
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
                if (window.location.href !== "https://step-intern-2020.appspot.com/player.html") {
                    window.location.replace("https://step-intern-2020.appspot.com/player.html");    // redirect to player
                }
            } else {
                if (!(window.location.href === "https://step-intern-2020.appspot.com/" || window.location.href === "https://step-intern-2020.appspot.com")) {
                    window.location.replace("https://step-intern-2020.appspot.com");    // redirect back to login page
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
        window.location.replace("https://step-intern-2020.appspot.com");    // redirect back to login page
    });
}

/**
 * Render a generic error with a `message`
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