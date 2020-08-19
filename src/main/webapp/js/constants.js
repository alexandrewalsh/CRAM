/**
 * Constants file for front-end JS scripts
 */

const MOCK_JSON_CAPTIONS = {
    url: 'mock',
    captions: [
        {'startTime': 0, 'endTime': 20, 'text': 'Mitochondria are membrane-bound cell organelles (mitochondrion, singular) that generate most of the chemical energy needed to power the cells biochemical reactions. Chemical energy produced by the mitochondria is stored in a small molecule called adenosine triphosphate (ATP).'},
        {'startTime': 0, 'endTime': 20, 'text': 'Ionic bonding is a type of chemical bonding that involves the electrostatic attraction between oppositely charged ions, and is the primary interaction occurring in ionic compounds. It is one of the main types of bonding along with covalent bonding and metallic bonding'},
        {'startTime': 0, 'endTime': 20, 'text': 'Being a liquid, water is not itself wet, but can make other solid materials wet. Wetness is the ability of a liquid to adhere to the surface of a solid, so when we say that something is wet, we mean that the liquid is sticking to the surface of a material.'},
        {'startTime': 0, 'endTime': 20, 'text': '51 Pegasi b (abbreviated 51 Peg b), unofficially dubbed Bellerophon, later formally named Dimidium, is an extrasolar planet approximately 50 light-years away in the constellation of Pegasus. It was the first exoplanet to be discovered orbiting a main-sequence star, the Sun-like 51 Pegasi, and marked a breakthrough in astronomical research.'}
    ]
}

const MOCK_NLP_OUTPUT = {
    mock: [0,1,2],
    mocker: [0,2],
    mockest: [1],
    apple: [30],
    banana: [0]
}

const GAPI_CLIENT = 'https://www.googleapis.com/discovery/v1/apis/youtube/v3/rest';

const PLAYER_URL = 'https://' + window.location.hostname + '/player.html';
const PLAYER_MOCK_URL = 'https://' + window.location.hostname + '/player.html?mock';
const PLAYER_MOCKALL_URL = 'https://' + window.location.hostname + '/player.html?mockall';
const LOGIN_URL_WITH_SLASH = 'https://' + window.location.hostname + '/';
const LOGIN_URL_WITHOUT_SLASH = 'https://' + window.location.hostname;

const IFRAME_API_URL = 'https://www.youtube.com/iframe_api';

const ESCAPE_HTML = (text) => {
    var map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, function(m) { return map[m]; });
}