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
				case 10: // ���ӹ� ������ ���� 
					setGame(ctx,data);
					break;
				case 11: // ���� �غ� �� ���� ���� �˸��� �̺�Ʈ 
					SendGameReady(data);
					break;
				case 20: // ���� �̺�Ʈ ó��(����)
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
	
	//���� ���� ��û 
	public void getGameInfo(ChannelHandlerContext ctx) throws JsonProcessingException { // ���� �� ������ ���� �⺻ ���� request
		
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		jsonMap.put("Event", "1"); 
        String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        ctx.writeAndFlush(buffer);
	
	}
	
	//���� �� ������ ���� 
	public void setGame(ChannelHandlerContext ctx,Map<String, Object > data) throws JsonProcessingException { // ���� �� ������ ���� �⺻ ���� request
		
		int userType=(int)data.get("UserType"); // 1����,2���� 
		
		if(userType==1) 
			CreateRoom(ctx,data); //����� 
		else if(userType==2) 
			EnterRoom(ctx,data);
	}

	//�����
	public void CreateRoom(ChannelHandlerContext ctx,Map<String, Object > data) 
	{
		String game_macid=(String)data.get("game_Macid");
		String game_sessionid=(String)data.get("game_Sessionid");
		String game_usernickname=(String)data.get("game_UserNickName");
		String game_gameIndex=(String)data.get("game_Ggameindex");
		System.out.println("Create Room UserName -> "+game_usernickname);
		GameServer.GameRoom.add(new GameRoom(game_usernickname, game_sessionid, game_macid, game_gameIndex,ctx,this));
	}
	
	//������ 
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
				GameServer.GameRoom.get(index).GameRun(); // ���� ���� 
				System.out.println("Enter Player2");
				break;
			}
			
		}
	
	}
	
	//���� Ȱ��ȭ
	void ActiveGame(int RoomNum) throws JsonProcessingException 
	{
	
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		jsonMap.put("Event", "20");  //���� ���� �̺�Ʈ 20�̻� ���� 
		
        String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        
        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        GameServer.GameRoom.get(RoomNum).player1.ctx.writeAndFlush(buffer);
       
        
        ByteBuf buffer2 = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        GameServer.GameRoom.get(RoomNum).player2.ctx.writeAndFlush(buffer2);

        System.out.println("Run ACTIVE MSG");
        GameServer.GameRoom.get(RoomNum).AutoTimeTask.RunningAutoTimer(); // Ÿ�̸ӿ� 
	}

	//���� �غ� �������ֱ� 
	void SendGameReady(Map<String, Object > data) throws JsonProcessingException 
	{
		
		String game_gameIndex = (String) data.get("game_Ggameindex");
		int userType=(int) data.get("UserType");
		
		mapper = new ObjectMapper();
		jsonMap = new HashMap<String, Object>();
		jsonMap.put("Event", "21");  //���� ���� �̺�Ʈ 20�̻� ���� 
		
        String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        ByteBuf buffer = Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8);
        
		for(int i=0;i<GameServer.GameRoom.size();i++) 
		{
			if(GameServer.GameRoom.get(i).game_Inex.equals(game_gameIndex)) 
			{
				if(userType==1) //���� ���濡�� �������ֱ� 
					GameServer.GameRoom.get(i).player2.ctx.writeAndFlush(buffer);
				else 
					GameServer.GameRoom.get(i).player1.ctx.writeAndFlush(buffer);
			
				System.out.println("RUNN USER TYPE GAME READ -> "+userType);
			}
		}
		
		
	}

	//���� �濡�ٰ� ���� �̺�Ʈ �ĳ��� 
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
	
	//�� ã���� 
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
	
	//�����̺�Ʈ ���� 
	void SendEventData(ChannelHandlerContext ctx,String jsondata) 
	{
		ByteBuf buffer = Unpooled.copiedBuffer(jsondata, CharsetUtil.UTF_8);
		ctx.writeAndFlush(buffer);
	
	}

}
