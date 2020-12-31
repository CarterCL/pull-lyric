package com.cl.pulllyric.utils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.IdleConnectionEvictor;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: CarterCL
 * @date: 2020/12/30 21:32
 * @description:
 */
public class HttpUtils {

    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * 最大连接数
     */
    private static final Integer MAX_TOTAL = 50;
    /**
     * 每个路由最大连接数
     */
    private static final Integer DEFAULT_MAX_PER_ROUTE = 10;

    /**
     * 从连接池获取连接超时时间
     */
    private static final Integer CONNECTION_REQUEST_TIMEOUT = 10 * 1000;
    /**
     * 建立连接超时时间
     */
    private static final Integer CONNECT_TIMEOUT = 10 * 1000;
    /**
     * 读取超时时间
     */
    private static final Integer SOCKET_TIMEOUT = 10 * 1000;

    private static volatile HttpClient client;

    private static volatile RequestConfig config;


    /**
     * GET请求
     *
     * @param url            请求地址
     * @param queryStringMap 参数Map
     * @return 响应字符串
     * @throws IOException IO异常
     */
    public static String get(String url, Map<String, String> queryStringMap) throws IOException {
        HttpGet get = new HttpGet();
        if (queryStringMap != null && queryStringMap.size() > 0) {
            String queryString = map2QueryString(queryStringMap);
            url = url + "?" + queryString;
        }

        get.setURI(URI.create(url));
        HttpResponse response = doExecute(get);
        if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return EntityUtils.toString(response.getEntity(), DEFAULT_ENCODING);
        }
        return null;
    }

    /**
     * GET请求
     *
     * @param url 请求地址
     * @return 响应字符串
     * @throws IOException IO异常
     */
    public static String get(String url) throws IOException {
        return get(url, null);
    }

    /**
     * POST请求-提交表单
     *
     * @param url     请求地址
     * @param formMap 表单Map
     * @return 响应字符串
     * @throws IOException IO异常
     */
    public static String postForm(String url, Map<String, String> formMap) throws IOException {
        HttpPost post = new HttpPost(url);
        if (formMap != null && formMap.size() > 0) {
            List<NameValuePair> paramList = map2ParamList(formMap);
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, DEFAULT_ENCODING);
            entity.setContentType(ContentType.APPLICATION_FORM_URLENCODED.toString());
            post.setEntity(entity);
        }
        HttpResponse response = doExecute(post);
        if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return EntityUtils.toString(response.getEntity(), DEFAULT_ENCODING);
        }
        return null;
    }

    /**
     * POST请求-提交Json
     *
     * @param url  请求地址
     * @param json Json字符串
     * @return 响应字符串
     * @throws IOException IO异常
     */
    public static String postJson(String url, String json) throws IOException {
        HttpPost post = new HttpPost(url);
        if (json != null && json.length() > 0) {
            StringEntity entity = new StringEntity(json, DEFAULT_ENCODING);
            entity.setContentType(ContentType.APPLICATION_JSON.toString());
            post.setEntity(entity);
        }
        HttpResponse response = doExecute(post);
        if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return EntityUtils.toString(response.getEntity(), DEFAULT_ENCODING);
        }
        return null;
    }

    /**
     * 执行请求
     *
     * @param request 请求实体
     * @return 响应实体
     * @throws IOException IO异常
     */
    public static HttpResponse doExecute(HttpRequestBase request) throws IOException {
        HttpClient client = getClientInstance();
        request.setConfig(getConfigInstance());

        return client.execute(request);
    }

    private static String map2QueryString(Map<String, String> paramMap) {

        return paramMap
                .entrySet()
                .stream()
                .map(e -> String.join("=", e.getKey(), e.getValue())).collect(Collectors.joining("&"));
    }

    private static List<NameValuePair> map2ParamList(Map<String, String> formMap) {
        return formMap
                .entrySet()
                .stream()
                .map(e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    private static HttpClient getClientInstance() {
        if (client == null) {
            synchronized (HttpUtils.class) {
                if (client == null) {
                    // init
                    PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
                    manager.setMaxTotal(MAX_TOTAL);
                    manager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
                    client = HttpClients.createMinimal(manager);
                    // 定期清理无效请求 线程
                    new IdleConnectionEvictor(manager, 5, TimeUnit.SECONDS).start();
                }
            }
        }
        return client;
    }

    private static RequestConfig getConfigInstance() {
        if (config == null) {
            synchronized (HttpUtils.class) {
                if (config == null) {
                    config = RequestConfig.custom()
                            .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                            .setConnectTimeout(CONNECT_TIMEOUT)
                            .setSocketTimeout(SOCKET_TIMEOUT)
                            .build();
                }
            }
        }
        return config;
    }
}
