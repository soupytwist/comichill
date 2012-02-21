var defaultRss = {
    enabled: false,
    id: -1,
    linkTag: "link",
    src: "http://www.",
    titlePattern: ".+",
    titleTag: "title",
    urlPattern: ".+",
    cid: -1
};

var defaultArchive = {
    id: -1,
    src: "http://www.",
    titlePattern: ".+",
    urlPattern: ".+",
    cid: -1,
    newestFirst: false
};

var defaultComic = {
	id: -1,
	label: "",
	title: "",
	author: "",
	homepage: "http://www.",
	numStrips: 0,
	tags: ""
};

var defaultStrip = {
	id: -1,
	cid: -1,
	sid: -1,
	title: "Untitled",
	url: "about:blank"
};

var defaultSubscription = {
	id: -1,
	cid: -1,
	bookmark: 0,
	latest: 0
}

function Rss() {
	var self = this;
	self.data = ko.mapping.fromJS(defaultRss);
	self.results = ko.observableArray();
	self.persisted = ko.computed(function() { return self.data.id() != -1; });
	self.fetch = function(success, error) {
		self.results.removeAll();
		if (self.data.src() != "http://www.") {
			server_post('rss_fetch', ko.mapping.toJS(self.data),
				function(jsonObj) {
					for (key in jsonObj) {
						self.results.push(jsonObj[key]);
					}
					if (success)
						success();
				}, function() { if (error) error() }
			);
		}
	};
}

function Archive() {
	var self = this;
	self.data = ko.mapping.fromJS(defaultArchive);
	self.results = ko.observableArray();
	self.persisted = ko.computed(function() { return self.data.id() != -1; });
	self.persistKey = null;
	self.fetch = function(success, error) {
		if (self.data.src() != "http://www.") {
			server_post('archive_fetch', ko.mapping.toJS(self.data),
				function(jsonObj) {
					self.results(jsonObj);
				}, function() { if (error) error() }
			);
		}
	};
}

function Comic() {
	var self = this;
	self.data = ko.mapping.fromJS(defaultComic);
	self.sub = ko.mapping.fromJS(defaultSubscription);
	self.persisted = ko.computed(function() { return self.data.id() != -1; });
	self.comicUrl = ko.computed(function() { return '/comics/'+self.data.label(); });
	self.bookmarkUrl = ko.computed(function() { return (self.sub.bookmark()==0)? '/comics/'+self.data.label()+'/1' : '/comics/'+self.data.label()+'/'+self.sub.bookmark(); });
	self.latestUrl = ko.computed(function() { return '/comics/'+self.data.label()+'/'+(self.sub.latest()+1); });
	self.unreadCount = ko.computed(function() {return (self.sub.latest()==0)? self.data.numStrips()-1 : self.data.numStrips() - self.sub.latest(); });
	self.details = ko.observable(false);
	self.showDetails = function() { self.details(true); };
	self.hideDetails = function() { self.details(false); };
	self.subscribe = function() {
		self.sub.cid(self.data.id());
		server_post('subscription', self.sub, function(data) { ko.mapping.fromJS(data, self.sub); });
		if (localStorage)
			delete localStorage.home_subscriptions;
	};
	self.unsubscribe = function() {
		server_delete('subscription', self.sub.id());
		ko.mapping.fromJS(defaultSubscription, self.sub);
		if (localStorage)
			delete localStorage.home_subscriptions;
	};
}

function Strip() {
	var self = this;
	self.strip = ko.mapping.fromJS(defaultStrip);
	self.comic = ko.mapping.fromJS(defaultComic);
	self.link = ko.computed(function() { return '/comics/'+self.comic.label()+'/'+self.strip.sid(); });
}

function server_get(route, params, successFct, errorFct) {
	$.ajax({
		url: route_get[route](params),
		type: "GET",
		success: successFct,
		error: errorFct,
	});
}

function server_post(route, obj, successFct, errorFct) {
	var model = ko.mapping.toJSON(obj);
	var routeSelect = (obj.id() == -1)? route_post[route] : route_put[route]({'id':obj.id()});
	var methodSelect = (obj.id() == -1)? "POST" : "PUT";
	$.ajax({
		url: routeSelect,
		type: methodSelect,
		success: successFct,
		error: errorFct,
		data: model,
		dataType: "json"
	});
}

function server_put(route, obj, successFct, errorFct) {
	var model = ko.mapping.toJSON(obj);
	$.ajax({
		url: route_put[route]({'id':obj.id()}),
		type: "PUT",
		success: successFct,
		error: errorFct,
		data: model,
		dataType: "json"
	});
}

function server_delete(route, id, successFct, errorFct) {
	$.ajax({
		url: route_delete[route]({'id':id}),
		type: "DELETE",
		success: successFct,
		error: errorFct
	});
}