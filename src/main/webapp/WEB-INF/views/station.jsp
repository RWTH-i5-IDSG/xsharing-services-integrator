<%@ include file="00-header.jsp" %>
<script>
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
<div class="panel panel-default margin-top-20">
    <div class="panel-body">
        <form id="params" action="${ctxPath}/stations/" method="get" class="form-horizontal">
            <div class="form-group">
                <label for="providerName" class="col-sm-2 control-label">Provider Name</label>
                <div class="col-sm-10">
                    <input id="providerName" name="providerName" class="form-control" value="${params.providerName}" type="text">
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

<c:forEach var="stopPoints" items="${stopPointsMap}">
    <div class="panel panel-primary margin-top-20">
        <div class="panel-heading"><h3 class="panel-title">${stopPoints.key}</h3></div>
        <table class="table table-striped resultTable">
            <thead>
                <tr>
                    <th>Stop Point Name</th>
                    <th>Stop Point ID</th>
                    <th>Avail Spaces</th>
                    <th>Avail Vehicles</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="stopPoint" items="${stopPoints.value}">
                    <tr>
                        <td>${stopPoint.stopPointName}</td>
                        <td><a href="${ctxPath}/stations/details?providerName=${stopPoint.provider}&amp;stopId=${stopPoint.stopPointId}&amp;timePeriodBegin=${params.timePeriodBegin}&amp;timePeriodEnd=${params.timePeriodEnd}">${stopPoint.stopPointId}</a></td>
                        <td>${stopPoint.availSpaces}</td>
                        <td>${stopPoint.availVehicles}</td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</c:forEach>
<%@ include file="00-footer.jsp" %>