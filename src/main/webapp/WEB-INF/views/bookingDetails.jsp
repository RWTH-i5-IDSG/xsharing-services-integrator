<%@ include file="00-header.jsp" %>
<div class="panel panel-default margin-top-20">
    <div class="panel-body">
        <form id="params" action="${ctxPath}/bookings/details/" method="get" class="form-horizontal">
            <div class="form-group">
                <label for="bookingId" class="col-sm-2 control-label">Booking ID</label>
                <div class="col-sm-10">
                    <input id="bookingId" name="bookingId" class="form-control" value="${bookingId}" type="text">
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <input type="submit" value="Get" class="btn btn-success">
                    <a class="btn btn-default" role="button" href="${ctxPath}/bookings/" >Go Back</a>
                </div>
            </div>
        </form>
    </div>
</div>

<div class="panel panel-primary margin-top-20">
    <div class="panel-heading"><h3 class="panel-title">Booking Create</h3></div>
    <table class="table table-striped resultTable">
        <tbody>
            <tr><td>Booking ID</td><td>${bookingCreate.bookingId}</td></tr>
            <tr><td>Booking Target ID</td><td>${bookingCreate.bookingTargetId}</td></tr>
            <tr><td>User ID</td><td>${bookingCreate.userId}</td></tr>
            <tr><td>Provider ID</td><td>${bookingCreate.providerId}</td></tr>
            <tr><td>Provider Name</td><td>${providerName}</td></tr>
            <tr><td>Booking Begin</td><td>${bookingCreate.timePeriod.begin}</td></tr>
            <tr><td>Booking End</td><td>${bookingCreate.timePeriod.end}</td></tr>
            <tr><td>From Place ID</td><td>${bookingCreate.fromPlaceId}</td></tr>
            <tr><td>To Place ID</td><td>${bookingCreate.toPlaceId}</td></tr>
            <tr><td>Event Timestamp</td><td>${bookingCreate.eventTimestamp}</td></tr>
            <tr><td>Event Origin</td><td>${bookingCreate.eventOrigin}</td></tr>
        </tbody>
    </table>
</div>

<div class="panel panel-primary">
    <div class="panel-heading"><h3 class="panel-title">Booking Changes</h3></div>
    <table class="table table-striped">
        <thead>
        <tr>
            <th>Booking New Begin</th>
            <th>Booking New End</th>
            <th>Change Type</th>
            <th>Event Timestamp</th>
            <th>Event Origin</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${bookingChanges}" var="bookingChange">
            <tr>
                <td>${bookingChange.newTimePeriod.begin}</td>
                <td>${bookingChange.newTimePeriod.end}</td>
                <td>${bookingChange.changeType}</td>
                <td>${bookingChange.eventTimestamp}</td>
                <td>${bookingChange.eventOrigin}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<div class="panel panel-primary">
    <div class="panel-heading"><h3 class="panel-title">Consumptions</h3></div>
    <table class="table table-striped">
        <thead>
        <tr>
            <th>Consumption Begin</th>
            <th>Consumption End</th>
            <th>Unit</th>
            <th>Value</th>
            <th>Finalized?</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${bookingConsumptions}" var="bookingConsumption">
            <tr>
                <td>${bookingConsumption.timePeriod.begin}</td>
                <td>${bookingConsumption.timePeriod.end}</td>
                <td>${bookingConsumption.unit}</td>
                <td>${bookingConsumption.value}</td>
                <td>${bookingConsumption.finalized}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<%@ include file="00-footer.jsp" %>