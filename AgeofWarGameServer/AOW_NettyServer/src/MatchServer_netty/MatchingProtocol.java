package MatchServer_netty;

import java.util.Random;
import java.util.Vector;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;

public class MatchingProtocol {

	static Vector<MatchingProtocol> ConnClientList; 
	
	ChannelHandlerContext ctx;
	ChannelId ctxid;  
	String SessionID; 
	UserInfo userinfo;
	
	int matchPoint; 
	boolean matchLastFlag=false; 
	boolean matchCancleFlag=false;
	
	
	public MatchingProtocol(ChannelHandlerContext ctx) {
			// TODO Auto-generated constructor stub
			this.ctx=ctx;
			
		}
		
	
	public MatchingProtocol(ChannelHandlerContext ctx,int SessionTear,String SessionID) {
		// TODO Auto-generated constructor stub
		this.ctx=ctx;
		this.SessionID=SessionID;
		userinfo=new UserInfo();
		userinfo.SessionTear=SessionTear;
	}
	
	
	public MatchingProtocol(
			ChannelHandlerContext ctx,int SessionTear,String SessionID,String SessionMac,
			String SessionEmail,String SessionNickName,ChannelId ctxid ) 
	{
		this.ctx=ctx;
		this.ctxid=ctxid;
		this.SessionID=SessionID;
		this.userinfo=new UserInfo(
				SessionTear,
				SessionMac,
				SessionEmail,
				SessionNickName
				);
	}
	
	
	public void SetMatchPoint() {
		Random rand = new Random();
		int iValue = rand.nextInt(2); 
		matchPoint=iValue;
	}
	


}
