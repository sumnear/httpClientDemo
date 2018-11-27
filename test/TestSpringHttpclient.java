import com.sumnear.commons.IResult;
import com.sumnear.service.spring.HttpApiService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations ={"classpath:spring.xml"})
@WebAppConfiguration
public abstract class TestSpringHttpclient
{
    @Autowired
    private HttpApiService httpApiService ;

    @Test
    public void upload()
    {
        String url = "http://dcs.yozosoft.com/testUpload";
        String filePath = "D:\\壁纸\\1.jpg";
        IResult<String> result = httpApiService.upload(url, filePath);
        System.out.println(result.isSuccess());
        System.out.println(result.getData());
    }

    @Test
    public void doGet() throws URISyntaxException
    {
        String url = "http://dcs.yozosoft.com/convert";
        Map<String, Object> params = new HashMap<>();
        params.put("convertType", "1");
        params.put("inputDir", "31a6cbe0-471c-4998-b7f7-05165faf80c1/1.jpg");
        IResult<String> result = httpApiService.httpGetRequest(url, params);
        System.out.println(result.isSuccess());
        System.out.println(result.getData());
    }

    @Test
    public void doPost() throws UnsupportedEncodingException
    {
        String url = "http://dcs.yozosoft.com/convert";
        Map<String, Object> params = new HashMap<>();
        params.put("convertType", "1");
        params.put("inputDir", "31a6cbe0-471c-4998-b7f7-05165faf80c1/1.jpg");
        IResult<String> result = httpApiService.httpPostRequest(url, params);
        System.out.println(result.isSuccess());
        System.out.println(result.getData());
    }


    @Test
    public void downLoad() throws URISyntaxException
    {
        String url = "http://dcs.yozosoft.com/example/doc/doctest.docx";
        String destFilePath = "C:\\Users\\Near\\Desktop\\1.docx";
        IResult<String> result = httpApiService.downLoad(url,destFilePath );
        System.out.println(result.isSuccess());
        System.out.println(result.getData());
    }
}
