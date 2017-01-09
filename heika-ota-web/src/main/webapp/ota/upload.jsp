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
    <jsp:include page="${otaPath}/include/headerinclude.jsp"></jsp:include>
</head>
<body class="index">
<script>

    $(document).ready(function () {

        $('div.header  div  ul  li:nth-child(4) a').addClass(" pure-menu-selected");
        $('#upload').on('click', function (e) {
            e.preventDefault();
            $('#message').removeClass();
            $('#message').addClass("messageok");
            $("#upload").prop('disabled', true);
            $("#upload").css('background', 'gray');
            $('#message').html("<img class=\"loadinggif\" src=\"${otaPath}/static/images/loading.gif\" />   " + "正在上传中...");

            var fd = new FormData();
            fd.append('codeBranch', $('[name="codeBranch"]').val());
            fd.append('version', $('[name="version"]').val());
            fd.append('type', $('[name="type"]').val());
            fd.append('env', $('[name="env"]').val());
            fd.append('uploadType', $('[name="uploadType"]').val());
            fd.append('comments', $('[name="comments"]').val());
            if ($('[name="forceOverriden"]').is(':checked'))
                fd.append('forceOverriden', $('[name="forceOverriden"]').val());

            fd.append('app', $('[name="app"]')[0].files[0]);

            $.ajax({
                url: '/ota/upload_manual',
                data: fd,
                processData: false,
                contentType: false,
                type: 'POST',
                success: function (data) {
                    console.log(data);
                    var message = JSON.parse(data);
                    console.log(message.message);
                    //$('#message').removeClass("invisible");
                    $('#message').text(message.message);

                    if (message.status == 0) {
                        $('#message').removeClass();
                        $('#message').addClass("messageok");
                    }
                    else {
                        $('#message').removeClass();
                        $('#message').addClass("messageerror");
                    }

                    $("#upload").css('background', '#1f8dd6');
                    $("input").prop('disabled', false);
                },

                error: function (jqXHR, textStatus, errorThrown) {

                    $("#upload").css('background', '#1f8dd6');
                    $('#message').removeClass();
                    $('#message').addClass("messageerror");
                    $('#message').text("服务器出现错误 '" + textStatus + ": " + errorThrown + "'请刷新后重试！");
                    $("input").prop('disabled', false);
                },

                timeout: 30000

            });


        });

    });


</script>

<div id="wrap">

    <jsp:include page="${otaPath}/include/header.jsp"></jsp:include>

    <div id="contain" class="main container">
        <h2>上传应用</h2>
        <div class="app-list">
            <div id="message" class="invisible "></div>
            <div id="uploaddiv">
                <form method="post" enctype="multipart/form-data" id="uploadForm" action="/ota/upload_manual">
                    <fieldset>
                        <legend>应用信息</legend>
                        <label for="app">应用包:</label>
                        <input type="file" name="app" accept=".apk,.ipa"><br/><br/>
                        <!--
                        <label for="type">平台:</label>
                        <select name="type">
                            <option value="android">ANDROID</option>
                            <option value="ios">IOS</option>
                        </select> <br/><br/>
                        -->
                        <label for="version">版本:</label>
                        <input type="text" name="version" placeholder="COMPATIBLITY" value="PAY-DAY-LOAN"><br/><br/>
                        <label for="codeBranch">代码分支:</label>
                        <input type="text" name="codeBranch" placeholder="master" value="master"><br/><br/>
                        <label for="env">环境:</label>
                        <input type="text" name="env" placeholder="test" value="test"><br/><br/>
                        <label for="comments">备注:</label>
                        <input type="text" name="comments" cols="30" placeholder="NONE"><br/><br/>
                        <label class="invisible" for="forceOverriden">强制覆盖:</label>
                        <input class="invisible"
                               type="checkbox" name="forceOverriden"
                               onclick="if(this.checked) alert('!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n选择此选项遇到文件名相同时，将会覆盖原文件，请再次确定是否选择此选项!\n================');"
                               value="1"><br/><br/>
                        <input type="hidden" name="uploadType" value="manually">
                        <input id="upload" type="submit" value="提交">
                    </fieldset>
                </form>

                <br/>
                <!--
                <div style="color:red;">备注：输入框中,不能使用逗号","。强制替换选项，慎重选择！</div>
                -->
            </div>
        </div>

    </div>


    <jsp:include page="${otaPath}/include/footer.jsp"></jsp:include>

</div>
</body>
</html>

