package com.xys_myproto.server;

import com.myproto.MyMessage.MyMessage;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class Server {

    public void bind(int port)throws Exception{
        /* ���÷���˵�NIO�߳��� */
        // NioEventLoopGroup�� �Ǹ��߳��飬����һ��NIO�̣߳����������¼��Ĵ���
        // ��ʵ����������Reactor�߳��飩��
        // ������2���߳��飬1���Ƿ���˽��տͻ��˵����ӣ���һ���ǽ���SocketChannel��
        // �����д
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup WorkerGroup = new NioEventLoopGroup();

        try {
            // ServerBootstrap �࣬������NIO�������ĸ���������
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,WorkerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch){
                            
                            // ����Ĵ���
                            ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            // ��Ҫ�����Ŀ����
                            ch.pipeline().addLast(new ProtobufDecoder(MyMessage.Message.getDefaultInstance()));

                            ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());

                            ch.pipeline().addLast(new ProtobufEncoder());

                            ch.pipeline().addLast(new ServerHandler());
                            
                            ch.pipeline().addLast(new ServerHeartBeatHandler());
                        }
                    });

            // �󶨶˿�,ͬ���ȴ��ɹ�
            ChannelFuture f= b.bind(port).sync();
            // �ȴ�����˼����˿ڹر�
            f.channel().closeFuture().sync();
        }finally {
            // �ͷ��̳߳���Դ
            bossGroup.shutdownGracefully();
            WorkerGroup.shutdownGracefully();
        }
    }

    public static void main(String[]args)throws Exception{
        int port = 8080;
        new Server().bind(port);
    }

}
