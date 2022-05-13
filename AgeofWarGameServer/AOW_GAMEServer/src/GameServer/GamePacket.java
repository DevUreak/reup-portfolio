package GameServer;

import java.util.Map;

public class GamePacket {

	final static int MONSTERBearTime=300;
	final static int SOLMurdockTime=500;
	int EventType; // 이벤트 종류 
	int orderPlayer; // 이벤트 요청자  1=p1 2=p2
	int spawnPosition; // 생산 위치
	int soliderType; // 생산할 병사 종류 
	int spawnTime; // 생산한 시간 
	
	String P1_Msg; //p1에게 보낼 메세지 
	String P2_Msg; // p2에게 보낼 메세지 
	Map<String, Object > data;
	
	GamePacket(int EventType,int orderPlayer,int spawnPosition,int soliderType,int spawnTime,Map <String, Object > data)
	{
		this.EventType=EventType;
		this.spawnPosition=spawnPosition;
		this.soliderType=soliderType;
		this.spawnTime=spawnTime;
		this.orderPlayer=orderPlayer;
		this.data=data;
		System.out.println("Create Event MSG");
		
	}
	
	//Event별로 메세지 만드는 함수 
	void CreateProductMsg() 
	{
	
	}

	
}
