<%@ include file="00-header.jsp" %>

<div class="panel panel-default margin-top-20">
    <div class="panel-heading">
        <h3 class="panel-title">Add/Update IXSI server system</h3>
    </div>
    <div class="panel-body">
        <form class="form-horizontal" method="POST" action="${ctxPath}/systems/${operationType}">

            <div class="col-sm-offset-2 col-sm-10 alert alert-warning" role="alert">
                <strong>Caution!</strong>
                Updating the fields has no immediate effect, when we are already connected to the partner.
                Changes made here are written to DB, and are only read/used when initiating new connections with the partner!
            </div>

            <input type="hidden" name="partnerId" value="${system.partnerId}">

            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <div class="checkbox">
                        <label>
                            <c:choose>
                                <c:when test="${system.enabled}">
                                    <input type="checkbox" name="enable" id="enable" checked> Enable?
                                </c:when>
                                <c:otherwise>
                                    <input type="checkbox" name="enable" id="enable"> Enable?
                                </c:otherwise>
                            </c:choose>
                        </label>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label for="partnerName" class="col-sm-2 control-label">Partner name</label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" name="partnerName" id="partnerName" value="${system.partnerName}">
                </div>
            </div>
            <div class="form-group">
                <label for="basePath" class="col-sm-2 control-label">Base path</label>
                <div class="col-sm-10">
                    <input type="text" class="form-control" name="basePath" id="basePath" value="${system.basePath}">
                </div>
            </div>
            <div class="form-group">
                <label for="numOfConnections" class="col-sm-2 control-label"># of connections</label>
                <div class="col-sm-10">
                    <input type="number" class="form-control" name="numOfConnections" id="numOfConnections" value="${system.numberOfConnections}">
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-2 control-label">IXSI Features</label>

                <div class="row">
                    <div class="col-sm-2">
                        <label for="queryStatic" class="control-label">QueryStatic</label>
                        <c:forEach items="${queryStatic}" var="item">
                            <div class="checkbox">
                                <label>
                                    <c:choose>
                                        <c:when test="${item.value}">
                                            <input name="queryStatic" id="queryStatic" type="checkbox" checked="checked" value="${item.key.name}">${item.key.name}
                                        </c:when>
                                        <c:otherwise>
                                            <input name="queryStatic" id="queryStatic" type="checkbox" value="${item.key.name}">${item.key.name}
                                        </c:otherwise>
                                    </c:choose>
                                </label>
                            </div>
                        </c:forEach>
                    </div>

                    <div class="col-sm-2">
                        <label for="queryUser" class="control-label">QueryUser</label>
                        <c:forEach items="${queryUser}" var="item">
                            <div class="checkbox">
                                <label>
                                    <c:choose>
                                        <c:when test="${item.value}">
                                            <input name="queryUser" id="queryUser" type="checkbox" checked="checked" value="${item.key.name}">${item.key.name}
                                        </c:when>
                                        <c:otherwise>
                                            <input name="queryUser" id="queryUser" type="checkbox" value="${item.key.name}">${item.key.name}
                                        </c:otherwise>
                                    </c:choose>
                                </label>
                            </div>
                        </c:forEach>
                    </div>

                    <div class="col-sm-2">
                        <label for="subscriptionAdmin" class="control-label">SubscriptionAdmin</label>
                        <c:forEach items="${subscriptionAdmin}" var="item">
                            <div class="checkbox">
                                <label>
                                    <c:choose>
                                        <c:when test="${item.value}">
                                            <input name="subscriptionAdmin" id="subscriptionAdmin" type="checkbox" checked="checked" value="${item.key.name}">${item.key.name}
                                        </c:when>
                                        <c:otherwise>
                                            <input name="subscriptionAdmin" id="subscriptionAdmin" type="checkbox" value="${item.key.name}">${item.key.name}
                                        </c:otherwise>
                                    </c:choose>
                                </label>
                            </div>
                        </c:forEach>
                    </div>

                    <div class="col-sm-2">
                        <label for="subscription" class="control-label">Subscription</label>
                        <c:forEach items="${subscription}" var="item">
                            <div class="checkbox">
                                <label>
                                    <c:choose>
                                        <c:when test="${item.value}">
                                            <input name="subscription" id="subscription" type="checkbox" checked="checked" value="${item.key.name}">${item.key.name}
                                        </c:when>
                                        <c:otherwise>
                                            <input name="subscription" id="subscription" type="checkbox" value="${item.key.name}">${item.key.name}
                                        </c:otherwise>
                                    </c:choose>
                                </label>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <button type="submit" class="btn btn-success">Save</button>
                    <a class="btn btn-default" role="button" href="${ctxPath}/systems/" >Go Back</a>
                </div>
            </div>
        </form>
    </div>
</div>

<%@ include file="00-footer.jsp" %>