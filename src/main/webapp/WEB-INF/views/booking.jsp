<%@ include file="00-header.jsp" %>

<div class="panel panel-default margin-top-20">
    <div class="panel-body">
        <form id="params" action="${ctxPath}/bookings/" method="get" class="form-horizontal">
                <div class="form-group">
                    <label for="bookingId" class="col-sm-2 control-label">Booking ID:</label>
                    <div class="col-sm-10">
                        <input id="bookingId" name="bookingId" class="form-control" value="${params.bookingId}" type="text">
                    </div>
                </div>
                <div class="form-group">
                    <label for="userId" class="col-sm-2 control-label">User ID:</label>
                    <div class="col-sm-10">
                        <input id="userId" name="userId" class="form-control" value="${params.userId}" type="text">
                    </div>
                </div>
                <div class="form-group">
                    <label for="vehicleId" class="col-sm-2 control-label">Vehicle ID:</label>
                    <div class="col-sm-10">
                        <input id="vehicleId" name="vehicleId" class="form-control" value="${params.vehicleId}" type="text">
                    </div>
                </div>
                <div class="form-group">
                    <label for="timePeriodBegin" class="col-sm-2 control-label">From:</label>
                    <div class="col-sm-10">
                        <input id="timePeriodBegin" name="timePeriodBegin" value="${params.timePeriodBegin}" class="dateTimePicker form-control" type="text"/>
                    </div>
                </div>
                <div class="form-group">
                    <label for="timePeriodEnd" class="col-sm-2 control-label">To:</label>
                    <div class="col-sm-10">
                        <input id="timePeriodEnd" name="timePeriodEnd" value="${params.timePeriodEnd}" class="dateTimePicker form-control" type="text"/>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <input type="submit" value="Get" class="btn btn-success">
                    </div>
                </div>
        </form>
    </div>
</div>

<div id="chart_div" class="panel timelineFullscreen"></div>

<script type="text/javascript">

    var documentDiv = document.getElementById('chart_div');
    var groups = new vis.DataSet();
    var dataTable = new vis.DataSet();

    <c:forEach items="${results}" var="entry" varStatus="entryStatus">
        groups.add(
                {
                    id: "${entry.key}",
                    content: "${entry.key}",
                    order: "${entry.key}",
                    className: '${entryStatus.index % 2 == 0 ? "timelineEvenGroup" : "timelineOddGroup"}'
                }
        );
        <c:forEach items="${entry.value}" var="item">
            dataTable.add(
                    {
                        start: new Date(${item.timePeriod.begin.time}),
                        end: new Date(${item.timePeriod.end.time}),
                        group: "${item.vehicleId}",
                        title: "${item.bookingId}",
                        content: createCustomHTMLContent(
                                '${item.bookingId}',
                                '${item.userId}',
                                '${item.vehicleId}',
                                new Date(${item.timePeriod.begin.time}),
                                new Date(${item.timePeriod.end.time})
                        )
                    }
        );
        </c:forEach>
    </c:forEach>

    var options = {};

    var timeline = new vis.Timeline(documentDiv, dataTable, options);

    timeline.setGroups(groups);

    timeline.on('select', function(properties) {
        var selections = timeline.getSelection();
        if (selections.length) {
            var timelineEventDiv = dataTable.get(
                    selections[0],
                    {
                        fields: ['title']
                    });
            var redirect = '${ctxPath}/bookings/details?bookingId=' + encodeURIComponent(timelineEventDiv.title);
            $(location).attr('href', redirect);
        }
    });
    
    function createCustomHTMLContent(bookingId, userId, vehicleId, startDate, endDate) {
        return '<div>' +
                'BookingId: ' + bookingId + '<br>' +
                'UserId: ' + userId + '<br>' +
                'VehicleId: ' + vehicleId + '<br>' +
                'Start: ' + startDate + '<br>' +
                'End: ' + endDate + '<br>' +
                '</div>';
    }

    $(document).ready(function () {
        var timePeriodBegin = $('#timePeriodBegin');
        var timePeriodEnd = $('#timePeriodEnd');
        $.timepicker.datetimeRange(
                timePeriodBegin,
                timePeriodEnd,
                {
                    minInterval: (1000 * 60 * 60), // 1hr
                    dateFormat: 'yy-mm-dd',
                    timeFormat: 'HH:mm:ss'
                }
        );
    });
</script>

<%@ include file="00-footer.jsp" %>