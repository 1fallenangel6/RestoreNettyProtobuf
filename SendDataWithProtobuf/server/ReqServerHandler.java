package com.SendDataWithProtobuf.server;

import com.test.protobuf.PersonProbuf;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.ChannelInboundHandlerAdapter;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by IntelliJ IDEA 14.
 * User: karl.zhao
 * Time: 2015/12/28 0028.
 */
@Sharable
public class ReqServerHandler extends ChannelInboundHandlerAdapter{
	private ChannelHandlerContext chc;
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg)throws Exception{
    	PersonProbuf.Person people  = (PersonProbuf.Person)msg;
    	for(SocketChannel ch : ReqServer.channelMap.values()) {
			if(ctx.channel().equals(ch))
				continue;
			ch.writeAndFlush(msg);
		}
    }

    private PersonProbuf.Person resp(long peopleID){
        PersonProbuf.Person.Builder builder = PersonProbuf.Person.newBuilder();
        builder.setId(peopleID);
        builder.setName("karl");
        builder.setSex("boy");
        builder.setTel("110");
        return builder.build();
    }

    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }
}
