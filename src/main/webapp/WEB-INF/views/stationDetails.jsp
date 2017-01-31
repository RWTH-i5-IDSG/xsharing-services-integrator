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
        <form id="params" action="${ctxPath}/stations/details" method="get" class="form-horizontal">
            <div class="form-group">
                <label for="providerName" class="col-sm-2 control-label">Provider Name</label>
                <div class="col-sm-10">
                    <input id="providerName" name="providerName" class="form-control" value="${params.providerName}" type="text">
                </div>
            </div>
            <div class="form-group">
                <label for="stopId" class="col-sm-2 control-label">Stop Point ID</label>
                <div class="col-sm-10">
                    <input id="stopId" name="stopId" class="form-control" value="${params.stopId}" type="text">
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
                    <a class="btn btn-default" role="button" href="${ctxPath}/stations/" >Go Back</a>
                </div>
            </div>
        </form>
    </div>
</div>

<div class="panel panel-primary margin-top-20">
    <div class="panel-heading"><h3 class="panel-title">Available Vehicles</h3></div>
    <table class="table table-striped">
        <thead>
            <tr class="panel-heading">
                <th>Vehicle ID</th>
                <th>Vehicle Name</th>
                <th>Modal Type</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${vehicles}" var="vehicle">
                <tr>
                    <td>${vehicle.vehicleId}</td>
                    <td>${vehicle.name}</td>
                    <td>${vehicle.modalType}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="00-footer.jsp" %>