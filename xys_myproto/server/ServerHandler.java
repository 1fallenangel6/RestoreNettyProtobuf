package com.xys_myproto.server;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.ByteString;
import com.myproto.MyMessage.MyMessage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter{
	private Map<String,String> user=new HashMap<String,String>();
	private Map<String, Boolean> nodeCheck = new ConcurrentHashMap<String, Boolean>();
	private String account;
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg)throws Exception{
    	MyMessage.Message message=(MyMessage.Message)msg;
    	String username=message.getRequest().getLogin().getUsername();
    	String password=message.getRequest().getLogin().getPassword();
    	account=username;
    	user.put("E11514003", "123456");
      	if(message.getHeader() != null
      			&& message.getHeader()==MyMessage.MSG.Login_Request) {
      		if(nodeCheck.containsKey(username)) {
      			ctx.writeAndFlush(LoginResponse(false,"ÖØ¸´µÇÂ¼"));
      		}else {
      			if(user.containsKey(username)) {
        			if(user.get(username).equals(password)) {
        				System.out.println(password);
        				nodeCheck.put(username, true);
        				ctx.writeAndFlush(LoginResponse(true,"µÇÂ¼³É¹¦"));
        			}else {
        				ctx.writeAndFlush(LoginResponse(false,"ÃÜÂë´íÎó"));
        			}
        		}else {
        			ctx.writeAndFlush(LoginResponse(false,"ÕËºÅ²»´æÔÚ"));
        		}
        		ctx.writeAndFlush(msg);
      		}
    	}else{
    		ctx.fireChannelRead(msg);
    	}
    }

    private MyMessage.Message LoginResponse(boolean result,String error) {
    
    	
    	MyMessage.Message.Builder builder = MyMessage.Message.newBuilder();
    	builder.setHeader(MyMessage.MSG.Login_Response);
    	
    	MyMessage.Response.Builder Res=MyMessage.Response.newBuilder();
    	Res.setResult(result);
    	Res.setErrorDescribe(error);
    	
    	builder.setResponse(Res.build());
        return builder.build();
    }
    
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    	    throws Exception {
    	cause.printStackTrace();
    	nodeCheck.remove(account);// É¾³ý»º´æ
    	ctx.close();
    	ctx.fireExceptionCaught(cause);
        }
}

