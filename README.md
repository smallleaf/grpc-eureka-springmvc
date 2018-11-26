### 1.本项目用来解决的问题。

1.让非springboot项目能在eureka-server注册中心注册服务，获取服务实例  
2.支持grpc框架，序列化方式采用protobuf。提供了grpc注册中心，负载均衡，与spring容器整合

注：我们依然使用springcloud来做注册中心服务，新的服务采用springcloud提供服务，老的项目采用改项目，使其能够支持eureka注册中心

### 2.本项目只能用在消费端。服务者请采用springcloud，如果要结合grpc请参考我的另外一个项目。地址：

### 3.项目待完成

1.项目支持springcloud restful请求


### 4.项目现在已经线上使用，经过半年时间的运行，每天大概500w的请求，现在暂时未发现问题。

### 5.具体使用



### 1.新建资源文件eureka-client.properties
````
# 控制是否注册自身到eureka中，本项目虽然不对外提供服务，但需要Eureka监控，在Eureka列表上显示
eureka.registration.enabled=false
# eureka相关配置
# 默认为true，以实现更好的基于区域的负载平衡。
eureka.preferSameZone=true
# 是否要使用基于DNS的查找来确定其他eureka服务器
eureka.shouldUseDns=false
# 由于shouldUseDns为false，因此我们使用以下属性来明确指定到eureka服务器的路由（eureka Server地址）
eureka.serviceUrl.default=http://XXXXXX/eureka/
# 客户识别此服务的虚拟主机名，这里指的是eureka服务本身
eureka.vipAddress=XXXplatform
#服务指定应用名，这里指的是eureka服务本身
eureka.name=XXXlatform
#服务将被识别并将提供请求的端口
eureka.port=8080
````

### 2.使用

1. 使用@EurekaAutoConfiguration开启注册功能
2. 
````$xslt
public class GrpcTagService {

    @GrpcClient("THEMESTORE-ACCOUNT")
    private Channel channel;
````

使用@GrpcClient 值是服务名


### 3.说明

服务开启后，会去注册中心拿到服务配置，根据GrpcClient配置的服务名，去取对应的grpc的ip和
使用端口。

定时监听，如果服务挂了，或者服务的grpc端口修改，会发送监听事件，注入的channel重新初始化。
保持channel及时的更新。


注意：服务中的grpc端口在注册中心中的元数据是grpc.server.port这个格式

请配合grpc-spring-cloud-starter来使用
