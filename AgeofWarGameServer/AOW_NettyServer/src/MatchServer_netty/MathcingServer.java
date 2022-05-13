package MatchServer_netty;

import java.io.IOException;
import java.util.Vector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class MathcingServer {
		
	
	  public static void main(String[] args) throws IOException, InterruptedException {
	  
		MathcingServer ms=new MathcingServer();
		MatchingProtocol.ConnClientList=new Vector<MatchingProtocol>(); 
		Matching.MATCHReadyList=new Vector<MatchingProtocol>();
		
		NioEventLoopGroup bossGroup = new NioEventLoopGroup(4); 
	    NioEventLoopGroup workerGroup = new NioEventLoopGroup(8); 
	   
	    ServerBootstrap bootstrap = new ServerBootstrap(); 
	    
	    bootstrap.group(bossGroup, workerGroup);
	    bootstrap.channel(NioServerSocketChannel.class);
	     
	    //EventExecutorGroup group = new DefaultEventExecutorGroup(500); 
	   
	    bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
	      @Override
	      protected void initChannel(SocketChannel ch) throws Exception {
	        ChannelPipeline pipeline = ch.pipeline();

	        pipeline.addLast(new ServerHandler()); 
	
	      }
	     
	      
	    });
	     
	    bootstrap.childOption(ChannelOption.TCP_NODELAY,true);
	    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
	    bootstrap.bind(9887).sync();
	    ms.runThread();
	  }
	  
	  public void runThread(){
		   Matching matching=new Matching();
		   CloseChannel closeChannel=new CloseChannel();
		   Thread th = new Thread(matching,"matching");
		   Thread th2 = new Thread(closeChannel,"closeChannel");  
		   th.start();
		   th2.start();
	  }
	  
	  
	  
	}