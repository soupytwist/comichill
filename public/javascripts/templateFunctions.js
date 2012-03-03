function extraBindings() {
	$("div.comic").unbind("mouseenter").unbind("mouseleave");
	$("div.comic").hover(function() { slideIn($(this)); }, function() { slideOut($(this)); });
}
function slideIn(elm) {
	$(".title", elm).stop(true,true).show('slide', {direction: 'up'}, 160);
	$(".author", elm).stop(true,true).show('slide', {direction: 'down'}, 160);
}
function slideOut(elm) {
	$(".title", elm).stop(true,true).hide('slide', {direction: 'up'}, 160);
	$(".author", elm).stop(true,true).hide('slide', {direction: 'down'}, 160);
}