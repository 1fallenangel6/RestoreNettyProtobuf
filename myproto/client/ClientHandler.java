package com.myproto.client;


import com.myproto.MyMessage.MyMessage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    public ClientHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
          ctx.writeAndFlush(LoginRequest());
    }

    private MyMessage.Message LoginRequest() {
    	MyMessage.LoginRequest.Builder Login=MyMessage.LoginRequest.newBuilder();
    	Login.setUsername("E11514003");
    	Login.setPassword("123456");

    	MyMessage.Request.Builder Request=MyMessage.Request.newBuilder();
    	Request.setLogin(Login.build());
    	
    	MyMessage.Message.Builder builder = MyMessage.Message.newBuilder();
    	builder.setHeader(MyMessage.MSG.Login_Request);
    	builder.setRequest(Request.build());
    	
        return builder.build();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) 
    		throws Exception {
    	MyMessage.Message message=(MyMessage.Message)msg;
      	if(message.getHeader() != null
      			&& message.getHeader()==MyMessage.MSG.Login_Response) {
    		if(message.getResponse().getResult()){
    			System.out.println("µÇÂ¼³É¹¦");
    			ctx.fireChannelRead(msg);
    		}
    		else {
    			System.out.println(message.getResponse().getErrorDescribe());
    			ctx.close();
    		}
    	}else{
    		ctx.fireChannelRead(msg);
    	}
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    	    throws Exception {
    	ctx.fireExceptionCaught(cause);
        }
}

