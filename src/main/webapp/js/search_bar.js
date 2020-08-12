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

function entitySearchSubmit() {
    console.log("Submit Button pushed");
    return;
}

function compareEntities(a, b) {
    var aText = $(a).text();
    var bText = $(b).text();
    if (aText > bText) {
        return 1;
    } 
    if (aText < bText) {
        return -1;
    }
    return 0;
}

function compareTimestamps(a, b) {
    var aText = $(a).text();
    var bText = $(b).text();
    var aList = timestamps[aText];
    var bList = timestamps[bText];
    console.log("NEW");
    console.log(aText);
    console.log(bText);

    var limit = (aList.length > bList.length) ? bList.length : aList.length;
    console.log(limit);
    for (var i = 0; i < limit; i++) {
        if (aList[i] > bList[i]) {
            return 1;
        }
        if (aList[i] < bList[i]) {
            return -1;
        } 
    }
    if (aList.length > bList.length) {
        return 1;
    }
    if (aList.length < bList.length) {
        return -1;
    }
    return compareEntities(a, b);
}


$(document).ready(function() {

    $('#entity-seachbar').on('keyup', function() {
        var query = $(this).val().toLowerCase();
        var entities = $('#output table tr td:first-child');
        if (query == '') {
            entities.each((i, elem) => {
                $(elem).parent().show();
            });
            return;
        }

        entities.each((i, elem) => {
            if ($(elem).text().toLowerCase().includes(query)) {
                $(elem).parent().show();
            } else {
                $(elem).parent().hide();
            }
        });
    });

    $('#entity-sort').change(() => {
        var optionSelected = $("option:selected", this).val();
        console.log(optionSelected)
        if (optionSelected == 'alphabetical') {
            var list = $('#output table tr td:first-child').sort(compareEntities).map((i, elem) => elem.textContent);
            $('#output').html(styleEntitiesFromList(list));
        }
        if (optionSelected == 'chronological') {
            var list = $('#output table tr td:first-child').sort(compareTimestamps).map((i, elem) => elem.textContent);
            $('#output').html(styleEntitiesFromList(list));      
        }
        setClickableEntities();
        setClickableTimestamps();
    });

});

