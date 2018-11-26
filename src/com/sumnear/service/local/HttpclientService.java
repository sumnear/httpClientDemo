package com.sumnear.service.local;


import com.sumnear.commons.DefaultResult;
import com.sumnear.commons.IResult;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
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
        return this.doGet(url, Collections.EMPTY_MAP, params);
    }

    public IResult<String> doGet(String url, Map<String, String> headers, Map<String, String> params)
    {
        return this.doGet(url,headers, params,CONTENT_CHARSET);
    }

    public IResult<String> doGet(String url, Map<String, String> headers, Map<String, String> params, String charset)
    {
        //构建GET请求头
        HttpGet httpGet =buildHttpGet(url, params,headers);
        CloseableHttpClient httpClient = buildHttpClient(true, httpPoolManager, requestConfig);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            // 强验证必须是200状态否则失败
            IResult<String> statusResult = assertStatus(response);
            if (!statusResult.isSuccess()) {
                return DefaultResult.failResult(statusResult.getMessage());
            }
            // 获取内容
            HttpEntity entity = response.getEntity();
            String returnStr = null;
            if (entity != null) {
                returnStr = EntityUtils.toString(entity, charset);
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

    public IResult<String> doPost(String apiUrl, Map<String, String> params)
    {
        return this.doPost(apiUrl, Collections.EMPTY_MAP, params);
    }

    public IResult<String> doPost(String apiUrl,
                         Map<String, String> headers,
                         Map<String, String> params
    )
    {
        return this.doPost(apiUrl, headers, params,CONTENT_CHARSET);


    }
    public IResult<String> doPost(String apiUrl,
                         Map<String, String> headers,
                         Map<String, String> params,String charset
    )
    {
        HttpPost httpPost = buildHttpPost(apiUrl, params,headers);
        CloseableHttpClient httpClient = buildHttpClient(true, httpPoolManager, requestConfig);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            IResult<String> statusResult = assertStatus(response);
            if (!statusResult.isSuccess()) {
                return DefaultResult.failResult(statusResult.getMessage());
            }
            HttpEntity entity = response.getEntity();
            String returnStr ;
            if (entity != null) {
                returnStr = EntityUtils.toString(entity, charset);
            }else{
                returnStr = null;
            }
            return DefaultResult.successResult(returnStr);
        } catch (IOException e) {
            e.printStackTrace();
            return DefaultResult.failResult("网络出错");
        } finally {
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
    private static HttpPost buildHttpPost(String url, Map<String, String> params,Map<String, String> headers)
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
    private static HttpGet buildHttpGet(String url, Map<String, String> params,Map<String, String> headers)
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

        Map<String,String> params = new HashMap<>();
        params.put("wd", "sumnear");
//        IResult<String> re = service.doGet(url,params);
//        if(re.isSuccess()){
//            System.out.println(re.getData());
//        }
        IResult<String> re2 = service.doPost(url,params);
        if(re2.isSuccess()){
            System.out.println(re2.getData());
        }


//		System.out.println("ddd");
//        IResult<String> re = doGet("http://xx12sdwwwwsds.com");
//				IResult<String> re = simpleGetInvoke("http://fscdntel.115.com/files/c200/0/ICJHpQabCXEwInDoLRm9FZW3mIf057sHvRkiMDKJ/%E5%9C%A8%E7%BA%BF%E9%A2%84%E8%A7%88%E4%BA%A7%E5%93%81%E5%AF%B9%E6%AF%94%E5%88%86%E6%9E%90%E8%A1%A8%282%29.docx?t=1473748817&u=3663376018-591688994-c9u65dqwv6osgdz9h&s=51200&k=AuOmBhnOJt8wxHbuQANLIg",

        // IResult<String> re = simpleGetInvoke("http://localhost:21000/getserver", new
        // HashMap<String, String>());
//		System.out.println(re.getData());
//		String result = re.getData();
//		Map<String, Object> map = JSON.parseObject(result, HashMap.class);
//		System.out.println(map.get("gt"));
//		StringBuffer sb = new StringBuffer("http://").append(map.get("ip")).append("?gt=").append(map.get("gt"))
//				.append("&sign=").append("&sign=").append(map.get("sign")).append("&ContentType=").append("0")
//				.append("downLoadUrl=").append("downLoadUrl=");
//		System.out.println(sb);

    }

}
