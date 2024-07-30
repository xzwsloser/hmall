# 黑马商城项目(SpringCloud框架学习)
## MyBatisPlus框架学习
[mybatisplus_learn.md](docs%2Fmybatisplus_learn.md)
## Docker学习
[docker_learn.md](docs%2Fdocker_learn.md)
## 微服务架构介绍
[mcrio-service.md](docs%2Fmcrio-service.md)
## 微服务拆分
[mcrio-service-broken.md](docs%2Fmcrio-service-broken.md)
## OpenFeign使用
[OpenFeign_learn.md](docs%2FOpenFeign_learn.md)
## 微服务拆分总结
[mcrio-service-broken-sum.md](docs%2Fmcrio-service-broken-sum.md)
## 网关和配置管理
[getway_use.md](docs%2Fgetway_use.md)
## 服务保护
[service_protect_trascation.md](docs%2Fservice_protect_trascation.md)
# 分布式事务
[distrubuted_transction.md](docs%2Fdistrubuted_transction.md)

SpringCloud框架中提供了各种组件用于解决分布式系统是可能遇到的各种问题,比如:
- Nacos: 用于解决微服务和各个微服务中的配置的管理的问题,可以结合OpenFeign来实现不同服务之间的调用,同时可以结合 bootstarp文件简化配置文件
- OpenFeign: 解决不同微服务之间的相互调用问题
- Gateway: 网关,用于解决前后端联调的问题,同时还可以用于登录校验等功能
- Sentinel: 用于提供服务雪崩的各种解决方案: 请求限流,服务隔离,fallback,服务熔断
- Seata: 用于解决反不是事务问题,提供 XA和AT两种模式,不同之处就在于提交事务的时机和回滚机制
