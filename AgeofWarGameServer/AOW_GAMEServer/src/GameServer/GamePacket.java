package GameServer;

import java.util.Map;

public class GamePacket {

	final static int MONSTERBearTime=300;
	final static int SOLMurdockTime=500;
	int EventType; // �̺�Ʈ ���� 
	int orderPlayer; // �̺�Ʈ ��û��  1=p1 2=p2
	int spawnPosition; // ���� ��ġ
	int soliderType; // ������ ���� ���� 
	int spawnTime; // ������ �ð� 
	
	String P1_Msg; //p1���� ���� �޼��� 
	String P2_Msg; // p2���� ���� �޼��� 
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
	
	//Event���� �޼��� ����� �Լ� 
	void CreateProductMsg() 
	{
	
	}

	
}
