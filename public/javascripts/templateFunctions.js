function extraBindings() {
	$("div.comic").unbind("mouseenter").unbind("mouseleave");
	$("div.comic").hover(function() { if (!$(this).hasClass("subscribing")) slideIn($(this)); }, function() { if (!$(this).hasClass("subscribing")) slideOut($(this)); });
}
function slideIn(elm) {
	$(".title", elm).stop(true,true).show('slide', {direction: 'up'}, 160);
	$(".author", elm).stop(true,true).show('slide', {direction: 'down'}, 160);
}
function slideOut(elm) {
	$(".title", elm).stop(true,true).hide('slide', {direction: 'up'}, 160);
	$(".author", elm).stop(true,true).hide('slide', {direction: 'down'}, 160);
}
function subscribePane(data, event) {
	var comic = $(event.target).parent();
	$(comic).addClass("subscribing");
	$(".subscribe-at", comic).fadeIn(250);
	var shadow = document.createElement("div");
	$(shadow).attr("id", "shadow");
	$("body").append(shadow);
	$(shadow).fadeIn().bind("click", hideSubscribePane);
}
function hideSubscribePane() {
	$(".subscribing .subscribe-at").fadeOut(250);
	slideOut($(".subscribing").removeClass("subscribing"));
	$("#shadow").fadeOut(250, function() { $(this).remove(); });
}