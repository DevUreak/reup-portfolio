package GameServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.plaf.synth.SynthSplitPaneUI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class GameHandler extends ChannelInboundHandlerAdapter{
	
	ObjectMapper mapper;
	Map<String ,Object> jsonMap;
	final static String DELIMITER="{"; 
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception { 
		// TODO Auto-generated method stub
		super.channelActive(ctx);
		System.out.println("Hello GameClient");
		getGameInfo(ctx);
	
	}
	
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws JsonMappingException, JsonProcessingException 
	{	
		try {
			
			mapper=new ObjectMapper();
			Map<String, Object > data = new HashMap<String, Object>();
			data = mapper.readValue(DELIMITER_MSG(msg), new TypeReference<Map<String, Object>>(){});
			int Event=(int)data.get("Event");
	
			switch(Event) 
			{
				case 10: // 게임방 생성및 입장 
					setGame(ctx,data);
					break;
				case 11: // 게임 준비 및 게임 시작 알리는 이벤트 
					SendGameReady(data);
					break;
				case 20: // 게임 이벤트 처리(생산)
					setGameEvent(data);
					break;
			}
		}
		catch (IOException e) {System.out.println("error ");e.printStackTrace();}
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
		return result;
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws IOException, Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);

	}
	
	//게임 정보 요청 
	public void getGameInfo(ChannelHandlerContext ctx) throws JsonProcessingException { // 게임 방 설정을 위한 기본 정보 request
		
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		jsonMap.put("Event", "1"); 
        String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        ctx.writeAndFlush(buffer);
	
	}
	
	//게임 방 생성및 입장 
	public void setGame(ChannelHandlerContext ctx,Map<String, Object > data) throws JsonProcessingException { // 게임 방 설정을 위한 기본 정보 request
		
		int userType=(int)data.get("UserType"); // 1생성,2입장 
		
		if(userType==1) 
			CreateRoom(ctx,data); //방생성 
		else if(userType==2) 
			EnterRoom(ctx,data);
	}

	//방생성
	public void CreateRoom(ChannelHandlerContext ctx,Map<String, Object > data) 
	{
		String game_macid=(String)data.get("game_Macid");
		String game_sessionid=(String)data.get("game_Sessionid");
		String game_usernickname=(String)data.get("game_UserNickName");
		String game_gameIndex=(String)data.get("game_Ggameindex");
		System.out.println("Create Room UserName -> "+game_usernickname);
		GameServer.GameRoom.add(new GameRoom(game_usernickname, game_sessionid, game_macid, game_gameIndex,ctx,this));
	}
	
	//방입장 
	public void EnterRoom(ChannelHandlerContext ctx,Map<String, Object > data) 
	{
		String game_macid = (String) data.get("game_Macid");
		String game_sessionid = (String) data.get("game_Sessionid");
		String game_usernickname = (String) data.get("game_UserNickName");
		String game_gameIndex = (String) data.get("game_Ggameindex");
		

		for(int index=0;index<=GameServer.GameRoom.size();++index) 
		{
			if(index==GameServer.GameRoom.size()) 
			{
				index=-1;
				continue;
			}	
			if (GameServer.GameRoom.get(index).game_Inex.equals(game_gameIndex)) {
				GameServer.GameRoom.get(index).player2.ctx=ctx;
				GameServer.GameRoom.get(index).player2.gameNickName=game_usernickname;
				GameServer.GameRoom.get(index).player2.sessionId=game_sessionid;
				GameServer.GameRoom.get(index).player2.sessionMac=game_macid;
				GameServer.GameRoom.get(index).GameRun(); // 게임 시작 
				System.out.println("Enter Player2");
				break;
			}
			
		}
	
	}
	
	//게임 활성화
	void ActiveGame(int RoomNum) throws JsonProcessingException 
	{
	
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		jsonMap.put("Event", "20");  //게임 관련 이벤트 20이상 부터 
		
        String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        
        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        GameServer.GameRoom.get(RoomNum).player1.ctx.writeAndFlush(buffer);
       
        
        ByteBuf buffer2 = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        GameServer.GameRoom.get(RoomNum).player2.ctx.writeAndFlush(buffer2);

        System.out.println("Run ACTIVE MSG");
        GameServer.GameRoom.get(RoomNum).AutoTimeTask.RunningAutoTimer(); // 타이머온 
	}

	//게임 준비 전송해주기 
	void SendGameReady(Map<String, Object > data) throws JsonProcessingException 
	{
		
		String game_gameIndex = (String) data.get("game_Ggameindex");
		int userType=(int) data.get("UserType");
		
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		jsonMap.put("Event", "21");  //게임 관련 이벤트 20이상 부터 
		
        String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        
		for(int i=0;i<GameServer.GameRoom.size();i++) 
		{
			if(GameServer.GameRoom.get(i).game_Inex.equals(game_gameIndex)) 
			{
				if(userType==1) //서로 상대방에게 전송해주기 
					GameServer.GameRoom.get(i).player2.ctx.writeAndFlush(buffer);
				else 
					GameServer.GameRoom.get(i).player1.ctx.writeAndFlush(buffer);
			
				System.out.println("RUNN USER TYPE GAME READ -> "+userType);
			}
		}
		
		
	}

	//게임 방에다가 생산 이벤트 쳐넣음 
	void setGameEvent(Map<String, Object> data) 
	{
		String game_gameIndex = (String) data.get("game_Ggameindex");
		int userType = (int) data.get("UserType");
		int spwanTime = (int) data.get("SpawnTime");
		int spwanPosition = (int) data.get("SpawnPosition");
		int soliderType = (int) data.get("SoliderType");
		int roomNum = SerachRoomNum(game_gameIndex);
		GameServer.GameRoom.get(roomNum).Task.AWaitCurrentQ.add(new GamePacket(
				20,
				userType,
				spwanPosition,
				soliderType,
				spwanTime, data));
	}
	
	//방 찾아줌 
	int SerachRoomNum(String gameIndex) 
	{
		int roomNum=0;
		for(int i=0;i<GameServer.GameRoom.size();i++) 
		{
			if(GameServer.GameRoom.get(i).game_Inex.equals(gameIndex)) 
			{
				roomNum=i;
				System.out.println("GET ROOM NUM -> "+i);
				break;
			}
		}
		return roomNum; 
	}
	
	//게임이벤트 전송 
	void SendEventData(ChannelHandlerContext ctx,String jsondata) 
	{
		ByteBuf buffer = Unpooled.copiedBuffer(jsondata, CharsetUtil.UTF_8);
		ctx.writeAndFlush(buffer);
	
	}

}
