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
 * Filters the entities based on the current text of the searchbar
 */
function filterEntities() {
    var query = $('#entity-seachbar').val().toLowerCase().trim();
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

function showSelectedSection(selected) {
    switch(selected) {
        case 'keywords':
            $('#keywords-output').show();
            $('#query-output').hide();
            $('#bookmarks-output').hide();
            break;
        case 'query':
            $('#keywords-output').hide();
            $('#query-output').show();
            $('#bookmarks-output').hide();
            break;
        case 'bookmarks':
            $('#keywords-output').hide();
            $('#query-output').hide();
            $('#bookmarks-output').show();
            break;
    }
}


$(document).ready(function() {

    // Searchbar to filter entities 
    $('#entity-seachbar').on('keyup', filterEntities);

    // Select to sort entities
    $('#entity-sort').change(sortEntities);

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

