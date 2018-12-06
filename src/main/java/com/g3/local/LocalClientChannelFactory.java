package com.g3.local;

import com.g3.GrpcChannelFactory;
import io.grpc.*;

import java.util.List;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/12/4
 */
public class LocalClientChannelFactory  implements GrpcChannelFactory {


    private String host;

    private int port;

    public LocalClientChannelFactory(String localGrpcServer){
        try {
            String[] address = localGrpcServer.split(":");
            host = address[0];
            port = Integer.valueOf(address[1]);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Channel createChannel(String name, List<ClientInterceptor> interceptors) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host,port)
                .usePlaintext(true)
                .build();
        return channel;
    }
}
