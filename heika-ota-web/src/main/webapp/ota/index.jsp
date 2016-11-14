<%--
  Created by IntelliJ IDEA.
  User: yangjian
  Date: 2016/8/22
  Time: 11:11
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("otaPath", "/ota");
%>

<html>
<head>
    <title>HeiKa OTA</title>
</head>
<body class="index">
<script>
    $(document).ready(function(){
        $('div.header  div  ul  li:nth-child(1) a').addClass(" pure-menu-selected");
    });

</script>

<div id="wrap">

    <jsp:include page="${otaPath}/include/headerinclude.jsp"></jsp:include>
    <div id="contain" class="main container">
        <h2>
            HOME
        </h2>
        <div class="welcome">
            <p>
                Ota for app download TEST
            </p>
            <p>
                <a href="/ota/list?type=ios" class="button-welcome pure-button">ios</a>
                <a href="/ota/list?type=android" class="button-secondary pure-button">android</a>
            </p>
        </div>
    </div>


    <script type="text/javascript" src="static/js/app.js?v=471a4a60173357278fb10d12adcbdbdc"></script>
    <jsp:include page="${otaPath}/include/footer.jsp"></jsp:include>
</div>

</body>
</html>
