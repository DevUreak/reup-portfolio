package GameServer;

public class GameAutoTimer implements Runnable {

	
	int GameTime;
	Boolean Threadrun;
	Boolean AutoThreadRun;
	
	GameAutoTimer()
	{
		GameTime=0;
		Threadrun=true;
		AutoThreadRun=true;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try {
		while(Threadrun){Thread.sleep(100);}
		
		while(AutoThreadRun) 
		{
			GameTime=GameTime+10;
			Thread.sleep(100);
		}
		
		
		} catch (InterruptedException e) {e.printStackTrace();}
		

	}
	
	void RunningAutoTimer()
	{
		Threadrun=false;
	}
	
	void StopThread()
	{
		AutoThreadRun=false;
	}
	

}
