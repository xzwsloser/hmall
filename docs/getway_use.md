# 网关介绍
- 目前的项目中存在的问题包含:
  - 用户请求一个地址时,由于原来的服务被拆成多个不同的服务,所以无法获取多个位置的信息
  - 多个服务无法交换用户信息
- 网关: 就是网络的关口,负责请求的路口,转发,身份校验
- 基础结构图:
![Screenshot_20240728_202517_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240728_202517_tv.danmaku.bilibilihd.jpg)
- 网关的存在使得前端向后端发送请求时,看不到后端的黑盒子的结构,可以把后端当成一个单体架构项目处理
- SpringCloud中网关的实现方式包含两种:
  - Spring Cloud Gateway: 基于 WebFlux 的响应式编程,无需调优就可以获得优异性能
  - Netfilx Zuul: 基于Servlet的阻塞时编程,需要调优才可以后的和 SpringCloudGateWay类似的功能
## 网关路由
- 网关实现路由的步骤:
  - 项目启动时,各个微服务模块完成路由注册
  - 前端向网关发送请求,网关判断前端发送请求的类型,根据请求的类型拉取对应的服务
  - 之后有注册中心的负载均衡算法分配访问地址,后端完成访问
- 实现步骤:
  - 首先创建新的模块
  - 引入网关依赖
  - 编写启动类
  - 配置路由规则
- 路由属性如下:
```yml
server:
  port: 8080
spring:
  application:
    name: gateway
  cloud:
    nacos:
      server-addr: 192.168.59.132:8848
    gateway:
      routes:
        - id: item-service
          uri: lb://item-service
          predicates:
            - Path=/items/**,/search/**   # 表示路由配置
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/addresses/**,/users/**
        - id: cart-service
          uri: lb://cart-service
          predicates:
            - Path=/carts/**
        - id: trade-service
          uri: lb://trade-service
          predicates:
            - Path=/orders/**
        - id: pay-service
          uri: lb://pay-service
          predicates:
            - Path=/pay-orders/**
```
## 路由配置
- 网关路由对应的java了类型就是 RouteDefinition,常见的属性如下:
  - id: 路由唯一标识
  - uri: 路由目标地址
  - predicates: 路由断言,判断请求是否符合当前路由
  - filters: 路由过滤器,对于请求和响应做出特殊处理
- Spring提供了12中基本的 RoutePredicateFactory实现:
![Screenshot_20240728_212050_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240728_212050_tv.danmaku.bilibilihd.jpg)
- 路由过滤器: 网关中提供了33中路由过滤器,每一种过滤器都有独特的作用:
![Screenshot_20240728_212340_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240728_212340_tv.danmaku.bilibilihd.jpg)
- 作用就是可以进行添加请求头等操作
- 如果想要变成全局的配置,那么就可以在过滤器中进行相应的配置 配置一个 default-filters: 过滤器配置 就可以了
- 演示如下:
```yml
  cloud:
    nacos:
      server-addr: 192.168.59.132:8848
    gateway:
      routes:
        - id: item-service
          uri: lb://item-service
          predicates:
            - Path=/items/**,/search/**   # 表示路由配置
          filters:
            - AddRequestHeader=truth,anyone long-press like button will be rich
      default-filters:
        - AddRequestHeader=truth,anyone long-press like button will be rich
```
## 网关登录校验的功能
- 网关需要做 Jwt 校验,就需要在网关把请求转发给微服务之间做校验
- 网关请求处理的流程如下:
  - 首先客户都安发送请求给 HandlerMapping,HandlerMapping把请求的路由存储到上下文中,之后把请求交给WebHandler处理
  - 请求处理器,默认实现时 FilterWebHanlder,是一个过滤器处理器,可以加载网关中配置的多个过滤器,然后放入到集合中进行排序,形成过滤器链,最后一次执行过滤器
  - 最后请求给 Netty 过滤器,负责把请求转发给微服务,并且当微服务返回结果存入到上下文中
- 每一个过滤器都分为 Pre Post两个部分,当请求和响应时都会发生作用
![Screenshot_20240728_214226_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240728_214226_tv.danmaku.bilibilihd.jpg)
- 网关登录校验中存在的问题:
  - 如果在网关转发之前做登录校验?       在 NettyFilter 之前在加一个过滤器进行 JWT 检验
  - 网关如何将用户信息传递给微服务?     请求头中携带用户信息发送给微服务
  - 如何在微服务之间传递用户信息?       利用 OpenFeign发送请求的而过程中携带请求头发送信息
### 自定义过滤器
- 网关提供了两种不同的过滤器:
  - GateWayFilter: 路由过滤器,作用域任意指定的路由,默认不生效,需要配置到路由之后生效
  - GlobalFilter: 全局过滤器,作用范围就是所有路由,声明之后自动生效
- 过滤器中的方法如下:
![Screenshot_20240728_214959_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240728_214959_tv.danmaku.bilibilihd.jpg)
- 小知识: 可以使用 ctrl + H 查看继承关系
- 定义过滤器如下,注意控制过滤器的执行顺序:
```java
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 模拟登录登录校验逻辑
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        System.out.println("header = " + headers);
        return chain.filter(exchange);  // 表示继续调用
    }

    @Override
    public int getOrder() {
        // 表示放在 NettyFilter 的前面
        return 0;
    }
```
### 自定义 GlobalFilter
- 一般不常用
- 自定义 GateWayFilter不是直接实现 GateWayFilter,而是实现AbstractGateWayFilterFactory
- 定义方式如下:
```java
@Component
public class PrintAnyGatewayFilterFactory extends AbstractGatewayFilterFactory {
    @Override
    public GatewayFilter apply(Object config) {
        return new OrderedGatewayFilter(new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                HttpHeaders headers = exchange.getRequest().getHeaders();
                System.out.println("headers = " + headers);
                return chain.filter(exchange);
            }
        },1);
    }
}
```
- 配置文件的书写方式:
```yml 
      default-filters:
        - AddRequestHeader=truth,anyone long-press like button will be rich
        - PrintAny  # 表示前缀
```
- 如果需要配置参数,那么就需要配置一个 config 类 ，config需要定义为一个静态类，同时需要把这一个类告诉给父类
- 创建方式如下:
```java
@Component
public class PrintAnyGatewayFilterFactory extends AbstractGatewayFilterFactory {
    @Override
    public GatewayFilter apply(Object config) {
        return new OrderedGatewayFilter(new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                HttpHeaders headers = exchange.getRequest().getHeaders();
                System.out.println("headers = " + headers);
                return chain.filter(exchange);
            }
        },1);
    }
    
    @Data 
    public static class Config{
        private String a;
        private String b;
        private String c;
    }
    
    // 返回参数
    @Override
    public List<String> shortcutFieldOrder(){
        return List.of("a","b","c");  // 表示参数的值
    }
    
    public PrintAnyGatewayFilterFactory(){
        super(Config.class);
    }
}
```
- 配置文件:
```yml 
      default-filters:
        - AddRequestHeader=truth,anyone long-press like button will be rich
        - PrintAny=1,2,3
```
### 网关传递用户到微服务
- 修改gateway模块中的登录校验拦截器,在检验成功之后保存用户到下游的请求的请求头中
- 需要修改到微服务的请求,需要使用ServerWebExchange提供的API
- 同时各个微服务中又可以定义一个拦截器把用户信息存储到 ThreadLocal中,之后需要使用用户信息只用在 ThreadLocal中取出用户信息就可以了
- 代码演示如下:
```java
   String userInfo = userId.toString();
        ServerWebExchange webExchange = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        // 6. 放行
        return chain.filter(webExchange);  
```
- 注意向拦截器这样很多微服务都需要使用的模块就可以定义在 common 模块中,共各种微服务使用
- 具体实现方式就是:
  - 首先在 common 模块中定义拦截器,这一个拦截器没有拦截作用,作用就是获取到用户信息存储到 ThreadLocal中,之后的微服务只用在 TheadLocal中拿到拦截器就可以了
  - 最后注意 MvcConfig无法被启动类扫描到,所以需要在 spring.factory中配置MvcConfig的相关信息,这样的话就会被扫描到
- 但是注意网关使用的时 WebFlux 的响应式变成,和 SpringMVC 没有关系,所以需要让 common 模块中的 MVCConfig在网关中不生效,所以可以使用@ConditionOnClass注解
- 代码演示如下:
```java
@Configuration
@ConditionalOnClass(DispatcherServlet.class)   // 表示只要有 SpringMVC 都会
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new UserInterceptor());   // 都需要作用,反正不用拦截
    }
}
```
```text
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.hmall.common.config.MyBatisConfig,\
  com.hmall.common.config.MvcConfig,\
  com.hmall.common.config.JsonConfig
```
### 利用 OpenFeign 传递用户
- 某些业务由于调用链比较长,所以需要在不同的微服务之间传递用户信息,这就需要利用 OpenFeign进行用户信息的传递
- 一般来说,一个业务中拿到数据的方式就是从 ThreadLocal中取出用户信息
![Screenshot_20240729_104759_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240729_104759_tv.danmaku.bilibilihd.jpg)
- OpenFeign中提供了一个拦截器接口,所有有 OpenFeign发起的请求都会首先带哦用拦截器处理请求,其中的 header 方法就可以对于某一个请求的请求头进行处理
- 这一过程中使用了三种拦截器:
  - 网关中的 GlobalFilter , 作用就是可以对前端发送过来的请求做一个登录拦截检验,并且把用户信息存储到请求头中发送给之后的微服务
  - 每一个微服务的前面都需要定义一个HandlerInterceptor拦截器,作用就是可以从请求头中获取数据并且存储到 ThreadLocal中
  - 同时一些业务涉及到微服务之间的项目调用,所以可以利用 OpenFeign中的 RequestInterceptor拦截器对于请求头中的信息进行校验,并且把请求头中的用户信息保存到 ThreadLocal中
- 架构图如下:
![Screenshot_20240729_110035_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240729_110035_tv.danmaku.bilibilihd.jpg)
# 配置管理
- 目前的项目中依然存在的问题:
  - 微服务中重复配置过多,维护成本高
  - 业务配置经常变动,每一次修改都需要重启服务
  - 网管路由配置写死,如果变更就需要重启网关
- nacos不只是可以作为注册中心,还可以作为配置管理中心
![Screenshot_20240729_110804_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240729_110804_tv.danmaku.bilibilihd.jpg)
## 共享配置
- 实现步骤: 只用在 nacos 的配置页面中添加一些配置信息到nacos就可以了,包含 jdbc,MyBatisPlus,日志,Swagger,OpenFeign等配置信息
- 拉取共享配置的流程:
  - 基于NacosConfig拉取共享配置代替微服务的本地配置
- SpringCloud读取配置文件并且初始化 ApplicationContext 的方式如下:
![Screenshot_20240729_112111_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240729_112111_tv.danmaku.bilibilihd.jpg)
- 需要引入依赖: spring-cloud-starter-alibaba-nacos-config 来拉取 nacos 配置
- 同时还需要引入依赖: spring-cloud-starter-bootstrap 来读取 bootstrap 文件中的信息
## 配置热更新
- 配置热更新: 当修改配置文件中的配置时,微服务无需紫气就可以时的配置生效
- 前提条件:
  - nacos中需要有一个和微服务名称有关的配置文件:  \[spring.application.name\]-\[spring.active.profile\].\[file-extension\](在 nacos 中配置这三个部分之后就不用配置了)
  - 微服务中需要一特定的方式来读取需要热更新的配置属性
- 步骤:
  - 首先定义一个配置类,用于记录需要配置的信息,注意使用 @ConfigurationProperties 修饰的配置类可以让这一个类和配置文件相关联
  - 之后在nacos配置中心中配置相关的属性,就是上面这一种形式的文件,这一个文件中写入需要变动的属性就可以了
- 配置类如下:
```java
@Data
@Component
@ConfigurationProperties(prefix = "hm.cart")
public class CartProperties {
    private Integer maxItems;
}
```
## 动态路由
- 动态路由: 网关配置文件中的路由如果发生改变,那么可以及时把这些改变推送给网关,从而使得网关做出相应的响应
- 实现动态路由就需要把路由配置到 Nacos 中,当 Nacos 中的路由配置发生改变的时候,推动最新的配置到网关中,实时更新王贯中的路由信息
- 我们需要完成两件事情:
  - 监听Nacos配置变更的信息
  - 当配置变更时,将最新的路由信息更新到网关路由表中
- 步骤:
  - 首先在 bootstarp.yaml中配置服务名称等信息:
```yml
spring:
  application:
    name: gateway    # 表示给一个微服务起一个名称
  profiles:
    active: dev
  cloud:
    nacos:
      server-addr: 192.168.59.132:8848
      config:
        file-extension: yaml
        shared-configs:
          - data-id: shared-log.yaml
# keytool -genkeypair -alias hmall -keyalg RSA -keypass hmall123 -keystore hmall.jks -storepass hmall123
```
  - 之后编写路由监听器:
```java
@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final NacosConfigManager nacosConfigManager;

    private final RouteDefinitionWriter routeDefinitionWriter;

    private final Set<String> router = new HashSet<>(); // 存储 id , 便于路由的删除

    private final String dataId = "gateway-routers.json";  // 使用 json 格式进行解析

    private final String group = "DEFAULT_GROUP";
    @PostConstruct  // 表示这一个类实例话之后执行
    public void initRouteConfigLoader() throws NacosException {
        // 1. 项目启动时,先拉取配置,之后添加配置监听器
        String configInfo = nacosConfigManager.getConfigService().getConfigAndSignListener(dataId, group, 5000, new Listener() {
            @Override
            public Executor getExecutor() {
                // 表示定义一个线程池,从线程池中取出线程执行这一个方法
                return null;
            }

            @Override
            public void receiveConfigInfo(String s) {
                // 2. 监听配置变更,需要更新路由
                updateConfig(s);
            }
        });
    }

    public void updateConfig(String configInfo){
        log.debug("开始更新路由信息");
        // 表示更新配置
        // 1. 解析配置文件,转化为 RouteDefination
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        // 删除旧的路由表
        // 开始删除旧的
        for (String r : router) {
            routeDefinitionWriter.delete(Mono.just(r)).subscribe();
        }
        // 清空路由表
        router.clear();
        for (RouteDefinition routeDefinition : routeDefinitions) {
                routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();  // 表示订阅容器中的消息
                // 记录路由 id
                router.add(routeDefinition.getId());
        }
    }
}
```
 - 最后把路由写入到nacos的注册中心中,注意dataId一定需要和程序中一致
          