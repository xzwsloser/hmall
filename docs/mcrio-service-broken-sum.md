# 微服务拆分自主任务总结
- 首先创建新的模块的时候注意不用创建 SpringBoot 项目了,这是由于 SpringBoot 项目的父类就是默认的,所以如果创建SpringBoot项目,那么相当于和总项目脱离了练习
一般就是最大的一个项目创建成 SpringBoot 项目,下面的各个微服务创建成一般的 maven 模块,创建之后就可以自己引入相关的依赖
  (父项目中使用 dependencymanager 进行管理,版本已经确定了,只用子模块自己引入依赖)
- 一般拆分服务从 domain 包开始引入依赖,就是从底层开始引入依赖,每次引入一个包就需要排除不需要的依赖,替换原来的包,这样才可以减少上层应用的报错
- 最后注意 MP 中 API的使用,Service层的API一般都是利用链式条件进行查询的,之后要自己做一个项目
- lambdaQuery() 底层可能会有问题
- 注意启动项上的注解：
  - @SpringBootApplication
  - @MapperScan(用于指定 mapper 文件的位置)
  - @EnableFeignCLients(basePackage=client所在的包名,defaultConfiguration=默认配置类.class)
- 公共的部分放在公共的一个项目中,公共的项目中可以放  DTO , Util , VO ,PO 等包
