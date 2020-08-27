/**
 * Toggle the search bar appearance based on event (To be moved to other file)
 * @param obj - the button invoking the click
 * @param evt - the click event
 */
function searchToggle(obj, evt){
    var container = $(obj).closest('.search-wrapper');

    if(!container.hasClass('active')){
        container.addClass('active');
        evt.preventDefault();
    }
    else if (container.hasClass('active') && $(obj).closest('.input-holder').length == 0){
        container.removeClass('active');
        // clear input
        container.find('.search-input').val('');
        // clear and hide result container when we press close
        container.find('.result-container').fadeOut(100, function(){$(this).empty();});
    }
}


/**
 * Comparator function that compares two entities alphabetically
 * @param a - a <td> tag object to compare the text of
 * @param b - a <td> tag object to compare the text of
 * @return  - positive if 'a' is larger than 'b'
 *          - negative if 'a' is smaller than 'b'
 *          - zero if 'a' equals 'b'
 */
function compareEntities(a, b) {
    // Gets the text from the tag
    var aText = $(a).text();
    var bText = $(b).text();

    // Returns numerical values based on the comparison of the texts
    if (aText > bText) {
        return 1;
    } 
    if (aText < bText) {
        return -1;
    }
    return 0;
}


/**
 * Comparator function that compares two entities chronologically by timestamps
 * @param a - a <td> tag object to compare the text timestamps of
 * @param b - a <td> tag object to compare the text timestamps of
 * @return  - positive if 'a' is larger than 'b'
 *          - negative if 'a' is smaller than 'b'
 *          - zero if 'a' equals 'b'
 */
function compareTimestamps(a, b) {
    // Gets the text and timestamp length
    var aText = $(a).text();
    var bText = $(b).text();
    var aList = timestamps[aText];
    var bList = timestamps[bText];

    // Iterates through the lists up until the shorter length, checking the values in order
    var limit = (aList.length > bList.length) ? bList.length : aList.length;
    for (var i = 0; i < limit; i++) {
        if (aList[i] > bList[i]) {
            return 1;
        }
        if (aList[i] < bList[i]) {
            return -1;
        } 
    }

    // If all contents are equal up until the limit, the shorter list will preceed the longer list
    if (aList.length > bList.length) {
        return 1;
    }
    if (aList.length < bList.length) {
        return -1;
    }

    // If the lists are equal, we sort alphabetically 
    return compareEntities(a, b);
}


/**
 * Comparator function that compares two bookmarks alphabetically
 * @param a - a <li> tag to compare the text of
 * @param b - a <li> tag to compare the text of
 * @return  - positive if 'a' is larger than 'b'
 *          - negative if 'a' is smaller than 'b'
 *          - zero if 'a' equals 'b'
 */
function compareBookmarkNames(a, b) {
    // Gets the text of the HTML tags
    var aText = $(a).children('.bookmark')[0].innerText;
    var bText = $(b).children('.bookmark')[0].innerText;

    // Returns the numerical value of the parameters' comparison
    if (aText > bText) {
        return 1;
    } 
    if (aText < bText) {
        return -1;
    }
    return 0;
}


/**
 * Comparator function that compares two bookmarks alphabetically
 * @param a - a <li> tag to compare the text of
 * @param b - a <li> tag to compare the text of
 * @return  - positive if 'a' is larger than 'b'
 *          - negative if 'a' is smaller than 'b'
 *          - zero if 'a' equals 'b'
 */
function compareBookmarkTimestamps(a, b) {
    // Gets the id and timestamps of the bookmark referenced by HTML tags
    var aId = $(a).children('.remove-bookmark')[0].value;
    var bId = $(b).children('.remove-bookmark')[0].value;
    var aTime = bookmarks[aId].timestamp;
    var bTime = bookmarks[bId].timestamp;

    // Returns a numerical value of the parameters' comparison
    if (aTime > bTime) {
        return 1;
    } 
    if (aTime < bTime) {
        return -1;
    }
    return compareBookmarkNames(a, b);
}


/**
 * Filters the entities based on the current text of the searchbar
 */
function filterEntities() {
    var query = $('#entity-searchbar').val().toLowerCase().trim();
    var entities = $('#keywords-output table tr td:first-child');

    // Case 1: Searchbar is empty, so all entities are displayed
    if (query == '') {
        entities.each((i, elem) => {
            $(elem).parent().show();
        });
        return;
    }

    // Case 2: Searchbar has text, so only entities the include the query are displayed
    entities.each((i, elem) => {
        if ($(elem).text().toLowerCase().includes(query)) {
            $(elem).parent().show();
        } else {
            $(elem).parent().hide();
        }
    });
}

/**
 * Filters the bookmarks based on the current text of the searchbar
 */
function filterBookmarks() {
    var query = $('#entity-searchbar').val().toLowerCase().trim();
    var entities = $('#bookmarks-output ul li');
    $('.content').css('maxHeight', '0px');

    // Case 1: Searchbar is empty, so all entities are displayed
    if (query == '') {
        entities.each((i, elem) => {
            $(elem).show();
        });
        return;
    }

    // Case 2: Searchbar has text, so only entities the include the query are displayed
    entities.each((i, elem) => {
        if ($(elem).children('.bookmark')[0].innerText.toLowerCase().includes(query)) {
            $(elem).show();
        } else {
            $(elem).hide();
        }
    });
}

/**
 * Filters based on the current state of the output
 */
function filterSearch() {
    if ($('#keywords-output').css('display') != 'none') {
        filterEntities();
        return;
    }
    if ($('#bookmarks-output').css('display') != 'none') {
        filterBookmarks();
    }
}


/**
 * Sorts the entities based on the current selector 
 */
function sortEntities() {
    var optionSelected = $("option:selected", '#entity-sort').val();

    // Case 1: Sorts the table alphabetically
    if (optionSelected == 'alphabetical') {
        var list = $('#keywords-output table tr td:first-child').sort(compareEntities).map((i, elem) => elem.textContent);
        $('#keywords-output').html(styleEntitiesFromList(list));

    // Case 2: Sorts the table chronologically
    } else if (optionSelected == 'chronological') {
        var list = $('#keywords-output table tr td:first-child').sort(compareTimestamps).map((i, elem) => elem.textContent);
        $('#keywords-output').html(styleEntitiesFromList(list));   

    // Case 3: Invalid selected option, so nothing happens   
    } else {
        return;
    }

    // Must refilter entities and remap clickable entities to their timestamps
    filterEntities();
    setClickableEntities();
    setClickableTimestamps();
}

/**
 * Sorts the entities based on the current selector
 */
function sortBookmarks() {
    var optionSelected = $("option:selected", '#entity-sort').val();

    // Case 1: Sorts the table alphabetically
    if (optionSelected == 'alphabetical') {
        var list = $.map($('#bookmarks-output ul li').sort(compareBookmarkNames), function(elem) {
            return $(elem).children('.remove-bookmark')[0].value
        });
        $('#bookmarks-output').html(styleBookmarksFromList(list));

    // Case 2: Sorts the table chronologically
    } else if (optionSelected == 'chronological') {
        var list = $.map($('#bookmarks-output ul li').sort(compareBookmarkTimestamps), function(elem) {
            return $(elem).children('.remove-bookmark')[0].value
        });        
        $('#bookmarks-output').html(styleBookmarksFromList(list)); 

    // Case 3: Invalid selected option, so nothing happens       
    } else {
        return;
    }

    filterBookmarks();
    addContentBookmarkListeners();
    addRemoveBookmarkListeners();
}


/**
 * Sorts based on the current state of the output
 */
function sortList() {
    if ($('#keywords-output').css('display') != 'none') {
        sortEntities();
        return;
    }
    if ($('#bookmarks-output').css('display') != 'none') {
        sortBookmarks();
    }
}


/**
 * Changes the display of the searchbar based on the current output state
 * @param selected The string of the current output state
 */
function editTabSearchbar(selected) {
    $('#entity-sort').css('margin-left', '5%');

    // Changes the searchbar display to include button when in the 'query' state
    if (selected == 'query') {
        $('#entity-searchbar').css('width', '80%');
        $('#entity-searchbar').css('border-radius', '50px 0px 0px 50px');
        $('#entity-searchbar-button').css('width', '20%');
        $('#entity-searchbar-button').css('border-radius', '0px 50px 50px 0px');
        $('#entity-searchbar-button').show();
        $('#entity-sort').hide();

    // Changes the searchbar display to include filtering and sorting otherwise
    } else {
        $('#entity-searchbar').css('width', '100%');
        $('#entity-searchbar').css('border-radius', '50px 50px 50px 50px');
        $('#entity-searchbar-button').hide(); 
        $('#entity-sort').show(); 
    }
}


/**
 * Changes the display of the output based on the current output state
 * @param selected The string of the current output state
 */
function showSelectedSection(selected) {
    // Resets the searchbar on state change
    $('#entity-searchbar').val('');

    // Displays the output based on the selected state
    switch(selected) {
        case 'keywords':
            $('#keywords-output').show();
            $('#keywords-toggle-button').addClass('active-tab');
            $('#query-output').hide();
            $('#query-toggle-button').removeClass('active-tab');
            $('#bookmarks-output').hide();
            $('#bookmarks-toggle-button').removeClass('active-tab');
            $('#keywords-output table tr').show();
            $('.content').css('maxHeight', '0px');
            break;
        case 'query':
            $('#keywords-output').hide();
            $('#keywords-toggle-button').removeClass('active-tab');
            $('#query-output').show();
            $('#query-toggle-button').addClass('active-tab');
            $('#bookmarks-output').hide();
            $('#bookmarks-toggle-button').removeClass('active-tab');
            $('.content').css('maxHeight', '0px');
            break;
        case 'bookmarks':
            $('#keywords-output').hide();
            $('#keywords-toggle-button').removeClass('active-tab');
            $('#query-output').hide();
            $('#query-toggle-button').removeClass('active-tab');
            $('#bookmarks-output').show();
            $('#bookmarks-toggle-button').addClass('active-tab');
            $('#bookmarks-output ul li').show();
            break;
    }

    // Edits the searchbar display based on the selected state
    editTabSearchbar(selected);
}


$(document).ready(function() {

    // Searchbar to filter entities 
    $('#entity-searchbar').on('keyup', filterSearch);

    // Select to sort entities
    $('#entity-sort').change(sortList);

    $('#keywords-toggle-button').click(() => {
        showSelectedSection('keywords');
    });
    $('#query-toggle-button').click(() => {
        showSelectedSection('query');
    });
    $('#bookmarks-toggle-button').click(() => {
        showSelectedSection('bookmarks');
    });


});

