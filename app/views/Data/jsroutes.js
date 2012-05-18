var route_get = {
	comic_by_label: #{jsAction @Comics.get(':label') /},
	comic: #{jsAction @Comics.get(':id') /},
	strip_by_sid: #{jsAction @Strips.getBySid(':label', ':sid') /},
	strip: #{jsAction @Strips.get(':id') /},
	strips_by_comic: #{jsAction @Strips.getByComic(':label') /},
	queue: #{jsAction @Users.getQueue(':id') /},
	noqueue: #{jsAction @Strips.getQueue(':id') /},
	subscription: #{jsAction @Subscriptions.get(':id') /},
	rss: #{jsAction @RssController.get(':id') /},
	archive: #{jsAction @ArchiveController.get(':id') /},
	commit_strips: #{jsAction @CommitController.commitStrips(':commitKey') /},
	subscriptions: #{jsAction @Subscriptions.getAll() /},
	comics: #{jsAction @Comics.getAll() /},
	visit: #{jsAction @Subscriptions.visit(':id') /},
	zoom: #{jsAction @Subscriptions.zoom(':cid', ':scale') /},
	startJob: #{jsAction @Jobs.startJob(':jobId') /},
	jobChartData: #{jsAction @Jobs.getJobResultsChart(':jobId', ':hours', ':params') /},
	lastResult: #{jsAction @Jobs.getLastResult(':jobId') /},
	jobStatus: #{jsAction @Jobs.checkJobStatus(':jobId') /}
}

var route_post = {
	comic: '@{Comics.create}',
	strip: '@{Strips.create}',
	subscription: '@{Subscriptions.create}',
	rss: '@{RssController.create}',
	archive: '@{ArchiveController.create}',
	rss_fetch: '@{RssController.fetch}',
	tagcategory: '@{TagCategories.create}'
}

var route_put = {
	comic: #{jsAction @Comics.update(':id') /},
	subscription: #{jsAction @Subscriptions.update(':id') /},
	rss: #{jsAction @RssController.update(':id') /},
	archive: #{jsAction @ArchiveController.update(':id') /},
	tagcategory: #{jsAction @TagCategories.update(':id') /}
}

var route_delete = {
	comic: #{jsAction @Comics.delete(':id') /},
	subscription: #{jsAction @Subscriptions.delete(':id') /},
	rss: #{jsAction @StripSourceController.delete(':id') /},
	archive: #{jsAction @StripSourceController.delete(':id') /},
	tagcategory: #{jsAction @TagCategories.delete(':id') /}
}