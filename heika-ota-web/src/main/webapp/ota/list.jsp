<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="com.heika.qadev.ota.OTAUtility" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String otaPath = "/ota";
    request.setAttribute("otaPath", otaPath);
%>

<html>
<head>
    <title>HeiKa OTA</title>
    <jsp:include page="${otaPath}/include/headerinclude.jsp"></jsp:include>
</head>
<body class="index">
<script>
    $(document).ready(function () {
        var type = "${type}";
        if (type.indexOf('android') > -1)  $('div.header  div  ul  li:nth-child(3) a').addClass(" pure-menu-selected");
        else  $('div.header  div  ul  li:nth-child(2) a').addClass(" pure-menu-selected");

        $('.versionspan').on('click', function (e) {
            if ($(this).parent().children('.builddiv').hasClass("invisible"))
                $(this).parent().children('.builddiv').removeClass("invisible");

            else $(this).parent().children('.builddiv').addClass("invisible");
        });

        $('.appspan').on('click', function (e) {
            if ($(this).parent().children('.qrcode').hasClass("invisible")) {
                $(this).parent().children('.qrcode').addClass("qrcodeShow")
                $(this).parent().children('.qrcode').removeClass("invisible");
                $(this).text("隐藏二维码");

            }
            else {
                $(this).parent().children('.qrcode').removeClass("qrcodeShow")
                $(this).parent().children('.qrcode').addClass("invisible");
                $(this).text("查看二维码");
            }
            console.log("qrcode click");
        });
    });


</script>

<div id="wrap">

    <jsp:include page="${otaPath}/include/header.jsp"></jsp:include>
    <div id="contain" class="main container">
        <h2><%=((String) request.getAttribute("type")).toUpperCase() %>
        </h2>
        <div class="app-list">
            <%
                List<String> versions = (List<String>) request.getAttribute("versions");
                Map<String, List<JSONObject>> versionDataMap = (Map<String, List<JSONObject>>) request.getAttribute("versionDataMap");
                for (String version : versions) {
                    out.println("<div class=\"versiondiv\"><h3 class=\"versionspan\">版本：" + version + "</h3>");

                    List<JSONObject> dataS = versionDataMap.get(version);
                    for (JSONObject data : dataS) {
                        out.println("<div class=\"builddiv invisible\">");
                        out.println("<div class=\"builddivheader\">");

                        String type = (String) request.getAttribute("type");

                        String buildId = (String) data.get(OTAUtility.KEY_JSON_BUILDID);
                        String buildTime = (String) data.get(OTAUtility.KEY_JSON_BUILDTIME);
                        String env = (String) data.get(OTAUtility.KEY_JSON_ENV);
                        String comments = (String) data.get(OTAUtility.KEY_JSON_COMMENTS);
                        String codeBranch = (String) data.get(OTAUtility.KEY_JSON_CODEBRANCH);
                        String uploadType = (String) data.get(OTAUtility.KEY_JSON_UPLOADTYPE);
                        if(type.equalsIgnoreCase(OTAUtility.KEY_ANDROID)) {
                            if (uploadType.equalsIgnoreCase(OTAUtility.CONSTANTS_UPLOADTYPE_JENKINS)) {
                                List<String> apks = (List<String>) data.get(OTAUtility.KEY_JSON_APPFILE);
                                for (String apkName : apks) {
                                    String apkURL = OTAUtility.TOMCAT_OTA_DATA_URL_BASE + "/" + type.toUpperCase() + "/" + version.toUpperCase() + "/" + env.toUpperCase() + "/" + apkName;
                                    String apkPNGURL = apkURL + ".png";

                                    out.println("<div class=\"appdiv\">" +
                                            "<span class=\"\"><a class=\"applink\" href=\"" + apkURL + "\" > " + apkName + " </a></span>" +
                                            "<span class=\"appspan\">查看二维码</span><span class=\"qrcode invisible\">" +
                                            "<img class=\"qrimg\" src=\"" + apkPNGURL + "\"/>\n" +
                                            "</span>" +
                                            "</div>");
                                }

                                out.println("<hr>");
//                                out.println("<span class=\"buildspan\">BuildId: <a href=\"" + OTAUtility.JENKINS_ANDROID_URL + buildId + "\" target=\"\"> " + buildId + " </a></span>");
                                out.println("<span class=\"buildspan\">代码分支: " + codeBranch + "</span>");
                                out.println("<span class=\"buildspan\">环境: " + env + "</span>");
                                out.println("<span class=\"buildspan\">构建时间: " + buildTime + "</span>");
                                out.println("<span class=\"buildspan\"><a href=\"/ota/delete/android/" + buildTime + "\" onclick=\"return confirm('确定删除');\" >删除</a></span>");
//                                out.println("<span class=\"buildspan\"><a href=\"" + OTAUtility.JENKINS_ANDROID_URL + buildId + "/changes\">更新记录</a></span>");
                                out.println("<br/>");
                                out.println("<span class=\"buildspan comments\">备注: " + comments + "</span>");
                                out.println("</div>");
                            } else {
                                String appFile = (String) data.get(OTAUtility.KEY_JSON_APPFILE);
                                String appURL = OTAUtility.TOMCAT_OTA_DATA_URL_BASE + "/" + type.toUpperCase() + "/" + version.toUpperCase() + "/" + env.toUpperCase() + "/" + appFile;
                                out.println("<div class=\"appdiv\">" +
                                        "<span class=\"\"><a class=\"applink\" href=\"" + appURL + "\" > " + appFile + " </a></span>" +
                                        "<span class=\"appspan\">查看二维码</span><span class=\"qrcode invisible\">" +
                                        "<img class=\"qrimg\" src=\"" + appURL + ".png\"/>\n" +
                                        "</span>" +
                                        "</div>");

                                out.println("<hr>");
//                                out.println("<span class=\"buildspan\">BuildId: 手动上传 </a></span>");
                                out.println("<span class=\"buildspan\">代码分支: " + codeBranch + "</span>");
                                out.println("<span class=\"buildspan\">环境: " + env + "</span>");
                                out.println("<span class=\"buildspan\">构建时间: " + buildTime + "</span>");
                                out.println("<span class=\"buildspan\"><a href=\"/ota/delete/android/" + buildTime + "\" onclick=\"return confirm('确定删除');\" >删除</a></span>");
                                out.println("<br/>");
                                out.println("<span class=\"buildspan comments\">备注: " + comments + "</span></div>");
                            }
                        } else {
                            String appFile = (String) data.get(OTAUtility.KEY_JSON_APPFILE);
                            String appURL = OTAUtility.IOS_HTTPS_URL_BASE + "/" + type.toUpperCase() + "/" + version.toUpperCase() +
                                    "/" + env.toUpperCase() + "/" + appFile.substring(0,appFile.lastIndexOf(".")) + ".plist";
                            appURL = "itms-services://?action=download-manifest&url=" + appURL;

                            String pngURL = OTAUtility.TOMCAT_OTA_DATA_URL_BASE + "/" + type.toUpperCase() + "/" + version.toUpperCase() +
                                    "/" + env.toUpperCase() + "/" + appFile + ".png";

                            out.println("<div class=\"appdiv\">" +
                                    "<span class=\"\"><a class=\"applink\" href=\"" + appURL + "\" > " + appFile + " </a></span>" +
                                    "<span class=\"appspan\">查看二维码</span><span class=\"qrcode invisible\">" +
                                    "<img class=\"qrimg\" src=\"" + pngURL + "\"/>\n" +
                                    "</span>" +
                                    "</div>");

                            out.println("<hr>");
//                            if (uploadType.equalsIgnoreCase(OTAUtility.CONSTANTS_UPLOADTYPE_JENKINS)) {
//                                out.println("<span class=\"buildspan\">BuildId: <a href=\"" + OTAUtility.JENKINS_IOS_URL + buildId + "\" target=\"\"> " + buildId + " </a></span>");
//                            } else {
//                                out.println("<span class=\"buildspan\">BuildId: 手动上传 </a></span>");
//                            }
                            out.println("<span class=\"buildspan\">代码分支: " + codeBranch + "</span>");
                            out.println("<span class=\"buildspan\">环境: " + env + "</span>");
                            out.println("<span class=\"buildspan\">构建时间: " + buildTime + "</span>");
                            out.println("<span class=\"buildspan\"><a href=\"/ota/delete/ios/" + buildTime + "\" onclick=\"return confirm('确定删除');\" >删除</a></span>");
                            out.println("<br/>");
                            out.println("<span class=\"buildspan comments\">备注: " + comments + "</span></div>");
                        }

                        out.println("</div>");

                    }
                    out.println("</div>");
                }
            %>
        </div>
        <jsp:include page="${otaPath}/include/footer.jsp"></jsp:include>
    </div>
</body>
</html>

