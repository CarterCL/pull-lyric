package com.cl.pulllyric.lyric;

import com.alibaba.fastjson.JSON;
import com.cl.pulllyric.response.KuGouGetAccessKeyResponse;
import com.cl.pulllyric.response.KuGouGetHashResponse;
import com.cl.pulllyric.response.KuGouGetLyricResponse;
import com.cl.pulllyric.utils.HttpUtils;
import com.cl.pulllyric.utils.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author: CarterCL
 * @date: 2021/1/5 21:58
 * @description:
 */
public class KuGou {

    /**
     * 查询歌词
     *
     * @param songName   歌曲名
     * @param artistName 歌手名
     * @return 歌词内容
     */
    public static String queryLyric(String songName, String artistName) throws IOException {
        String queryName = StringUtils.isEmpty(artistName) ? songName : (songName.trim() + " - " + artistName);

        String nameEncode = URLEncoder.encode(queryName, StandardCharsets.UTF_8.displayName()).replaceAll("\\+", "%20");
        String url = "http://mobilecdn.kugou.com/api/v3/search/song?keyword=" + nameEncode + "&page=1&pagesize=1";
        String responseStr = HttpUtils.get(url);
        KuGouGetHashResponse hashResponse = JSON.parseObject(responseStr, KuGouGetHashResponse.class);

        if (hashResponse == null
                || hashResponse.getData() == null
                || hashResponse.getData().getInfo() == null
                || hashResponse.getData().getInfo().length == 0) {
            return null;
        }
        // 获取到歌曲hash
        String hash = hashResponse.getData().getInfo()[0].getHash();

        url = "http://lyrics.kugou.com/search?ver=1&man=yes&client=pc&keyword=" + nameEncode + "&hash=" + hash;
        responseStr = HttpUtils.get(url);
        KuGouGetAccessKeyResponse accessKeyResponse = JSON.parseObject(responseStr, KuGouGetAccessKeyResponse.class);
        if (accessKeyResponse.getCandidates() == null || accessKeyResponse.getCandidates().length == 0) {
            return null;
        }
        // 获取到歌曲accessKey和id
        String id = accessKeyResponse.getCandidates()[0].getId();
        String accessKey = accessKeyResponse.getCandidates()[0].getAccesskey();

        url = "http://lyrics.kugou.com/download?ver=1&client=pc&id=" + id + "&accesskey=" + accessKey + "&fmt=lrc&charset=utf8";
        responseStr = HttpUtils.get(url);
        KuGouGetLyricResponse lyricResponse = JSON.parseObject(responseStr, KuGouGetLyricResponse.class);
        if (lyricResponse == null || StringUtils.isEmpty(lyricResponse.getContent())) {
            return null;
        }

        // 将歌词进行Base64解码，得到歌词文本
        return new String(Base64.getDecoder().decode(lyricResponse.getContent().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
}
