package com.test.server;

import com.test.protobuf.PersonProbuf;

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

/**
 * Created by IntelliJ IDEA 14.
 * User: karl.zhao
 * Time: 2015/12/25 0025.
 */
public class ReqServer {

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
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch){
                            // protobufDecoder����������룬����֧�ֶ������������֮ǰ��һ��Ҫ�ж�����Ĵ�������
                            // �����ַ�ʽ����ѡ��
                            // ʹ��netty�ṩProtobufVarint32FrameDecoder
                            // �̳�netty�ṩ��ͨ�ð�������� LengthFieldBasedFrameDecoder
                            // �̳�ByteToMessageDecoder�࣬�Լ�������

                            // ����Ĵ���
                            ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            // ��Ҫ�����Ŀ����
                            ch.pipeline().addLast(new ProtobufDecoder(PersonProbuf.Person.getDefaultInstance()));

                            ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());

                            ch.pipeline().addLast(new ProtobufEncoder());

                            ch.pipeline().addLast(new ReqServerHandler());
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
        if(args!=null && args.length>0){
            try {
                port = Integer.valueOf(args[0]);
            }
            catch (NumberFormatException ex){}
        }
        new ReqServer().bind(port);
    }

}

