# 介绍
- `MyBatisPlus`其实对于`MyBatis`的一种增强,但不是`MyBatis`的替代,而是`MyBatisPlus`的合作伙伴
- 只需要简单配置,就可以进行单表的增删改查,不用进行重复的`CRUD`操作
# 入门学习
## 快速入门

- 使用步骤:
   - 引入依赖: `mybatis-plus-boot-starter`引入这一个依赖相当于引入了 `mybatis`的依赖
   - 让自定义的`mapper`继承`mybatis`提供的`BaseMapper`接口(提供了各种`CRUD`的方法) 注意指定泛型为操作的类型
- 同时 `MyBatis-plus`使用之后,原来使用配置文件结合`sql`语句的方法任然可以使用
- 演示代码如下:
```java
public interface UserMapper extends BaseMapper<User> {

    void saveUser(User user);

    void deleteUser(Long id);

    void updateUser(User user);

    User queryUserById(@Param("id") Long id);

    List<User> queryUserByIds(@Param("ids") List<Long> ids);
}

```
```java
@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testInsert() {
        User user = new User();
        user.setId(5L);
        user.setUsername("Lucy");
        user.setPassword("123");
        user.setPhone("18688990011");
        user.setBalance(200);
        user.setInfo("{\"age\": 24, \"intro\": \"英文老师\", \"gender\": \"female\"}");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.saveUser(user);
    }

    @Test
    void testSelectById() {
        User user = userMapper.queryUserById(5L);
        System.out.println("user = " + user);
    }


    @Test
    void testQueryByIds() {
        List<User> users = userMapper.queryUserByIds(List.of(1L, 2L, 3L, 4L));
        users.forEach(System.out::println);
    }

    @Test
    void testUpdateById() {
        User user = new User();
        user.setId(5L);
        user.setBalance(20000);
        userMapper.updateUser(user);
    }

    @Test
    void testDeleteUser() {
        userMapper.deleteUser(5L);
    }
}
```
## 常用注解

- `MyBatisPlus`通过扫描实体类,并且基于反射获取到实体类信息作为数据库表信息,根据实体类如何找到数据库和数据库中的字段信息(约定大于配置):
   - 类名驼峰转为下划线作为表名
   - 名字为`id`的字段作为主键
   - 变量名驼峰转为下划线作为表的字段名
- 如果不符合约定,就需要使用注解进行相关配置,常用的配置信息的注解如下:
   - `@TableName`用于指定表名
   - `@TableId`用于指定表中的主键
   - `@TableField`用于指定表中普通的字段信息
- `@TableId`注解的属性有：
   - `value`用于指定主键名称
   - `type`可以赋值为`IdType`枚举类型,枚举类型中包含以下几种情况:
      - `AUTO`表示数据库自增长
      - `INPUT`表示通过`set`方法自行输入
      - `ASSIGN_ID`分配`ID`,接口`IndentifierGenerator`的方法`nextid`生成`id`,默认的实现类`DefaultIndentifierGenerator`底层利用了雪花算法(没有指定类型,默认就会使用雪花算法)
- 使用`@TableField`注解的常见场景:
   - 成员变量名和数据库字段名不一致
   - 成员变量名以`is`开头，并且是布尔类型
   - 成员变量与数据库名称冲突,需要加上'' 作为转义字符,比如 `@TableField("`order`")`这个就是`mysql`中的转义字符
   - 成员变量在数据库中不存在 `@TableField(exist = false)`
## 常见配置

- 支持`MyBatis`的原生配置和自己的一些特有的配置,可以在官网查看所有配置,并且尝尽配置基本都有默认值
```yaml
mybatis-plus:
  type-aliases-package: com.itheima.mp.domain.po
  mapper-locations: classpath*:mapper/**/*.xml   # 表示 mapper 包下所有目录,也就是默认值
  global-config:
    db-config:
      id-type: auto   # 自定义主键增长策略
```
# 核心功能
## 条件构造器

- 一般使用 `Wrapper`类的中的方法进行条件构造器的构建,`Wrapper`方法还有各种子类
- 如下图中,`AbstractWrapper`中定义了各种基本的条件查询的方法比如`gt,lt,like`等方法,同时继承它的类`UpdateWrapper`和`QueryWrapper`中分别补充了`set`和`query`方法
- 一般使用`QueryWrapper`来拼接`where`条件,可以用于`Update`语句和`query`语句,可以理解成 `Where`条件的拼接
- 使用`UpdateWrapper`语句完成`set`语句的设计和相关`sql`语句的拼接
- 为了防止硬编码,最好使用`LambdaUpdateWrapper`和`LambdaQueryWrapper`结合方法引用构建条件
- 代码演示如下:
```java
    @Test
    void testQueryWrapper(){
        // 查看返回值就可以了
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                    .select("id","username","info","balance")
                    .like("username","o")
                    .ge("balance",1000);

        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(System.out::println);  // 方法引用
    }
```
```java
    @Test
    void testUpdateWrapper(){
        List<Long> ids = List.of(1L,2L,4L);
        UpdateWrapper<User> wrapper = new UpdateWrapper<User>()
                                    .setSql("balance = balance - 200")
                                    .in("id",ids);
        userMapper.update(null,wrapper);   // 这就是可以进行 sql 语句的更新
    }

```
```java
    @Test
    void testLambdaQueryWrapper(){
        // 查看返回值就可以了
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .select(User::getId,User::getUsername,User::getInfo,User::getBalance)
                .like(User::getUsername,"o")
                .ge(User::getBalance,1000);

        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(System.out::println);  // 方法引用
    
```
## 自定义 sql

- 我们可以使用`MyBatisPlus`中的`Wrapper`构建比较复杂的`Where`语句,然后自己定义`sql`语句中剩余的部分
- 这是由于`MyBatisPlus`擅于拼接比较复杂的`Where`语句,但是前面的 `sql`语句拼接比较复杂
- 要实现这一个功能就需要把生成的`Wrapper`条件动态拼接到`sql`语句中
- 实现步骤:
   - 首先需要使用 `Wrapper`构建条件
   - 自定义方法，把`Wrapper`条件作为参数传入到 方法中
   - `sql`语句中引用条件就可以了
- 代码实现:
```java
    @Test
    void testCustomSqlUpdate(){
        List<Long> ids = List.of(1L,2L,4L);
        int amount = 200;
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>().in(User::getId,ids);
        userMapper.updateBanlanceById(wrapper,amount);
    }
```
```java
void updateBanlanceById(@Param("ew") LambdaUpdateWrapper<User> wrapper, @Param("amount") int amount);
```
```sql
  <update id="updateBanlanceById">
        UPDATE tb_user SET balance = balance - #{amount} - #{amount} ${ew.customSqlSegment}
  </update>
```
## Service 接口

- `Service`接口也可以通过继承的方法来拓展方法,不用自己调用`mapper`中的方法完成业务操作
- 分析继承实现关系: `Service`接口继承了`IService`接口,那么`ServiceImpl`就需要实现`IService`接口中的所有方法,`mybatis-plus`中提供了一个实现类`ServiceImpl`实现了接口中的所有方法,只用让自己的实现类继承`ServiceImpl`就可以了
- 注意实现类泛型还需要指定对应的 `mapper`接口
### Service接口的使用方式

- 如果是比较简单的 `CRUD` 操作,那么就可以直接使用 `UserService`中的方法操作
- 如果是比较复杂的,涉及业务逻辑的代码,那么就需要自己定义方法,自己写 `sql`语句
### Service接口的Lambad查询

- 使用 `lambdaQuery`和`lambdaUpdate`可以用于构建复杂条件,同时最后的终止条件多样化,比如`list`,`update`,`page`等,十分方便
- 代码演示
```java
  List<User> users = lambdaQuery()
                .like(User::getUsername, userQuery.getName())
                .eq(User::getStatus, userQuery.getStatus())
                .ge(User::getBalance, userQuery.getMinBalance())
                .le(User::getBalance, userQuery.getMaxBalance())
                .list();
```
```java
  lambdaUpdate().set(User::getBalance,remained)
                      .set(remained == 0 , User::getStatus,0)
                      .eq(User::getId,id)
                      .eq(User::getBalance,user.getBalance())    // 防止并发问题
                      .update();
```
### 批量新增

- 直接利用`save`方法那么就会导致一条数据一条数据地提交,性能比较差
- 如果利用 `saveBatch`方法,那么底层虽然是合并了 `sql`语句但是还是批量提交,性能不错
- 开启`rewriteBatchedStatement=true`参数那么`mysql驱动`的底层就可以自动拼装`sql`一起提交
```java
    @Test
    void insertBatch(){
        // 演示批量处理方案
        long begin = System.currentTimeMillis();
        List<User> list = new ArrayList<>(1000);
        for(int i = 1; i <= 100000 ; i ++){
            list.add(buildUser());
            if(i % 1000 == 0){
                // 开始出入数据
                userService.saveBatch(list);
                list.clear();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("耗费时间为:" + (end - begin));
    }
```
# 扩展功能
## 代码生成器

- 可以使用 `MyBatisPlus`插件就可以生成代码了,插件的位置如下图：

![image.png](https://cdn.nlark.com/yuque/0/2024/png/40754486/1721703223648-46df1fee-d60f-4df2-88ea-66b6f034c739.png#averageHue=%232c2e32&clientId=u9430649e-6e31-4&from=paste&height=565&id=uc096fae8&originHeight=848&originWidth=1119&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=163415&status=done&style=none&taskId=u8b51029b-7056-457c-94d6-ecb68a54b56&title=&width=746)
## 静态工具

- 静态类中`Db`中的各种方法基本和`Service`接口中的方法一致,但是由于是静态方法,所以需要传入字节码文件,基本上静态类`Db`和`Service`的作用基本一样
- 使用情况: 解决循环依赖的情况(`UserService`中使用`OrderService`,同时 `OrderService`中需要使用`UserService`)
- 注意`Db`中的方法和`Service`中的方法完全一致
- 这里纠正一个偏差:
   - `LambdaQueryWrapper`和`LambadUpdateWrapper`是条件构造器,主要用于 `mapper`层中方法的条件参数
   - 注意和`Service`层中的静态方法`lambdaQuery()`和`lambdaUpdate()`混淆,这两种方法是万能方法,可以进行条件拼接和查询,另外 `Db`中的方法和`Service`中的方法完全相同
```java
    @Override
    public List<UserVO> getUserListsAddress(List<Long> ids) {
        // 查询所有的 id 用户并且查询到地址
        List<User> userList = lambdaQuery().in(User::getId, ids).list();
        if(userList == null || CollUtil.isEmpty(userList)){
            throw new RuntimeException("程序运行错误");
        }
        // 开始查询地址
        List<UserVO> userVOS  = new ArrayList<>();
        // 注意最好减少数据库查询的操作,查询操作为一次最好,可以提前把结果放在一个 map 集合中
        for (User user : userList) {
            // 开始查询订单
            List<Address> addresses = Db.lambdaQuery(Address.class).eq(Address::getUserId, user.getId()).list();
            UserVO userVO = new UserVO();
            BeanUtil.copyProperties(user,userVO);
            if(CollUtil.isNotEmpty(addresses)){
                List<AddressVO> addressVOS = BeanUtil.copyToList(addresses, AddressVO.class);
                userVO.setAddressVOS(addressVOS);
            }
            // 添加到集合中
            userVOS.add(userVO);
        }
        return userVOS;
    }
```
## 逻辑删除

- 比如像订单这样比较重要的数据,就可以利用逻辑删除的方式对于字段进行逻辑删除,逻辑删除的数据只用首先确定是否这一个字段是否被逻辑删除掉了,之后进行查询
   - 删除操作:  `UPDATE user SET deleted = 1 WHERE id = 1 AND deleted = 0`
   - 查询操作: `SELECT * FORM user WHERE deleted = 0`
- `MyBatisPlus` 中提供了逻辑删除的功能,无法改变方法的调用方式,而是底层帮助我们自动修改`CRUD`的语句,只用在 `application.yaml`中进行相关数据的配置
- 配置方式如下:
```yaml
mybatis-plus:
  type-aliases-package: com.itheima.mp.domain.po
  mapper-locations: classpath*:mapper/**/*.xml   # 表示 mapper 包下所有目录,也就是默认值
  global-config:
    db-config:
      id-type: auto   # 自定义主键增长策略
      logic-delete-field: deleted  # 配置逻辑删除的字段
      logic-delete-value: 1    # 逻辑已删除值
      logic-not-delete-value: 0   # 表示逻辑没有删除值
```

- 但是利用逻辑删除的方式会导致 `sql`语句执行效率低下,并且空间占用率比较高,所以这里可以采用数据迁移的方法,真的删除数据,但是删除的时候对于数据进行迁移
## 枚举处理器

- 作用: 把 `java`中的枚举类型转换为数据库表中的`int`类型,常用的注解如下:
   - `@EnumValue`表示把这一个字段写入到数据库表中
   - `@JsonValue`表示`SpringMVC`底层把这一个字段返回给前端
- 使用步骤:
   - 给枚举中的和数据库对应的`value`值添加`@EnumValue`注解
   - 再配置文件中配置统一的枚举处理器,实现类型转换
```java
public enum UserStatus {
    NORMAL(1,"正常"),
    FROZEN(2,"冻结");

    @EnumValue   // 表示用这一个枚举作为类型时,就需要把这一个字段写入到数据库中
    private final int value;
    @JsonValue   // 表示 SpringMVC 返回这一个信息给前端
    private final String desc;

    UserStatus(int value,String desc){
        this.value = value;
        this.desc = desc;
    }
}
```
```java
mybatis-plus:
  type-aliases-package: com.itheima.mp.domain.po
  mapper-locations: classpath*:mapper/**/*.xml   # 表示 mapper 包下所有目录,也就是默认值
  global-config:
    db-config:
      id-type: auto   # 自定义主键增长策略
      logic-delete-field: deleted  # 配置逻辑删除的字段
      logic-delete-value: 1    # 逻辑已删除值
      logic-not-delete-value: 0   # 表示逻辑没有删除值
  configuration:   // 就是配置类型转换器
    default-enum-type-handler: com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler
```
## Json处理器

- `json`处理器解决的问题就是完成`java`对象和数据库中`json`对象的相互转化
- 使用步骤:
   - 首先定义一个类来表示需要转换为 `json`类型的`java`对象
   - 再需要转换的字段上面加上`@TableField(value = '数据库中的字段名',typeHandler = JacksonTypeHandler`注解就可以了,注意 `typeHandler`底层的实现有三种,但是利用`JacksonTypeHandler`就利用了`SpringMVC`底层的处理器不用自己引入依赖
   - 最后注意再表名上加上结果集映射`@TableName(value = '表名',autoResultMap=true`
- 代码演示如下:
```java
@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private String name;
    private Integer age;
    private String gender;
}
```
```java
    @TableField(value = "`username`",typeHandler = JacksonTypeHandler.class)  // 表示指定 json 处理器,把数据库表中的 json数据转换为 java对象类型
    private String username;

```
```java
@Data
@TableName(value = "tb_user",autoResultMap = true)  // 表示自动开启结果集映射
public class User {
```
# 插件功能
## 分页插件基本使用
### 分页插件的使用方法

- 首先需要再配置类中注册`MyBatisPlus`的核心插件,同时添加分页插件
- 构建 `Page`对象,并且构建添加排序规则
```java
@Configuration
public class MyBatisConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 1. 创建分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 添加分页插件
        paginationInnerInterceptor.setMaxLimit(1000L);  // 配置上限
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
}
```
```java
    @Test
    void testPageQuery(){
        // 1. 准备分页条件
        int pageNo = 1;
        int pageSize = 2;
        // 1.1 分页条件
        Page<User> page = Page.of(pageNo, pageSize);
        // 1.2 排序条件
        page.addOrder(new OrderItem("balance",true));  // 表示升序排列
        page.addOrder(new OrderItem("id",true));  // 设置多条配置条件
        // 2. 分页查询
        Page<User> userPage = userService.page(page);
        // 3. 开始解析,和 PageHelper 中的 Page 基本一致
        long total = userPage.getTotal();
        System.out.println("total = " + total);
        long pages = userPage.getPages();
        System.out.println("pages = " + pages);
        List<User> records = userPage.getRecords();
        records.forEach(System.out::println);
    }
```
### 通用插件实体

- 应用场景: 有时前端在进行分页查询的时候会传输过来各种各样的查询条件，并且针对于不用的表,查询条件也各不相同，所以最常用的解决方法就是首先定义一个 `PageQuery`类来定义通用的条件,比如页码,每一页的记录数等条件,同时每一张表也可以根据自己的字段定义相应的查询条件,可以通过继承`PageQuery`类来拓展查询条件
- 同时注意返回给前端的数据也要做一定的规定,这里可以封装成`PageDTO`对象，里面可以包含总条数和总页码数,还有查询结果等信息
- 这一种思想和 `XxxDTO`和`XxxVO`类似
```java
@Data
public class PageQuery {  // 分页查询实体
    private Integer pageNo;
    private Integer pageSize;
    private String sortBy;
    private Boolean isAsc;

}
```
```java
@Data

public class UserQuery extends PageQuery {

    private String name;

    private Integer status;

    private Integer minBalance;

    private Integer maxBalance;
}

```
```java
    @Override
    public PageDTO<UserVO> queryUsersPage(UserQuery userQuery) {
        String name = userQuery.getName();  // 根据哪一个字段查询
        Integer status = userQuery.getStatus();
        // 1. 首先构建查询条件
        Page<User> page = Page.of(userQuery.getPageNo(),userQuery.getPageSize());
        if(StrUtil.isNotBlank(userQuery.getSortBy())){
            // 不为空
            page.addOrder(new OrderItem(userQuery.getSortBy(),userQuery.getIsAsc()));  // 表示指定排序方式
        } else {
            page.addOrder(new OrderItem("update_time",false));
        }
        // 2. 分页查询
        Page<User> userPage = lambdaQuery()
                            .like(name != null,User::getUsername,name)
                            .eq(status != null,User::getStatus,status)
                            .page(page);  // 注意需要传递 Page 对象过去
        // 3. 封装 VO 结果
        PageDTO<UserVO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(userPage.getTotal());
        pageDTO.setPages(userPage.getPages());
        // 当前页数据
        List<User> userList = page.getRecords();
        if(CollUtil.isEmpty(userList)){
            pageDTO.setList(Collections.emptyList());
            return pageDTO;
        }
        // 4. 返回对象
        List<UserVO> res = BeanUtil.copyToList(userList,UserVO.class);
        pageDTO.setList(res);
        return pageDTO;
    }
```
```java
@Data
@NoArgsConstructor
@AllArgsConstructor    // 表示分页结果
public class PageDTO<V> {
    private Long total;
    private Long pages;  // 表示总页数
    private List<V> list;  // 使用泛型指定总页数等信息
}

```
#### 通用实体和MP的相互转换

- 由于利用 `PageQuery`封装成 `Page`对象的过程比较麻烦并且重复,`Page`转换为`PageDTO`的方法也比较重复,所以如果可以实现以下两个需求那么就可以减少重复代码:
   - 在`PageQuery`中定义方法,将`PageQuery`对象转化为`MyBatisPlus`中的`Page`对象
   - 在`PageDTO`中定义方法,将`MyBatisPlus`中的`Page`对象转换为`PageDTO`结果
- 思路就是分别在`PageQuery`和`PageDTO`对象中定义相关的方法来进行类型的转换
- 注意函数式接口`Function<R,V>`的使用方法和泛型的使用方法(当成已知量）
- 如果像使用对象本生的话(`this`) 就需要使用非静态方法,否则使用静态方法
```java
@Data
public class PageQuery {  // 分页查询实体
    private Integer pageNo;
    private Integer pageSize;
    private String sortBy;
    private Boolean isAsc;

    /**
     * 表示把 PageQuery 对象转换为 Page 对象
     * @param orders
     * @return
     * @param <T>
     */
    public <T>  Page<T> toMpPage(OrderItem ... orders){
        // 1.分页条件
        Page<T> p = Page.of(pageNo, pageSize);
        // 2.排序条件
        // 2.1.先看前端有没有传排序字段
        if (sortBy != null) {
            p.addOrder(new OrderItem(sortBy, isAsc));
            return p;
        }
        // 2.2.再看有没有手动指定排序字段
        if(orders != null){
            p.addOrder(orders);  // 不断添加条件
        }
        return p;
    }

    /**
     *  只用传递默认排序字段
     * @param defaultSortBy
     * @param isAsc
     * @return
     * @param <T>
     */
    public <T> Page<T> toMpPage(String defaultSortBy, boolean isAsc){
        return this.toMpPage(new OrderItem(defaultSortBy, isAsc));
    }


    /**
     * 默认按照更新事件进行排序
     * @return
     * @param <T>
     */
    public <T> Page<T> toMpPageDefaultSortByCreateTimeDesc() {
        return toMpPage("create_time", false);
    }

    /**
     *  默认按照更新时间进行配置
     * @return
     * @param <T>
     */
    public <T> Page<T> toMpPageDefaultSortByUpdateTimeDesc() {
        return toMpPage("update_time", false);
    }
}
```
```java
@Data
@NoArgsConstructor
@AllArgsConstructor    // 表示分页结果
public class PageDTO<V> {
    private Long total;
    private Long pages;  // 表示总页数
    private List<V> list;  // 使用泛型指定总页数等信息

    /**
     * 返回空分页结果
     * @param p MybatisPlus的分页结果
     * @param <V> 目标VO类型
     * @param <P> 原始PO类型
     * @return VO的分页对象
     */
    public static <V, P> PageDTO<V> empty(Page<P> p){
        return new PageDTO<>(p.getTotal(), p.getPages(), Collections.emptyList());
    }

    /**
     * 将MybatisPlus分页结果转为 VO分页结果
     * @param p MybatisPlus的分页结果
     * @param voClass 目标VO类型的字节码
     * @param <V> 目标VO类型
     * @param <P> 原始PO类型
     * @return VO的分页对象
     */
    public static <V, P> PageDTO<V> of(Page<P> p, Class<V> voClass) {
        // 1.非空校验
        List<P> records = p.getRecords();
        if (records == null || records.isEmpty()) {
            // 无数据，返回空结果
            return empty(p);
        }
        // 2.数据转换
        List<V> vos = BeanUtil.copyToList(records, voClass);
        // 3.封装返回
        return new PageDTO<>(p.getTotal(), p.getPages(), vos);
    }

    /**
     *
     *   注意传递行为相当于 C 语言中的函数指针,就是函数式接口 Function<P,V>
     * 将MybatisPlus分页结果转为 VO分页结果，允许用户自定义PO到VO的转换方式
     * @param p MybatisPlus的分页结果
     * @param convertor PO到VO的转换函数
     * @param <V> 目标VO类型
     * @param <P> 原始PO类型
     * @return VO的分页对象
     */
    public static <V, P> PageDTO<V> of(Page<P> p, Function<P, V> convertor) {
        // 1.非空校验
        List<P> records = p.getRecords();
        if (records == null || records.isEmpty()) {
            // 无数据，返回空结果
            return empty(p);
        }
        // 2.数据转换
        List<V> vos = records.stream().map(convertor).collect(Collectors.toList());
        // 3.封装返回
        return new PageDTO<>(p.getTotal(), p.getPages(), vos);
    }
}
```
