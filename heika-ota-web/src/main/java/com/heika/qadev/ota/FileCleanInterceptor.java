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

        filterFile(OTAUtility.CONSTANTS_IOS_DATA_FILE, OTAUtility.TOMCAT_OTA_DATA_DIR + "/IOS");
        filterFile(OTAUtility.CONSTANTS_ANDROID_DATA_FILE, OTAUtility.TOMCAT_OTA_DATA_DIR + "/ANDROID");

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    public void filterFile(String dataFilePath, String dataDirPath) {

        BufferedWriter out = null;

        try {
            File dataFile = new File(dataFilePath);

            if (dataFile.exists()) {
                OTAController.LOCK_FILE.readLock().lock();
                List<String> dataStrList = OTAUtility.readFileAsListOfStrings(dataFilePath);
                OTAController.LOCK_FILE.readLock().unlock();
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

                if(versionDirs == null){
                    return;
                }

                for (File versionDir : versionDirs) {
                    if (versionDir.getName().equals("COMPATIBILITY") || versionDir.getName().equals("TRAIN")) {
                        continue;
                    } else {
                        long currentTime = System.currentTimeMillis();
                        long modifyTime = versionDir.lastModified();
                        // 删除45天之前的APP文件
                        if (currentTime > (modifyTime + (long)1000 * 60 * 60 * 24 * 45)) {
                            FileUtils.deleteDirectory(versionDir);
                            System.out.println("################### Delete Dir : " + versionDir.getName());
                            filteredDataMap.remove(versionDir.getName());
                        }
                    }
                }

                Iterator<List<String>> dataIterator = filteredDataMap.values().iterator();
                List<String> filteredData = new ArrayList<>();
                while (dataIterator.hasNext()) {
                    filteredData.addAll(dataIterator.next());
                }

                OTAController.LOCK_FILE.writeLock().lock();
                out = new BufferedWriter(new FileWriter(dataFile));
                for (String data : filteredData) {
                    out.write(data);
                    out.write(System.lineSeparator());
                }
                OTAController.LOCK_FILE.writeLock().unlock();
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
        System.out.println((long)1000 * 60 * 60 * 24 * 45);
        System.out.println(System.currentTimeMillis() + (long)1000 * 60 * 60 * 24 * 45);
        long currentTime = System.currentTimeMillis();
        System.out.println(currentTime > (System.currentTimeMillis() + (long)1000 * 60 * 60 * 24 * 45));
    }
}
