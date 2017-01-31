<%@ page contentType="text/html" pageEncoding="utf-8" language="java" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%-- This is the global variable to be used in every jsp file as the prefix of the links --%>
<c:set var="ctxPath" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="en">
<head>
    <title>MobilityBroker Adapter</title>

    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link href="${ctxPath}/resources/css/bootstrap.min.css" rel="stylesheet" type="text/css">
    <link href="${ctxPath}/resources/css/bootstrap-select.min.css" rel="stylesheet" type="text/css">
    <link href="${ctxPath}/resources/css/jquery-ui.min.css" rel="stylesheet" type="text/css" >
    <link href="${ctxPath}/resources/css/jquery-ui-timepicker-addon.min.css" rel="stylesheet" type="text/css" >
    <link href="${ctxPath}/resources/css/vis.min.css" rel="stylesheet" type="text/css">
    <link href="${ctxPath}/resources/css/style.css" rel="stylesheet" type="text/css">

    <script src="${ctxPath}/resources/js/jquery-2.1.4.min.js" type="text/javascript" ></script>
    <script src="${ctxPath}/resources/js/bootstrap.min.js" type="text/javascript" ></script>
    <script src="${ctxPath}/resources/js/bootstrap-select.min.js" type="text/javascript" ></script>
    <script src="${ctxPath}/resources/js/jquery-ui.min.js" type="text/javascript" ></script>
    <script src="${ctxPath}/resources/js/jquery-ui-timepicker-addon.min.js" type="text/javascript" ></script>
    <script src="${ctxPath}/resources/js/vis.min.js" type="text/javascript" ></script>
</head>
<body>
<div class="container">

    <a class="btn btn-info margin-top-20" role="button" href="${ctxPath}/monitor/" >Go Home</a>
