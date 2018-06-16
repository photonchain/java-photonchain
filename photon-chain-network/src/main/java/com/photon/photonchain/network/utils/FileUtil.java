package com.photon.photonchain.network.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:PTN
 * @Description:
 * @Date:15:14 2018/1/11
 * @Modified by:
 */
public class FileUtil {

    public static boolean writeFileContent(String filepath, String newstr) throws IOException {
        Boolean bool = false;
        String filein = newstr + "\r\n";
        String temp = "";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        FileOutputStream fos = null;
        PrintWriter pw = null;
        try {
            File file = new File(filepath);
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; (temp = br.readLine()) != null; i++) {
                buffer.append(temp);
                buffer = buffer.append(System.getProperty("line.separator"));
            }
            buffer.append(filein);
            fos = new FileOutputStream(file);
            pw = new PrintWriter(fos);
            pw.write(buffer.toString().toCharArray());
            pw.flush();
            bool = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (fos != null) {
                fos.close();
            }
            if (br != null) {
                br.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
        return bool;
    }

    public static List<String> readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        List<String> list = new ArrayList<String>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                list.add(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return list;
    }

    public static Map<String, String> traverseFolder(String path) {
        String encoding = "UTF-8";
        Map<String, String> resultMap = new HashMap<>();
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
        } else {
            for (File file2 : files) {
                String account = "";
                String address = "";
                if (file2.isDirectory()) {
                    traverseFolder(file2.getAbsolutePath());
                } else {
                    try {

                        BufferedReader reader = new BufferedReader(new FileReader(file2));
                        String tempString = null;
                        while ((tempString = reader.readLine()) != null) {
                            account = account+tempString;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    address = file2.getName();
                }
                if(address.contains("px")){
                    resultMap.put(address, account);
                }
            }
        }

        return resultMap;
    }

    public static <T extends Serializable> T clone(T obj) {
        T cloneObj = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream obs = new ObjectOutputStream(out);
            obs.writeObject(obj);
            obs.close();
            ByteArrayInputStream ios = new ByteArrayInputStream(out.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(ios);
            cloneObj = (T) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cloneObj;
    }
}