<select name="partnerId" class="selectpicker">
  <%@ include file="selectSnippet.jsp" %>
  <c:forEach items="${partnerList}" var="item">
    <option value="${item.partnerId}">${item.partnerName}</option>
  </c:forEach>
</select>
