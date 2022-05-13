package MatchServer_netty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


public class ServerHandler extends ChannelInboundHandlerAdapter {
	
	MatchingProtocol jsonMsg;
	ObjectMapper mapper;
	Map<String ,Object> jsonMap;
	final static String DELIMITER="{"; 
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception { 
		// TODO Auto-generated method stub
		super.channelActive(ctx);
		ReadyMatching_Q(ctx);
	}
	
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws JsonMappingException, JsonProcessingException {	
	try {
			mapper=new ObjectMapper();
			Map<String, Object > datamap = new HashMap<String, Object>();
			datamap = mapper.readValue(DELIMITER_MSG(msg), new TypeReference<Map<String, Object>>(){});
			int Event=(int)datamap.get("Event");
			
			switch(Event) 
			{
			
			case 10: 
				setUserInfo(datamap,ctx);
				System.out.println("Run10");
				break;
				
			case 20: 
				InsertMatch(datamap,ctx);
				break;
				
			case 30: 
				CancleMatch(datamap,ctx);
				break;
				
			case 40: 
				LastCancle(datamap,ctx);
				break;
				
			case 50:
				LastSuccess(datamap,ctx);
				break;
	
			}
		}
		catch (IOException e) {
			System.out.println("error ");
			e.printStackTrace();
		}
	}
	
	@Override // 연결 close
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws IOException, Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
		System.out.println("error close ->"+cause.toString());

	}
	
	public String DELIMITER_MSG(Object msg)
	{ 
		String result;
		ByteBuf msgBuffer = (ByteBuf) msg;
		
		for(int i=0;;i++) {
			char ch = msgBuffer.getChar(i);
			String st=Character.toString(ch);
			
			if(DELIMITER.equals(st)) {
				msgBuffer.readByte();
				result =msgBuffer.toString(CharsetUtil.UTF_8);
				break;
			}
			msgBuffer.readByte();
		}
		msgBuffer.clear();
		System.out.println("-> "+result);
		return result;
	}

	public void ReadyMatching_Q(ChannelHandlerContext ctx) throws JsonProcessingException {
		
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		jsonMap.put("Event", "10"); 
        String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        ctx.writeAndFlush(buffer);
		
	}

	public void setUserInfo(Map<String, Object > map, ChannelHandlerContext ctx) {
		
		MatchingProtocol.ConnClientList.add(new MatchingProtocol(
				ctx,
				(int)map.get("SessionTear"),
				(String)map.get("SessionID"),
				(String)map.get("SessionMac"),
				(String)map.get("SessionEmail"),
				(String)map.get("SessionNickName"),
				ctx.channel().id())
				);
		
	}
	
	public void CancleMatch(Map<String, Object > map,ChannelHandlerContext ctx) 
	{
		String macID=(String)map.get("SessionMac");
		for(int i=0;i<Matching.MATCHReadyList.size();i++) 
		{
			if(Matching.MATCHReadyList.get(i).userinfo.SessionMac.equals(macID))
			{
				Matching.MATCHReadyList.get(i).matchCancleFlag=true;
				Matching.MATCHReadyList.remove(i);
				break;
			}
			
			
		}
		
		for(int i=0;i<Matching.MATCHINGList_STEP1.size();i++) 
		{
			if(Matching.MATCHINGList_STEP1.get(i).userinfo.SessionMac.equals(macID)) 
			{
				Matching.MATCHINGList_STEP1.get(i).matchCancleFlag=true;
				Matching.MATCHINGList_STEP1.remove(i);
				break;
			}
		}
	}
	
	public void InsertMatch(Map<String, Object > map,ChannelHandlerContext ctx) throws IOException
	{
		int index=0;
		boolean sessionClose=false;
		String sessID=(String)map.get("SessionID");
		for(int i=0;i<MatchingProtocol.ConnClientList.size();i++)  
		{ 
		
			if(MatchingProtocol.ConnClientList.get(i).SessionID.equals(sessID) &&
					MatchingProtocol.ConnClientList.get(i).matchCancleFlag==false) //연결 중인 세션 확인  
			{
				index=i;
				sessionClose=true;
				break;
			}
		}
		if(sessionClose==false) 
			return;
		
		if(Matching.MATCHReadyList.size()!=0)
		{
			for(int j=0;j<Matching.MATCHReadyList.size();j++) 
			{
				if(Matching.MATCHReadyList.get(j).SessionID.equals(MatchingProtocol.ConnClientList.get(index).SessionID)) 
					return;
			}
		}
		Matching.MATCHReadyList.add(InsertMatch_copyObject(index));
		System.out.println("1차 매칭 등록완료 ");
		
	}
	
	public void LastMatching(ChannelHandlerContext ctx) throws JsonProcessingException
	{ 
		
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		jsonMap.put("Event", "11");
        String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
		ctx.writeAndFlush(buffer);
	}
	
	public void LastMatchingTimeOut(ChannelHandlerContext ctx) throws JsonProcessingException
	{ 
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		jsonMap.put("Event", "15"); 
        String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
		ctx.writeAndFlush(buffer);
	}
	
	public void LastCancle(Map<String, Object > map,ChannelHandlerContext ctx) throws JsonProcessingException {
		
		String macID=(String)map.get("SessionMac");
		for(int i=0;i<Matching.tmpMATCHINGList.size();i++) 
		{
			if(Matching.tmpMATCHINGList.get(i).firstUser.userinfo.SessionMac.equals(macID) || 
					Matching.tmpMATCHINGList.get(i).SecondUser.userinfo.SessionMac.equals(macID))
			{
				LastCancleUIEvent(Matching.tmpMATCHINGList.get(i).firstUser.ctx,Matching.tmpMATCHINGList.get(i).SecondUser.ctx,macID,i);
				return;
			}
			
		}
		
	}
	
	public void LastSuccess(Map<String, Object > map,ChannelHandlerContext ctx) throws JsonProcessingException {
		
		String macID=(String)map.get("SessionMac");
		for(int i=0;i<Matching.tmpMATCHINGList.size();i++) 
		{
			if(Matching.tmpMATCHINGList.get(i).firstUser.userinfo.SessionMac.equals(macID))
			{
				LastSuccessUIEvent(Matching.tmpMATCHINGList.get(i).firstUser.ctx,Matching.tmpMATCHINGList.get(i).SecondUser.ctx,macID,i);
				Matching.tmpMATCHINGList.get(i).firstUser.matchLastFlag=true;
				return;
			}
			if(Matching.tmpMATCHINGList.get(i).SecondUser.userinfo.SessionMac.equals(macID)) 
			{
				LastSuccessUIEvent(Matching.tmpMATCHINGList.get(i).firstUser.ctx,Matching.tmpMATCHINGList.get(i).SecondUser.ctx,macID,i);
				Matching.tmpMATCHINGList.get(i).SecondUser.matchLastFlag=true;
				return;
			}
			
		}
		
	}

	public void LastCancleUIEvent(ChannelHandlerContext ct1,ChannelHandlerContext ct2,String RequestMacID,int tmpMacthingListIndex) throws JsonProcessingException {
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		jsonMap.put("LastMatchingType","2");
		jsonMap.put("Event", "12");
		if(Matching.tmpMATCHINGList.get(tmpMacthingListIndex).firstUser.userinfo.SessionMac.equals(RequestMacID)) 
		{
			
			jsonMap.put("RequestOwnerType","1");
			String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
	        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
			Matching.tmpMATCHINGList.get(tmpMacthingListIndex).firstUser.ctx.writeAndFlush(buffer);
			Matching.tmpMATCHINGList.get(tmpMacthingListIndex).firstUser.ctx.flush();
			
			jsonMap.put("RequestOwnerType","0");
			jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
			buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
			Matching.tmpMATCHINGList.get(tmpMacthingListIndex).SecondUser.ctx.writeAndFlush(buffer);
			Matching.tmpMATCHINGList.get(tmpMacthingListIndex).SecondUser.ctx.flush();
			
		}else 
		{
			jsonMap.put("RequestOwnerType","1");
			String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
	        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
			Matching.tmpMATCHINGList.get(tmpMacthingListIndex).SecondUser.ctx.writeAndFlush(buffer);
			Matching.tmpMATCHINGList.get(tmpMacthingListIndex).SecondUser.ctx.flush();
	
			jsonMap.put("RequestOwnerType","0");
			jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
			buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
			Matching.tmpMATCHINGList.get(tmpMacthingListIndex).firstUser.ctx.writeAndFlush(buffer);
			Matching.tmpMATCHINGList.get(tmpMacthingListIndex).firstUser.ctx.flush();
		
		}
		Matching.tmpMATCHINGList.get(tmpMacthingListIndex).firstUser.matchCancleFlag=true;
		Matching.tmpMATCHINGList.get(tmpMacthingListIndex).SecondUser.matchCancleFlag=true;
		
	}
	
	public void LastSuccessUIEvent(ChannelHandlerContext ct1,ChannelHandlerContext ct2,String RequestMacID,int tmpMacthingListIndex) throws JsonProcessingException {
			mapper = new ObjectMapper();
			jsonMap = new HashMap<String, Object>();
			jsonMap.put("Event", "12");
			jsonMap.put("LastMatchingType","1");
			
			if(Matching.tmpMATCHINGList.get(tmpMacthingListIndex).firstUser.userinfo.SessionMac.equals(RequestMacID)) 
			{
				jsonMap.put("RequestOwnerType","1");
				String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
		        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
				Matching.tmpMATCHINGList.get(tmpMacthingListIndex).firstUser.ctx.writeAndFlush(buffer);
								
				jsonMap.put("RequestOwnerType","0");
				jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
				buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
				Matching.tmpMATCHINGList.get(tmpMacthingListIndex).SecondUser.ctx.writeAndFlush(buffer);

			}else {
			
				jsonMap.put("RequestOwnerType","1");
				String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
		        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
				Matching.tmpMATCHINGList.get(tmpMacthingListIndex).SecondUser.ctx.writeAndFlush(buffer);
			
				jsonMap.put("RequestOwnerType","0");
				jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
				buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
				Matching.tmpMATCHINGList.get(tmpMacthingListIndex).firstUser.ctx.writeAndFlush(buffer);
			
			}
		}
	
	public MatchingProtocol InsertMatch_copyObject(int index) {
		MatchingProtocol mp= new MatchingProtocol(
				MatchingProtocol.ConnClientList.get(index).ctx,
				MatchingProtocol.ConnClientList.get(index).userinfo.SessionTear,
				MatchingProtocol.ConnClientList.get(index).SessionID,
				MatchingProtocol.ConnClientList.get(index).userinfo.SessionMac,
				MatchingProtocol.ConnClientList.get(index).userinfo.SessionEmail,
				MatchingProtocol.ConnClientList.get(index).userinfo.SessionNickName,
				MatchingProtocol.ConnClientList.get(index).ctx.channel().id()
				);
		return mp;
	}
	
	
	public void setGame(MatchingProtocol firstUser,MatchingProtocol SecondUser) throws JsonProcessingException // set game start logic 
	{
		String MatchingIndex=firstUser.userinfo.SessionNickName+SecondUser.userinfo.SessionNickName;
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("Event", "20");
		jsonMap.put("MatchingIndex", MatchingIndex);
		
		String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
		jsonStr="?"+jsonStr;
        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        firstUser.ctx.writeAndFlush(buffer);
	
        buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        SecondUser.ctx.writeAndFlush(buffer);
        
        firstUser.ctx.close();
        SecondUser.ctx.close(); 
	}

}
