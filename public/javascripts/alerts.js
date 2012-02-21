function myAlert(message) {
	var a = document.createElement("div");
	$(a).appendTo("body");
	$(a).addClass("my-alert").text(message).fadeIn(100).delay(5000).fadeOut('slow', function() { $(a).remove(); });
}