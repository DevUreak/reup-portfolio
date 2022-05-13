package MatchServer_netty;

public class CloseChannel implements Runnable { 

	static boolean CloseChannelFlag=true; 

	@Override
	public void run() 
	{

		while(CloseChannelFlag) 
		{	
			try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();} 
			int index=0;
			while(index<MatchingProtocol.ConnClientList.size()) 
			{

				if(MatchingProtocol.ConnClientList.get(index).ctx.channel().isOpen()==false &&
						MatchingProtocol.ConnClientList.get(index).ctx.channel().isActive()==false) 
				{ 
					MatchingProtocol.ConnClientList.remove(index);
					System.out.println("discon -> "+index + "conn size -> "+MatchingProtocol.ConnClientList.size());
					continue;
				}
				index++;
			}
				
		}
			
	}

}
