# 微服务保护
- 目前已经解决的微服务中的问题:
  - 远程调用:  OpenFeign
  - 服务治理: Nacos
  - 请求路由和身份认证: Gateway
  - 配置管理: Nacos
## 雪崩问题
- 微服务调用链路中的某一个服务出现故障,引发整个链路中所有的微服务都不可用,这就是雪崩
- 雪崩的含义就是一个小的问题最后导致大的问题
- 雪崩产生的原因:
  - 微服务之间相互调用,服务提供者出现故障或者阻塞
  - 服务调用这没有做好异常处理,导致自身故障
  - 调用链中的所有服务级联失败,导致整个集群故障
- 解决问题的思路: 
  - 避免服务出现故障(写好代码)
  - 服务调用者遇到调用的服务出现异常的情况,及时进行处理
### 解决方案
- 请求限流: 限制访问微服务的请求的并发量,避免服务因流量激增出现故障
![Screenshot_20240729_175828_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240729_175828_tv.danmaku.bilibilihd.jpg)
- 线程隔离: 也叫做船壁,模拟船舱隔板的防水原理,通过限定每一个业务所用的线程数量而将故障业务隔离,避免故障,但是还是会导致如果服务卡住还是会消耗 cpu 的资源来向卡住的服务发送请求
![Screenshot_20240729_180343_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240729_180343_tv.danmaku.bilibilihd.jpg)
- 服务熔断: 有断路器统计请求的异常比例或者满调用比例,如果超出阈值就会熔断这一个业务,则拦截接口的请求,熔断期间,所有请求快速失败,都会走 fallback 逻辑
![Screenshot_20240729_180716_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240729_180716_tv.danmaku.bilibilihd.jpg)
- 服务保护技术如下:
![Screenshot_20240729_180955_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240729_180955_tv.danmaku.bilibilihd.jpg)
# Sentinel
## Sentinel 介绍
- 一块微服务流量控制组件
- 使用方式:
  - 引入依赖
  - 启动 Sentinel 控制台
  - 配置 Sentinel 控制台的访问信息
- 监控指标:  qps即每秒查询率，是对一个特定的查询服务器在规定时间内所处理流量多少的衡量标准
- 簇点链路: 就是单机调用链路,时每一次请求进入服务之后经过的每一个被 Sentinel 监控的资源链,默认Sentinel 会监控SpringMVC 的每一个 Endpoint(http接口)
限流,熔断都是针对于簇点链路中的资源设置的,而资源名默认就是接口的请求路径
- Restful风格的API请求路径一般都相同,这就会导致簇点资源名称重复,因此我们需要修改配置,把请求方式+请求路径作为簇点资源名称
```yml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8090   # 表示 sentinel 的配置信息
      http-method-specify: true # 表示设置请求路径不同
```
## 请求限流
- 在簇点链路的后面可以直接点击流控按钮,就可以进行限流配置
## 线程隔离
- 当商品服务出现阻塞或者故障时,调用商品服务的购物车服务可能因此就会被拖慢,甚至资源耗尽,所以必须限制购物车服务中查询商品这一个业务中的可用线程数,实现线程隔离
- 做法就是给这一个微服务中的某一个部分规定可以使用的线程数量
- 实现步骤还是一样的,就是在 Sentinel 中设置并发线程数
## Fallback
- 实现步骤:
  - 首先将 FeignClient 作为 Sentinel 的簇点资源
  - FeignClient中的Fallback有两种配置方式:
    - 方式一: FallbackClass,无法对远程调用的异常做出反应
    - 方式二: FallbackFactory,可以对于远程的调用的异常做处理,通常都会使用着一种方式
- 实现方式:
  - 首先定义一个类继承 ItemClientFallback 实现 FallbackFactory接口
  - 这一个接口中返回一个 XxxClient其中定义各种 fallback 方法
  - 最后注意在 ItemClient 中设置Fallback的属性值
```java
@Slf4j
public class ItemClientFallback implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("查询商品失败",cause);
                return Collections.emptyList();
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                log.error("扣减库存失败",cause);   // cause 表示异常
                throw  new RuntimeException(cause);
            }
        };
    }
}
```

```java
@Configuration
public class FallbackConfig {
    @Bean 
    public ItemClientFallback itemClientFallback(){
        return new ItemClientFallback();
    }
}
```

```java
@FeignClient(value = "item-service" , fallbackFactory = ItemClientFallback.class)  // 请求的服务名称
```
### 服务熔断
- 熔断时解决雪崩问题的重要手段,思路就是断路器统计服务调用的异常比例,满请求的比例,如果超出阈值就会熔断该服务,即拦截访问该服务的一切请求,而当服务恢复时,断路器就会放行这一个服务的请求
- 熔断器的原理:
  - 熔断器分为三种状态: Closed(表示熔断器关闭)   Open(表示熔断器打开)   Half-Open(表示熔断器测试)
![Screenshot_20240730_101311_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240730_101311_tv.danmaku.bilibilihd.jpg)
- 还是可以直接在熔断器中设置熔断条件,可以根据慢调用,异常比例,异常数量设置熔断条件
