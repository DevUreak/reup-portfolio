package MatchServer_netty;

public class UserInfo {

	String SessionMac;
	String SessionNickName;
	String SessionEmail;
	int SessionTear;
	long st_matchTime; 
	
	UserInfo(){}
	
	UserInfo(int SessionTear,String SessionMac,String SessionEmail,String SessionNickName)
	{
		this.SessionMac=SessionMac;
		this.SessionTear=SessionTear;
		this.SessionNickName=SessionNickName;
		this.SessionEmail=SessionEmail;
	}
	
		
}

