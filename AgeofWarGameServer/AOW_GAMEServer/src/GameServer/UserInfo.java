package GameServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;

public class UserInfo {

	String sessionMac;
	String sessionId;
	String gameNickName;
	ChannelHandlerContext ctx;
	ChannelId ctxid;  
	
	UserInfo(){}
	
	UserInfo(String sessionMac,String sessionId,String gameNickName,ChannelHandlerContext ctx)
	{
		this.sessionMac=sessionMac;
		this.sessionId=sessionId;
		this.gameNickName=gameNickName;
		this.ctx=ctx;
	
	}
	
}
