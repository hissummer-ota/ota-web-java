<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    request.setAttribute("otaPath", "/ota");
%>
<head>
    <meta charset="utf-8"/>
    <title>HeiKa OTA</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${otaPath}/static/lib/pure.css?v=201604251">
    <script type="text/javascript" src="${otaPath}/static/lib/jquery.js?v=5790ead7ad3ba27397aedfa3d263b867"></script>
    <!--[if lt IE 9]>
    <script src="${otaPath}/static/lib/html5.js?v=c9e1409caaa1980a03f6e438bf921061"></script>
    <![endif]-->
    <!--[if lte IE 8]>
    <link rel="stylesheet" href="${otaPath}/static/lib/grids-responsive-old-ie.css?v=d2d7f538a184697108d2198137731f19">
    <![endif]-->
    <!--[if gt IE 8]><!-->
    <link rel="stylesheet" href="${otaPath}/static/lib/grids-responsive.css?v=097136401fe7dc64557bc53d5045adfc">
    <!--<![endif]-->
    <link rel="stylesheet" href="${otaPath}/static/css/aio.css?v=6b28c65f9c90f5995ff860314e97cea9">
    <link rel="stylesheet" href="${otaPath}/static/css/ota.css?v=6b28c65f9c90f5995ff860314e97cea9">
</head>
