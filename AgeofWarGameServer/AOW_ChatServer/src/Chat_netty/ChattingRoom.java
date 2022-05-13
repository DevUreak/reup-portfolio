package Chat_netty;

import java.util.Vector;

import io.netty.channel.ChannelHandlerContext;

public class ChattingRoom {

	Vector<ChattingClient> chatUserList; // ChatRoom chat user list 
	Vector<ChattingProtocol> sendChatMsg; // send user list 
	int RoomNum; 
	final int RoomMax=3; // chat max user size  
	
	ChattingRoom()
	{
		this.RoomNum=ChattingHandler.CHATTINGRoom.size();
		this.chatUserList = new Vector<ChattingClient>();
	}
	
	ChattingRoom(ChattingClient chatUser,ChannelHandlerContext ctx) // user add to enter  
	{
		chatUserList.add(chatUser);
	}
	
	// user setting , user add first on enter  
	public void addUser(ChannelHandlerContext ctx) 
	{
		chatUserList.add(new ChattingClient(ctx));
		
	}
	
	//now chatting user size 
	public int getRoomSize() 
	{		
		return chatUserList.size();
	}
}
