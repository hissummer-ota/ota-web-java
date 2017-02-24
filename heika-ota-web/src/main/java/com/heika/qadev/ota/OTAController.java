package com.heika.qadev.ota;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Controller
public class OTAController {

    public static final ReadWriteLock LOCK_FILE = new ReentrantReadWriteLock();

    private String getHeikaLogoPath(){
        String webRootRealPath = ContextLoader.getCurrentWebApplicationContext().getServletContext().getRealPath("");
        return webRootRealPath + "/ota/logo.png";
    }

    @RequestMapping("/ota/index")
    public String index() {
        return "ota/index";
    }

    @RequestMapping("/ota")
    public String welcome() {
        return "ota/index";
    }

    @RequestMapping("/ota/list")
    public String list(String type, ModelMap modelMap) throws Exception {

        Map<String, List<JSONObject>> versionDataMap = new HashMap<>();
        String dataFile = getFileByFileType(type);
        LOCK_FILE.readLock().lock();
        List<String> dataS = OTAUtility.readFileAsListOfStrings(dataFile);
        LOCK_FILE.readLock().unlock();
        //TODO
        for (String data : dataS) {
            if (data.trim().length() > 0) {
                JSONObject jo = (JSONObject) JSONObject.parse(data);
                String joVersion = (String) jo.get(OTAUtility.KEY_JSON_VERSION);
                if (!versionDataMap.containsKey(joVersion)) {
                    versionDataMap.put(joVersion, new ArrayList<>());
                }
                List versionDataS = versionDataMap.get(joVersion);
                versionDataS.add(jo);
            }
        }

        List<String> versions = new ArrayList<>();
        versions.addAll(versionDataMap.keySet());
        Collections.sort(versions, new Comparator<String>() {
            // 降序排列
            @Override
            public int compare(String original, String toCompare) {
                return 0 - original.compareTo(toCompare);
            }
        });

        for (String version : versions) {
            Collections.sort(versionDataMap.get(version), new Comparator<JSONObject>() {
                // 降序排列
                @Override
                public int compare(JSONObject original, JSONObject toCompare) {

                    if(version.equalsIgnoreCase("COMPATIBLITY") || version.equalsIgnoreCase("TRAIN")){
                        String orgComments = original.getString(OTAUtility.KEY_JSON_COMMENTS);
                        String toComments = toCompare.getString(OTAUtility.KEY_JSON_COMMENTS);
                        if(!orgComments.equals(toComments)){
                            return 0 - orgComments.compareTo(toComments);
                        } else {
                            String orgEnv = original.getString(OTAUtility.KEY_JSON_ENV);
                            String toEnv = toCompare.getString(OTAUtility.KEY_JSON_ENV);
                            return 0 - orgEnv.compareTo(toEnv);
                        }
                    }

                    String originalBuildTime = (String) original.get(OTAUtility.KEY_JSON_BUILDTIME);
                    String toCompareBuildTime = (String) toCompare.get(OTAUtility.KEY_JSON_BUILDTIME);
                    return 0 - originalBuildTime.compareTo(toCompareBuildTime);
                }
            });
        }

        modelMap.put("versions", versions);
        modelMap.put("versionDataMap", versionDataMap);
        modelMap.put("type", type);

        return "ota/list";
    }

    @RequestMapping("/ota/config")
    public void reloadConfig(HttpServletResponse servletResponse) {
        try {
            OTAUtility.loadConfig();
            sendMSG(servletResponse, OTAUtility.OTA_CONFIG.toString(), true);
        } catch (IOException e) {
            e.printStackTrace();
            sendMSG(servletResponse, e.toString(), false);
        }
    }

    @RequestMapping("/ota/delete/{type}/{buildTime}")
    public String delete(@PathVariable("type") String type, @PathVariable("buildTime") String buildTime) {
        LOCK_FILE.writeLock().lock();

        try {
            String dataPath = getFileByFileType(type);
            List<String> dataS = OTAUtility.readFileAsListOfStrings(dataPath);

            String dataToDelete = null;
            for (String data : dataS) {
                JSONObject dataJson = JSONObject.parseObject(data);
                String _buildTime = dataJson.getString(OTAUtility.KEY_JSON_BUILDTIME);
                if (_buildTime.equals(buildTime)) {
                    String version = dataJson.getString(OTAUtility.KEY_JSON_VERSION);
                    String env = dataJson.getString(OTAUtility.KEY_JSON_ENV);
                    String basePath = getAppFileBasePath(type, version, env);

                    JSONArray files = dataJson.getJSONArray(OTAUtility.KEY_JSON_APPFILE);
                    int size = files.size();
                    for (int i = 0; i < size; i++) {
                        String fileName = (String) files.get(0);
                        FileUtils.forceDelete(new File(basePath + fileName));
                    }

                    dataToDelete = data;

                    break;
                }
            }

            dataS.remove(dataToDelete);
            File dataFile = new File(dataPath);
            File dataBackupFile = new File(dataFile.getParentFile(),
                    "datafilebackup/" + dataFile.getName() + "." + simpleFileNameDateFormat.format(new Date()));
            FileUtils.moveFile(dataFile, dataBackupFile);

            for(String content : dataS) {
                OTAUtility.writeFile(dataPath, content);
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        LOCK_FILE.writeLock().unlock();

        return "ota/list";
    }

    public static void main(String[] args){
        File file = new File("test");
        File file2 = new File(file.getParentFile(), "datafilebackup/" + file.getName() + "." + simpleFileNameDateFormat.format(new Date()));


        System.out.println(file.getAbsolutePath());
        System.out.println(file2.getAbsolutePath());
        System.out.println(file2.getParentFile().getAbsolutePath());
    }

    @RequestMapping(value = "/ota/upload")
    public String upload(HttpServletRequest servletRequest, HttpServletResponse servletResponse, String type,
                         String buildId, String version, String env, String codeBranch, String comments) {

        if(comments == null || comments.trim().length() == 0) {
            comments = getComments(OTAUtility.CONSTANTS_UPLOADTYPE_JENKINS, buildId, env);
        }

        if(env.equalsIgnoreCase("train")){
            comments = version;
            version = "TRAIN";
        }

        JSONObject jsonObj = getCommonFileJsonObj(buildId,env,version,comments,codeBranch);
        jsonObj.put(OTAUtility.KEY_JSON_UPLOADTYPE, OTAUtility.CONSTANTS_UPLOADTYPE_JENKINS);

        String basePath = getAppFileBasePath(type, version, env);
        if(type.equalsIgnoreCase(OTAUtility.KEY_ANDROID)) {
            List<String> apkPngS;
            List<String> apkList = new ArrayList<>();
            try {
                apkPngS = OTAUtility.getApkPngS(type, buildId);
                for (String apkPng : apkPngS) {
                    String apkName = apkPng.substring(apkPng.indexOf("outputs/apk/"), apkPng.indexOf(".png")).substring("outputs/apk/".length());
                    apkName = apkName.replace(":", "-");

//                    if (!apkName.startsWith("heika_")){
//                        continue;
//                    }

                    apkList.add(apkName);

                    String apkURL = apkPng.substring(0, apkPng.indexOf(".png"));

                    // download apk file
                    OTAUtility.httpDownloadFile(apkURL, basePath + apkName);

                    // generate QR image
                    String appURL = getAppURLBasePath(servletRequest, type,version,env) + apkName;
                    QRCodeUtil.encode(appURL, getHeikaLogoPath() , basePath, apkName, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendMSG(servletResponse, e.toString(),false);
                return null;
            }

            jsonObj.put(OTAUtility.KEY_JSON_APPFILE, apkList);

            LOCK_FILE.writeLock().lock();
            String dataFile = getFileByFileType(type);
            try {
                OTAUtility.writeFile(dataFile, jsonObj.toJSONString());
            } catch (IOException e) {
                sendMSG(servletResponse, e.toString(),false);
                e.printStackTrace();
                return null;
            } finally {
                LOCK_FILE.writeLock().unlock();
            }

        } else if (type.equalsIgnoreCase(OTAUtility.KEY_IOS)){
            String ipaPgn = null;
            try {
                ipaPgn = OTAUtility.getIpaPng(type, buildId);

                String ipaName = ipaPgn.substring(ipaPgn.indexOf("output/"), ipaPgn.indexOf(".png")).substring("output/".length()) + ".ipa";
                ipaName = ipaName.replace(":", "-");

                String ipaURL = ipaPgn.substring(0, ipaPgn.indexOf(".png")) + ".ipa";

                // download apk file
                OTAUtility.httpDownloadFile(ipaURL, basePath + ipaName);

                // generate plist file
                String pListName = ipaName.substring(0, ipaName.indexOf(".ipa")) + ".plist";
                String appURL = getAppURLBasePath(servletRequest, type, version, env) + ipaName;
                OTAUtility.writeFile(basePath + pListName, OTAUtility.generatepListContent(appURL, ipaName.substring(0, ipaName.indexOf(".ipa"))));

                // generate QR image
                appURL = getISOHTTPSURLBasePath(type, version, env) + pListName;
                appURL = "itms-services://?action=download-manifest&url=" + appURL;
                QRCodeUtil.encode(appURL, getHeikaLogoPath(), basePath, ipaName, true);

                jsonObj.put(OTAUtility.KEY_JSON_APPFILE, ipaName);
            } catch (Exception e) {
                e.printStackTrace();
                sendMSG(servletResponse, e.toString(),false);
                return null;
            }

            LOCK_FILE.writeLock().lock();
            String dataFile = getFileByFileType(type);
            try {
                OTAUtility.writeFile(dataFile, jsonObj.toJSONString());
            } catch (Exception e) {
                e.printStackTrace();
                sendMSG(servletResponse, e.toString(), false);
                return null;
            } finally {
                LOCK_FILE.writeLock().unlock();
            }
        }

        sendMSG(servletResponse, "上传成功",true);
        return null;
    }

    private String getAppFileBasePath(String type, String version, String env){
        return OTAUtility.TOMCAT_OTA_DATA_DIR + "/" + type.toUpperCase() + "/" + version.toUpperCase() + "/" + env.toUpperCase() + "/";
    }

    private String getAppURLBasePath(HttpServletRequest servletRequest, String type, String version, String env){
        String requestURL = servletRequest.getRequestURL().toString();
        requestURL = requestURL.substring(0,requestURL.indexOf("/ota"));
        return requestURL + OTAUtility.TOMCAT_OTA_DATA_URL_BASE + "/" + type.toUpperCase() + "/" + version.toUpperCase() + "/" + env.toUpperCase() + "/";
    }

    private String getISOHTTPSURLBasePath(String type, String version, String env){
        return OTAUtility.IOS_HTTPS_URL_BASE + "/" + type.toUpperCase() + "/" + version.toUpperCase() + "/" + env.toUpperCase() + "/";
    }

    private String getComments(String uploadType, String buildId, String env) {

        String prefix = buildId;

        if (uploadType.equalsIgnoreCase(OTAUtility.CONSTANTS_UPLOADTYPE_MANUAL)) {
            prefix = "手动上传";
        }

        String comments;

        switch (env) {
            case "113":
                comments = prefix + "-113-48环境";
                break;
            case "38":
                comments = prefix + "-38-4环境";
                break;
            case "train":
                comments = prefix + "-小秘书环境";
                break;
            case "release":
                comments = prefix + "-线上环境";
                break;
            case "online":
                comments = prefix + "-线上环境";
                break;
            case "backup":
                comments = prefix + "-备机环境";
                break;
            default:
                comments = "无";
        }

        return comments;
    }


    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    private static final SimpleDateFormat simpleFileNameDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private JSONObject getCommonFileJsonObj(String buildId, String env, String version, String comments, String codeBranch) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(OTAUtility.KEY_JSON_BUILDID, buildId);
        jsonObject.put(OTAUtility.KEY_JSON_ENV, env);
        jsonObject.put(OTAUtility.KEY_JSON_VERSION, version);
        jsonObject.put(OTAUtility.KEY_JSON_COMMENTS, comments);
        jsonObject.put(OTAUtility.KEY_JSON_CODEBRANCH, codeBranch);

        jsonObject.put(OTAUtility.KEY_JSON_BUILDTIME, simpleDateFormat.format(new Date()));

        return jsonObject;
    }


    @RequestMapping("/ota/upload_manual")
    public String upload_manual(HttpServletRequest servletRequest, HttpServletResponse servletResponse,
                                String buildId, String version, String env, String codeBranch, String comments,
                                @RequestParam CommonsMultipartFile app) {

        String appFileName = app.getOriginalFilename();
        if(!(appFileName.endsWith(".apk") || appFileName.endsWith(".ipa"))){
            sendMSG(servletResponse, "上传应用失败，应用需为apk或者ipa文件!", false);
            return null;
        }

        String type;

        if(appFileName.endsWith(".apk")){
            type = OTAUtility.KEY_ANDROID;
        } else {
            type = OTAUtility.KEY_IOS;
        }

        if(comments == null || comments.trim().length() == 0) {
            comments = getComments(OTAUtility.CONSTANTS_UPLOADTYPE_MANUAL, buildId, env);
        }

        JSONObject jsonObj = getCommonFileJsonObj(buildId,env,version,comments,codeBranch);

        String appFileBasePath = getAppFileBasePath(type,version,env);
        String appFilePath = appFileBasePath + appFileName;

        File appFile = new File(appFilePath);

        if(appFile.exists()){
            sendMSG(servletResponse, "上传应用失败，文件已存在!", false);
            return null;
        }

        appFile.getParentFile().mkdirs();

        try {
            app.transferTo(appFile);

            String appURL = getAppURLBasePath(servletRequest, type, version, env) + appFileName;
            if (type.equals(OTAUtility.KEY_ANDROID)) {
                QRCodeUtil.encode(appURL, getHeikaLogoPath(), appFileBasePath, appFileName, true);
            } else {
                // generate plist file
                String pListName = appFileName.substring(0, appFileName.indexOf(".ipa")) + ".plist";
                OTAUtility.writeFile(appFileBasePath + pListName, OTAUtility.generatepListContent(appURL, appFileName.substring(0, appFileName.indexOf(".ipa"))));

                String plistURL = getISOHTTPSURLBasePath(type, version, env) + pListName;
                plistURL = "itms-services://?action=download-manifest&url=" + plistURL;
                QRCodeUtil.encode(plistURL, getHeikaLogoPath(), appFileBasePath, appFileName, true);
            }

            jsonObj.put(OTAUtility.KEY_JSON_UPLOADTYPE, OTAUtility.CONSTANTS_UPLOADTYPE_MANUAL);
            jsonObj.put(OTAUtility.KEY_JSON_APPFILE, appFileName);


            String dataFile = getFileByFileType(type);
            LOCK_FILE.writeLock().lock();
            OTAUtility.writeFile(dataFile, jsonObj.toJSONString());
            LOCK_FILE.writeLock().unlock();
        } catch (Exception e) {
            e.printStackTrace();
            sendMSG(servletResponse, e.toString(), false);
        }

        sendMSG(servletResponse, "上传应用成功！", true);

        return null;
    }

    @RequestMapping("/ota/upload_manual_show")
    public String upload_manual_show() throws Exception {
        return "/ota/upload";
    }

    private String getFileByFileType(String type) {
        if (type.equalsIgnoreCase(OTAUtility.KEY_ANDROID)) {
            return OTAUtility.CONSTANTS_ANDROID_DATA_FILE;
        } else {
            return OTAUtility.CONSTANTS_IOS_DATA_FILE;
        }
    }

    private static void sendMSG(HttpServletResponse httpServletResponse,String message, boolean isSuccess) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("message", message);
        jsonObj.put("status", isSuccess ? 0 : 1);

        httpServletResponse.setContentType("text/html;charset=utf-8");
        PrintWriter out = null;
        try {
            out = httpServletResponse.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.print(jsonObj.toString());
        out.flush();
        out.close();
    }
}
