
package com.myproto.server;

import com.myproto.MyMessage.MyMessage;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHeartBeatHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
	    throws Exception {
    MyMessage.Message message=(MyMessage.Message)msg;
	// 返回心跳应答消息
	if (message.getHeader()==MyMessage.MSG.HeartBeat_Request) {
	    System.out.println("Receive client heart beat message : ---> "
		    + message);
	    MyMessage.Message heartBeat = buildHeatBeat();
	    System.out
		    .println("Send heart beat response message to client : ---> "
			    + heartBeat); 
	    ctx.writeAndFlush(heartBeat);
	} else
	    ctx.fireChannelRead(msg);
    }

    private MyMessage.Message buildHeatBeat() {

    	MyMessage.Message.Builder builder = MyMessage.Message.newBuilder();
    	builder.setHeader(MyMessage.MSG.HeartBeat_Response);
        return builder.build();
    }
}


