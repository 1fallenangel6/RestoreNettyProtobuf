package com.xys_myproto.client;


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
/*方案1 初始化与连接不分开
 * 即在connect方法中执行初始化和发起连接
 * 
 *
*/
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
                            //
                            ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            ch.pipeline().addLast(new ProtobufDecoder(MyMessage.Message.getDefaultInstance()));
                            ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            ch.pipeline().addLast(new ProtobufEncoder());
                            ch.pipeline().addLast(new ClientHandler());
                            ch.pipeline().addLast(new HeartBeatHandler());

                        }
                    });

            // 发起异步连接操作
            ChannelFuture f= b.connect(host,port).sync();
            // 等待客服端链路关闭
            f.channel().closeFuture().sync();
        }finally {
        	/* 在此处执行优雅退出如何？会有何影响？,影响如下
        	 * 警告: Force-closing a channel whose registration task was not accepted by an event loop: [id: 0x7fea1235]
			 *java.util.concurrent.RejectedExecutionException: event executor terminated
			 *如果在此处优雅退出，后续重连操作将不能正确执行,原因是group在connect方法外面定义，关闭之后，递归
			 *调用connect时因其被关闭，故发起异步连接操作失败，将其放在connect内就没有问题了
			 *问题在于每次重连都需要重新初始化group，因此建议将初始化单独放在一个方法中
        	 */
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
    			    System.out.println(e.getMessage());
    			}
    		    } catch (InterruptedException e) {
    			e.printStackTrace();
    			System.out.println(e.getMessage());
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
