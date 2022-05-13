package MatchServer_netty;


import java.util.Vector;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandlerContext;

public class Matching implements Runnable{

	static Vector<MatchingProtocol> MATCHReadyList; 
	static Vector<MatchingProtocol> MATCHINGList_STEP1; 
	Vector<Matching> LastMATCHINGList; 
	static Vector<Matching> tmpMATCHINGList; 

	MatchingProtocol firstUser; 
	MatchingProtocol SecondUser; 
	LastMatchConfirm lastMatchConfirm;
	long LastMatchingTime; 
	Thread th;
	ServerHandler sv; 
	
	Matching(){}
	Matching(MatchingProtocol firstUser,MatchingProtocol SecondUser){
		this.firstUser=firstUser;
		this.SecondUser=SecondUser;
		LastMatchingTime=System.currentTimeMillis();
	}

	//1차 매칭 
	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		MATCHReadyList=new Vector<MatchingProtocol>();
		MATCHINGList_STEP1=new Vector<MatchingProtocol>(); 
		
		LastMATCHINGList=new Vector<Matching>();
		lastMatchConfirm=new LastMatchConfirm();
		sv=new ServerHandler();
		th=new Thread(lastMatchConfirm,"lastmatch");
		th.start();
		System.out.println("매칭 시스템 초기화 ");
		
		while(true)
		{
			try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
		
			
			if(MATCHReadyList.size()>=2) 
			{ 
				try {
					
					Matching_1_list_add(); 
					SearchMatch(); 
					
				} catch (JsonProcessingException e) {e.printStackTrace();}
			}
		}
		
		
	}

	public MatchingProtocol stepl_copyObject() {
		MatchingProtocol mp= new MatchingProtocol(
				MATCHReadyList.get(0).ctx,
				MATCHReadyList.get(0).userinfo.SessionTear,
				MATCHReadyList.get(0).SessionID,
				MATCHReadyList.get(0).userinfo.SessionMac,
				MATCHReadyList.get(0).userinfo.SessionEmail,
				MATCHReadyList.get(0).userinfo.SessionNickName,
				MATCHReadyList.get(0).ctx.channel().id()
				);
		return mp;
	}

	public MatchingProtocol LastMatching_copyObject(int index) {

		MatchingProtocol mp= new MatchingProtocol(
				MATCHINGList_STEP1.get(index).ctx,
				MATCHINGList_STEP1.get(index).userinfo.SessionTear,
				MATCHINGList_STEP1.get(index).SessionID,
				MATCHINGList_STEP1.get(index).userinfo.SessionMac,
				MATCHINGList_STEP1.get(index).userinfo.SessionEmail,
				MATCHINGList_STEP1.get(index).userinfo.SessionNickName,
				MATCHINGList_STEP1.get(index).ctx.channel().id()
				);
		return mp;
	}

	public MatchingProtocol LastMatching_copyObject_1(MatchingProtocol matchPivot) {
		MatchingProtocol mp= new MatchingProtocol(
				matchPivot.ctx,
				matchPivot.userinfo.SessionTear,
				matchPivot.SessionID,
				matchPivot.userinfo.SessionMac,
				matchPivot.userinfo.SessionEmail,
				matchPivot.userinfo.SessionNickName,
				matchPivot.ctx.channel().id()
				);
		return mp;
	}

	
	public void Matching_1_list_add() 
	{
		System.out.println("첫 매칭 사이즈 ->"+MATCHReadyList.size());
		int index=MATCHReadyList.size();
		for(int i=0; i<index;i++) 
		{ 
			if(MATCHReadyList.get(0).matchCancleFlag==false) 
			{
				MATCHINGList_STEP1.add(stepl_copyObject());
				MATCHINGList_STEP1.get(i).SetMatchPoint();
				MATCHReadyList.remove(0);
			}else {
				MATCHReadyList.remove(0);
				System.out.println("delete step1");
			}
		}
		
	}
		
	public void SearchMatch() throws JsonProcessingException 
	{
		MatchingProtocol matchPivot=null;
		for(;;) //1차매칭 서치 시작 
		{ 
			if(MATCHINGList_STEP1.size()<1) {  
				System.out.println("alonebreak now size-> "+MATCHINGList_STEP1.size());
				break;
			}
			
			matchPivot=MATCHINGList_STEP1.get(0);
		
			for(int i=1;i<MATCHINGList_STEP1.size();i++) 
			{ 
				
				if(matchPivot.matchPoint==MATCHINGList_STEP1.get(i).matchPoint)
				{
					if(sessionCheck(matchPivot.ctx,MATCHINGList_STEP1.get(i).ctx) && cancleCheck(matchPivot,MATCHINGList_STEP1.get(i))) 
						insetLastMatching(i,matchPivot); 
					else { 
						deleteSession();
					}
					break;
				}else   
					MATCHINGList_STEP1.get(i).SetMatchPoint();
			
				if(i==(MATCHINGList_STEP1.size()-1)) {
					System.out.println("reset matchPoint->"+MATCHINGList_STEP1.size());
					i=0;
				}
			
			}
		}
		
	}
	
	public void insetLastMatching(int index,MatchingProtocol matchPivot) throws JsonProcessingException 
	{ 
		LastMATCHINGList.add(new Matching(
				LastMatching_copyObject_1(matchPivot),
				LastMatching_copyObject(index)));

		sv.LastMatching(matchPivot.ctx); 
		sv.LastMatching(MATCHINGList_STEP1.get(index).ctx);
		MATCHINGList_STEP1.remove(index);
		MATCHINGList_STEP1.remove(0);
	
		System.out.println("lastmatching size"+LastMATCHINGList.size());
	}
	
	public boolean sessionCheck(ChannelHandlerContext ctx1,ChannelHandlerContext ctx2) {
		boolean ct1=ctx1.channel().isOpen();
		boolean ct2=ctx1.channel().isActive();
		boolean ct3=ctx2.channel().isOpen();
		boolean ct4=ctx2.channel().isActive();
		if(ct1 && ct2 && ct3 && ct4)
			return true;
		else
			return false;
	}
	
	public boolean cancleCheck(MatchingProtocol cl1 ,MatchingProtocol cl2) {
		boolean flag=false;
		if(cl1.matchCancleFlag==true || cl2.matchCancleFlag==true) {
			deleteSession();
			System.out.println("클라이언트 중 누군가 MATCHING을 취소했습니다.");
		}
		if(cl1.matchCancleFlag == false && cl2.matchCancleFlag == false) {
			System.out.println("클라이언트 양쪽다 문제없음");
			return true;
		}

		return flag;
	}
	

	public void deleteSession() 
	{
		
		for(int i=0 ;i<MATCHINGList_STEP1.size();i++) 
		{
			if(MATCHINGList_STEP1.get(i).ctx.channel().isActive()==false || 
					MATCHINGList_STEP1.get(i).ctx.channel().isOpen()==false ||
						MATCHINGList_STEP1.get(i).matchCancleFlag) 
			{ 
				MATCHINGList_STEP1.remove(i);
				
				if(MATCHINGList_STEP1.size()==0) 
					break;
				else {
					i--;
					continue;
				}
			}
		}
	
	}
	
	
	class LastMatchConfirm  implements Runnable{
		

		@Override
		public void run()
		{
			System.out.println("Last Matching sys_setting");
			// TODO Auto-generated method stub
			tmpMATCHINGList=new Vector<Matching>();
			while(true)
			{
				
				for(int i=0;i<tmpMATCHINGList.size();i++) 
				{
					if(tmpMATCHINGList.get(i).firstUser.matchLastFlag && tmpMATCHINGList.get(i).SecondUser.matchLastFlag) // last matching success 
					{
						
						try {
							sv.setGame(tmpMATCHINGList.get(i).firstUser,tmpMATCHINGList.get(i).SecondUser);
						} catch (JsonProcessingException e) {e.printStackTrace();}
						tmpMATCHINGList.remove(i);
						System.out.println("Last Matching success !!");
						break;
					}
					if(tmpMATCHINGList.get(i).firstUser.matchCancleFlag || tmpMATCHINGList.get(i).SecondUser.matchCancleFlag) 
					{
						System.out.println("Last Matching negative ");
						tmpMATCHINGList.remove(i);
						break;
					}
					try 
					{
						timeoutCheck(i); 
						setLastMatchData();
					} catch (JsonProcessingException e) {e.printStackTrace();}
				}
				setLastMatchData();
			}
			
			
		}
		
		
		public void setLastMatchData() {
			int index=LastMATCHINGList.size();
			for(int j=0;j<index;j++) 
			{
				tmpMATCHINGList.add(new Matching(
						copyObject(LastMATCHINGList.get(0).firstUser),
						copyObject(LastMATCHINGList.get(0).SecondUser)));
			
				System.out.println("add data");
				System.out.println("user info -> "+LastMATCHINGList.get(0).firstUser.userinfo.SessionNickName);
				System.out.println("user info2 -> "+LastMATCHINGList.get(0).SecondUser.userinfo.SessionNickName);
				LastMATCHINGList.remove(0);
			}
			
		}
		
		public void timeoutCheck(int index) throws JsonProcessingException
		{
			long endtime= System.currentTimeMillis();
			long result=(long)((endtime-tmpMATCHINGList.get(index).LastMatchingTime)/1000.0);
			if(result>=11) 
			{
				tmpMATCHINGList.get(index).firstUser.matchCancleFlag=true;
				tmpMATCHINGList.get(index).SecondUser.matchCancleFlag=true;
				sv.LastMatchingTimeOut(tmpMATCHINGList.get(index).firstUser.ctx);
				sv.LastMatchingTimeOut(tmpMATCHINGList.get(index).SecondUser.ctx);
			}
		}
	
		public MatchingProtocol copyObject(MatchingProtocol matchPivot)
		{
			MatchingProtocol mp= new MatchingProtocol(
					matchPivot.ctx,
					matchPivot.userinfo.SessionTear,
					matchPivot.SessionID,
					matchPivot.userinfo.SessionMac,
					matchPivot.userinfo.SessionEmail,
					matchPivot.userinfo.SessionNickName,
					matchPivot.ctx.channel().id()
					);
			return mp;
		}
		
	
		
	} 
}
