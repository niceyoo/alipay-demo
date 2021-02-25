### 一、前言

[支付宝支付—沙箱环境使用](https://www.cnblogs.com/niceyoo/p/12196095.html)

[支付宝新版SDK-PC端扫码支付 + 手机浏览器H5支付](#)「**本文**」

PC端扫码支付，其实就是就是 **电脑网站支付**，本文基于支付宝沙箱环境，不了解的可以看一下上边的链接。

PS：本文是基于支付宝新版 SDK 重写的，用法非常简单，之前的旧版 SDK 就别参考了~

本文环境：

- JDK1.8
- IDEA 2020.3.2
- SpringBoot 2.2.2
- alipay-easysdk 2.2.0
- 支付宝沙箱环境

### 二、引入依赖

源码地址：

创建一个 SpringBoot 应用（有基础的建议直接看↑源码），在 pom 中引入如下依赖：

```
<!-- alipay -->
<dependency>
    <groupId>com.alipay.sdk</groupId>
    <artifactId>alipay-easysdk</artifactId>
    <version>2.2.0</version>
</dependency>
```

在 application.yml 文件中添加如下配置：

```
## 支付宝配置
alipay:
  # 应用ID
  appId: 你的appid
  # 应用私钥
  privateKey: 你的应用私钥privateKey
  # 支付宝公钥，注意不是生成的应用公钥
  publicKey: 你的支付宝公钥publicKey
  #支付网关配置，这一项是写死的，正式环境是openapi.alipay.com
  gateway: openapi.alipaydev.com
  # 支付宝前台跳转地址
  returnUrl: http://ngrok.sscai.club/html/success
  # 支付宝后台通知地址
  notifyUrl: http://ngrok.sscai.club/api/alipay/notify_url
  # 支付宝前台手机网页支付中途取消跳转地址
  errorUrl: http://ngrok.sscai.club/html/error
```

这些参数在哪里获取的？

##### 1、appId（应用ID）

https://open.alipay.com/platform/appDaily.htm?tab=info

具体见下图所示：

![](https://gitee.com/niceyoo/blog/raw/master/img/image-20210208111034103.png)

##### 2、publicKey、privateKey（支付宝公钥、应用私钥）

windows工具：[点击此链接](https://ideservice.alipay.com/ide/getPluginUrl.htm?clientType=assistant&platform=win&channelType=WEB)

macos工具：[点击此链接](https://ideservice.alipay.com/ide/getPluginUrl.htm?clientType=assistant&platform=mac&channelType=WEB)

这块参考这篇文章的第三节吧：https://www.cnblogs.com/niceyoo/p/12196095.html

![](https://gitee.com/niceyoo/blog/raw/master/img/image-20210208111918801.png)

##### 3、gateway（支付网关配置）

测试环境：openapi.alipaydev.com

正式环境：openapi.alipay.com

##### 4、returnUrl、notifyUrl、errorUrl（跳转地址）

这三个地址在测试环境，可以使用内网映射的地址，尤其是 notifyUrl ，这个是支付宝的回调 URl，必须外网可访问。

内网映射可以使用免费的 **natapp** 。

natapp链接：https://natapp.cn/

具体用法可自行百度。

> 内网映射就是将内网映射到外网，实现通过外网链接访问本地的效果。
> 例如上方的：**http://ngrok.sscai.club** 指向我本地的 **http://127.0.0.1:port**

### 三、代码部分

##### 1、支付宝参数配置类

上面定义的参数有了，接下来就是如何使用了。新增一个配置类(AlipayConfig)：

```
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author : niceyoo
 * @version : 1.0
 * @title : AlipayApplication
 * @description : 支付宝支付配置类
 * @copyright :
 * @date :
 */
@Component
public class AlipayConfig implements ApplicationRunner {

    /**
     * 应用id
      */
    @Value("${alipay.appId}")
    private String appId;

    /**
     * 私钥
     */
    @Value("${alipay.privateKey}")
    private String privateKey;

    /**
     * 公钥
     */
    @Value("${alipay.publicKey}")
    private String publicKey;

    /**
     * 支付宝网关
     */
    @Value("${alipay.gateway}")
    private String gateway;

    /**
     * 支付成功后的接口回调地址，不是回调的友好页面，不要弄混了
     */
    @Value("${alipay.notifyUrl}")
    private String notifyUrl;

    /**
     *  项目初始化事件
     * */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        /**初始化支付宝SDK**/
        Factory.setOptions(getOptions());
        System.out.println("**********支付宝SDK初始化完成**********");
    }

    private Config getOptions() {
        /**这里省略了一些不必要的配置，可参考文档的说明**/
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = this.gateway;
        config.signType = "RSA2";
        config.appId = this.appId;
        /** 为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中 **/
        config.merchantPrivateKey = this.privateKey;
        /** 注：如果采用非证书模式，则无需赋值上面的三个证书路径，改为赋值如下的支付宝公钥字符串即可 **/
        config.alipayPublicKey = this.publicKey;
        /** 可设置异步通知接收服务地址（可选）**/
        config.notifyUrl = notifyUrl;
        return config;
    }
}
```

PS：详细的参数文档：https://opendocs.alipay.com/apis/00y8k9

##### 2、模拟订单Controller类

```
/**
 * @author : niceyoo
 * @version : 1.0
 * @title : AlipayController
 * @description : 订单支付控制层-测试代码
 * @copyright :
 * @date :
 */
@RestController
@RequestMapping("/api/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    /**
     * @description: 支付宝电脑网页支付
     * @param subject: 订单名称
     * @param total: 金额
     * @return java.lang.String
     */
    @PostMapping("/page")
    public String page(String subject, String total) {
        subject = "测试支付";
        total = "1000";
        return alipayService.page(subject, total);
    }

    /**
     * @description: 支付宝手机网页支付
     * @param subject: 订单名称
     * @param total: 金额
     * @return java.lang.String
     */
    @PostMapping("/wap")
    public String wap(String subject, String total) {
        subject = "测试支付";
        total = "1000";
        return alipayService.wap(subject, total);
    }

    /**
     * @description: 支付宝异步回调
     * @param request: 请求
     * @return java.lang.String
     */
    @PostMapping("/notify_url")
    public String notify_url(HttpServletRequest request) throws Exception {

        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            System.out.println("=========支付宝异步回调========");

            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            /** 支付宝验签 **/
            if (Factory.Payment.Common().verifyNotify(params)) {
                /** 验签通过 **/
                System.out.println("交易名称: " + params.get("subject"));
                System.out.println("交易状态: " + params.get("trade_status"));
                System.out.println("支付宝交易凭证号: " + params.get("trade_no"));
                System.out.println("商户订单号: " + params.get("out_trade_no"));
                System.out.println("交易金额: " + params.get("total_amount"));
                System.out.println("买家在支付宝唯一id: " + params.get("buyer_id"));
                System.out.println("买家付款时间: " + params.get("gmt_payment"));
                System.out.println("买家付款金额: " + params.get("buyer_pay_amount"));
            }
        }
        return "success";
    }

    /**
     * @description: 支付宝退款
     * @param outTradeNo: 商家订单号
     * @param refundAmount: 退款金额(不能大于交易金额)
     * @return java.lang.String
     */
    @PostMapping("/refund")
    public String refund(String outTradeNo, String refundAmount) {
        return alipayService.refund(outTradeNo, refundAmount);
    }

}
```

订单 Controller 类主要模拟了四个方法：

- page：网站支付
- wap：手机支付
- notify_url：支付回调方法
- refund：退款调用方法

##### 3、模拟订单Service类

```
/**
 * @author : niceyoo
 * @version : 1.0
 * @title : AlipayService
 * @description :
 * @copyright :
 * @date : 2021/2/8 12:27
 */
@Service
public class AlipayService {

    /**
     * 支付成功后要跳转的页面
     */
    @Value("${alipay.returnUrl}")
    private String returnUrl;

    /**
     * 支付宝前台手机网页支付中途取消跳转地址
     */
    @Value("${alipay.errorUrl}")
    private String errorUrl;

    /**
     *
     * @param subject
     * @param total
     * @return
     */
    public String page(String subject, String total) {

        try {
            AlipayTradePagePayResponse response = Factory.Payment
                /** 选择电脑网站 **/
                .Page()
                /** 调用支付方法(订单名称, 商家订单号, 金额, 成功页面) **/
                .pay(subject, OrderUtil.getOrderNo(), total, returnUrl);

            return response.body;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * @param subject
     * @param total
     * @return
     */
    public String wap(String subject, String total) {

        try {
            AlipayTradeWapPayResponse response = Factory.Payment
                /** 选择手机网站 **/
                .Wap()
                /** 调用支付方法(订单名称, 商家订单号, 金额, 中途退出页面, 成功页面) **/
                .pay(subject, OrderUtil.getOrderNo(), total, errorUrl, returnUrl);

            return response.body;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * @param outTradeNo
     * @param refundAmount
     * @return
     */
    public String refund(String outTradeNo, String refundAmount) {
        try {
            AlipayTradeRefundResponse response = Factory.Payment
                .Common()
                /** 调用交易退款(商家订单号, 退款金额) **/
                .refund(outTradeNo, refundAmount);

            if (response.getMsg().equals("Success")) {return "退款成功";}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "退款失败";
    }

}
```

Service 主要调用支付宝支付提供的便捷方法，大家可以看一下方法中的写法。

用到的模拟生成订单号的工具类：

```
public class OrderUtil {

    /**
     *  根据时间戳生成订单号
     * */
    public static String getOrderNo () {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        LocalDateTime localDateTime = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
        return df.format(localDateTime);
    }

}
```

### 4、前端截图以及支付演示

由于使用了 thymeleaf，增加了三个前端界面：

- 首页调用支付
- 支付成功界面
- 支付失败界面

首页主要就是 form 表单的提交，调用了两个方法：网页支付、手机网站支付

```
<form enctype="multipart/form-data" action="/api/alipay/page" method="post">
    <button type="submit">电脑确认支付</button>
</form>
<form enctype="multipart/form-data" action="/api/alipay/wap" method="post">
    <button type="submit">手机确认支付</button>
</form>
```

支付时需要用到沙箱账号，即支付宝沙箱版，下载链接如下，一个我的下载链接，一个官方的，哪个下载快用哪个。

[https://niceyoo.lanzous.com/i7QFulh3uri](https://niceyoo.lanzous.com/i7QFulh3uri)

[https://sandbox.alipaydev.com/user/downloadApp.htm](https://sandbox.alipaydev.com/user/downloadApp.htm)

![](https://gitee.com/niceyoo/blog/raw/master/img/image-20210208141123299.png)

##### 4.1、点击电脑确认支付：PC端支付截图

![](https://gitee.com/niceyoo/blog/raw/master/img/image-20210225232841917.png)

![](https://gitee.com/niceyoo/blog/raw/master/img/image-20210208154557579.png)

##### 4.2、点击手机确认支付：手机端支付截图

PS：进行支付测试，注意付款要用沙箱环境提供的支付宝<b>APK</b>。且环境必须手机浏览器环境下，非PC端操作。

![](https://gitee.com/niceyoo/blog/raw/master/img/image-20210225231643477.png)

支付成功后会支付宝会调回调方法：（下图是我的代理调用截图）

![](https://gitee.com/niceyoo/blog/raw/master/img/image-20210225231928250.png)

至此，从以上看来，其实不难发现支付宝支付是非常简单的。

ok，这篇文章就到这结束了，上边并没有详细介绍接口调用、参数说明等，详细介绍请查看官方文档：[https://docs.open.alipay.com/270/105902/](https://docs.open.alipay.com/270/105902/)

