# 阅文微信分销接口说明

## 一、通用参数说明

以下参数在所有api调用均时传入，以下各个api使用时不再重复描述。
所有的请求和响应数据编码皆为utf-8格式，url里的所有参数值签名时无需提前做任何编码，但发起请求时需做urlencode编码。

| 参数 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| email | string | 是 | 合作方登录阅文开放平台的邮箱 |
| version | number | 是 | 接口版本号，如无特殊说明，当前版本号为1 |
| timestamp | number | 是 | unix时间戳（秒），接口允许时间差为 ±300 秒 |
| sign | string | 是 | 参数签名，签名算法见目录第二章 |

## 二、签名参数sign算法

为了保障数据安全，调用任何一个API都需要进行签名验证。
目前支持的签名算法为：MD5(sign_method=md5)，签名过程如下：
1. 将请求参数（包含公共参数与业务参数但不包含sign参数）进行首字母以ASCII方式升序排列，对于相同字母则使用下个字母做二次排序，字母序为从左到右，以此类推。
2. 排序后的结果按照参数名（key）参数值（value）的次序进行字符串拼接，拼接处不包含任何其他字符（格式：key1value1key2value2…keyNvalueN）
3. 在拼接完成的字符串头部拼接appSecret，完成签名字符串的组装。
4. 最后对字符串进行MD5签名，将得到的MD5加密密文转为大写，即为sign值。

### PHP签名示例：

```php
//当前接口需要的完整参数
$requestParams = [
    'appflag' => 'aaa',
    'version' => 1,
    'timestamp' => 1516611600,
    'cbid' => '8626591604595403',
];
function createSign($requestParams = [], $salt = ''){
    $sign = $requestParams['sign'];
    unset($requestParams['sign']);
    ksort($requestParams, SORT_REGULAR);
    $splicedString = '';
    foreach ($requestParams as $paramKey => $paramValue) {
        if(!in_array($paramKey, $excludeKeys)){
            $splicedString .= $paramKey . $paramValue;
        }
    }
    $splicedString = $salt . $splicedString;
    return  strtoupper(md5($splicedString));
}    
```

### GO签名示例

```go
requestParams := map[string]string{
    "appflag": "aaa",
    "version": "1",
    "timestamp": "1516611600",
    "cbid": "8626591604595403",
};

sign := createSign(context.Background(), requestParams, "xxxx")

// checkSign 校验签名信息
func createSign(ctx context.Context, params map[string]string, apiSecret string) string {
	var keys = make([]string, 0, len(params))
	for k := range params {
		if k == "sign" {
			continue
		}
		keys = append(keys, k)
	}
	sort.Strings(keys)
	signature := apiSecret
	for _, k := range keys {
		v := params[k]
		signature += k + v
	}
	md5Ctx := md5.New()
	md5Ctx.Write([]byte(signature))
	signString := md5Ctx.Sum(nil)
	return strings.ToUpper(hex.EncodeToString(signString))
}
```

### JAVA签名示例

```java
public static String generateMd5Sign(Map<String, String> requestParams, String appSecret) {
    TreeMap orderMap = new TreeMap();
    orderMap.putAll(requestParams);
    // orderMap.remove("sign");
    StringBuilder splicedString = new StringBuilder(appSecret);
    orderMap.forEach((k, v) -> {
        splicedString.append(k).append(v);
    });
    return DigestUtils.md5Hex(splicedString.toString()).toUpperCase();
}
```

## 三、修订记录

| 日期 | 修订版本 | 描述 |
| :--- | :--- | :--- |
| 2023.04.11 | v1.0.0 | 微信分销接口文档v1.0.0 |
| 2023.04.25 | v1.0.1 | 4.3 接口返回字段新增：corp_id，kf_id，external_userid |
| 2023.05.11 | v1.0.2 | 新增4.11和4.12活动链接接口 |
| 2023.05.16 | v1.0.3 | 新增4.13 corpid转换接口 |
| 2023.05.23 | v1.0.4 | 5.3 巨量引擎自行回传接口调整请求方式和参数 |
| 2023.06.20 | v1.0.5 | 4.2 接口返回字段新增【归因媒体】和【归因AID】 |
| 2023.07.04 | v1.0.6 | 4.2 接口【上报状态】字段调整 |
| 2023.08.24 | v1.0.7 | 新增4.14创建模版消耗活动链接接口 |
| 2024.01.04 | v1.0.8 | 4.7、4.9、4.10新增企微强关 |
| 2024.11.29 | v1.0.9 | 4.9、4.10新增小程序相关字段 |
| 2024.12.05 | v1.0.10 | 新增4.15获取参与过活动的用户列表 |
| 2024.12.18 | v1.0.11 | 4.7、4.8新增小程序相关字段 |
| 2024.12.27 | v1.0.12 | 4.7、4.8、4.9、4.10新增小程序相关字段 |
| 2025.06.19 | v1.0.13 | 4.7、4.8、4.9、4.10新增小程序相关字段 |
| 2025.07.02 | v.1.0.14 | 4.18新增老链接转换接口 |

## 四、接口说明

### 4.1、获取产品标识列表（appflag）

**简要描述：**
获取指定时间内更新的产品标识列表
注： 1.此接口有调用频率限制，相同查询条件每分钟仅能请求一次
	2.单页返回100条数据
	3.查询的时间段不能超过30天

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/GetAppList`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| start_time | 否 | number | 查询起始时间戳(UNIX时间戳，单位秒)，不传默认当天0点，不早于2019年1月1日 |
| end_time | 否 | number | 查询结束时间戳(UNIX时间戳，单位秒)，不传默认当前时间 |
| page | 否 | number | 分页，默认为1 |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| page | number | 查询的页码 |
| total_count | number | 当前查询条件的总数据行数 |
| list | array | 结果集数组 |
| app_name | string | 产品名称 |
| appflag | string | 产品唯一标识 |
| wx_appid | string | 关联的公众号 wx_appid |

**返回值示例：**

```json
{
	"code": 0,
	"msg": "成功",
	"data": {
		"page": 1,
		"total_count": 1,
		"list": [{
			"app_name": "测试帐号1",
            "appflag": "djfxxxxxx",
		},{
			"app_name": "测试帐号2",
            "appflag": "djfxxxxxx",
		}]
	}
}
```

### 4.2、获取充值记录

**简要描述：**
获取指定时间内用户充值明细
注：1.此接口有调用频率限制，相同查询条件每分钟仅能请求一次
	2.单页返回100条数据
 3.查询的时间段不能超过24小时

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/Recharge/GetRechargeLog`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| start_time | 是 | number | 查询起始时间戳(UNIX时间戳，单位秒) |
| end_time | 是 | number | 查询结束时间戳(UNIX时间戳，单位秒) |
| page | 否 | number | 分页，默认为1 |
| page_size | 否 | number | 单页查询数据量，最大100条，默认10条 |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| guid | 否 | number | 阅文用户ID，传此字段时必传对应的appflag |
| openid | 否 | string | 用户ID，传此字段时必传对应的appflag |
| order_id | 否 | string | 微信订单号 |
| order_status | 否 | number | 订单状态：1待支付、2已支付 |
| last_min_id | 否 | number | 上一次查询返回的min_id，分页大于1时必传 |
| last_max_id | 否 | number | 上一次查询返回的max_id，分页大于1时必传 |
| total_count | 否 | number | 上一次查询返回的total_count，分页大于1时必传 |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| page | number | 查询的页码 |
| total_count | number | 当前查询条件的总数据行数 |
| min_id | number | 最小ID用于分页查询 |
| max_id | number | 最大ID用于分页查询 |
| list | array | 结果集数组 |
| app_name | string | 产品名称 |
| appflag | string | 产品唯一标识 |
| amount | string | 充值金额（单位：元） |
| yworder_id | number | 阅文订单号 |
| order_id | string | 微信支付订单号 |
| order_time | string | 下单时间（创建订单时间） |
| pay_time | string | 下单支付时间（实际充值到账时间） |
| order_status | number | 订单状态：1待支付、2已支付 |
| order_type | number | 订单类型：1充值、2包年、3道具 |
| openid | string | 用户ID |
| guid | number | 阅文用户ID |
| nickname | string | 用户昵称 |
| sex | number | 用户性别：1男、2女、0未知 |
| reg_time | string | 用户注册时间 |
| sub_time | string | 用户最新关注时间，未关注为空 |
| channel_id | number | 外推链接ID |
| original_channel_id | string | 外推链接原始ID |
| channel_name | string | 外推链接名称 |
| inner_channel_id | number | 内推链接ID |
| inner_channel_name | string | 内推链接名称 |
| book_id | number | 下单时作品ID |
| book_name | string | 下单时作品名称 |
| wx_appid | string | 微信公众号appid |
| report_status | number | 媒体平台上报状态：0未上报，1成功，2失败（v1.0.6已废弃） |
| item_name | string | 下单时所属活动名称 |
| order_channel | number | 充值渠道ID 支付宝：1 微信：2抖音：28 |
| ad_attributions（v1.0.5新增） | array | 广告归因列表 |
| site_label（v1.0.5新增） | string | 归因媒体网站名 |
| site（v1.0.5新增） | number | 归因媒体网站 1、广点通 2、微信广告 3、巨量引擎 4、快手 5、UC 6、腾讯广告 7、百度 8、OPPO 9、VIVO 10、新浪微博 11、巨量星图 |
| aid（v1.0.5新增） | string | 归因aid |
| report_status（v1.0.6新增） | number | 0上报成功 3上报失败可手动重试 6不可手动上报 |
| report_status_name（v1.0.6新增） | string | 对report_status扩展说明 |
| report_type（v1.0.6新增） | number | 1系统上报 2手动上报 |

**返回值示例：**

```json
{
    "code": 0,
    "data": {
        "page": 1,
        "total_count": 1,
        "min_id": 7241930787602301026,
        "max_id": 7241930787602301026,
        "list": [
            {
                "app_name": "非常爱读书",
                "appflag": "xxxxxxxx",
                "amount": "0.08",
                "yworder_id": "xxxxxxxx",
                "order_id": "xxxxxxxxx",
                "order_time": "2023-06-07 21:09:55",
                "pay_time": "2023-06-07 21:10:06",
                "order_status": 2,
                "order_type": 1,
                "openid": "o3_xxxxxx",
                "guid": 644600001439,
                "nickname": "",
                "reg_time": "2023-04-19 12:32:20",
                "channel_id": 0,
                "original_channel_id": "",
                "channel_name": "",
                "order_channel": 2,
                "book_id": "0",
                "book_name": "",
                "report_status": 2,
                "item_name": "",
                "wx_appid": "xxxxxxxx",
                "sub_time": "",
                "sex": 0,
                "inner_channel_id": 0,
                "inner_channel_name": "",
                "ad_attributions": [
                    {
                        "site_label": "广点通",
                        "site": 1,
                        "aid": ""
                    },
                    {
                        "site_label": "百度",
                        "site": 7,
                        "aid": ""
                    }
                ]
            }
        ]
    },
    "message": "success"
}
```

### 4.3、获取用户信息

**简要描述：**
获取指定时间内更新的用户数据
注：1.此接口有调用频率限制，相同查询条件每分钟仅能请求一次
2.单页返回100条数据
3.时间跨度不能超过一周

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/User/GetUserInfo`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| start_time | 否 | number | 查询起始时间戳(UNIX时间戳，单位秒)，传openid时忽略，其他情况不传默认当天0点 |
| end_time | 否 | number | 查询结束时间戳(UNIX时间戳，单位秒)，不可大于当前北京时间值（开始结束时间间隔不能超过7天），传openid时忽略，其他情况不传默认当前时间 |
| page | 否 | number | 分页，默认为1 |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| openid | 否 | string | 用户ID |
| next_id | 否 | string | 上一次查询返回的next_id，分页大于1时必传 |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| next_id | string | 下一页id |
| total_count | number | 当前查询条件的总数据行数 |
| appflag | string | 产品标识 |
| area_name | string | 主appid映射的作品小程序名称 |
| app_name | string | 产品名称 |
| openid | string | 用户ID |
| guid | number | 阅文用户id |
| nickname | string | 用户昵称 |
| charge_amount | number | 累计充值金额（单位：分） |
| charge_num | number | 累计充值次数 |
| create_time | string | 用户注册时间 |
| subscribe_time | string | 用户最新关注时间，未关注为空 |
| vip_end_time | string | 包年结束时间 |
| is_subscribe | number | 用户是否关注：1是，0否 |
| sex | number | 用户性别：1男，2女，0未知 |
| channel_id | number | 外推链接ID（用户来源） |
| original_channel_id | string | 外推链接原始ID（用户来源） |
| channel_name | string | 外推链接名称（用户来源） |
| book_id | number | 外推作品ID（用户来源） |
| book_name | string | 外推作品名称（用户来源） |
| update_time | string | 用户更新时间 |
| os_type | string | 操作系统名称：android、iOS |
| wx_version | String | 微信版本号：8.0.11.1980 |
| external_userid | array | 外部用户标识(array of String)已废弃 |
| external_userinfo | array | 外部用户信息列表 |
| corp_id | string | 合作机构ID |
| kf_id | string | 客服ID |
| external_userid | string | 外部用户标识 |

**返回值示例：**

```json
{
    "code": 0,
    "data": {
        "next_id": "DnF1ZXJ5VGhlbkZldGNo2JydndwAAAAAyiVsOFkxGZUY2Jyd1FxYWFTTHpZWnZ2QXp3",
        "total_count": 1,
        "list": [
            {
                "charge_amount": 110,
                "charge_num": 2,
                "guid": 643601455,
                "openid": "oA9sq5YDaFY-LnFx0g",
                "nickname": "用户001455",
                "sex": 0,
                "is_subscribe": 0,
                "appflag": "hongtao",
                "area_name": "作品001",
                "app_name": "小剧场",
                "create_time": "2022-08-05 16:21:32",
                "subscribe_time": "2022-10-09 15:55:24",
                "vip_end_time": "1970-01-01 08:00:00",
                "update_time": "2023-02-01 16:32:46",
                "seq_time": "1970-01-01 08:00:00",
                "reflux_time": "1970-01-01 08:00:00",
                "channel_id": 4,
                "original_channel_id": "spread_22104b8ao7160",
                "channel_name": "2022-10-09 15:20:13",
                "book_id": "24084001",
                "book_name": "酒托",
                "os_type": "iOS 10.0.1",
                "manufacturer": "",
                "external_userinfo": [{
				    "corp_id": "xxxx",
				    "kf_id": "xxx",
				    "external_userid": "xxxx"
			    }]
            }
        ]
    },
    "message": "success"
}
```

### 4.4、获取消费记录

**简要描述：**
获取指定时间内用户消费明细数据
注：1.此接口有调用频率限制，相同查询条件每分钟仅能请求一次
2.单页返回100条数据
3.只可查询半年以内记录，且时间跨度不可超24小时

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/Recharge/GetConsumeLog`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| start_time | 否 | number | 查询起始时间戳(UNIX时间戳，单位秒)（仅支持最近半年的消费记录查询、不能超过24小时)，不传默认当天0点 |
| end_time | 否 | number | 查询结束时间戳(UNIX时间戳，单位秒)，不传默认当前时间 |
| page | 否 | number | 分页，默认为1 |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| openid | 否 | string | 用户ID（openid、guid必传其一） |
| guid | 否 | number | 阅文用户ID（openid、guid必传其一） |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| total_count | number | 当前查询条件的总数据行数 |
| appflag | string | 产品标识 |
| openid | string | 用户ID |
| guid | number | 阅文用户ID |
| order_id | string | 订单号 |
| consume_id | string | 消费订单号id |
| worth_amount | number | 消费有价币总金额 |
| free_amount | number | 消费免费币总金额 |
| consume_time | string | 消费时间 |
| book_id | number | 订阅作品id |
| book_name | string | 订阅作品名称 |
| chapter_id | number | 订阅作品章节id |
| chapter_name | string | 订阅作品章节名称 |

**返回值示例：**

```json
{
    "code": 0,
    "data": {
        "total_count": 1,
        "list": [
            {
                "appflag": "hongtao",
                "openid": "oA9sq5a8eiGal8",
                "guid": 6451749,
                "order_id": "f92e20f944c6514b729",
                "consume_id": "",
                "consume_time": "2023-02-06 11:28:31",
                "worth_amount": 110,
                "free_amount": 290,
                "book_id": "23868501",
                "book_name": "开挂人生",
                "chapter_id": "640737963506",
                "chapter_name": "04.mp4"
            }
        ]
    },
    "message": "success"
}
```

### 4.5、获取作品信息

**简要描述：**
获取指定作品信息
注：1.此接口有调用频率限制，相同查询条件每分钟仅能请求一次

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/Book/GetNovelInfo`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| book_id | 是 | number | 作品唯一标识 |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| book_id | number | 作品唯一标识 |
| book_name | string | 书名 |
| author_name | string | 作者名称 |
| all_words | number | 作品当前全部正文章节的字数 |
| update_time | string | 最近一个章节的更新时间 |
| charge_chapter | number | 收费起始章节 |

**返回示例：**

```json
{
    "code": 0,
    "data": {
        "book_id": "1455704",
        "book_name": "起点579809",
        "author_name": "太阳",
        "all_words": 25869,
        "update_time": "2019-09-17 12:55:07",
        "charge_chapter": 3
    },
    "message": "success"
}
```

### 4.6、获取作品免费章节列表

**简要描述：**
获取指定作品的免费章节列表
注：此接口有调用频率限制，相同查询条件每分钟仅能请求一次

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/Book/GetFreeChapterList`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| book_id | 是 | number | 作品唯一标识 |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| book_id | number | 作品ID |
| chapter_list | array | 章节列表 |
| chapter_id | number | 章节ID |
| chapter_name | string | 章节名 |
| chapter_seq | number | 章节序号 |

**返回值示例：**

```json
{
    "code": 0,
    "data": {
        "chapter_list": [
            {
                "chapter_id": "64073770000",
                "chapter_name": "01.mp4",
                "chapter_seq": 1
            },
            {
                "chapter_id": "640700000",
                "chapter_name": "02.mp4",
                "chapter_seq": 2
            }
        ],
        "book_id": "238689760000"
    },
    "message": "success"
}
```

### 4.7、创建作品推广链接

**简要描述：**
创建指定作品指定章节的推广链接
注：此接口有调用频率限制，相同条件每分钟仅能请求一次

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/Book/AddH5Spread`

**请求方式：**
POST

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| book_id | 是 | number | 作品唯一标识 |
| chapter_id | 是 | number | 章节ID |
| name | 是 | String | 渠道名称 |
| channel_type | 是 | Int | 推广类型：1外部，2内部 |
| force_style | 是 | Int | 强关设置：1不设置强关，2 主动关注，3 强制关注 |
| bottom_QR | 是 | Int | 底部关注：1是，0否 |
| force_chapter | 是 | String | 强关章节序号 |
| cost | 否 | float | 推广成本(单位：元，保留小数点后两位) |
| wxwork_force_style（v1.0.8） | 否 | Int | 企微强关设置：1不设置强关，2 主动关注，3 强制关注 |
| wxwork_force_chapter（v1.0.8） | 否 | string | 企微强关章节序号（企微强关设置传值时，此值必传） |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| channel_id | number | 链接ID |
| original_channel_id | string | 原始链接id |
| channel_url | string | 推广链接 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| origin_miniapp_id | string | 绑定的小程序原始ID |
| miniapp_text_link | string | 绑定的小程序页面文字链接 |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_channel_list | array | 多小程序场景下，绑定的小程序列表 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| miniapp_text_link | string | 绑定的小程序页面文字链接 |
| origin_miniapp_id | string | 绑定的小程序的原小程序ID |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_name | string | 绑定的小程序名称 |

**返回值示例：**

```json
{
    "code": 0,
    "data": {
        "channel_id": 10042,
        "original_channel_id": "spread_21552579i8sh1679",
        "channel_url": "https://wx44.wxcp.qidian.com/ygf/DIg"
    },
    "message": "success"
}
```

### 4.8、创建页面推广链接

**简要描述：**
创建指定作品页面的推广链接
注：此接口有调用频率限制，相同查询条件每分钟仅能请求一次

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/Book/AddPageSpread`

**请求方式：**
POST

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| name | 是 | string | 渠道名称 |
| channel_type | 是 | number | 推广类型：1外部，2内部 |
| page_type | 是 | number | 1 书城首页<br>2 排行榜(自动)<br>3 排行榜(男生)<br>4 排行榜(女生)<br>5 充值页<br>6 最近阅读(列表)<br>7 限免专区<br>8 签到页面<br>9 个人中心<br>10 置顶公众号引导<br>11 新用户充值活动页<br>12 最近阅读(最近阅读作品)<br>13 意见反馈 14 活动中心<br>15 福利页(仅IAA小程序支持，商户后台需开启福利页配置，如未开启用户通过推广链接会进入首页) |
| cost | 否 | number | 推广成本(单位：元，保留小数点后两位） |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| channel_id | number | 链接ID |
| original_channel_id | string | 原始链接ID |
| channel_url | string | 推广链接 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| origin_miniapp_id | string | 绑定的小程序原始ID |
| miniapp_text_link | string | 绑定的小程序页面文字链接 |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_channel_list | array | 多小程序场景下，绑定的小程序列表 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| miniapp_text_link | string | 绑定的小程序页面文字链接 |
| origin_miniapp_id | string | 绑定的小程序的原小程序ID |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_name | string | 绑定的小程序名称 |

**返回值示例：**

```json
{
    "code": 0,
    "data": {
        "channel_id": 100091745,
        "original_channel_id": "spread_249y2c3209",
        "channel_url": "https://w4ca339137.wxcp.qidian.com/fbm/I6"
    },
    "message": "success"
}
```

### 4.9、获取作品推广链接列表

**简要描述：**
获取指定时间内创建的推广链接明细
注：1.此接口有调用频率限制，相同查询条件每分钟仅能请求一次
       2.单页返回20条数据
3.查询的时间段不能超过24小时

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/Book/GetSpreadList`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| type | 是 | number | 链接类型：1页面链接，2作品链接 |
| channel_type | 是 | number | 推广类型：1外部，2内部 |
| recycle | 否 | number | 删除状态：1有效，0无效，默认1 |
| start_time | 否 | number | 查询起始时间戳，查询时间段内创建的链接，不能超过24小时，不传默认当天0点 |
| end_time | 否 | number | 查询结束时间戳，查询时间段内创建的链接，不能超过24小时，不传默认当前时间 |
| page | 否 | number | 当前页数，默认1 |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| total_count | number | 当前查询条件的数据总行数 |
| list | array | 推广链接列表 |
| channel_name | string | 链接名称 |
| channel_id | number | 链接ID |
| original_channel_id | string | 原始链接ID |
| channel_type | number | 推广类型：1外部，2内部 |
| page_name | string | 页面/作品信息 |
| appflag | string | 产品标识 |
| app_name | string | 产品名称 |
| create_time | string | 创建时间 |
| book_id | number | 作品ID |
| chapter_id | number | 章节ID |
| force_style | number | 关注提示：1不设置提示,  2高频提示，4单次提示 |
| force_chapter | number | 关注章节序号 |
| wxwork_force_style（v1.0.8） | number | 企微强关设置：1不设置提示,  2高频提示，4单次提示 |
| wxwork_force_chapter（v1.0.8） | number | 企微强关章节序号 |
| bottom_QR | Int | 底部二维码开关：1 显示；0 隐藏 |
| url | String | 推广长链接 |
| short_url | String | 推广短链接 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| miniapp_text_link | string | 绑定的小程序的文字链接 |
| origin_miniapp_id | string | 绑定的小程序原始ID |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_channel_list | array | 多小程序场景下，绑定的小程序列表 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| miniapp_text_link | string | 绑定的小程序页面文字链接 |
| origin_miniapp_id | string | 绑定的小程序的原小程序ID |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_name | string | 绑定的小程序名称 |

**返回值示例：**

```json
{
  "code": 0,
  "data": {
    "total_count": 2,
    "list": [
      {
        "channel_name": "测试",
        "channel_id": 100091742,
        "original_channel_id": "spread_29i8sh1679",
        "channel_type": 1,
        "page_name": "《神秘老公，晚上见！》第1章 24小时的效用（1）",
        "appflag": "wx7",
        "app_name": "爱读书",
        "create_time": "2023-03-27 14:15:52",
        "book_id": "3321770503",
        "chapter_id": "89169310730",
        "force_chapter": 1,
        "force_style": 1,
        "bottom_QR": 1,
        "url": "https://wx339137.wxcp.qidian.com/wx7/read.html?cbid=332177920503&ccid=8916931671130730&channel_id=spread_23032702i8sh1679",
        "short_url": "https://wx39137.wxcp.qidian.com/xdf/DIg"
      }
    ]
  },
  "message": "success"
}
```

### 4.10、获取作品推广链接详情

**简要描述：**
根据channel_id或original_channel_id获取推广链接明细
注：此接口有调用频率限制，相同查询条件每分钟仅能请求一次

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/Book/GetSpread`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| channel_id | 否 | number | 链接ID |
| original_channel_id | 否 | string | 原始链接ID，两者必传其一，channel_id优先级更高 |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| channel_name | string | 链接名称 |
| channel_id | number | 链接ID |
| original_channel_id | string | 原始链接id |
| channel_type | number | 推广类型：1外部，2内部 |
| type | number | 链接类型：1页面链接，2作品链接 |
| recycle | number | 删除状态：1有效，0无效 |
| page_name | string | 页面/作品信息 |
| appflag | string | 产品标识 |
| app_name | string | 产品名称 |
| create_time | string | 创建时间 |
| book_id | number | 作品ID |
| chapter_id | number | 章节ID |
| force_style | number | 关注提示：1不设置提示,  2高频提示，4单次提示 |
| force_chapter | number | 关注章节序号 |
| wxwork_force_style（v1.0.8） | number | 企微强关设置：1不设置提示,  2高频提示，4单次提示 |
| wxwork_force_chapter（v1.0.8） | number | 企微强关章节序号 |
| bottom_QR | Int | 底部二维码开关：1 显示；0 隐藏 |
| url | String | 推广长链接 |
| short_url | String | 推广短链接 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| miniapp_text_link | string | 绑定的小程序的文字链接 |
| origin_miniapp_id | string | 绑定的小程序原始ID |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_channel_list | array | 多小程序场景下，绑定的小程序列表 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| miniapp_text_link | string | 绑定的小程序页面文字链接 |
| origin_miniapp_id | string | 绑定的小程序的原小程序ID |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_name | string | 绑定的小程序名称 |

**返回值示例：**

```json
{
    "code": 0,
    "data": {
        "channel_name": "测试",
        "channel_id": 100091742,
        "original_channel_id": "spread_22579i8sh1679",
        "channel_type": 1,
        "type": 2,
        "recycle": 1,
        "page_name": "《神秘老公，晚上见！》第1章 24小时的效用（1）",
        "appflag": "wx7",
        "app_name": "爱读书",
        "create_time": "2023-03-27 14:15:52",
        "book_id": "33217420503",
        "chapter_id": "89169310730",
        "force_chapter": 1,
        "force_style": 1,
        "bottom_QR": 1,
        "url": "https://wxxxxxx.wxcp.qidian.com/wx7/read.html?cbid=332170420503&ccid=8916931671130730&channel_id=spread_552579i8sh1679",
        "short_url": "https://wxa339137.wxcp.qidian.com/pzq/Ig"
    },
    "message": "success"
}
```

### 4.11、创建活动链接

**简要描述：**
创建活动页推广链接
注：此接口有调用频率限制，相同条件每分钟仅能请求一次

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/Activity/AddActivitySpread`

**请求方式：**
POST

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| name | 是 | string | 链接名称 |
| channel_type | 是 | Int | 推广类型：1外部，2内部 |
| recharge_activity_id | 是 | Int | 活动id（可从后台>>活动中心>>微信分销充值活动 >> 列表获取ID值） |
| cost | 否 | float | 推广成本(单位：元，保留小数点后两位) |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| channel_id | number | 链接ID |
| original_channel_id | string | 原始链接id |
| channel_url | string | 推广链接 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| origin_miniapp_id | string | 绑定的小程序原始ID |
| miniapp_text_link | string | 绑定的小程序页面文字链接 |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_channel_list | array | 多小程序场景下，绑定的小程序列表 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| miniapp_text_link | string | 绑定的小程序页面文字链接 |
| origin_miniapp_id | string | 绑定的小程序的原小程序ID |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_name | string | 绑定的小程序名称 |

**返回值示例：**

```json
{
    "code": 0,
    "data": {
        "channel_id": 10042,
        "original_channel_id": "spread_2105118i8sh1679",
        "channel_url": "https://wx44.wxcp.qidian.com/ygf/DIg"
    },
    "message": "success"
}
```

### 4.12、获取活动链接列表

**简要描述：**
获取指定时间内创建的活动推广链接明细
注：1.此接口有调用频率限制，相同查询条件每分钟仅能请求一次
       2.单页返回20条数据
3.查询的时间段不能超过24小时

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/Activity/GetActivitySpreadList`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| channel_type | 是 | number | 推广类型：1外部，2内部 |
| recycle | 否 | number | 删除状态：1有效，0无效，默认1 |
| start_time | 否 | number | 查询起始时间戳，查询时间段内创建的链接，不能超过24小时，不传默认当天0点 |
| end_time | 否 | number | 查询结束时间戳，查询时间段内创建的链接，不能超过24小时，不传默认当前时间 |
| page | 否 | number | 当前页数，默认1 |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| total_count | number | 当前查询条件的数据总行数 |
| list | array | 推广链接列表 |
| channel_name | string | 链接名称 |
| channel_id | number | 链接ID |
| original_channel_id | string | 原始链接ID |
| channel_type | number | 推广类型：1外部，2内部 |
| recharge_activity_id | number | 活动id |
| recharge_activity_name | string | 活动名称 |
| appflag | string | 产品标识 |
| app_name | string | 产品名称 |
| create_time | string | 创建时间 |
| url | String | 推广长链接 |
| short_url | String | 推广短链接 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| miniapp_text_link | string | 绑定的小程序的文字链接 |
| origin_miniapp_id | string | 绑定的小程序原始ID |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_channel_list | array | 多小程序场景下，绑定的小程序列表 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| miniapp_text_link | string | 绑定的小程序页面文字链接 |
| origin_miniapp_id | string | 绑定的小程序的原小程序ID |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_name | string | 绑定的小程序名称 |

**返回值示例：**

```json
{
  "code": 0,
  "data": {
    "total_count": 2,
    "list": [
      {
        "channel_name": "测试活动",
        "channel_id": 100091742,
        "original_channel_id": "spread_2305118i8sh1679",
        "channel_type": 1,
        "recharge_activity_id": 232,
        "recharge_activity_name": "活动A",
        "appflag": "wx7",
        "app_name": "爱读书",
        "create_time": "2023-05-11 14:15:52",
        "url": "https://wx339137.wxcp.qidian.com/wx7/read.html?recharge_activity_id=232&channel_id=spread_2305118i8sh1679",
        "short_url": "https://wx39137.wxcp.qidian.com/xdf/DIg"
      }
    ]
  },
  "message": "success"
}
```

### 4.13、corpid转换接口

**简要描述：**
将阅文侧 corp_id 转换为实际微信侧 corp_id，用于在私域场景下调用微信相关接口
注：如不涉及微信接口调用或无需实际corp_id，可忽略此接口

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/User/ConvertCorpId`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| appflag | 是 | string | 产品标识 |
| corp_id | 是 | string | 阅文侧合作机构ID（可从4.3获取用户信息接口中获取） |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| corp_id | string | 微信侧实际企业ID |

**返回值示例：**

```json
{
    "code": 0,
    "data": {
        "corp_id": "wwxxxxxxxxxx"
    },
    "message": "success"
}
```

### 4.14、创建模版消耗活动链接

**简要描述：**
创建模版消耗活动链接接口
注：此接口有调用频率限制，相同条件每分钟仅能请求一次

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/Activity/AddConsumeSpread`

**请求方式：**
POST

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| name | 是 | string | 链接名称 |
| channel_type | 是 | Int | 推广类型：1外部，2内部 |
| activity_id | 是 | Int | 活动id（可从后台>>活动中心>>消耗活动 >> 列表获取ID值） |
| cost | 否 | float | 推广成本(单位：元，保留小数点后两位) |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| channel_id | number | 链接ID |
| original_channel_id | string | 原始链接id |
| channel_url | string | 推广链接 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| origin_miniapp_id | string | 绑定的小程序原始ID |
| miniapp_text_link | string | 绑定的小程序页面文字链接 |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_channel_list | array | 多小程序场景下，绑定的小程序列表 |
| miniapp_wxappid | string | 绑定的小程序wxappid |
| miniapp_page_path | string | 绑定的小程序页面路径 |
| miniapp_text_link | string | 绑定的小程序页面文字链接 |
| origin_miniapp_id | string | 绑定的小程序的原小程序ID |
| miniapp_url_link | string | 绑定的小程序页面urlLink |
| miniapp_name | string | 绑定的小程序名称 |

**返回值示例：**

```json
{
    "code": 0,
    "data": {
        "channel_id": 10042,
        "original_channel_id": "spread_2105118i8sh1679",
        "channel_url": "https://wx44.wxcp.qidian.com/ygf/DIg"
    },
    "message": "success"
}
```

### 4.15、获取参与过活动的用户列表

**简要描述：**
获取过指定活动的用户列表
注：此接口有调用频率限制，相同条件每分钟仅能请求一次

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/Activity/GetActivityUserList`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| start_time | 是 | number | 查询起始时间戳 |
| end_time | 是 | number | 查询结束时间戳 |
| page | 否 | number | 当前页数，默认1 |
| activity_id | 是 | Int | 活动id (多个活动id用逗号分隔，最多支持10个) |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| page | number | 页码 |
| count | number | 该页总记录数 |
| list | array | 用户列表 |
| guid | number | 用户guid |
| openid | string | 用户openid |

**返回值示例：**

```json
{
    "code": 0,
    "data": {
        "page": 1,
        "count": 1,
        "list": [
            {
                "guid": 10042000000,
                "openid": "xxxxxx"
            }
        ]
    },
    "message": "success"
}
```

### 4.16、获取Iaa广告统计数据

**简要描述：**
注：1. 此接口有调用频率限制，相同查询条件每分钟仅能请求一次
	2. 当天数据请在14点以后请求

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/getadstats`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| date | 是 | string | 统计数据的日期，格式yyyy-mm-dd |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| date | string | 统计数据的日期 |
| miniapp_wxappid | string | 小程序wxappid |
| miniapp_name | string | 小程序名称 |
| click_count | number | 点击数 |
| exposure_count | number | 曝光次数 |
| req_succ_count | number | 拉取次数 |
| income | number | 单日总广告收入(分) |
| ecpm | string | eCPM(分) |
| exposure_rate | string | 曝光率 |
| click_rate | string | 点击率 |

**返回示例：**

```json
{
    "code": 0,
    "data": {
        "date": "2025-04-06",
        "miniapp_wxappid": "wxxxxxxxxxxx",
        "miniapp_name": "xxxx",
        "req_succ_count": 443610,
        "exposure_count": 181814,
        "exposure_rate": "0.409850995",
        "click_count": 10095,
        "click_rate": "0.055523777",
        "income": 104350,
        "ecpm": "286.969100289"
    },
    "message": "success"
}
```

### 4.17、获取活跃用户的小程序openid

**简要描述：**
获取活跃用户列表
注：1. 此接口有调用频率限制，相同查询条件每分钟仅能请求一次
2. 单页返回100条数据
3. 支持查询最近15天的数据

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/user/getactiveuserminiappopenid`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| date | 是 | string | 统计数据的日期，格式yyyy-mm-dd |
| next_id | 是 | number | 非首页必传 |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| miniapp_wxappid | string | 关联小程序的wxappid |
| next_id | number | 游标ID |
| total_count | number | 总数（已废弃） |
| has_more | bool | 是否更多数据 |
| list | array | openid列表 |
| openid | string | openid |

**返回示例：**

```json
{
    "code": 0,
    "data": {
        "list": [
            {
                "openid": "o0Dq86--BVLQeqjfi9UrmK6MAsxQ"
            },
            {
                "openid": "o0Dq86--BcZSEQEOrhRrCvnQQOfU"
            },
            {
                "openid": "o0Dq86--ED4Xi_CSqgIVXLyurW6Q"
            },
            {
                "openid": "o0Dq86--ElAWzZtXjIsQt1aNRYbA"
            },
            {
                "openid": "o0Dq86--HAYKnp3hKFZJF-4QTzaQ"
            }
        ],
        "miniapp_wxappid": "wx0780xxxxxxxxxxxx",
        "next_id": 8429855,
        "total_count": 0,
        "has_more": true
    },
    "message": "success"
}
```

### 4.18、老链接转换

**简要描述：**
链接路径和域名发生调整，通过此接口把老旧链接转换成最新链接
注：1. 此接口有调用频率限制，相同查询条件每分钟仅能请求一次

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/tuneurl`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| url | 否 | string | 空时，返回domain; 非空，返回老链接对应的新链接 |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| domain | string | 域名 |
| url | string | 最新链接 |

**返回示例：**

```json
{
    "code": 0,
    "data": {
         "domain": "wx123456789-c.xdxiaoshuo.com",
         "url": "https://wx123456789-c.xdxiaoshuo.com/wxfx17/read.html?cbid=&ccid=&channel_id=spread_123456" 
    },
    "message": "success"
}
```

### 4.19、获取IAA广告用户维度数据

**简要描述：**
获取指定时间内更新的用户维度IAA广告数据列表
注：
1.此j接口有调用频率限制，相同查询条件每分钟仅能请求一次
2.单页返回100条数据
3.不可查询25年8月以前的数据

**请求接口地址：**
`https://fxapi.yuewen.com/wechat/Statics/GetIaaUserAdStatList`

**请求方式：**
GET

**请求接口参数：**

| 请求参数 | 必须 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| start_time | 是 | number | 查询起始时间yyyymmdd(废弃） |
| end_time | 是 | number | 查询结束时间yyyymmdd（废弃） |
| page | 否 | number | 分页，默认为1（废弃） |
| appflag | 是 | string | 产品标识（可从后台>>合作产品管理 >> 业务管理获取），每次请求仅能传一个。 |
| date | 是 | number | 统计数据的日期，格式yyyymmdd |
| next_id | 是 | number | 非首页必传 |

**接口返回字段：**

| 接口字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| total_count | number | 当前查询条件的总数据行数（废弃） |
| next_id | number | 游标ID |
| has_more | bool | 是否更多数据 |
| list | array | 结果集数组 |
| statis_day | number | 统计日期 |
| guid | number | 阅文用户ID |
| reg_time | string | 注册时间 |
| channel_id | number | 外推链接ID |
| original_channel_id | string | 外推链接原始ID |
| origin_book_id | string | 外推链接对应书籍id |
| origin_book_name | string | 外推链接对应书名 |
| book_id | number | 小说书ID |
| book_name | string | 小说书名称 |
| play_ad_cnt | number | 广告曝光次数 |
| fully_play_jl_ad_cnt | number | 广告解锁章节次数 |
| play_cp_ad_cnt | number | 插屏广告曝光次数 |
| play_cy_ad_cnt | number | 插页广告曝光次数 |

**返回值示例：**

```json
{
    "code": 0,
    "data": {
        "total_count": 60,
        "list": [
            {
                "statis_day": 20251111,
                "guid": 1234567,
                "reg_time": "2025-06-13 16:53:23",
                "origin_channel_id": "spread_xxxxxxx",
                "channel_id": "xxxxxxx",
                "origin_book_id": "xxxxxxxxxxxxxxxxx",
                "origin_book_name": "xxxx",
                "book_id": "xxxxxxxxxxxxxxxx",
                "book_name": "xxxxxxxxx",
                "play_ad_cnt": 0,
                "fully_play_jl_ad_cnt": 0,
                "play_cp_ad_cnt": 0,
                "play_cy_ad_cnt": 0
            }
        ]
    },
    "message": "success"
}
```

## 五、错误码说明

| 错误码 | 错误码描述 |
| :--- | :--- |
| 0 | 成功 |
| 1 | 未定义异常 |
| 400 | 公共参数错误 |
| 401 | 签名信息校验失败 |
| 1000 | 业务类型错误，一般为API地址错误 |
| 1001 | API版本号参数错误 |
| 1003 | 当前账号未开通API权限 |
| 1004 | 当前账号暂无产品管理权限 |
| 1006 | 当前请求IP不在白名单 |
| 1007 | 当前邮箱账号信息错误，一般为邮箱错误 |
| 1008 | 当前请求中AppFlag参数错误或不存在 |
| 1009 | 当前接口未授权 |
| 1010 | 该appflag不适用于当前业务接口 |
| 1110 | 业务API请求参数错误 |
| 1111 | 业务API请求参数中时间字段错误 |
| 1112 | 查询产品实例列表错误 |
| 1113 | 业务中最大允许数据条数超限 |
| 1114 | 查询用户信息失败，请重试或检查openid |
| 1115 | 调用频率超限 |
| 1120 | 查询时间范围过大或时间参数错误 |
| 1121 | 订单类型参数错误 |
| 1122 | 缺少分页查询相关参数 |
| 1123 | 查询充值记录失败 |
| 1124 | 查询充值记录附加数据查询失败 |
| 1141 | 获取作品信息错误 |
| 1142 | 强关章节不能超过免费章节 |
| 1143 | 关联公众号appid不存在 |
| 1144 | 页面类型参数错误 |
