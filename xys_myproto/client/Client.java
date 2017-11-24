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
/*����1 ��ʼ�������Ӳ��ֿ�
 * ����connect������ִ�г�ʼ���ͷ�������
 * 
 *
*/
	public void connect(String host,int port)throws Exception{
        // ���÷���˵�NIO�߳���
		
        try {
            // Bootstrap �࣬������NIO�������ĸ���������
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

            // �����첽���Ӳ���
            ChannelFuture f= b.connect(host,port).sync();
            // �ȴ��ͷ�����·�ر�
            f.channel().closeFuture().sync();
        }finally {
        	/* �ڴ˴�ִ�������˳���Σ����к�Ӱ�죿,Ӱ������
        	 * ����: Force-closing a channel whose registration task was not accepted by an event loop: [id: 0x7fea1235]
			 *java.util.concurrent.RejectedExecutionException: event executor terminated
			 *����ڴ˴������˳�����������������������ȷִ��,ԭ����group��connect�������涨�壬�ر�֮�󣬵ݹ�
			 *����connectʱ���䱻�رգ��ʷ����첽���Ӳ���ʧ�ܣ��������connect�ھ�û��������
			 *��������ÿ����������Ҫ���³�ʼ��group����˽��齫��ʼ����������һ��������
        	 */
    	    // ������Դ�ͷ����֮�������Դ���ٴη�����������
    	    executor.execute(new Runnable() {
    		@Override
    		public void run() {
    		    try {
    			TimeUnit.SECONDS.sleep(1);
    			try {
    			    connect(host,port);// ������������
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
