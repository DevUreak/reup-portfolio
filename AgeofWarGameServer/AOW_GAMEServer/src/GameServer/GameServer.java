package GameServer;

import java.io.IOException;
import java.util.Vector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class GameServer {
	
	static Vector<GameRoom> GameRoom;
	
	  public static void main(String[] args) throws IOException, InterruptedException {
	  
		NioEventLoopGroup bossGroup = new NioEventLoopGroup(4); 
	    NioEventLoopGroup workerGroup = new NioEventLoopGroup(8);  
	    ServerBootstrap bootstrap = new ServerBootstrap(); 
	    GameRoom=new Vector<GameRoom>(); // 게임 방 생성 
	    
	    bootstrap.group(bossGroup, workerGroup);
	    bootstrap.channel(NioServerSocketChannel.class);
	     
	    //EventExecutorGroup group = new DefaultEventExecutorGroup(500); 
	   
	    bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
	      @Override
	      protected void initChannel(SocketChannel ch) throws Exception {
	        ChannelPipeline pipeline = ch.pipeline();

	        pipeline.addLast(new GameHandler()); 
	
	      }
	     
	      
	    });
	     
	    bootstrap.childOption(ChannelOption.TCP_NODELAY,true);
	    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
	    bootstrap.bind(7777).sync();

	  }
	  

}
