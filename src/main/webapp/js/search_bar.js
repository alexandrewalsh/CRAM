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
/*
        for (var i = 0; i < entities.length; i++) {
            if (entities[i].textContent.toLowerCase().includes(query)) {
                $(entities[i]).parent().show();
            } else {
                $(entities[i]).parent().hide();
            }
        }*/
    });
});

