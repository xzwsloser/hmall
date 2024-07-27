# 微服务拆分
## 项目分析
- 黑马商城分为如下模块:
  - 用户模块
  - 商品模块
  - 购物车模块
  - 订单模块
  - 支付模块
## 微服务拆分原则
- 需要拆分的情况:
  - 创业型项目: 先采用单体架构,快速开发,快速试错,随着规模的扩大,逐渐拆分
  - 确定的大型项目: 资金充足,目标明确,可以直接选择微服务架构,避免后续拆分的
- 拆分方法:
  - 高内聚: 每一个微服务的职责需要尽量单一,包含的业务相互关联度高,完整度高
  - 低耦合: 每一个微服务的功能需要相对独立,尽量减少对于其他微服务的依赖
- 拆分方式,一般包含两种方式:
  - 纵向拆分: 按照业务模块拆分
  - 横向拆分: 抽取公共服务(登录等功能),提供复用性
## 拆分服务
- 黑马商城五个不同的服务采用纵向拆分的方式进行拆分,不同的模块作为一个项目
- 微服务中的两种工程结构:
  - 独立Project(每一个项目就是一个项目,独自占一个文件夹)
  - Maven聚合(每一个服务就是一个 moudle),比较常用
### 拆分商品模块
- 需求:
  - 把 hm-service 中商品管理相关的功能拆分到一个微服务 moudle 中,命名为 item-service
  - 将 hm-service 中商品管理相关的功能拆分到一个微服务 moudle 中,命名为 cart-service
- 一个小的知识点: maven中 dependencymanagerment 标签中的依赖的作用就是管理依赖,但是如果子工程中需要使用以来的话还需要自己引入依赖
# 远程调用
- 不同的微服务互不相关,但是如果一个微服务中需要远程调用另外一个微服务的相关接口,或者需要查询另外一个微服务的数据库,就需要使用到远程调用的方法
- 这一个过程可以使用模拟前端向后端发送请求的过程,可以使用 httpClient ? 
- Spring提供了一个 RestTemplate 工具,可以方便的实现 http 请求的发送,使用步骤如下:
  - 注入 RestTemplate 到 Spring 容器中
  - 发起远程调用
- 发起远程调用的方法如下:
- 注意下面一个代码中的细节:
  - 传入的参数: url method,RequestEntity 返回结果的集合(相当于返回结果封装成什么类型,这里不可以使用字节码指定),最后就是 url 中的变量
- 注意此时不要使用 @Autowired 进行注入,否则就会发生警告
- 这里采用构造函数注入的方式,但是同时还需要防止其他的一些不相关的属性被注入,所以可以使用 private final 关键字修饰需要注入的属性,最后利用 lombook 中的 @requiredArgsConstructor 注解进行注入就可以了
```java
    private void handleCartItems(List<CartVO> vos) {
        Set<Long> itemIds = vos.stream().map(CartVO::getItemId).collect(Collectors.toSet());
        // 发送一个 http 请求
        ResponseEntity<List<ItemDTO>> response = restTemplate.exchange("http://localhost:8081/items?ids={ids}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ItemDTO>>() {
                },
                Map.of("ids", CollUtils.join(itemIds, ",")));
        // 1.获取商品id
        // 2.查询商品
        if(!response.getStatusCode().is2xxSuccessful()){
            log.error("查询失败");
            return ;
        }
        List<ItemDTO> items = response.getBody();
        if (CollUtils.isEmpty(items)) {
            return;
        }
        // 3.转为 id 到 item的map
        Map<Long, ItemDTO> itemMap = items.stream().collect(Collectors.toMap(ItemDTO::getId, Function.identity()));
        // 4.写入vo
        for (CartVO v : vos) {
            ItemDTO item = itemMap.get(v.getItemId());
            if (item == null) {
                continue;
            }
            v.setNewPrice(item.getPrice());
            v.setStatus(item.getStatus());
            v.setStock(item.getStock());
        }
    }
```
# 服务治理
## 注册中心原理
- 服务远程调用存在的问题: 如果以上写死请求的地址,如果请求的服务器挂了,或者同时开启了多个服务器,那么以方便无法感知到服务器数量的变化,另外一方面重度依赖于某一个服务器
就会发生一系列的问题
- 微服务中分为服务调用者和服务提供者,但是服务的调用者和服务的提供者不是绝对的,而是相对的,服务的调用者也可以是服务的提供者:
  - 服务掉提供者开启时自动向注册中心发起请求,注册服务信息,同时服务调用者每隔一段时间都会向注册中心发送请求表示心跳续约,如果某一个服务调用这出现故障,那么注册中心就可以感知到变化自动把这一个服务注销
  并且向服务的调用者推送信息,让调用者停止调用这一个服务
  - 服务的调用者只用定于相关信息,订阅之后,注册中心就会把相关的信息发送给服务的调用者,服务的调用者就可以根据服务的提供者的相关信息进行负载均衡从而选择适合的服务调用这完成服务
![Screenshot_20240727_115622_tv.danmaku.bilibilihd.jpg](imgs%2FScreenshot_20240727_115622_tv.danmaku.bilibilihd.jpg)
- 服务提供者: 暴露服务接口,供其他服务调用
- 服务消费者: 调用其他服务提供的接口
- 注册中心: 记录并且监控微服务各个实例状态,推动服务变更信息
## Nacos 注册中心
- Nacos 是一种使用比较广泛的注册中心,类似的组件还有很多,这些组件的使用方式差不多
- 利用 docker 容器进行 nacos 的部署,命令如下:
```bash
sudo docker run -d \
--name nacos \
--env-file ./nacos/custom.env \
-p 8848:8848 \
-p 9848:9848 \
-p 9849:9849 \
--restart=always \   
nacos/nacos-server
```
- 直接访问: http://nacos:port/nacos 就可以访问到 nacos 的注册中心
## 服务注册
- 服务注册的步骤如下:
  - 引入 nacos discovery 的依赖(不用指定版本这是因为在 SpringCloudAlibaba中已经配置了各种依赖的地址了)
  - 配置 nacos 地址
```java
 <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```
## 服务发现
- 消费者需要连接nacos 并且拉取和订阅服务,因此服务发现的前面两步和服务注册是一样的,后面加上服务调用就可以了:
  - 引入 nacos discovery 依赖
  - 配置 nacos 地址
  - 服务发现
- 服务发现过程中使用的 API 就是 discoveryClient 
- 可以自己指定负载均衡的算法
- 使用过程如下:
  - 首先拉取相关的服务
  - 之后手写负载均衡代码获取服务编号
  - 根据服务编号获取请求的 uri (注意 uri 和 url 的区别)
  - 最后利用 uri 就可以拼出服务的地址了
```java
// 发送一个 http 请求
        // 1.1 根据服务的名称获取服务的实例列表
        List<ServiceInstance> instances = discoveryClient.getInstances("item-service");
        if(CollUtils.isEmpty(instances)){
            return ;
        }
        // 1.2 进行负载均衡算法挑选服务
        ServiceInstance serviceInstance = instances.get(RandomUtil.randomInt(instances.size()));// 负载均衡算法
        URI uri = serviceInstance.getUri();  // uri 表示 http://localhost:8080
        ResponseEntity<List<ItemDTO>> response = restTemplate.exchange(uri + "/items?ids={ids}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ItemDTO>>() {
                },
                Map.of("ids", CollUtils.join(itemIds, ",")));
```
