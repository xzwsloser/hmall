# Docker介绍
- 一种快速构建,运行,管理应用的工具
# Docker快速入门
## 安装 Docker

- 首先需要卸载 `docker` : `sudo apt remove docker*`
- 之后运行脚本,参见: [https://github.com/tech-shrimp/docker_installer](https://github.com/tech-shrimp/docker_installer)
## 利用 Docker 部署 Mysql

- 首先需要找到国内可以使用的镜像网站,还是参考上面的链接
- 注意运行`docker`命令时需要在前面加上`sudo`
- 执行如下命令:
```shell
$ sudo docker run -d --name mysql-test -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql
```

- 可以把 `docker` 下载的软件理解成 `windows`中的安装包,点击就可以使用了
### 镜像和容器

- 当我们使用 `Docker`安装应用时，`Docker`会自动搜索并且下载应用**镜像(**`**image**`**) **。镜像不仅仅包含应用本身,还包含应用运行需要的环境，配置和系统函数库等信息 。`Docker`会在运行镜像的时候创建一个隔离环境,称为**容器(**`**container**`**)**
- 这就解决了多个服务运行在同一个服务器上产生的依赖冲突问题,比如 `jdk`的冲突等问题,这是由于每一个容器之间都是隔离的,并且镜像可以多次启动,只用拉去一次到本地仓库就可以了
- 镜像仓库:  存储和管理镜像的平台, `Docker`官方提供了一个公共仓库: `Docker Hub`(但是现在已经被封了,可以使用国内的镜像(阿里云不可以用))
- `Docker`拉取镜像的过程如下：

![Screenshot_20240724_110751_tv.danmaku.bilibilihd.jpg](https://cdn.nlark.com/yuque/0/2024/jpeg/40754486/1721790518035-1e1fe994-ead6-4827-8821-8b0b88266d8a.jpeg#averageHue=%23e1c9b8&clientId=u2dbbca3d-8ff8-4&from=paste&height=800&id=u07d29734&originHeight=1200&originWidth=2000&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=480322&status=done&style=none&taskId=u19d7ab9b-f4ad-4fd6-ac7a-e221d133890&title=&width=1333.3333333333333)
### 命令解读

- 利用 `Docker`部署 `mysql`的命令如下:
```shell
$ sudo docker run -d --name mysql-test -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql
```

- 命令解读:
   - `docker run`: 表示创建并且运行一个容器,`-d`让容器在后台运行
   - `--name mysql`表示指定容器名称为 `mysql`(注意需要唯一)
   - `-p 3306:3306`表示设置端口映射,这是因为容器有自己的 `IP`地址甚至文件系统,但是外界如果直接访问容器的 `IP`地址无法成功,所以需要进行端口映射,让外界访问服务器的某一个端口时就可以访问到对应容器的某一个端口(相当于一个转发) `-p 宿主端口:容器端口`宿主机端口可以发生变化,容器里面的端口一致
   - `-e KEY=VALUE`: 表示设置环境变量,可以根据官方文档确定就可以了
   - `mysql`指定运行镜像的名称
- 镜像名称的指定方式:
   - `[repository]:[tag]`,其中`repository`表示进行名称,`tag`表示镜像版本,比如 `mysql:5.7`
# Docker基础
## 常见命令

- 利用 `docker pull`从镜像仓库拉取镜像
- 利用 `docker push` 把本地镜像推送到镜像仓库(可以是公司内部的私服)
- 利用 `docker images`查看本地所有镜像
- 利用 `docker rmi`删除本地镜像
- 利用 `docker build`根据 `dockerfile`构建镜像
- 利用 `docker save`把镜像打成压缩包
- 利用 `docker load`把压缩包解压成镜像
- 利用 `docker run`命令利用镜像创建容器并且运行容器
- 利用 `docker stop`停止容器中的相关服务的进程
- 利用 `docker start`开启容器中的相关服务
- 利用 `docker ps`查看容器中的进程
- 利用 `docker rm`删除容器
- 利用 `docker logs`查看容器运行时产生的日志
- 利用 `docker exec`进行容器执行某些命令

![Screenshot_20240724_113601_tv.danmaku.bilibilihd.jpg](https://cdn.nlark.com/yuque/0/2024/jpeg/40754486/1721792181434-5f857b4a-7ec6-4171-b966-481d0272cfa8.jpeg#averageHue=%23c8c8c7&clientId=u2dbbca3d-8ff8-4&from=paste&height=800&id=u3202c1bd&originHeight=1200&originWidth=2000&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=388214&status=done&style=none&taskId=u455a604e-3ef3-4238-bb6e-771cd1dc8c7&title=&width=1333.3333333333333)

- 一般拉取镜像使用镜像的方式如下:
   - 首先查看官方文档中有关镜像的信息
   - 之后利用 `docker pull`拉取镜像到本地供容器使用
- 注意学会使用 `sudo docker Xxx --help`命令查看命令用法
- 一个小的技巧,在 `linux`中可以通过编辑 `~/.bashrc`的方式来配置命令的别名,配置文件内容如下：
```shell
alias dps='sudo docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Ports}}\t{{.Status}}"'
alias dis='sudo docker images'
```

- 最后使用 `source ~/.bashrc`就可以使得配置文件生效
## 数据卷

- 数据卷(`volume`)是一个**虚拟目录**,是**容器内目录和宿主机目录之间映射**的桥梁
- 利用 `Docker`容器创建虚拟目录之后,那么就可以在宿主机上面创建文件,一般都会在 `/var/lib/docker/volumes/`目录下创建相应的数据卷,之后只用把数据卷挂载到容器中的某一个目录上就可以完成宿主姐目录和容器目录的双向绑定,就可以在宿主机上修改文件从而引发容器内配置文件的变化
- 常见命令如(通过 `docker volume --help`查看)下:
   - `docker volume create` 创建数据卷
   - `docker volume ls` 查看所有数据卷
   - `docker volume rm` 删除指定的数据卷
   - `docker volume inspect` 查看某一个数据卷的详情(包含数据卷的挂载位置) 
   - `docker volume prune` 清除数据卷
- 注意在执行 `docker run`命令时,使用 `-v 数据卷:容器内目录`的方式进行数据卷的挂载,当创建容器时,如果挂在了数据卷但是数据卷不存在,就会自动创建数据卷
## 本地目录挂载

- `mysql`的容器创建的时候会自动把自己的存储数据的目录和本地目录进行挂载,从而节约容器内部的空间
- 实现方式: 在执行 `docker run`命令时,使用 `**-v 本地目录:容器内目录**`就可以完成本地目录的挂载
- 但是本地目录都需要用 `/` 或者 `./`开头,否则就会被识别为数据卷 ，比如 `./mysql`
- 容器内的相关信息可以在官方镜像文档中找到
## Dockfile语法

- 镜像就是包含了应用程序,程序运行的系统函数库,运行配置等文件的文件包,构建镜像的过程就是把上述文件打包的方式
- 镜像结构: 
   - 层(`Layer`):添加安装包,依赖,配置等信息,每一次操作都会形成一层
   - 基础镜像(`BaseImage`)应用依赖的系统函数库,环境,配置文件等信息
   - 入口(`Entrypoint`)镜像运行入口,一般是程序启动的脚本和参数
- `Dockerfile`就是一个文本文件,其中包含了一个一个的指令(`Instruction`),用指令来说明需要执行什么操作构建镜像,将来`Docker`可以根据`Dockerfile`来帮助我们构建镜像,命令如下:
| 指令 | 说明 | 实例 |
| --- | --- | --- |
| `FROM` | 指定基础镜像 | `FROM centos:6` |
| `ENV` | 设置环境变量,可以在后面的指令使用  | `ENV key value` |
| `COPY` | 拷贝本地文件到镜像的指定目录 | `COPY ./jrell.tar.gz  /tmp` |
| `RUN` | 执行`Linux`的`shell`命令,一般是安装过程额命令 | `RUN tar -zxvf /tmp.tar.gz && EXPORTSpath = /tmp/jrell:$path` |
| `EXPOSE` | 指定容器运行时监听的端口号,一般是给镜像使用者看的 | `EXPOSE 8080` |
| `ENTRYPOINT` | 镜像中的应用启动命令，容器运行时调用 | `ENTRYPOINT java -jar xx.jar`  |

### 自定义镜像

- 编写好镜像之后,可以使用如下命令构建镜像: `docker build -t myImage:1.0 .`:
   - `-t`就是给镜像起名,格式依然是`repository:tag`的格式,没有指定`tag`时,默认为`latest`
   - `.`是指定`Dockerfile`所在的目录,如果就在当前目录,就可以指定为 `"."`
- 总结一下镜像基于`Dockerfile`的启动方式:
   - 首先基于基础镜像(`FROM`指定的镜像)拉取镜像
   - 之后配置环境变量(利用 `ENV`,之后的程序中可以使用 `$环境变量名`来取出环境变量)
   - 利用`RUN`命令结合环境变量,配置容器运行时需要的环境变量
   - 执行`COPY`命令把宿主机的文件(`jar包`)拷贝到相关的目录,用于启动容器
   - 最后利用`ENTEYPOINT`指定程序的入口,容器运行时就是从这一个入口进入到程序中
## 网络

- 默认条件下,所有的容器都可以通过`bridge`的方式连接到`Docker`的一个虚拟网桥上:

![Screenshot_20240724_173454_tv.danmaku.bilibilihd.jpg](https://cdn.nlark.com/yuque/0/2024/jpeg/40754486/1721813720866-384d2fe7-f9d3-40ae-9532-5b584f030a9b.jpeg#averageHue=%23c7c5c4&clientId=u33633643-b1f8-4&from=paste&height=800&id=ubc425c1a&originHeight=1200&originWidth=2000&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=315486&status=done&style=none&taskId=u7503f073-5d6a-46ae-a404-5c8e15e0281&title=&width=1333.3333333333333)

- 网络常见的命令如下:
   - `docker network create`  创建一个网络
   - `docker network ls`  查看所有网络
   - `docker network rm` 删除指定的网络
   - `docker network prune`  清除没有使用的网络
   - `docker network connect`  让指定的容器加入到某一个网络中
   - `docker network disconnect` 使得指定容器连接离开某一个网络
   - `docker network inspect`  查看网络详细信息
- 同时也可以使用  `--network  网桥名称`就可以在容器创建的同时自动把某一个容器加入到某一个网段
# 项目的部署
## 后端 java 项目的部署

- 首先利用 `maven` 得到 `jar`包，之后编写 `Dockfile` 上传到远程的服务器,利用 `docker build` 创建镜像，之后利用 `docker run` 命令构建启动容器
- 注意需要把容器和需要使用的服务放在一个网络中,可以使用 `docker network connet` 完成操作
- 如果遇到错误情况,可以使用 `docker logs -f`查看日志信息
## 前端项目的部署

- 首先在前端页面中进行相应的配置，主要就是配置前端页面的访问地址,`nginx`反向代理的地址(后端项目的地址(一般就是之前构建的容器的地址))
- 把创建的 `nginx`容器的放 `html`文件的目录(可以在官方查看相关信息)和自己的 `html` 目录进行关联,并且把容器中的 `conf`文件和自己的 `nginx.conf`文件进行关联就可以了
- 最后创建注意把前端项目和后端项目放在一个网络中(`docker run`命令或者`docker network connect`命令就可以了)
## DockerCompose

- `Docker Compose`通过一个单独的 `docker-compose.yml`模板文件来定义一组关联的应用容器，帮助我们实现**多个相互关联的**`**Docker**`**容器的快速部署**
- 一个项目(`project`)中就包含多个服务(`service`),可以配置每一个 `service`的参数信息,比如容器名称和镜像等信息
- 之后就可以利用 `docker compose [OPTIONS] [COMMAND]`命令就可以进行一键部署了
- `OPTIONS`参数:
   - `-f` 指定`compose`文件的路径和名称
   - `-p` 指定`project`名称
   - `-d` 表示后台运行
- `COMMAND`参数:
   - `up` 创建并且启动所有 `service`容器
   - `down` 停止并且移除所有的容器和网络
   - `ps` 列出所有运行中的容器
   - `logs`查看指定容器的日志信息
   - `stop` 停止容器
   - `start` 启动容器
   - `restart` 重启容器
   - `top` 查看运行中的进程
   - `exec` 在指定的运行的容器中执行命令
- 和 `docker` 命令的区别就是作用区域就是就是本 `project`文件
- 比如`docker-compose.yml`的内容:
   - 其实就是定义服项目中需要使用到的容器的相关信息，如果是已经存在的容器那么就需要指定镜像和端口号等信息，如果是自定义容器还需要指定 `Dockerfile`的路径
```yaml
version: "3.8"

services:
  mysql:
    image: mysql
    container_name: mysql
    ports:
      - "3306:3306"
    environment:
      TZ: Asia/Shanghai
      MYSQL_ROOT_PASSWORD: 123
    volumes:
      - "./mysql/conf:/etc/mysql/conf.d"
      - "./mysql/data:/var/lib/mysql"
      - "./mysql/init:/docker-entrypoint-initdb.d"
    networks:
      - hm-net
  hmall:
    build: 
      context: .
      dockerfile: Dockerfile
    container_name: hmall
    ports:
      - "8080:8080"
    networks:
      - hm-net
    depends_on:
      - mysql
  nginx:
    image: nginx
    container_name: nginx
    ports:
      - "18080:18080"
      - "18081:18081"
    volumes:
      - "./nginx/nginx.conf:/etc/nginx/nginx.conf"
      - "./nginx/html:/usr/share/nginx/html"
    depends_on:
      - hmall
    networks:
      - hm-net
networks:
  hm-net:
    name: hmall
```


