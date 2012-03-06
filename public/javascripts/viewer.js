ko.bindingHandlers.cframeSrc = {
    update: function(element, valueAccessor, allBindingsAccessor) {
        // First get the latest data that we're bound to
        var value = valueAccessor(), allBindings = allBindingsAccessor();
         
        // Next, whether or not the supplied model property is observable, get its current value
        var valueUnwrapped = ko.utils.unwrapObservable(value); 
        
        // Now manipulate the DOM element
        if (allowChange) { // hack to stop from updating incorrectly
        	element.contentWindow.location.replace(valueUnwrapped)
        }
    }
};

ko.bindingHandlers.fadeVisible = {
	    update: function(element, valueAccessor, allBindingsAccessor) {
	        // First get the latest data that we're bound to
	        var value = valueAccessor(), allBindings = allBindingsAccessor();
	         
	        // Next, whether or not the supplied model property is observable, get its current value
	        var valueUnwrapped = ko.utils.unwrapObservable(value); 
	        
	        // Now manipulate the DOM element
	        if (valueUnwrapped)
	        	$(element).fadeIn(500);
	        else
	        	$(element).fadeOut(500);
	    }
};

var queue = Array();
var comics = Array();
var qdir=1;
var allowChange = true;

function Frame() {
	var self = this;
	self.data = new Strip();
	self.visible = ko.observable(true);
}

function AppViewModel() {
	var self = this;
	self.q = ko.observable(0);
	self.qlen = ko.observable(0);
	self.f1 = new Frame();
	self.f2 = new Frame();
	self.cur = self.f1;
	self.off = self.f2;
	self.off.visible(false);
	self.preloadStrip = function(qpos) {
		if (qpos >= 0 && qpos < self.qlen()) {
			var data = queue[qpos];
			ko.mapping.fromJS(data, self.off.data.strip);
			ko.mapping.fromJS(comics[data.cid], self.off.data.comic);
		} else {
			qdir = -qdir;
		}
	};
	self.changeStrip = function() {
		if (queue[self.q()].id != self.off.data.strip.id())
			self.preloadStrip(self.q());
		
		self.switchFrames();
		if (loggedIn) server_get("visit", {id: self.cur.data.strip.id()});
		var title = self.cur.data.comic.title()+' - '+self.cur.data.strip.title();
		window.history.replaceState({strip: ko.mapping.toJSON(self.cur.data.strip), q: self.q()}, title, '/'+(useQueue? "queue" : "comics")+'/'+self.cur.data.comic.label()+'/'+self.cur.data.strip.sid());
		document.title = title;
	};
	self.switchFrames = function() {
		allowChange = false;
		self.cur.visible(false);
		self.off.visible(true);
		allowChange = true;
		var tmp = self.cur;
		self.cur = self.off;
		self.off = tmp;
		window.setTimeout("viewModel.preloadStrip(viewModel.q()+qdir);", 500);
	}
	self.init = function(stripData, comicData) {
		ko.mapping.fromJS(stripData, self.cur.data.strip);
		ko.mapping.fromJS(comicData, self.cur.data.comic);
		
		// Apply HTML5 history API JS bindings
		if (window.history.pushState) {
			var title = self.cur.data.comic.label()+' - '+self.cur.data.strip.sid();
			window.history.replaceState({strip: ko.mapping.toJSON(self.cur.data.strip)}, title);
			server_get((useQueue? "queue" : "noqueue"), {'id': self.cur.data.strip.id()},
					function(data) {
						queue = data[0];
						comics = data[1];
						viewModel.q(data[2]);
						viewModel.qlen(data[3]);
						if (self.q()+qdir > 0 && self.q()+qdir < self.qlen()) self.preloadStrip(self.q()+qdir);
						// Apply HTML5 history API JS bindings
						$('a#nav-next').bind('click', function(e) { qdir=1; viewModel.q(viewModel.q()+1); viewModel.changeStrip(); e.preventDefault(); });
						$('a#nav-prev').bind('click', function(e) { qdir=-1; viewModel.q(viewModel.q()-1); viewModel.changeStrip(); e.preventDefault(); });
						$('a#nav-next').attr('data-bind', "visible: q() < qlen()-1");
						$('a#nav-prev').attr('data-bind', "visible: q() > 0");
						$('.nav-disabled').removeClass('nav-disabled');
						ko.applyBindings(self);
					});
		}
	};
}
var viewModel = new AppViewModel();
ko.applyBindings(viewModel);

function showSeek() {
	var seekwin = document.createElement('div');
	var shadow = document.createElement('div');
	$(seekwin).attr('id', 'seekWindow');
	$(shadow).attr('id', 'shadow');
	$(shadow).bind('click', hideSeek);
	
	var seeklist = document.createElement('ul');
	$(seeklist).attr('id', 'seekList');
	
	for (var idx in queue) {
		var strip = queue[idx];
		var item = document.createElement('li');
		$(item).attr('q', idx);
		if (idx == viewModel.q()) $(item).attr('id', 'strip-current');
		$(item).html(comics[strip.cid].label + ' - ' + strip.sid + "&nbsp;&nbsp;-&nbsp;&nbsp;" + strip.title);
		$(item).bind('click', function() { seekTo(Number($(this).attr('q'))); });
		$(seeklist).append(item);
	}
	
	$(seekwin).append(seeklist);
	$('body').append(shadow);
	$('#shadow').fadeIn(250);
	$('body').append(seekwin);
}

function seekTo(id) {
	viewModel.q(id);
	viewModel.changeStrip();
	hideSeek();
}

function hideSeek() {
	$('#shadow').fadeOut(250, function () { $('#shadow').remove(); });
	$('#seekWindow').remove();
}
