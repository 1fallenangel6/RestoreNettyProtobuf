package com.netty.client;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.netty.MessageType;
import com.netty.struct.Header;
import com.netty.struct.NettyMessage;

/**
 * @author Lilinfeng
 * @date 2014��3��15��
 * @version 1.0
 */
public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {

    private volatile ScheduledFuture<?> heartBeat;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
	    throws Exception {
	NettyMessage message = (NettyMessage) msg;
	// ���ֳɹ�����������������Ϣ
	if (message.getHeader() != null
		&& message.getHeader().getType() == MessageType.LOGIN_RESP
			.value()) {
	    heartBeat = ctx.executor().scheduleAtFixedRate(
		    new HeartBeatReqHandler.HeartBeatTask(ctx), 0, 5000,
		    TimeUnit.MILLISECONDS);
	} else if (message.getHeader() != null
		&& message.getHeader().getType() == MessageType.HEARTBEAT_RESP
			.value()) {
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
	    NettyMessage heatBeat = buildHeatBeat();
	    System.out
		    .println("Client send heart beat messsage to server : ---> "
			    + heatBeat);
	    ctx.writeAndFlush(heatBeat);
	}

	private NettyMessage buildHeatBeat() {
	    NettyMessage message = new NettyMessage();
	    Header header = new Header();
	    header.setType(MessageType.HEARTBEAT_REQ.value());
	    message.setHeader(header);
	    return message;
	}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	    throws Exception {
	cause.printStackTrace();
	if (heartBeat != null) {
	    heartBeat.cancel(true);
	    System.out
	    .println("------------------------Heart Beat == NULL------------------------");
	    heartBeat = null;
	}
	ctx.fireExceptionCaught(cause);
    }
}
