package Chat_netty;

import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.UTF8JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Vector;



public class ChattingHandler extends ChannelInboundHandlerAdapter {

	ChattingClient jsonMsg;
	ObjectMapper mapper;
	Map<String ,Object> jsonMap;
	final static String DELIMITER="{"; 
	static Vector<ChattingRoom> CHATTINGRoom; // Chatting Room 
	
	
	@Override // first channel entering -> user base setting try  
	public void channelActive(ChannelHandlerContext ctx) throws Exception { 
		// TODO Auto-generated method stub
		super.channelActive(ctx);
		
		System.out.println("HiClient ");
		int RoomNum=SearchRoom(ctx);
		getUserInfo(RoomNum,ctx); // chatting room using -> user info request to client  

	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception, JsonMappingException, JsonProcessingException{
	
		mapper=new ObjectMapper();
		Map<String, Object > datamap = new HashMap<String, Object>();
		datamap = mapper.readValue(DELIMITER_MSG(msg), new TypeReference<Map<String, Object>>(){});
		int Event=(int)datamap.get("Event");
		
		switch(Event) 
		{
		
		case 20: // user info setting
			setUserInfo(datamap,ctx);
			System.out.println("User chaanel set");
			break;
			
		case 30: // channel list size get 
			getChannelList(ctx);
			break;
			
		case 40: //  channel move, send chaanel user list 
			getChannelUserList(datamap,ctx);
			System.out.println("channel user list set up ing");
			break;
		case 41: // first chaanel entring handling, user list send 
			first_getChannelUserList((int)datamap.get("ChannelNum"),ctx);
			break;
			
		case 50: // chatting msg send 
			sendChattingMsg(datamap);
			break;
			
		case 100: // enmerency msg handling  
			System.out.println("BoradCast MSG");
			EnmergencySendMessage(datamap);
			break;
		}
		
	}
	
	
	@Override 
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws IOException, Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
		System.out.println("error close ->"+cause.toString());


	}
	
	public String DELIMITER_MSG(Object msg) 
	{ 
		
		int index=0;
		String result;
		String EnmergencyResult;
		ByteBuf msgBuffer = (ByteBuf) msg;
		EnmergencyResult=msgBuffer.toString(Charset.defaultCharset());
		for(int i=0;;i++) 
		{
			char ch = msgBuffer.getChar(i);
			String st=Character.toString(ch);
			if(DELIMITER.equals(st)) 
			{
				msgBuffer.readByte();
				result =msgBuffer.toString(CharsetUtil.UTF_8);
				break;
			}
			try {
				msgBuffer.readByte();
			}catch(IndexOutOfBoundsException e) {
				
				System.out.println("en msg");
				result=EnmergencyResult;
				break;
			}
		}
		msgBuffer.clear();
		return result;
	}


	// enter chatting room setting -> did creating room check or empty chatting check
	public int SearchRoom(ChannelHandlerContext ctx) 
	{
		int roomNum;
		if(CHATTINGRoom.size()==0) //if all channel empty -> Logic that can be a problem in actual service 
		{
			CHATTINGRoom.add(new ChattingRoom());
			if(CHATTINGRoom.get(0).chatUserList==null) 
				CHATTINGRoom.get(0).chatUserList = new Vector<ChattingClient>();
			return 0;
		}
		return getChannelRoomNum();
	}
	
	// channel cufusion check -> user give ChatRoomNum to enter
	public int getChannelRoomNum() 
	{
		int bestRoomNum=0; // now low Confusion RoomNum 
		int tmp=CHATTINGRoom.size();
		for(int i=0;i<tmp;i++)
		{
			if((i+1==tmp) && CHATTINGRoom.get(i).RoomMax==CHATTINGRoom.get(i).getRoomSize())  //if last channel out of size? -> create channel  
			{																				 //Logic to be modified...
				CHATTINGRoom.add(new ChattingRoom());
				bestRoomNum=CHATTINGRoom.size()-1;
				if(CHATTINGRoom.get(bestRoomNum).chatUserList==null) 
					CHATTINGRoom.get(bestRoomNum).chatUserList = new Vector<ChattingClient>();
			}
				
			if(CHATTINGRoom.get(i).RoomMax==CHATTINGRoom.get(i).getRoomSize()) //channel out of size
				continue;	
			
			if(CHATTINGRoom.get(bestRoomNum).getRoomSize()>CHATTINGRoom.get(i).getRoomSize()) // Comparison of current Congestion and next congestion.
				bestRoomNum=i;
		}
	
		return bestRoomNum;
	}
	
	//user info get to add that chatting 
	public void getUserInfo(int RoomNum,ChannelHandlerContext ctx) throws JsonProcessingException 
	{
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		jsonMap.put("Event", "50"); 
		jsonMap.put("ChannelNum",RoomNum); 
        String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        ctx.writeAndFlush(buffer);
		
	}
	
	//user info setting + enter chatting room  
	public void setUserInfo(Map<String, Object > data,ChannelHandlerContext ctx) throws JsonProcessingException 
	{
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		int RoomNum=(int)data.get("ChannelNum");
		String SessionNickName=(String)data.get("SessionNickName");
		String SessionID=(String)data.get("SessionID");
		CHATTINGRoom.get(RoomNum).chatUserList.add(new ChattingClient(ctx,SessionNickName,SessionID)); //add chatUser to chattingroom 
	
		
		jsonMap.put("Event","51"); // first entering userlist synchronization
		jsonMap.put("ChannelNum",RoomNum);
        String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        ctx.writeAndFlush(buffer);

	}


	//  response
	public void getChannelList(ChannelHandlerContext ctx) throws JsonProcessingException 
	{
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		ArrayList<Integer> channerllistusersize=getChannelUserListSize();
		jsonMap.put("Event", "60"); 
		jsonMap.put("ChannelListSize",channerllistusersize); 
        String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        ctx.writeAndFlush(buffer);
	}
	
	//each chaanel user size get  
	public ArrayList<Integer> getChannelUserListSize() 
	{
		ArrayList<Integer> tmp= new ArrayList<>();
		int index=CHATTINGRoom.size();
		for(int i=0;i<index;i++) 
		{
			tmp.add(CHATTINGRoom.get(i).getRoomSize());
		}
		
		return tmp;
	}
	
	// each channel user list response  
	public void getChannelUserList(Map<String, Object > data,ChannelHandlerContext ctx) throws JsonProcessingException 
	{
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		
		int ChannelNum=(int) data.get("ChannelNum");
		int PreviousChannelNum=(int) data.get("PreviousChannelNum");
		String PreviousSessionID=(String) data.get("SessionID");
		ArrayList<String> channerlUserList=new ArrayList<>();
		int checkflag=0;
		int Previousindex=CHATTINGRoom.get(PreviousChannelNum).chatUserList.size(); //previous chaanel 
		int index;
		
		if(!(CHATTINGRoom.get(ChannelNum).RoomMax==CHATTINGRoom.get(ChannelNum).getRoomSize())) { // dont move channel
			
			for(int i=0;i<Previousindex;i++) //search removing channel 
			{
				if(CHATTINGRoom.get(PreviousChannelNum).chatUserList.get(i).SessionID.equals(PreviousSessionID)) 
				{
					System.out.println("get it");
					CHATTINGRoom.get(ChannelNum).chatUserList.add(new ChattingClient(
							ctx,
							CHATTINGRoom.get(PreviousChannelNum).chatUserList.get(i).msg.SessionNickName,
							PreviousSessionID)); //moveing ChatUser add  
					CHATTINGRoom.get(PreviousChannelNum).chatUserList.remove(i); //remove  
					
					System.out.println("moveing ChaanelUser nickname ->"+CHATTINGRoom.get(ChannelNum).chatUserList.get(0).msg.SessionNickName);
					break;
				}
			}
			
			index=CHATTINGRoom.get(ChannelNum).chatUserList.size();
			for(int i=0;i<index;i++) 
			{
				channerlUserList.add(CHATTINGRoom.get(ChannelNum).chatUserList.get(i).msg.SessionNickName); // Userlist data setup each channel 
				System.out.println("dataRun?");
			}
			checkflag=1;
		}
		
		jsonMap.put("Event", "70");
		jsonMap.put("ChannelUserList",channerlUserList); 
		jsonMap.put("ChannelNum", ChannelNum);
		jsonMap.put("CheckFlag",checkflag); //1 change the channel, 0 dont change 
        String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        for(int i=0;i<CHATTINGRoom.get(ChannelNum).chatUserList.size();i++) 
		{
			ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
			CHATTINGRoom.get(ChannelNum).chatUserList.get(i).ctx.writeAndFlush(buffer);
			System.out.println("run buffer inside");
		}

	}
	
	// each channel userlist response to entering first channel
	public void first_getChannelUserList(int RoomNum,ChannelHandlerContext ctx) throws JsonProcessingException 
	{
			mapper = new ObjectMapper();
			jsonMap = new HashMap<String, Object>();
			int ChannelNum=RoomNum;
			int index=CHATTINGRoom.get(ChannelNum).chatUserList.size();
			int checkflag=1; // must enter! 
			ArrayList<String> channerlUserList=new ArrayList<>();
			
			for(int i=0;i<index;i++) 
			{
				channerlUserList.add(CHATTINGRoom.get(ChannelNum).chatUserList.get(i).msg.SessionNickName); // data setup each user list  
				System.out.println("check");
			}
			
			jsonMap.put("Event", "70");
			jsonMap.put("ChannelUserList",channerlUserList); 
			jsonMap.put("ChannelNum", ChannelNum);
			jsonMap.put("CheckFlag",checkflag); //1 change the channel, 0 dont change 
			String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
			for(int i=0;i<CHATTINGRoom.get(ChannelNum).chatUserList.size();i++) 
			{
				ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
				CHATTINGRoom.get(ChannelNum).chatUserList.get(i).ctx.writeAndFlush(buffer);
				System.out.println("run buffer inside");
			}
	
	}
	
	// EmergencyMsg send
	public void EnmergencySendMessage(Map<String, Object > data) throws JsonProcessingException 
	{
		String EmergencyMsg=(String) data.get("EmergencyMsg");
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		jsonMap.put("Event",100);
		jsonMap.put("EmergencyMsg",EmergencyMsg);
		String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
		for(int i=0;i<CHATTINGRoom.size();i++) // msg broadcast 
		{
			for(int j=0;j<CHATTINGRoom.get(i).chatUserList.size();j++)
			{	
				ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
				CHATTINGRoom.get(i).chatUserList.get(j).ctx.writeAndFlush(buffer);
				System.out.println("EnmergencySend Complite");
			}
		}
	}
		
	// chatting msg send
	public void sendChattingMsg(Map<String, Object > data) throws JsonProcessingException 
	{
		
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		String msg=(String)data.get("MSG");
		String senderNickName=(String)data.get("SenderNickName");
		int channelNum=(int)data.get("ChannelNum");
		jsonMap.put("Event","80"); //  event synchronization to enter first channel userlist 
		jsonMap.put("SenderNickName",senderNickName);
		jsonMap.put("MSG",msg);
		System.out.println("send name->"+senderNickName);
		int index=CHATTINGRoom.get(channelNum).chatUserList.size();
		for(int i=0;i<index;i++) 
		{
			 String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
			 ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
			 CHATTINGRoom.get(channelNum).chatUserList.get(i).ctx.writeAndFlush(buffer);
		}
	}
	
	
	
	
}
