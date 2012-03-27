google.setOnLoadCallback(drawAll);
google.load("visualization", "1", {packages:["corechart"]});

var timeSpan = 36; // hours

function drawAll() {
	$(".job-results").each(function(i1, job) {
		var jobId = $(job).attr("jobId");
		$(".job-results-chart", job).each(function(i2, chart) { drawChart(jobId, chart); });
		$(".message-wrapper", job).each(function(index, message) {
			server_get("lastResult", {jobId: jobId}, function(data) {
				$(message).html(data['message']);
				var timestamp = new Date(data['startTime']).toString();
				$(message).append("<div class='message-timestamp'>"+timestamp.substr(0, timestamp.indexOf("GMT")-1)+"</div>");
			});
		});
	});
	$(".start-job-button").bind("click", startJobAction);
}

function drawChart(jobId, chartElm) {
	var params = JSON.parse($(chartElm).attr("params"));
	server_get("jobChartData", {jobId: jobId, hours: timeSpan, params: params},
		function(data) {
			for (r in data)
				data[r][0] = new Date(data[r][0]);
			var dt = new google.visualization.DataTable();
			dt.addColumn('datetime', 'Time');
			for (var param in params)
				dt.addColumn('number', params[param]);
			dt.addRows(data);
			var chart = new google.visualization.AreaChart(chartElm);
			chart.draw(dt, {pointSize: 3, legend: {position: "bottom"}, chartArea: {width: "80%"}, hAxis: { viewWindowMode: 'explicit', viewWindow: {min: new Date( new Date().getTime() - timeSpan*3600000), max: new Date()}}});
		},
		function(jqXHR, textStatus, errorThrown) { console.log(textStatus + " : " + errorThrown); }
	);
}

var jobStatusTimer;

function startJobAction() {
	var elm = $(this);
	var jobId = $(elm).parents(".job-results").attr("jobId");
	server_get("startJob", {jobId: jobId});
	$(elm).text("Started...");
	$(elm).attr("disabled", "disabled");
	jobStatusTimer = window.setInterval(function() { checkJobStatus(elm); }, 500);
}

function checkJobStatus(elm) {
	var jobId = $(elm).parents(".job-results").attr("jobId");
	
	server_get("jobStatus", {jobId: jobId},
		function(data) {
			if (data && data != "") { 
				$(elm).text("Done");
				$(".job-results-chart", $(elm).parent().parent()).each(function (index, chart) { drawChart(jobId, chart); });
				window.clearInterval(jobStatusTimer);
		}},
		function() { window.clearInterval(jobStatusTimer); $(elm).text("Failed"); }
	);
}