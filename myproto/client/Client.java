package com.myproto.client;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.myproto.MyMessage.MyMessage;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class Client {
	private ScheduledExecutorService executor = Executors
		    .newScheduledThreadPool(1);
	EventLoopGroup group = new NioEventLoopGroup();
	
	public void connect(String host,int port)throws Exception{
        // 配置服务端的NIO线程组
        try {
            // Bootstrap 类，是启动NIO服务器的辅助启动类
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception{
                        	//解码用 
                            ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            //构造函数传递要解码成的类型
                            ch.pipeline().addLast(new ProtobufDecoder(MyMessage.Message.getDefaultInstance()));
                            
                          //编码用  
                            ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            ch.pipeline().addLast(new ProtobufEncoder());
                          //业务逻辑用  这里如果再有一个LocalDateClientHandler的话应该怎么处理
                            ch.pipeline().addLast(new ClientHandler());
                            ch.pipeline().addLast(new HeartBeatHandler());

                        }
                    });

            // 发起异步连接操作
            ChannelFuture f= b.connect(host,port).sync();
            // 等待客服端链路关闭
            f.channel().closeFuture().sync();
        }finally {
    	    // 所有资源释放完成之后，清空资源，再次发起重连操作
    	    executor.execute(new Runnable() {
    		@Override
    		public void run() {
    		    try {
    			TimeUnit.SECONDS.sleep(1);
    			try {
    			    connect(host,port);// 发起重连操作
    			} catch (Exception e) {
    			    e.printStackTrace();
    			}
    		    } catch (InterruptedException e) {
    			e.printStackTrace();
    		    }
    		}
    	    });
    	}
    }

    public static void main(String[]args)throws Exception{
        int port = 8080;
        new Client().connect("127.0.0.1",port);
    }
}
