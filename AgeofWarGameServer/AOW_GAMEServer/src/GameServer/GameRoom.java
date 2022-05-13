package GameServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

public class GameRoom {
	
	UserInfo player1;
	UserInfo player2;

	int RoomNo; // ������ ���ȣ // �������������������� 
	String game_Inex; //���� �� ���� �ĺ��� 
	GameEventHandler Task; // ���ӿ� ���Ǵ� �̺�Ʈ �ڵ鸵 
	GameAutoTimer AutoTimeTask; // ���� Ÿ�� �½�ũ 
	GameHandler gameHandler; // ������ ���� �ڵ鷯 ���� 
	
	GameRoom(String game_UserNickName,
			String game_Sessionid,
			String game_Macid,
			String game_gameIndex,ChannelHandlerContext ctx,GameHandler gameHandler)
	{
		
		player1=new UserInfo();
		player2=new UserInfo();
		
		this.RoomNo=GameServer.GameRoom.size();
		this.player1.gameNickName=game_UserNickName;
		this.player1.sessionId=game_Sessionid;
		this.player1.sessionMac=game_Macid;
		this.game_Inex=game_gameIndex;
		this.player1.ctx=ctx;
		this.gameHandler=gameHandler;
		System.out.println("Create Room num -> "+ RoomNo);
	}
	
	// ���� ��� ���� ���� 
	public void GameRun() 
	{
		AutoTimeTask=new GameAutoTimer();
		Thread th2= new Thread(AutoTimeTask,"GameAutoTime");
		th2.start();
		
		Task=new GameEventHandler();
		Thread th = new Thread(Task, "Game");
		th.start();
		
	}
	
	class GameEventHandler implements Runnable {

		Boolean endGameThread;
		ObjectMapper mapper;
		Vector<GamePacket> AWaitCurrentQ; // �޼��� ���ť 
		Vector<GamePacket> AWaitSendQ; // ���� �޼��� ����ť 
		Map<String ,Object> jsonMap;
		
		GameEventHandler(){
			endGameThread=true;
			AWaitCurrentQ=new Vector<GamePacket>();
			AWaitSendQ=new Vector<GamePacket>();
		}
		
		@Override
		public void run() 
		{
			// TODO Auto-generated method stub
			
			try 
			{
				gameHandler.ActiveGame(RoomNo); // ���� Ȱ��ȭ
			} catch (JsonProcessingException e) {e.printStackTrace();}
			
			//���� �̺�Ʈ ó�� 
			while(endGameThread) 
			{
				if(AWaitCurrentQ.isEmpty()) 	
					continue;
				else {
					try {
						CheckEventData();
					} catch (JsonProcessingException e) {e.printStackTrace();};
				}
			}
			
			
		}
		void StopGame() 
		{
			endGameThread=false;
		}
		
		void CheckEventData() throws JsonProcessingException 
		{
			int readSize;
			int pivotTime=AWaitCurrentQ.get(0).spawnTime;			
			while((pivotTime+20)<=AutoTimeTask.GameTime) {}//�����Ͻ� ó��
			readSize=AWaitCurrentQ.size();
			
			for(int i=0;i<readSize;i++) 
			{
				
				int Event=AWaitCurrentQ.get(i).EventType; 
				
				switch(Event) 
				{
					case 20: //���� 
						MSGProduction(i);
						break;
						
					case 21:
						break;
				}
				
				
			}
			RemoveQ(readSize);
			
		}
		
		//����
		void MSGProduction(int packetNum) throws JsonProcessingException
		{
			mapper = new ObjectMapper();
			jsonMap = new HashMap<String, Object>();
			jsonMap.put("Event", "22");  
			jsonMap.put("UserType",AWaitCurrentQ.get(packetNum).orderPlayer);
			jsonMap.put("SoliderType",AWaitCurrentQ.get(packetNum).soliderType);
			jsonMap.put("SpawnPosition",AWaitCurrentQ.get(packetNum).spawnPosition);
			jsonMap.put("CompleteTime",getCompleteTime(AWaitCurrentQ.get(packetNum).spawnTime,AWaitCurrentQ.get(packetNum).soliderType));
			
			String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
			
			gameHandler.SendEventData(player1.ctx, jsonStr);
			gameHandler.SendEventData(player2.ctx, jsonStr);
		}
		
		void RemoveQ(int readSize) 
		{
			for(int i=0;i<readSize;i++) 
				AWaitCurrentQ.remove(i);
		}
		
		//����Ϸ� �ð� get 
		int getCompleteTime(int spawnTime, int soliderType) 
		{
			int completeTime=0;
			
			if(soliderType==1) { //Bear
				completeTime=spawnTime+GamePacket.MONSTERBearTime;
				
			}else if(soliderType==2) { //Murdock GameTime
				
				completeTime=spawnTime+GamePacket.SOLMurdockTime;
			}
			
			System.out.println("complete time -> "+completeTime);
			return completeTime;
		}

	
	}

}
