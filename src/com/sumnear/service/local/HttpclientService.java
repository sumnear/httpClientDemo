package com.sumnear.service.local;


import com.sumnear.commons.DefaultResult;
import com.sumnear.commons.IResult;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class HttpclientService
{
    private static final int DEFAULT_POOL_MAX_TOTAL = 200;     //连接池中连接最大数量
    private static final int DEFAULT_POOL_MAX_PER_ROUTE = 200;   //连接池中同一路由最大连接数
    private static final int DEFAULT_CONNECT_REQUEST_TIMEOUT = 500;  //从连接池获取连接的超时时间
    private static final int DEFAULT_CONNECT_TIMEOUT = 500;   //tcp连接超时时间
    private static final int DEFAULT_SOCKET_TIMEOUT = 2000;  // 数据交互超时时间 tcp io的读写超时时间

    /**
     * httpclient读取内容时使用的字符集
     */
    private static final String CONTENT_CHARSET = "UTF-8";

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final Charset GBK = Charset.forName("GBK");

    // 连接池的最大连接数
    private final int maxTotal;
    // 连接池按route配置的最大连接数
    private final int maxPerRoute;
    // tcp connect的超时时间
    private final int connectTimeout;
    // 从连接池获取连接的超时时间
    private final int connectRequestTimeout;
    // tcp io的读写超时时间
    private final int socketTimeout;

    private PoolingHttpClientConnectionManager httpPoolManager;      //http连接池管理

//    private CloseableHttpClient httpClient;                       //httpclient

    private RequestConfig requestConfig;

    private IdleConnectionMonitorThread idleThread;     //自动回收空闲连接

    public HttpclientService()
    {
        this(
                HttpclientService.DEFAULT_POOL_MAX_TOTAL,
                HttpclientService.DEFAULT_POOL_MAX_PER_ROUTE,
                HttpclientService.DEFAULT_CONNECT_TIMEOUT,
                HttpclientService.DEFAULT_CONNECT_REQUEST_TIMEOUT,
                HttpclientService.DEFAULT_SOCKET_TIMEOUT
        );
    }

    public HttpclientService(
            int maxTotal,
            int maxPerRoute,
            int connectTimeout,
            int connectRequestTimeout,
            int socketTimeout
    )
    {

        this.maxTotal = maxTotal;
        this.maxPerRoute = maxPerRoute;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.connectRequestTimeout = connectRequestTimeout;

        //构建公用 PoolingHttpClientConnectionManager
        httpPoolManager = buildPoolingHttpClientConnectionManager(maxTotal, maxPerRoute);
        //构建公用RequestConfig
        requestConfig = buildRequestConfig(connectTimeout, socketTimeout, connectRequestTimeout);
        //构建公用httpClient，默认为连接池模式
//        httpClient = buildHttpClient(true, httpPoolManager, requestConfig);
        //启动清理线程
        idleThread = new IdleConnectionMonitorThread(this.httpPoolManager);
        idleThread.start();
    }

    public IResult<String> doGet(String url)
    {
        return this.doGet(url, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
    }

    public IResult<String> doGet(String url, Map<String, String> params)
    {
        return this.doGet(url, params, Collections.EMPTY_MAP);
    }

    public IResult<String> doGet(String url, Map<String, String> params, Map<String, String> headers)
    {
        return this.doGet(url, params, headers, CONTENT_CHARSET);
    }

    public IResult<String> doGet(String url, Map<String, String> params, Map<String, String> headers, String charset)
    {
        //构建GET请求头
        HttpGet httpGet = buildHttpGet(url, params, headers);
        return getResponseEntity(httpGet, charset);
//        CloseableHttpClient httpClient = buildHttpClient(true, httpPoolManager, requestConfig);
//        CloseableHttpResponse response = null;
//        try {
//            response = httpClient.execute(httpGet);
//            // 强验证必须是200状态否则失败
//            IResult<String> statusResult = assertStatus(response);
//            if (!statusResult.isSuccess()) {
//                return DefaultResult.failResult(statusResult.getMessage());
//            }
//            // 获取内容
//            HttpEntity entity = response.getEntity();
//            String returnStr = null;
//            if (entity != null) {
//                returnStr = EntityUtils.toString(entity, charset);
//            }else{
//                returnStr = null;
//            }
//            return DefaultResult.successResult(returnStr);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return DefaultResult.failResult(e.getMessage());
//        } finally {
//            if (response != null) {
//                try {
//                    response.close();
//                } catch (IOException e) {
//                }
//            }
//        }
    }

    public IResult<String> doPost(String url, Map<String, String> params)
    {
        return this.doPost(url, params, Collections.EMPTY_MAP);
    }

    public IResult<String> doPost(String url,
                                  Map<String, String> params,
                                  Map<String, String> headers
    )
    {
        return this.doPost(url,  params, headers,CONTENT_CHARSET);


    }

    public IResult<String> doPost(String url,Map<String, String> params,
                                  Map<String, String> headers,String charset
    )
    {
        HttpPost httpPost = buildHttpPost(url, params, headers);

        return getResponseEntity(httpPost, charset);
//        CloseableHttpClient httpClient = buildHttpClient(true, httpPoolManager, requestConfig);
//        CloseableHttpResponse response = null;
//        try {
//            response = httpClient.execute(httpPost);
//            IResult<String> statusResult = assertStatus(response);
//            if (!statusResult.isSuccess()) {
//                return DefaultResult.failResult(statusResult.getMessage());
//            }
//            HttpEntity entity = response.getEntity();
//            String returnStr ;
//            if (entity != null) {
//                returnStr = EntityUtils.toString(entity, charset);
//            }else{
//                returnStr = null;
//            }
//            return DefaultResult.successResult(returnStr);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return DefaultResult.failResult("网络出错");
//        } finally {
//
//            if (response != null) {
//                try {
//                    response.close();
//                } catch (IOException e) {
//                }
//            }
//        }
    }

    /**
     * 上传方法
     */
    public IResult<String> upload(String url, String filePath)
    {
        return   this.upload(url, filePath, null, null, null);
    }

    public IResult<String> upload(String url, String filePath,
                                  Map<String, String> headers,
                                  Map<String, String> params, String charset
    )
    {
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder mEntityBuilder = MultipartEntityBuilder.create();
        mEntityBuilder.addBinaryBody("file", new File(filePath));
        //相当于 input
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                mEntityBuilder.addPart(entry.getKey(), new StringBody(entry.getValue(), ContentType.create("text/plain", Consts.UTF_8)));
            }
        }
        httpPost.setEntity(mEntityBuilder.build());
        //设置header
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return getResponseEntity(httpPost, charset);
//        CloseableHttpClient httpClient = buildHttpClient(true, httpPoolManager, requestConfig);
//        CloseableHttpResponse response = null;
//        try {
//            response = httpClient.execute(httpPost);
//            IResult<String> statusResult = assertStatus(response);
//            if (!statusResult.isSuccess()) {
//                return DefaultResult.failResult(statusResult.getMessage());
//            }
//            HttpEntity entity = response.getEntity();
//            String returnStr ;
//            if (entity != null) {
//                returnStr = EntityUtils.toString(entity, charset);
//            }else{
//                returnStr = null;
//            }
//            return DefaultResult.successResult(returnStr);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return DefaultResult.failResult("网络出错");
//        } finally {
//
//            if (response != null) {
//                try {
//                    response.close();
//                } catch (IOException e) {
//                }
//            }
//        }
    }

    /**
     * 下载方法
     */
    public IResult<String> downLoad(String url, String destFilePath)
    {
        return this.downLoad(url, destFilePath, null, null);
    }

    public IResult<String> downLoad(String url, String destFilePath,Map<String, String> params, Map<String, String> headers)
    {
        //构建GET请求头
        HttpGet httpGet = buildHttpGet(url, params, headers);
        CloseableHttpClient httpClient = buildHttpClient(true, httpPoolManager, requestConfig);
        //response要把流写到本地
        CloseableHttpResponse response = null;
        FileOutputStream fout = null;
        InputStream in = null;
        try {
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            in = entity.getContent();
            File file = new File(destFilePath);
            fout = new FileOutputStream(file);
                int l = -1;
                byte[] tmp = new byte[1024];
                while ((l = in.read(tmp)) != -1) {
                    // 注意这里如果用OutputStream.write(buff)的话，图片会失真
                    fout.write(tmp, 0, l);
                }
            fout.flush();
            return DefaultResult.successResult("下载成功");
        } catch (Exception e) {
            e.printStackTrace();
            return DefaultResult.failResult("下载失败");
        }finally {
            try {
                fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                }
            }
        }

    }


    public void shutdown()
    {
        idleThread.shutdown();
    }

    // 监控有异常的链接
    private class IdleConnectionMonitorThread extends Thread
    {

        private final HttpClientConnectionManager connMgr;
        private volatile boolean exitFlag = false;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr)
        {
            this.connMgr = connMgr;
            setDaemon(true);
        }

        @Override
        public void run()
        {
            while (!this.exitFlag) {
                synchronized (this) {
                    try {
                        this.wait(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // 关闭失效的连接
                connMgr.closeExpiredConnections();
                // 可选的, 关闭30秒内不活动的连接
                connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
            }
        }

        public void shutdown()
        {
            this.exitFlag = true;
            synchronized (this) {
                notify();
            }
        }

    }

    /**
     * 获取execute结果
     */
    private IResult<String> getResponseEntity(HttpUriRequest request, String charset)
    {
        CloseableHttpClient httpClient = buildHttpClient(true, httpPoolManager, requestConfig);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            IResult<String> statusResult = assertStatus(response);
            if (!statusResult.isSuccess()) {
                return DefaultResult.failResult(statusResult.getMessage());
            }
            HttpEntity entity = response.getEntity();
            String returnStr;
            if (entity != null) {
                returnStr = EntityUtils.toString(entity, charset);
            } else {
                returnStr = null;
            }
            return DefaultResult.successResult(returnStr);
        } catch (IOException e) {
            e.printStackTrace();
            return DefaultResult.failResult(e.getMessage());
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                }
            }
        }
    }


    /**
     * 创建HttpClient
     *
     * @param isMultiThread
     * @return
     */
    private static CloseableHttpClient buildHttpClient(boolean isMultiThread,
                                                       PoolingHttpClientConnectionManager httpPoolManager, RequestConfig requestConfig)
    {
        CloseableHttpClient client;
        if (isMultiThread)
            client = HttpClients.custom().setConnectionManager(httpPoolManager)
                    .setDefaultRequestConfig(requestConfig)
                    .build();// HttpClientBuilder.create() == HttpClients.custom()
        else {
            client = HttpClients.custom().build();
        }
        // 设置代理服务器地址和端口
        // client.getHostConfiguration().setProxy("proxy_host_addr",proxy_port);
        return client;
    }

    /**
     * 构建公用RequestConfig
     *
     * @return
     */
    private static RequestConfig buildRequestConfig(Integer connectTimeout, Integer socketTimeout, Integer connectRequestTimeout)
    {
        // 设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)                     // 设置连接超时
                .setSocketTimeout(socketTimeout)                       // 设置读取超时
                .setConnectionRequestTimeout(connectRequestTimeout)    // 设置从连接池获取连接实例的超时
                .build();
        return requestConfig;
    }

    /**
     * 构建共用 PoolingHttpClientConnectionManager
     */
    private static PoolingHttpClientConnectionManager buildPoolingHttpClientConnectionManager(Integer maxTotal, Integer maxPerRoute)
    {
        PoolingHttpClientConnectionManager httpPoolManager = null;
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();

        httpPoolManager = new PoolingHttpClientConnectionManager(registry);
        httpPoolManager.setMaxTotal(maxTotal);
        httpPoolManager.setDefaultMaxPerRoute(maxPerRoute);
        return httpPoolManager;
    }


    /**
     * 构建httpPost对象
     */
    private static HttpPost buildHttpPost(String url, Map<String, String> params, Map<String, String> headers)
    {
        HttpPost httpPost = new HttpPost(url);
        // 配置请求headers
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }
        }
        //配置请求参数
        HttpEntity he = null;
        if (params != null) {
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            for (String key : params.keySet()) {
                formparams.add(new BasicNameValuePair(key, params.get(key)));
            }
            he = new UrlEncodedFormEntity(formparams, UTF_8);
            httpPost.setEntity(he);
        }
        // 在RequestContent.process中会自动写入消息体的长度，自己不用写入，写入反而检测报错
        // setContentLength(post, he);
        return httpPost;

    }

    /**
     * 构建httpGet对象
     */
    private static HttpGet buildHttpGet(String url, Map<String, String> params, Map<String, String> headers)
    {
        StringBuffer uriStr = new StringBuffer(url);
        if (params != null) {
            List<NameValuePair> ps = new ArrayList<NameValuePair>();
            for (String key : params.keySet()) {
                ps.add(new BasicNameValuePair(key, params.get(key)));
            }
            uriStr.append("?");
            uriStr.append(URLEncodedUtils.format(ps, UTF_8));
        }
        HttpGet httpGet = new HttpGet(uriStr.toString());
        // 设置header信息
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return httpGet;
    }

    /**
     * 设置成消息体的长度 setting MessageBody length
     *
     * @param httpMethod
     * @param he
     */
    private static void setContentLength(HttpRequestBase httpMethod, HttpEntity he)
    {
        if (he == null) {
            return;
        }
        httpMethod.setHeader(HTTP.CONTENT_LEN, String.valueOf(he.getContentLength()));
    }


    /**
     * 强验证必须是200状态否则报异常
     *
     * @param res
     * @throws HttpException
     */
    private static IResult<String> assertStatus(HttpResponse res)
    {
        if (res == null || "".equals(res)) {
            return DefaultResult.failResult("http响应对象为null");
        }
        if (res.getStatusLine() == null) {
            return DefaultResult.failResult("http响应对象的状态为null");
        }
        int stat = res.getStatusLine().getStatusCode();
        switch (stat) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_CREATED:
            case HttpStatus.SC_ACCEPTED:
            case HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION:
            case HttpStatus.SC_NO_CONTENT:
            case HttpStatus.SC_RESET_CONTENT:
            case HttpStatus.SC_PARTIAL_CONTENT:
            case HttpStatus.SC_MULTI_STATUS:
                return DefaultResult.successResult();
            default:
                return DefaultResult.failResult("服务器响应状态异常:" + stat);
        }
    }

    public static void main(String[] args) throws ClientProtocolException, IOException, URISyntaxException
    {
        HttpclientService service = new HttpclientService();
        String url = "http://www.baidu.com/s";

        Map<String, String> params = new HashMap<>();
        params.put("wd", "sumnear");
//        IResult<String> re = service.doGet(url,params);
//        if(re.isSuccess()){
//            System.out.println(re.getData());
//        }
        IResult<String> re2 = service.doPost(url, params);
        if (re2.isSuccess()) {
            System.out.println(re2.getData());
        }
    }

}
