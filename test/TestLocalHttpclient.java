import com.sumnear.commons.IResult;
import com.sumnear.service.local.HttpclientService;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * ${DESCRIPTION}
 *
 * @authore sumnear
 * @create 2018-11-27 8:48
 */

public class TestLocalHttpclient
{
    private static HttpclientService httpclientService = new HttpclientService();

    @Test
    public void upload()
    {
        String url = "http://dcs.yozosoft.com/testUpload";
        String filePath = "D:\\壁纸\\1.jpg";
        IResult<String> result = httpclientService.upload(url, filePath);
        System.out.println(result.isSuccess());
        System.out.println(result.getData());
    }

    @Test
    public void doGet()
    {
        String url = "http://www.baidu.com/s";
        Map<String, String> params = new HashMap<>();
        params.put("wd", "文档预览");
        IResult<String> result = httpclientService.doGet(url, params);
        System.out.println(result.isSuccess());
        System.out.println(result.getData());
    }

    @Test
    public void doPost()
    {
        String url = "http://dcs.yozosoft.com/convert";
        Map<String, String> params = new HashMap<>();
        params.put("convertType", "1");
        params.put("inputDir", "31a6cbe0-471c-4998-b7f7-05165faf80c1/1.jpg");
        IResult<String> result = httpclientService.doPost(url, params);
        System.out.println(result.isSuccess());
        System.out.println(result.getData());
    }


    @Test
    public void downLoad()
    {
        String url = "http://dcs.yozosoft.com/example/doc/doctest.docx";
        String destFilePath = "C:\\Users\\Near\\Desktop\\1.docx";
        IResult<String> result = httpclientService.downLoad(url,destFilePath );
        System.out.println(result.isSuccess());
        System.out.println(result.getData());
    }
}
