package com.netty.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

import com.netty.MessageType;
import com.netty.struct.Header;
import com.netty.struct.NettyMessage;

/**
 * @author Lilinfeng
 * @date 2014年3月15日
 * @version 1.0
 */
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {

    /**
     * Calls {@link ChannelHandlerContext#fireChannelActive()} to forward to the
     * next {@link ChannelHandler} in the {@link ChannelPipeline}.
     * 
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
	ctx.writeAndFlush(buildLoginReq());
    }

    /**
     * Calls {@link ChannelHandlerContext#fireChannelRead(Object)} to forward to
     * the next {@link ChannelHandler} in the {@link ChannelPipeline}.
     * 
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
	    throws Exception {
	NettyMessage message = (NettyMessage) msg;

	// 如果是握手应答消息，需要判断是否认证成功
	if (message.getHeader() != null
		&& message.getHeader().getType() == MessageType.LOGIN_RESP
			.value()) {
	    byte loginResult = (byte) message.getBody();
	    if (loginResult != (byte) 0) {
		// 握手失败，关闭连接
		ctx.close();
	    } else {
		System.out.println("Login is ok : " + message);
		ctx.fireChannelRead(msg);
	    }
	} else
	    ctx.fireChannelRead(msg);
    }

    private NettyMessage buildLoginReq() {
	NettyMessage message = new NettyMessage();
	Header header = new Header();
	header.setType(MessageType.LOGIN_REQ.value());
	message.setHeader(header);
	return message;
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	    throws Exception {
	ctx.fireExceptionCaught(cause);
    }
}

