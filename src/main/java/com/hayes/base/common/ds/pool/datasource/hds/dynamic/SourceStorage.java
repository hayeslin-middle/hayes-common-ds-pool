package com.hayes.base.common.ds.pool.datasource.hds.dynamic;

import java.io.*;
import java.util.Optional;

/**
 * @program: hayes-common-ds-pool
 * @Class SourceStorage
 * @description: 关于此类的描述说明
 * @author: Mr.HayesLin
 * @create: 2022-02-13 15:26
 **/
public class SourceStorage {


    private static final String LOCAL_STORE_BASE_PATH = System.getProperty("user.home") + File.separator + "hds";
    private static final String LOCAL_SUFFIX = ".json";

    /**
     * 加载文件内容
     *
     * @param applicationName
     * @return
     * @throws IOException
     */
    public static Optional<String> loadFile(String applicationName) throws IOException {

        File file = new File(getLocalConfigDictionary(applicationName));
        if (!file.exists()) return Optional.empty();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            StringBuilder sb = new StringBuilder();
            String tmp;
            while ((tmp = reader.readLine()) != null)
                sb.append(tmp);
            return Optional.of(sb).map(StringBuilder::toString);

        }


    }

    /**
     * 保存文件内容到本地
     *
     * @param applicationName
     * @param content
     * @return
     */
    public static void saveFile(String applicationName, String content) throws IOException {

        createFile(applicationName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(getLocalConfigDictionary(applicationName)))) {
            fileOutputStream.write(content.getBytes("UTF-8"));
        }

    }

    /**
     * 文件存放目录
     *
     * @param applicationName
     * @return
     */
    private static String getLocalConfigDictionary(String applicationName) {
        return LOCAL_STORE_BASE_PATH + File.separator + applicationName + LOCAL_SUFFIX;
    }

    /**
     * 创建文件
     *
     * @param applicationName
     * @throws IOException
     */
    private static void createFile(String applicationName) throws IOException {
        File file = new File(getLocalConfigDictionary(applicationName));
        if (file.exists()) return;
        File dictionary = new File(LOCAL_STORE_BASE_PATH);
        dictionary.mkdirs();
        file.createNewFile();

    }


}
