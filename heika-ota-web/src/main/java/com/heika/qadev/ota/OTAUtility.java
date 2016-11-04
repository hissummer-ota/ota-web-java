package com.heika.qadev.ota;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yangjian on 2016/8/22.
 */

public class OTAUtility {

    public static String CONSTANTS_UPLOADTYPE_JENKINS = "jenkins";
    public static String CONSTANTS_UPLOADTYPE_MANUAL = "manual";
    public static String KEY_IOS = "ios";
    public static String KEY_ANDROID = "android";
    public static String KEY_JSON_UPLOADTYPE = "uploadtype";
    public static String KEY_JSON_APPFILE = "appfile";
    public static String KEY_JSON_CODEBRANCH = "codeBranch";
    public static String KEY_JSON_BUILDID = "buildId";
    public static String KEY_JSON_VERSION = "version";
    public static String KEY_JSON_COMMENTS = "comments";
    public static String KEY_JSON_ENV = "env";
    public static String KEY_JSON_BUILDTIME = "buildTime";

    public static Properties OTA_CONFIG = new Properties();
    public static String TOMCAT_APP_BASE_DIR;
    public static String TOMCAT_OTA_DATA_DIR;

    public static String CONSTANTS_IOS_DATA_FILE;
    public static String CONSTANTS_ANDROID_DATA_FILE;

    public static String TOMCAT_OTA_DATA_URL_BASE;
    public static String IOS_HTTPS_URL_BASE;

    public static String JENKINS_WEBAPP_CONTEXT;
    public static String JENKINS_ANDROID_URL;
    public static String JENKINS_IOS_URL;
    public static String JENKINS_QR_ANDROID_POSTFIX;
    public static String JENKINS_QR_IOS_POSTFIX;

    static {
        try {
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void loadConfig() throws IOException {

        OTA_CONFIG.load(OTAUtility.class.getResourceAsStream("/config-ota.properties"));

        TOMCAT_APP_BASE_DIR = OTA_CONFIG.getProperty("TOMCAT_APP_BASE_DIR");
        IOS_HTTPS_URL_BASE = "https://" + OTA_CONFIG.getProperty("IOS_HTTPS_HOST_PORT") + "/otadata";

        TOMCAT_OTA_DATA_DIR = TOMCAT_APP_BASE_DIR + "/ota/otadata";
        CONSTANTS_IOS_DATA_FILE = TOMCAT_OTA_DATA_DIR + "/ios_data.dat";
        CONSTANTS_ANDROID_DATA_FILE = TOMCAT_OTA_DATA_DIR + "/android_data.dat";
        TOMCAT_OTA_DATA_URL_BASE = "/ota/otadata";

        JENKINS_WEBAPP_CONTEXT = OTA_CONFIG.getProperty("JENKINS_WEBAPP_CONTEXT");
        JENKINS_ANDROID_URL = "http://" + OTA_CONFIG.getProperty("JENKINS_HOST_PORT") +
                (JENKINS_WEBAPP_CONTEXT.trim().length() > 0 ? "/" + JENKINS_WEBAPP_CONTEXT.trim() : "") +
                "/job/" + OTA_CONFIG.getProperty("JENKINS_JOB_ANDROID_NAME") + "/";
        JENKINS_IOS_URL = "http://" + OTA_CONFIG.getProperty("JENKINS_HOST_PORT") +
                (JENKINS_WEBAPP_CONTEXT.trim().length() > 0 ? "/" + JENKINS_WEBAPP_CONTEXT.trim() : "") +
                "/job/" + OTA_CONFIG.getProperty("JENKINS_JOB_IOS_NAME") + "/";
        JENKINS_QR_ANDROID_POSTFIX = OTA_CONFIG.getProperty("JENKINS_QR_ANDROID_POSTFIX");
        JENKINS_QR_IOS_POSTFIX = OTA_CONFIG.getProperty("JENKINS_QR_IOS_POSTFIX");
    }

    public static List<String> readFileAsListOfStrings(String filename) throws Exception {
        List<String> records = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            records.add(line);
        }
        reader.close();
        return records;
    }

    public static void writeFile(String canonicalFilename, String text)
            throws IOException {
        File file = new File(canonicalFilename);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
        out.write(text);
        out.write(System.lineSeparator());
        out.close();
    }

    public static String httpDownloadFile(String urlToDownload, String localFilePath) {

        // in case DNS can not be resolved
        if (urlToDownload.indexOf("qa.heika.com") > -1) {
            urlToDownload = urlToDownload.replace("qa.heika.com", "172.16.2.37");
        }

        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(urlToDownload);
            HttpResponse response = httpclient.execute(httpget);

            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            File file = new File(localFilePath);
            file.getParentFile().mkdirs();
            FileOutputStream fileOut = new FileOutputStream(file);

            int cache = 10 * 1024;
            byte[] buffer = new byte[cache];
            int ch = 0;
            while ((ch = is.read(buffer)) != -1) {
                fileOut.write(buffer, 0, ch);
            }
            is.close();
            fileOut.flush();
            fileOut.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<String> getApkPngS(String type, String buildId) throws Exception {
        String content = getQRCodeHTMLContent(type, buildId);

        List<String> apkPngSList = new ArrayList<>();

        Pattern pattern = Pattern.compile("<img src=\"(.*)\"");
        Matcher matcher = pattern.matcher(content.toString());
        while (matcher.find()) {
            apkPngSList.add(matcher.group(1));
        }

        int index = 1;
        for (String apk : apkPngSList) {
            System.out.println(index + "    " + apk.substring(0, apk.indexOf(".png")));
            System.out.println(index + "    " + apk.substring(apk.indexOf("outputs/apk/"), apk.indexOf(".png")).substring("outputs/apk/".length()));
            index++;
        }

        return apkPngSList;

    }

    private static String getQRCodeHTMLContent(String type, String buildId) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet;
        if (type.equalsIgnoreCase(KEY_IOS)) {
            httpGet = new HttpGet(JENKINS_IOS_URL + buildId + JENKINS_QR_IOS_POSTFIX);
        } else {
            httpGet = new HttpGet(JENKINS_ANDROID_URL + buildId + JENKINS_QR_ANDROID_POSTFIX);
        }

        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity resEntity = response.getEntity();
        InputStream inputStream = resEntity.getContent();
        InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");

        StringBuilder content = new StringBuilder();

        char[] buff = new char[4096];
        int length;
        while ((length = reader.read(buff)) != -1) {
            content.append(new String(buff, 0, length));
        }

        reader.close();
        inputStream.close();
        httpclient.close();

        return content.toString();
    }

    public static String getIpaPng(String type, String buildId) throws Exception {

        String content = getQRCodeHTMLContent(type, buildId);
        String ipa = "";

        Pattern pattern = Pattern.compile("<img src=\"(.*)\"");
        Matcher matcher = pattern.matcher(content.toString());
        if (matcher.find()) {
            ipa = matcher.group(1);
        }

        return ipa;
    }

    public static String generatepListContent(String ipaURL, String ipaName) throws Exception {
        String pList = "<plist version=\"1.0\">" +
                "<dict>" +
                "<key>items</key>" +
                "<array>" +
                "<dict>" +
                "<key>assets</key>" +
                "<array>" +
                "<dict>" +
                "<key>kind</key>" +
                "<string>software-package</string>" +
                "<key>url</key>" +
                "<string>" + ipaURL +
                "</string>" +
                "</dict>" +
                "</array>" +
                "<key>metadata</key>" +
                "<dict>" +
                "<key>bundle-identifier</key>" +
                "<string>com.renrendai.heika</string>" +
                "<key>bundle-version</key>" +
                "<string>1.0</string>" +
                "<key>kind</key>" +
                "<string>software</string>" +
                "<key>title</key>" +
                "<string>" + ipaName +
                "</string>" +
                "</dict>" +
                "</dict>" +
                "</array>" +
                "</dict>" +
                "</plist>";


        return pList;
    }

    public static void main(String[] args) throws Exception {
    }

}
