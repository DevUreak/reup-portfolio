package Chat_netty;


public class ChattingProtocol{

	int chatMsgTimestamp; // chatting msg ordering  
	String SessionNickName;

	
	ChattingProtocol(String SessionNickName)
	{
		this.SessionNickName=SessionNickName;
	}
}
