# OpenFeign
- OpenFeign是一种 http 客户端,可以用于发送http请求,简化向 nacos请求服务的步骤
## OpenFeign 快速入门
- 引入依赖,包含 OpenFeign和负载均衡组件 SpringCloudLoadBanlancer 
- 通过@EnableFeignCLients 注解,开启 OpenFeign 功能
- 定义FeignClient 注解
- 使用 FeignClient , 实现远程调用,利用 SpringMVC 的注解来标记注册中心相应服务的地址和请求方式
- 代码演示如下:
```xml
  <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

```java
@FeignClient("item-service")  // 请求的服务名称
public interface ItemClient {
    
    @GetMapping("/items")  // 请求的地址
    List<ItemDTO> queryItemByIds(@RequestParam("ids") List<Long> ids);  // 请求的参数和返回值,底层自动通过动态代理的方式获取到返回值  
}
```
- 底层会使用动态代理的方式获取请求,使用时直接利用ItemClient实例对象就可以了
- OpenFeign底层默认使用 jdk 自带的http客户端发送请求,只是利用默认的输入输出流进行连接,所以效率比较低下,所以可以使用其他框架作为连接池提高效率：
- 常见的框架比如:
  - HttpURLConnection: 默认实现,不支持连接池
  - Apache HttpClient: 支持连接池
  - OKHttp: 支持连接池
- 比如利用 OKHttp 的步骤:
  - 引入依赖
  - 开启连接池功能(yml中)
- 演示如下:
```xml
        <dependency>
            <groupId>io.github.openfeign</groupId>
            <artifactId>feign-okhttp</artifactId>
        </dependency>
```
```yml
feign:
  okhttp:
    enabled: true  # 表示开启连接池功能
```
## OpenFeign使用的最佳实践
- 目前使用 OpenFeign的问题:
  - 如果多个位置需要使用发送 Http 请求用以查询相关信息的位置,那么就需要编写多个 Item ,造成代码复用性低的问题
  - 如果相应的 Service 发生改变,那么所有的配置 OpenFeign 的位置都会发生改变,造成重复修改代码的问题
- 两种最佳实践方案:
  - 服务提供者相关的模块提供相应的功能模块,比如 Item-service 服务中就需要定义好相关的FeignClient 和其它类中共享的 DTO,如果其他模块想要使用这些模块的话,只需要引入这一个模块对应的坐标就可以了
  - 另外一种方法就是提供一个工具类,这一个工具类单独作为一个模块,同时这一个模块中可以提供公用的代码,如果其他服务想要引入这些模块就只用引入这一个模块就可以了
- 第一种实现方式比较复杂,但是耦合度更低,第二种耦合度较高
- 注意这里创建的工具模块可以不是被 Spring 管理的,只用完成他自己的功能(实例化 FeignClient 对象就可以了)
```java
@SpringBootApplication
@EnableFeignClients(basePackages = "com.hmall.api.client")  // 表示开启  OpenFeign,并且指定扫描包的位置
```
## OpenFeign日志输出
- OpenFeign只会在FeignClient所在的包的日志级别为DEBUG的时候,才会输出日志,而且日志级别有4级:
  - NONE: 不记录任何日志信息,这就是默认值
  - BASIC: 仅仅记录请求的方式,URL以及相应的状态码和执行时间
  - HEADERS: 在BASIC的基础上,额外记录了请求和响应的头信息
  - FULL: 记录所有请求和相应的明细信息,包含头信息和请求体和元数据
- 由于默认日志级别是 NONE,所以看不到日志
- 日志级别的设置:
  - 在需要需要自定义FeignClient日志级别的服务中定义一个类型为 Logger.Lever 的 Bean,返回需要的日志级别
  - 但是此时这一个 Bean 没有生效(没有 @Configuration注解),要想配置FeignClient的日志,可以在 @FeignClient注解中声明,那么只有这一个 Feign客户端的日志启动生效
  - 如果想要全局配置,让所有的FeignClient都按照这一个日志进行配置,需要在@EnableFeignClients注解中声明(配置在启动类上,是全局的)
```java
public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;  // 表示记录所有的注解
    }
}
```
```java
@SpringBootApplication
@EnableFeignClients(basePackages = "com.hmall.api.client",defaultConfiguration = DefaultFeignConfig.class)  // 表示开启  OpenFeign,并且指定扫描包的位置
public class CartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
    // 注意启动类也是一个配置类,也可以进行相关的一些配置
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
```
## OpenFeign总结
![Screenshot_20240727_181811_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240727_181811_tv.danmaku.bilibilihd.jpg)