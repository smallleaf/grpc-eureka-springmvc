### 1.本项目用来解决的问题。

1.让非springboot项目能在eureka-server注册中心注册服务，获取服务实例  
2.支持grpc框架，序列化方式采用protobuf。提供了grpc注册中心，负载均衡，与spring容器整合

注：我们依然使用springcloud来做注册中心服务，新的服务采用springcloud提供服务，老的项目采用改项目，使其能够支持eureka注册中心

### 2.本项目只能用在消费端。服务者请采用springcloud，如果要结合grpc请参考我的另外一个项目。地址：[grpc-spring-cloud-start](https://github.com/smallleaf/grpc-spring-cloud-start)

### 3.项目待完成

1.grpc全局的拦截器，用来做个性化的处理
2.项目支持springcloud restful请求
3.完善非springboot项目注册到注册中心，提供服务
4.支持zk注册中心


### 4.项目现在已经线上使用，经过半年时间的运行，每天大概500w的请求。

### 5.使用方法

#### 1.资源目录建资源文件eureka-client.properties
````
# 本项目现在不提供服服务的，但是可以设置
eureka.registration.enabled=false
# eureka相关配置
# 默认为true，以实现更好的基于区域的负载平衡。
eureka.preferSameZone=true
# 由于shouldUseDns为false，因此我们使用以下属性来明确指定到eureka服务器的路由（eureka Server地址）
eureka.serviceUrl.default=http://XXXXXX/eureka/
````

#### 2.使用@EurekaAutoConfiguration开启注册功能
#### 3.@GrpcClient使用
````$xslt
public class GrpcTagService {

    @GrpcClient("THEMESTORE-ACCOUNT")
    private Channel channel;
````
使用@GrpcClient 值是服务名，这个服务名就是注册中心的服务名，根据这个名称去去得对应的服务的信息


### 6.源码解析
这里我从

1.如何获取eureka注册中心服务配置。   
2.grpc是如何创建channel，并注入到spring bean的字段当中。    
3.grpc的channel如何做负载均衡。  
4.如果注册中心获取的服务挂了，怎么处理。   


#### eureka是如何获取服务器配置信息
![](http://m.qpic.cn/psb?/V11QGjwg27loKQ/WGVPjGlHWJ*4a7GykMwMRIPESe2sjmvAes*eKg9sqKk!/b/dFIBAAAAAAAA&bo=9ATuAgAAAAADBz4!&rf=viewer_4)
第一步创建SpringEurekaClient,去服务获取服务，再根据服务名称去获取指定的服务配置信息。

#### grpc是如何创建channel
关于grpc有篇很好的博客[grpc源码解析](https://blog.csdn.net/omnispace/article/details/80167076)，大家可以去参考下。
这里我主要是讲解grpc是如何创建channel的，我们只要拿到channel才能去使用grpc服务。
![](http://m.qpic.cn/psb?/V11QGjwg27loKQ/o8kJpi6.JoRIiNSOOU5592Ng6c9954*a6BQaG3IPyH4!/b/dDQBAAAAAAAA&bo=8gc4BAAAAAADB.s!&rf=viewer_4)

@GrpcClient在spring的生命周期当中，会去查找哪些字段属性被@GrpcClient给注解了，然后根据服务名去取Grpc的ip和端口信息。创建Channel



#### grpc的channel如何做负载均衡
![](http://m.qpic.cn/psb?/V11QGjwg27loKQ/1ReGwEiLz8uqLAgsmAHDXXnJxgFdd369ZeT.7tJycxg!/b/dDYBAAAAAAAA&bo=TgXYAwAAAAADB7I!&rf=viewer_4)
1.NameResolver，start()会去eureka注册中心获取的服务配置，去根据对应的服务名称去拿，ip和端口。拿到之后就通过一定的策略放入subChannels. 
2.我们用channle(其实就是ManagedChannelImpl)进行grpc请求的时候，会去调用pickerCopy.pickSubchannel。也就是
```aidl
@Override
    public PickResult pickSubchannel(PickSubchannelArgs args) {
      if (list.size() > 0) {
        return PickResult.withSubchannel(nextSubchannel());
      }

      if (status != null) {
        return PickResult.withError(status);
      }

      return PickResult.withNoResult();
    }
    
    
    private Subchannel nextSubchannel() {
      if (list.isEmpty()) {
        throw new NoSuchElementException();
      }
      int size = list.size();

      int i = indexUpdater.incrementAndGet(this);
      if (i >= size) {
        int oldi = i;
        i %= size;
        indexUpdater.compareAndSet(this, oldi, i);
      }
      return list.get(i);
    }

```
从代码可以看出，每次去取subChannel是用的轮询，这样就形成了我们的负载均衡。

### eureka注册中心提供的服务，如果挂掉，或者服务信息修改，例如grpc的监听端口修改，如何处理
![](http://m.qpic.cn/psb?/V11QGjwg27loKQ/8RF9CjHCCaJi2KOPQsbNRKjL47e32neXb5QDqPD9WAY!/b/dFQBAAAAAAAA&bo=5gXaAwAAAAADBxg!&rf=viewer_4)

1.DiscoveryClient,会定时去注册中心更新配置信息，默认是30秒。    
2.心跳事件一直保持这个监听，如果grpc服务挂了，或者grpc的服务端口发生了改变了。那么取的配置就发生了改变了，于是listener
重新初始化地址信息。这样服务重启的时间 就能及时更新。 


### 如果eureka注册中心挂了怎么办

1.如果eureka中心挂了,此时是收不到获取注册中心配置的监听事件的，只要grpc服务没有挂，grpc服务的信息，依然保持在本地，依然可以使用。


### 如何使用
本项目使用来做客户端用的，与服务端放在一起，打包成单独的jar，提供给其他的服务。   
例如账号中心：多模块  
1.account-core:提供服务，注册到注册中心，依赖于account-model。   
2.account-model:protobuf文件编写，实体类。   
3.account-client：提供给其他服务使用，其他服务只需要调用此jar，然后调用方法就行了，不需要关注底层如何实现，
底层全部由服务端的人员编写来控制。依赖于account-model   
![](http://m.qpic.cn/psb?/V11QGjwg27loKQ/RNvCcENUhkIIYCeWI2ZhIb2a3C65MLBDcwTdSsLm5ho!/b/dLwAAAAAAAAA&bo=pgg4BAAAAAARB6I!&rf=viewer_4)



### 更新日志
#### v1.3.0  

1.添加本地可以直接连接grpc服务，方便本地测试
2.直连的grpc服务地址，可以作为eureka备份地址，如果刚启动eureka注册中心挂掉，从本地服务地址去拿，当然如果是服务启动中，eureka注册中心服务挂掉了，会直接从本地缓存中去拿已经存在的服务信息：
eureka-client.properties添加如下配置
```aidl
# 注册的类型，eureka,local,eureka表示使用eureka注册中心，local本地直接连接
rpc.register.type=eureka

# grpc服务地址，
rpc.server.local=127.0.0.1:8366

```





