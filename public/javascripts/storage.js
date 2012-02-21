function loadSubscriptions(dest_arr) {
	if (localStorage && localStorage.home_comics) {
		addComics(JSON.parse(localStorage.home_comics), dest_arr);
		if (localStorage && localStorage.home_subscriptions)
			addSubscriptions(JSON.parse(localStorage.home_subscriptions), dest_arr);
		
		server_get('subscriptions', {}, function(subData) {
			addSubscriptions(subData, dest_arr);
			if (localStorage)
				localStorage.home_subscriptions = ko.mapping.toJSON(subData);
		});
	} else {
		loadComics(dest_arr);
	}
}

if (localStorage && localStorage.home_comics) {
	addComics(JSON.parse(localStorage.home_comics));
	if (localStorage.home_subscriptions) {
		addSubscriptions(JSON.parse(localStorage.home_subscriptions));
	}
	if (viewModel.loggedIn()){
		server_get('subscriptions', {}, function(subData) {
			addSubscriptions(subData);
			if (localStorage)
				localStorage.home_subscriptions = ko.mapping.toJSON(subData);
		});
	}
} else {
	server_get('comics', {}, function(comicData) {
		addComics(comicData);
		server_get('subscriptions', {}, function(subData) {
			addSubscriptions(subData);
			if (localStorage)
				localStorage.home_subscriptions = ko.mapping.toJSON(subData);
		});
		if (localStorage)
			localStorage.home_comics = ko.mapping.toJSON(comicData);
	});
}

function addComics(comicData, dest_arr) {
	$.each(comicData,
		function(index, value) {
			var newComic = new Comic();
			ko.mapping.fromJS(value, newComic.data);
			dest_arr.push(newComic);
		});
}

function addSubscriptions(subData, dest_arr) {
	$.each(subData,
		function(index, value) {
			var comic = null;
			for (var tmp in dest_arr()) {
				if (dest_arr()[tmp].data.id() == index) {
					comic = dest_arr()[tmp];
					break;
				}
			}
			ko.mapping.fromJS(value, comic.sub);
		});
}