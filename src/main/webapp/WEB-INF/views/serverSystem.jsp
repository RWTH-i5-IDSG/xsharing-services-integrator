<%@ include file="00-header.jsp" %>

    <script type="text/javascript">
        $(document).ready(function () {
            $('[data-toggle="tooltip"]').tooltip()
        });
    </script>

    <div class="panel panel-primary margin-top-20">
        <div class="panel-heading"><h3 class="panel-title">Registered IXSI Systems</h3></div>
        <table class="table table-striped">
            <thead>
            <tr>
                <th>Partner ID</th>
                <th>Partner Name</th>
                <th>Base path</th>
                <th># of connections</th>
                <th>Enabled?</th>
                <th><a class="btn btn-success" role="button" href="${ctxPath}/systems/add" >Add new</a></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${serverSystemList}" var="system">
                <tr>
                    <td>${system.partnerId}</td>
                    <td><a href="${ctxPath}/systems/update/${system.partnerId}">${system.partnerName}</a></td>
                    <td>${system.basePath}</td>
                    <td>${system.numberOfConnections}</td>
                    <td>${system.enabled}</td>
                    <td>
                        <form method="POST" action="${ctxPath}/systems/delete">
                            <input type="hidden" name="partnerId" value="${system.partnerId}">
                            <button type="submit" class="btn btn-danger" data-toggle="tooltip" data-placement="right" title="Are you sure? You will lose IXSI data!">Delete</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

<%@ include file="00-footer.jsp" %>