package com.sumnear.service.spring;

import com.sumnear.commons.DefaultResult;
import com.sumnear.commons.IResult;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;


@Service("httpApiService")
public class HttpApiService
{
    private static final String UTF_8 = "UTF-8";

    @Autowired
    private CloseableHttpClient httpClient;


    public IResult<String> httpGetRequest(String url)
    {
        HttpGet httpGet = new HttpGet(url);
        return getResult(httpGet);
    }

    public IResult<String> httpGetRequest(String url, Map<String, Object> params) throws URISyntaxException
    {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);
        HttpGet httpGet = new HttpGet(ub.build());
        return getResult(httpGet);
    }

    public IResult<String> httpGetRequest(String url, Map<String, Object> headers, Map<String, Object> params)
            throws URISyntaxException
    {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        return getResult(httpGet);
    }

    public IResult<String> httpPostRequest(String url)
    {
        HttpPost httpPost = new HttpPost(url);
        return getResult(httpPost);
    }

    public IResult<String> httpPostRequest(String url, Map<String, Object> params) throws UnsupportedEncodingException
    {
        HttpPost httpPost = new HttpPost(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost);
    }

    public IResult<String> httpPostRequest(String url, Map<String, Object> params, Map<String, Object> headers)
            throws UnsupportedEncodingException
    {
        HttpPost httpPost = new HttpPost(url);

        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost);
    }


    /**
     * 提交json数据
     *
     * @param url
     * @param json
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public IResult<String> httpPostJsonRequest(String url, String json)
    {
        // 创建http POST请求
        HttpPost httpPost = new HttpPost(url);
        if (json != null) {
            // 构造一个请求实体
            StringEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
            // 将请求实体设置到httpPost对象中
            httpPost.setEntity(stringEntity);
        }
        return getResult(httpPost);
    }
    private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params)
    {
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
        }

        return pairs;
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
        return getResult(httpPost);
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
    public IResult<String> downLoad(String url, String destFilePath) throws URISyntaxException
    {
        return this.downLoad(url, destFilePath, null, null);
    }

    public IResult<String> downLoad(String url, String destFilePath,Map<String, Object> params,
                                    Map<String, String> headers) throws URISyntaxException
    {
        //构建GET请求头
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);
        HttpGet httpGet = new HttpGet(ub.build());
        for (Map.Entry<String, String> param : headers.entrySet()) {
            httpGet.addHeader(param.getKey(), param.getValue());
        }
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


    /**
     * 处理Http请求
     *
     * @param request
     * @return
     */
    private IResult<String> getResult(HttpRequestBase request)
    {
        CloseableHttpResponse response = null;
        String result = null;
        // 设置请求参数 使用默认
        // request.setConfig(requestConfig);
        try {
            response = httpClient.execute(request);
            // response.getStatusLine().getStatusCode();
            IResult<String> statusResult = assertStatus(response);
            if (!statusResult.isSuccess()) {
                return DefaultResult.failResult(request.getURI() + statusResult.getMessage());
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // long len = entity.getContentLength();// -1 表示长度未知
                result = EntityUtils.toString(entity);
            }
            return DefaultResult.successResult(result);
        } catch (IOException e) {
            e.printStackTrace();
            return DefaultResult.failResult(request.getURI() + "网络出错 " + e.getMessage());
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            request.releaseConnection();
        }
    }
    /**
     * 强验证必须是200状态否则报异常
     *
     * @param res
     * @throws HttpException
     */
    private static IResult<String> assertStatus(HttpResponse res)
    {
        if (StringUtils.isEmpty(res)) {
            return DefaultResult.failResult("http响应对象为null");
        }
        if (StringUtils.isEmpty(res.getStatusLine())) {
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

    /**
     * 构建自定义RequestConfig
     */
    private static RequestConfig buildRequestConfig(int socketTimeout, int connectionTimeout)
    {
        // 设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectionTimeout).build();
        return requestConfig;
    }
}
