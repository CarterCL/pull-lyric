package com.cl.pulllyric;

import com.alibaba.fastjson.JSON;
import com.cl.pulllyric.response.KuGouGetAccessKeyResponse;
import com.cl.pulllyric.response.KuGouGetHashResponse;
import com.cl.pulllyric.response.KuGouGetLyricResponse;
import com.cl.pulllyric.utils.HttpUtils;
import com.cl.pulllyric.utils.StringUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 */
public class App {
    private static final HashSet<String> EX_NAME_SET;

    private static int success = 0;

    private static List<String> failedNameList;

    // 初始化音乐文件类型
    static {
        EX_NAME_SET = new HashSet<>(16);
        EX_NAME_SET.add("mp3");
        EX_NAME_SET.add("flac");
        EX_NAME_SET.add("ape");
        EX_NAME_SET.add("wav");
        EX_NAME_SET.add("mid");
        EX_NAME_SET.add("wma");
        EX_NAME_SET.add("aac");
        EX_NAME_SET.add("dff");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args == null || args.length < 1) {
            System.out.println("参数不能为空");
            return;
        }

        List<String> filenameList = getFilenameList(args[0]);
        if (filenameList.isEmpty()) {
            System.out.println("文件夹内无歌曲文件");
            return;
        }

        System.out.println("共" + filenameList.size() + "歌曲文件，开始下载歌词");
        failedNameList = new ArrayList<>(filenameList.size());

        Map<String, String> nameHashMap = getNameHashMap(filenameList);

        generateLyric(nameHashMap, args[0]);

        System.out.println("完成执行，共" + filenameList.size() + "，成功" + success + "，失败" + failedNameList.size());
        if(!failedNameList.isEmpty()){
            System.out.println("下载失败歌曲:");
            failedNameList.forEach(System.out::println);
        }
    }

    /**
     * 获取文件夹中所有的文件名
     *
     * @param folderPath 文件夹
     * @return 文件名List
     */
    private static List<String> getFilenameList(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("文件夹内无文件");
            System.exit(0);
        }
        List<String> filenameList = new ArrayList<>(files.length);
        for (File file : files) {
            String name = file.getName();
            if (EX_NAME_SET.contains(FilenameUtils.getExtension(file.getName()))) {
                filenameList.add(FilenameUtils.getBaseName(name));
            }
        }
        return filenameList;
    }

    /**
     * 根据歌曲名获取kugou的hash
     *
     * @param filenameList 文件名list
     * @return 歌曲名-hash Map
     */
    private static Map<String, String> getNameHashMap(List<String> filenameList) throws IOException, InterruptedException {
        Map<String, String> map = new HashMap<>(filenameList.size());
        for (String name : filenameList) {
            String[] arr = name.split("-");
            String songName = arr[0].trim() + " - " + arr[1].trim();

            // 根据关键字，获取歌曲Hash
            String nameEncode = URLEncoder.encode(songName, StandardCharsets.UTF_8.displayName()).replaceAll("\\+", "%20");
            String url = "http://mobilecdn.kugou.com/api/v3/search/song?keyword=" + nameEncode + "&page=1&pagesize=1";
            String responseStr = HttpUtils.get(url);
            KuGouGetHashResponse response = JSON.parseObject(responseStr, KuGouGetHashResponse.class);
            if (response != null
                    && Integer.valueOf(1).equals(response.getStatus())
                    && StringUtils.isEmpty(response.getError())) {
                if (response.getData() != null
                        && response.getData().getInfo() != null
                        && response.getData().getInfo().length > 0) {

                    String hash = response.getData().getInfo()[0].getHash();
                    map.put(songName, hash);
                    TimeUnit.MILLISECONDS.sleep(500);
                    continue;
                }
            }

            TimeUnit.MILLISECONDS.sleep(500);
            System.out.println("歌曲:" + name + "|查询HASH失败|" + responseStr);
            failedNameList.add(name);
        }
        return map;
    }

    /**
     * 根据hash获取歌词
     *
     * @param nameHashMap 歌曲名-hash Map
     * @param folderPath  存放歌词的文件夹
     */
    private static void generateLyric(Map<String, String> nameHashMap, String folderPath) throws IOException, InterruptedException {
        for (Map.Entry<String, String> entry : nameHashMap.entrySet()) {
            String[] arr = entry.getKey().split("-");
            String songName = arr[0].trim() + " - " + arr[1].trim();
            // 根据歌曲Hash，获取歌词候选，取候选第1个，拿到accesskey 和 id
            String nameEncode = URLEncoder.encode(songName, StandardCharsets.UTF_8.displayName()).replaceAll("\\+", "%20");
            String url = "http://lyrics.kugou.com/search?ver=1&man=yes&client=pc&keyword=" + nameEncode + "&hash=" + entry.getValue();
            String responseStr = HttpUtils.get(url);
            KuGouGetAccessKeyResponse response = JSON.parseObject(responseStr, KuGouGetAccessKeyResponse.class);
            if (Integer.valueOf(200).equals(response.getErrcode())
                    && response.getCandidates() != null
                    && response.getCandidates().length > 0) {
                // 根据歌词的accesskey 和 id，获取歌词内容
                url = "http://lyrics.kugou.com/download?ver=1&client=pc&id="
                        + response.getCandidates()[0].getId()
                        + "&accesskey=" + response.getCandidates()[0].getAccesskey() + "&fmt=lrc&charset=utf8";
                responseStr = HttpUtils.get(url);
                KuGouGetLyricResponse lyricResponse = JSON.parseObject(responseStr, KuGouGetLyricResponse.class);
                if (Integer.valueOf(200).equals(lyricResponse.getStatus()) && !StringUtils.isEmpty(lyricResponse.getContent())) {
                    String content = lyricResponse.getContent();
                    // 将歌词内容进行Base64解码，写入到歌曲同名的lrc文件
                    String lyric = new String(Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
                    Files.writeString(Paths.get(folderPath, entry.getKey() + ".lrc"), lyric, StandardCharsets.UTF_8);
                    System.out.println("歌曲:" + entry.getKey() + "|歌词下载完成");
                    success++;
                    TimeUnit.MILLISECONDS.sleep(500);
                    continue;
                }
            }
            TimeUnit.MILLISECONDS.sleep(500);
            System.out.println("歌曲:" + entry.getKey() + "|歌词下载失败|" + responseStr);
            failedNameList.add(entry.getKey());
        }
    }
}
