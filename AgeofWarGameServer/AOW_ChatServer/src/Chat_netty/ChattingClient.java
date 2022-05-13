package Chat_netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;

public class ChattingClient {

	ChattingProtocol msg; // Client info 
	ChannelHandlerContext ctx; // Client chaanel ctx  
	ChannelId id; 
	String SessionID;
	
	// Chat user Base Setting
	ChattingClient(ChannelHandlerContext ctx)
	{
		this.ctx=ctx;
		this.id=ctx.channel().id();
	}
	
	ChattingClient(ChannelHandlerContext ctx,String SessionNickName,String SessionID)
	{
		this.ctx=ctx;
		this.id=ctx.channel().id();
		this.SessionID=SessionID;
		msg=new ChattingProtocol(SessionNickName);
		
	}
	
}
