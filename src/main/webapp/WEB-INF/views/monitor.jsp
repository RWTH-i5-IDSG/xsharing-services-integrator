<%@ include file="00-header.jsp" %>

    <div class="btn-group pull-right margin-top-20" role="group">
        <a class="btn btn-default" role="button" href="${ctxPath}/stations/" >
            Stations Overview
        </a>
        <a class="btn btn-default" role="button" href="${ctxPath}/bookings/" >
            Booking Overview
        </a>
        <a class="btn btn-default" role="button" href="${ctxPath}/systems/" >
            Manage Server Systems
        </a>
    </div>

    <nav class="navbar navbar-default margin-top-20">
        <div class="navbar-header col-md-3">
            <span class="navbar-brand" href="#">Ixsi Commands</span>
        </div>
        <div class="collapse navbar-collapse">
            <form class="navbar-form navbar-left form-inline" method="POST" action="${ctxPath}/monitor/ixsi/basic-commands">
                <div class="form-group">
                    <%@ include file="partnerSelectSnippet.jsp" %>
                </div>
                <div class="form-group">
                    <select name="ixsiBasicCommand" class="selectpicker" data-width="auto">
                        <%@ include file="selectSnippet.jsp" %>
                        <c:forEach items="${ixsiCommandList}" var="mapItem">
                            <optgroup label="${mapItem.key}">
                                <c:forEach items="${mapItem.value}" var="listItem">
                                    <option value="${listItem}">${listItem}</option>
                                </c:forEach>
                            </optgroup>
                        </c:forEach>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary">Go!</button>
            </form>
        </div>
    </nav>

    <nav class="navbar navbar-default">
        <div class="navbar-header col-md-3">
            <span class="navbar-brand" href="#">Other</span>
        </div>
        <div class="collapse navbar-collapse">
            <form class="navbar-form navbar-left form-inline" method="POST" action="${ctxPath}/monitor/other-commands">
                <div class="form-group">
                    <select name="otherCommand" class="selectpicker" data-width="auto">
                        <%@ include file="selectSnippet.jsp" %>
                        <c:forEach items="${otherCommandList}" var="item">
                            <option value="${item}">${item}</option>
                        </c:forEach>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary">Go!</button>
            </form>
        </div>
    </nav>

    <div class="panel panel-primary">
        <div class="panel-heading"><h3 class="panel-title">Connections</h3></div>
        <table class="table table-striped">
            <thead>
            <tr>
                <th>Partner Name</th>
                <th>Session ID</th>
                <th>Open since</th>
                <th>Open for</th>
                <th>State</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${ixsiConnectionStatus}" var="endpoint">
                <tr>
                    <td><a href="${ctxPath}/systems/update/${endpoint.partnerId}">${endpoint.partnerName}</a></td>
                    <td>${endpoint.sessionId}</td>
                    <td>${endpoint.openSince}</td>
                    <td>${endpoint.openFor}</td>
                    <td>${endpoint.state}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <div class="row">
        <div class="col-md-7">
            <div class="panel panel-primary">
                <div class="panel-heading"><h3 class="panel-title">Subscription Stores</h3></div>
                <table class="table table-striped">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th># of items</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${ixsiStoreList}" var="item" >
                        <tr><td><a href="${ctxPath}/monitor${item.link}">${item.name}</a></td><td>${item.size}</td></tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="col-md-5">
            <div class="panel panel-success">
                <div class="panel-heading"><h3 class="panel-title">Misc. Stuff</h3></div>
                <table class="table table-striped">
                    <thead>
                    <tr>
                        <th>Key</th>
                        <th>Value</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr><td># of transactions so far (req/res)</td><td>${numberOfRequests} / ${numberOfResponses}</td></tr>
                    <tr><td><a href="${ctxPath}/monitor/ixsi/store/context/in-out">InOut context store size</a></td><td>${inOutContextStoreSize}</td></tr>
                    <tr><td><a href="${ctxPath}/monitor/ixsi/store/context/user-in-out">UserInOut context store size</a></td><td>${userInOutContextStoreSize}</td></tr>
                    <tr><td>RegioITPushService disabled?</td><td>${isPushServiceDisabled}</td></tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <footer>
        <p class="text-right">
            <a href="${ctxPath}/monitor/log">Log</a> |
            <a href="${ctxPath}/monitor/ixsi/xsd">IXSI Schema</a> |
            <a href="${ctxPath}/monitor/commit-info">Commit Info</a>
        </p>
        <p class="text-right">
            Date/time when page is loaded: ${currentTime}
        </p>
    </footer>

<%@ include file="00-footer.jsp" %>
