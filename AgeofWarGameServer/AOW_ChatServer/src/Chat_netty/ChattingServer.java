package Chat_netty;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ChattingServer {


	  public static void main(String[] args) throws IOException, InterruptedException
	  {
	    

		NioEventLoopGroup bossGroup = new NioEventLoopGroup(4); 
	    NioEventLoopGroup workerGroup = new NioEventLoopGroup(8);
	    ServerBootstrap bootstrap = new ServerBootstrap(); 
	    ChattingHandler.CHATTINGRoom=new Vector<ChattingRoom>(); //create chatting room 
	    
	    
	    ChattingHandler.CHATTINGRoom.add(new ChattingRoom()); // temp chatting room add -> loginc that can be remove  
	    ChattingHandler.CHATTINGRoom.add(new ChattingRoom());
	    ChattingHandler.CHATTINGRoom.add(new ChattingRoom());
	    
	    bootstrap.group(bossGroup, workerGroup);
	    bootstrap.channel(NioServerSocketChannel.class);
	     
	    //EventExecutorGroup group = new DefaultEventExecutorGroup(500); 
	   
	    bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
	      @Override
	      protected void initChannel(SocketChannel ch) throws Exception {
	        ChannelPipeline pipeline = ch.pipeline();
	        
	        pipeline.addLast(new ChattingHandler()); 
	
	      }
	     
	    });
	    System.out.println("chatserver run");
	    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
	    bootstrap.bind(9777).sync();
	    
	    ChatCloseChannel chatTh= new ChatCloseChannel();
		Thread th = new Thread(chatTh,"CloseChatting");
		th.start();
		
	
	  }
	 
	  
}
