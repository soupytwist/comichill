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
    cid: -1
};

var defaultComic = {
	id: -1,
	label: "",
	title: "",
	author: "",
	homepage: "http://www.",
	numStrips: 0,
	tags: "",
	rankPop: 0,
	rankHits: 0,
	enabled: false
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
			var send = ko.mapping.fromJS(ko.mapping.toJS(self.data));
			send.id(-1);
			server_post('rss_fetch', send,
				function(jsonObj) {
					for (key in jsonObj) {
						self.results.push(jsonObj[key]);
					}
				}, function() { if (error) error() }
			);
		}
	};
}

function Archive() {
	var self = this;
	self.data = ko.mapping.fromJS(defaultArchive);
	self.persisted = ko.computed(function() { return self.data.id() != -1; });
	self.persistKey = null;
}

function Comic() {
	var self = this;
	self.data = ko.mapping.fromJS(defaultComic);
	self.sub = ko.mapping.fromJS(defaultSubscription);
	self.persisted = ko.computed(function() { return self.data.id() != -1; });
	self.comicUrl = ko.computed(function() { return '/comics/'+self.data.label(); });
	self.bookmarkUrl = ko.computed(function() { return (self.sub.bookmark()==0)? '/comics/'+self.data.label()+'/1' : '/comics/'+self.data.label()+'/'+self.sub.bookmark(); });
	self.latestUrl = ko.computed(function() { return '/comics/'+self.data.label()+'/'+(self.sub.latest()+1); });
	self.unreadCount = ko.computed(function() {if (self.sub.id() == -1) return 0; else return (self.sub.latest()==0)? self.data.numStrips()-1 : self.data.numStrips() - self.sub.latest(); });
	self.subscribe = function(data, event) {
		self.sub.cid(self.data.id());
		if ($(event.target).hasClass('at-end')) {
			self.sub.bookmark(self.data.numStrips());
			self.sub.latest(self.sub.bookmark());
		}
		server_post('subscription', self.sub, function(data) { ko.mapping.fromJS(data, self.sub); extraBindings(); myAlert("Subscription added!"); });
		if (hideSubscribePane) hideSubscribePane();
	};
	self.unsubscribe = function() {
		server_delete('subscription', self.sub.id());
		ko.mapping.fromJS(defaultSubscription, self.sub);
		extraBindings();
	};
}

function Strip() {
	var self = this;
	self.data = ko.mapping.fromJS(defaultStrip);
	self.comic = ko.mapping.fromJS(defaultComic);
	self.persisted = ko.computed(function() { return self.data.id() != -1; });
	self.link = ko.computed(function() { return '/comics/'+self.comic.label()+'/'+self.data.sid(); });
}

function loadSubscriptions(refresh, callback, errorfct) {
	if (!refresh && sessionStorage && sessionStorage.subs)
		callback(JSON.parse(sessionStorage.subs));
	else
		server_get('subscriptions', {}, function(data) { if (sessionStorage) sessionStorage.subs = JSON.stringify(data); callback(data); }, errorfct);
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
	var model = ko.mapping.toJSON(obj).replace("%", "%25");
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