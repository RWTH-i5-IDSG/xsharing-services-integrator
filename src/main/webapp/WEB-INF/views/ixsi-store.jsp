<%@ include file="00-header.jsp" %>

<div class="page-header">
    <h1>${store.name}</h1>
</div>

<div class="row">
    <div class="col-md-12">
        <table class="table table-striped">
            <thead>
            <tr>
                <th>${store.itemKey}</th>
                <th>${store.itemDescription}</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${store.itemList}" var="item">
                <tr>
                    <td>${item.key}</td>
                    <td>${item.value}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
<%@ include file="00-footer.jsp" %>
