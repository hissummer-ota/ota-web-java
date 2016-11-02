package com.heika.qadev.ota;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FileCleanInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        System.out.println("################### Begin : FileCleanInterceptor.postHandle");
        OTAController.LOCK_FILE.writeLock().lock();

        filterFile(OTAUtility.CONSTANTS_IOS_DATA_FILE, OTAUtility.TOMCAT_OTA_DATA_DIR + "/IOS");
        filterFile(OTAUtility.CONSTANTS_ANDROID_DATA_FILE, OTAUtility.TOMCAT_OTA_DATA_DIR + "/ANDROID");

        OTAController.LOCK_FILE.writeLock().unlock();
        System.out.println("################### Finish : FileCleanInterceptor.postHandle");
    }

    public void filterFile(String dataFilePath, String dataDirPath) {

        BufferedWriter out = null;

        try {
            File dataFile = new File(dataFilePath);

            if (dataFile.exists()) {
                List<String> dataStrList = OTAUtility.readFileAsListOfStrings(dataFilePath);
                Map<String, List<String>> filteredDataMap = new HashMap<>();
                for (String dataStr : dataStrList) {
                    if (dataStr.trim().length() > 0) {
                        JSONObject jo = JSONObject.parseObject(dataStr);
                        String version = jo.getString(OTAUtility.KEY_JSON_VERSION);
                        if (!filteredDataMap.containsKey(version)) {
                            filteredDataMap.put(version, new ArrayList<>());
                        }

                        filteredDataMap.get(version).add(dataStr);
                    }
                }

                File dataDir = new File(dataDirPath);
                File[] versionDirs = dataDir.listFiles();
                for (File versionDir : versionDirs) {
                    if (versionDir.getName().equals("兼容包")) {
                        continue;
                    } else {
                        long currentTime = System.currentTimeMillis();
                        long modifyTime = versionDir.lastModified();
                        // 删除45天之间的APP文件
//                        if (currentTime > (modifyTime + 1000 * 60 * 60 * 24 * 45)) {
                        if (currentTime > (modifyTime + 1000 * 60 * 60 * 24 * 1)) {
                            FileUtils.deleteDirectory(versionDir);
                            filteredDataMap.remove(versionDir.getName());
                        }
                    }
                }

                Iterator<List<String>> dataIterator = filteredDataMap.values().iterator();
                List<String> filteredData = new ArrayList<>();
                while (dataIterator.hasNext()) {
                    filteredData.addAll(dataIterator.next());
                }

                out = new BufferedWriter(new FileWriter(dataFile));
                for (String data : filteredData) {
                    out.write(data);
                    out.write(System.lineSeparator());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        System.out.println(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 45);
    }
}
