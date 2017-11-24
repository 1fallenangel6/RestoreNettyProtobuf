package com.xys_myproto.client;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.myproto.MyMessage.MyMessage;
import com.netty.MessageType;
import com.netty.struct.Header;
import com.netty.struct.NettyMessage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private volatile ScheduledFuture<?> heartBeat;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
	    throws Exception {
    MyMessage.Message message=(MyMessage.Message)msg;
	// 握手成功，主动发送心跳消息
	if (message.getHeader()!=null
			&&message.getHeader()==MyMessage.MSG.Login_Response) {
	    heartBeat = ctx.executor().scheduleAtFixedRate(
		    new HeartBeatHandler.HeartBeatTask(ctx), 0, 5000,
		    TimeUnit.MILLISECONDS);
	} else if (message.getHeader() != null
			&& message.getHeader()==MyMessage.MSG.HeartBeat_Response) {
	    System.out
		    .println("Client receive server heart beat message : ---> "
			    + message);
	} else
	    ctx.fireChannelRead(msg);
    }

    private class HeartBeatTask implements Runnable {
	private final ChannelHandlerContext ctx;

	public HeartBeatTask(final ChannelHandlerContext ctx) {
	    this.ctx = ctx;
	}

	@Override
	public void run() {
	    MyMessage.Message heartBeat = buildHeatBeat();
	    System.out
		    .println("222Client send heart beat messsage to server : ---> "
			    + heartBeat);
	    ctx.writeAndFlush(heartBeat);
	}

	private MyMessage.Message buildHeatBeat() {

	    	MyMessage.Message.Builder builder = MyMessage.Message.newBuilder();
	    	builder.setHeader(MyMessage.MSG.HeartBeat_Request);
	        return builder.build();
	    }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	    throws Exception {
	cause.printStackTrace();
	if (heartBeat != null) {
	    heartBeat.cancel(true);
	    heartBeat = null;
	}
	ctx.fireExceptionCaught(cause);
    }
}
