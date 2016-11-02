<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    request.setAttribute("otaPath", "/ota");
%>

<div class="header">
    <div class="pure-menu pure-menu-open pure-menu-horizontal">
        <ul>
            <li><a href="/ota/index">Home</a></li>
            <li><a href="/ota/list?type=ios">ios</a></li>
            <li><a href="/ota/list?type=android">android</a></li>
            <li><a href="/ota/upload_manual_show">upload</a></li>
            <li><a href="/ota/cert/ota.crt">证书</a></li>
        </ul>
    </div>
</div>