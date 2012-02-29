function extraBindings() {
	$("div.comic").unbind("mouseenter");
	$("div.comic").unbind("mouseleave");
	$("div.comic").bind("mouseenter", function() { slideIn($(this)); });
	$("div.comic").bind("mouseleave", function() { slideOut($(this)); });
}
function slideIn(elm) {
	console.log("in");
	$(".title", elm).show('slide', {direction: 'up'}, 160);
	$(".author", elm).show('slide', {direction: 'down'}, 160);
}
function slideOut(elm) {
	console.log("slide out");
	$(".title", elm).hide('slide', {direction: 'up'}, 160);
	$(".author", elm).hide('slide', {direction: 'down'}, 160);
}