package Chat_netty;

public class ChatCloseChannel implements Runnable { 

	static boolean CloseChannelFlag=true; 

	@Override
	public void run() 
	{
		System.out.println("disconn chat run!");
		while(CloseChannelFlag) 
		{	
			try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();} 
			int index=0;
			
			while(index<ChattingHandler.CHATTINGRoom.size()) 
			{
				try {
					
					for(int i=0;i<ChattingHandler.CHATTINGRoom.get(index).chatUserList.size();i++) 
					{
						if(ChattingHandler.CHATTINGRoom.get(index).chatUserList.get(i).ctx.channel().isOpen() == false &&
								ChattingHandler.CHATTINGRoom.get(index).chatUserList.get(i).ctx.channel().isActive()==false) 
						{ 
							ChattingHandler.CHATTINGRoom.get(index).chatUserList.remove(i); //
							ChattingHandler.CHATTINGRoom.get(index).chatUserList.get(i).ctx.close(); // 
							System.out.println("Chat discon");
						}
						
					}
				}catch(IndexOutOfBoundsException e) {
					
				}
			
				index++;
			}
				
		}
			
	}

}
