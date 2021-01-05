package com.cl.pulllyric;

import com.cl.pulllyric.lyric.KuGou;
import com.cl.pulllyric.utils.OutUtils;
import com.cl.pulllyric.utils.StringUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: CarterCL
 * @date: 2020/12/30 21:32
 * @description:
 */
public class App {
    private static final HashSet<String> EX_NAME_SET;

    private static int success = 0;

    private static List<String> warnNameList;
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
            OutUtils.error("参数不能为空");
            return;
        }
        String songFolderPath = args[0];
        String lyricFolderPath = args.length >= 2 ? args[1] : args[0];

        // 获取文件夹中所有音乐文件名
        List<String> filenameList = getFilenameList(songFolderPath);
        if (filenameList.isEmpty()) {
            OutUtils.warn("文件夹内无歌曲文件");
            return;
        }

        OutUtils.info("共" + filenameList.size() + "歌曲文件，开始下载歌词……");
        failedNameList = new ArrayList<>(filenameList.size());
        warnNameList = new ArrayList<>(filenameList.size());

        for (String filename : filenameList) {
            pullLyric(filename, lyricFolderPath);
            TimeUnit.MILLISECONDS.sleep(300);
        }

        OutUtils.out("------------------------------------------------------------");
        OutUtils.out("完成执行，共" + filenameList.size() + "，成功" + success + "，失败" + failedNameList.size());
        OutUtils.out("------------------------------------------------------------");
        if (!failedNameList.isEmpty()) {
            OutUtils.out("下载歌词失败：");
            failedNameList.forEach(OutUtils::out);
        }

        if (!warnNameList.isEmpty()) {
            OutUtils.out("歌词可能存在问题：");
            warnNameList.forEach(OutUtils::out);
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
     * 获取歌词
     *
     * @param filename   歌曲文件名
     * @param folderPath 歌词存放位置
     * @throws IOException IO异常
     */
    private static void pullLyric(String filename, String folderPath) throws IOException {
        String[] infoArr = filename.split("-");
        String songName = infoArr[1].trim();
        String artistName = infoArr[0].trim();

        String lyric = KuGou.queryLyric(songName, artistName);
        if (StringUtils.isEmpty(lyric)) {
            OutUtils.warn("歌曲：" + filename + "|查询失败，尝试只查询歌曲名……（注意：此方法查询的歌词可能不准确）");
            lyric = KuGou.queryLyric(songName, null);
            if (StringUtils.isEmpty(lyric)) {
                failedNameList.add(filename);
                OutUtils.warn("歌曲：" + filename + "|歌词下载失败");
                return;
            }
            warnNameList.add(filename);
        }
        Files.writeString(Paths.get(folderPath, filename + ".lrc"), lyric, StandardCharsets.UTF_8);
        OutUtils.info("歌曲：" + filename + "|歌词下载成功");
        success++;
    }
}
