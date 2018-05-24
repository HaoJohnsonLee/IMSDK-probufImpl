/*
 * Copyright (C) 2017  即时通讯网(52im.net) & Jack Jiang.
 * The MobileIMSDK_X_netty (MobileIMSDK v3.x Netty版) Project. 
 * All rights reserved.
 * 
 * > Github地址: https://github.com/JackJiang2011/MobileIMSDK
 * > 文档地址: http://www.52im.net/forum-89-1.html
 * > 即时通讯技术社区：http://www.52im.net/
 * > 即时通讯技术交流群：320837163 (http://www.52im.net/topic-qqgroup.html)
 *  
 * "即时通讯网(52im.net) - 即时通讯开发者社区!" 推荐开源工程。
 * 
 * ServerLauncher.java at 2017-12-9 11:24:33, code by Jack Jiang.
 * You can contact author with jack.jiang@52im.net or jb2011@163.com.
 */
package per.johnson.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.io.IOException;

import per.johnson.server.event.MessageQoSEventListenerS2C;
import per.johnson.server.event.ServerEventListener;
import per.johnson.server.qos.QoS4ReciveDaemonC2S;
import per.johnson.server.qos.QoS4SendDaemonS2C;
import per.johnson.server.bridge.QoS4ReciveDaemonC2B;
import per.johnson.server.bridge.QoS4SendDaemonB2C;
import per.johnson.server.netty.MBUDPClientInboundHandler;
import per.johnson.server.netty.MBUDPServerChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServerLauncher 
{
	private static Logger logger = LoggerFactory.getLogger(ServerLauncher.class); 
	
	public static boolean debug = true;
	public static String appKey = null;
    public static int PORT = 9523;
    public static int SESSION_RECYCLER_EXPIRE = 10;
    public static boolean bridgeEnabled = false;
    
    private boolean running = false;
    protected ServerCoreHandler serverCoreHandler = null;
    
 	private final EventLoopGroup __bossGroup4Netty = new NioEventLoopGroup();
 	private final EventLoopGroup __workerGroup4Netty = new DefaultEventLoopGroup();
 	private Channel __serverChannel4Netty = null;

 	public ServerLauncher() throws IOException 
 	{
 		// default do nothing
 	}

 	public boolean isRunning()
 	{
 		return running;
 	}

 	public void startup() throws Exception
 	{	
 		if(!this.running)
 		{
 			serverCoreHandler = initServerCoreHandler();
 			initListeners();
 			ServerBootstrap bootstrap = initServerBootstrap4Netty();
 			QoS4ReciveDaemonC2S.getInstance().startup();
 			QoS4SendDaemonS2C.getInstance().startup(true).setServerLauncher(this);
 			if(ServerLauncher.bridgeEnabled){
 				QoS4ReciveDaemonC2B.getInstance().startup();
 				QoS4SendDaemonB2C.getInstance().startup(true).setServerLauncher(this);
 				serverCoreHandler.lazyStartupBridgeProcessor();
 				logger.info("[IMCORE-netty] 配置项：已开启与MobileIMSDK Web的互通.");
 			}
 			else{
 				logger.info("[IMCORE-netty] 配置项：未开启与MobileIMSDK Web的互通.");
 			}

 			ChannelFuture cf = bootstrap.bind("0.0.0.0", PORT).syncUninterruptibly();
 			__serverChannel4Netty = cf.channel();

 			this.running = true;
 			logger.info("[IMCORE-netty] 基于MobileIMSDK的UDP服务正在端口" + PORT+"上监听中...");

 			__serverChannel4Netty.closeFuture().await();
 		}
 		else
 		{
 			logger.warn("[IMCORE-netty] 基于MobileIMSDK的UDP服务正在运行中" +
 					"，本次startup()失败，请先调用shutdown()后再试！");
 		}
    }

    public void shutdown()
    {
    	if (__serverChannel4Netty != null) 
    		__serverChannel4Netty.close();

		__bossGroup4Netty.shutdownGracefully();
		__workerGroup4Netty.shutdownGracefully();
		
    	QoS4ReciveDaemonC2S.getInstance().stop();
    	QoS4SendDaemonS2C.getInstance().stop();
    	
    	if(ServerLauncher.bridgeEnabled){
    		QoS4ReciveDaemonC2B.getInstance().stop();
    		QoS4SendDaemonB2C.getInstance().stop();
    	}
    	
    	this.running = false;
    }
    
    protected ServerCoreHandler initServerCoreHandler()
    {
    	return new ServerCoreHandler();
    }
    
    protected abstract void initListeners();
    
    protected ServerBootstrap initServerBootstrap4Netty()
    {
    	return new ServerBootstrap()
    		.group(__bossGroup4Netty, __workerGroup4Netty)
    		.channel(MBUDPServerChannel.class)
    		.childHandler(initChildChannelHandler4Netty());
    }
    
	protected ChannelHandler initChildChannelHandler4Netty()
	{
		return new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) throws Exception {
				channel.pipeline()
					.addLast(new ReadTimeoutHandler(SESSION_RECYCLER_EXPIRE))
					.addLast(new MBUDPClientInboundHandler(serverCoreHandler));
			}
		};
	}
    
    public ServerEventListener getServerEventListener()
	{
		return serverCoreHandler.getServerEventListener();
	}
	public void setServerEventListener(ServerEventListener serverEventListener)
	{
		this.serverCoreHandler.setServerEventListener(serverEventListener);
	}
	
	public MessageQoSEventListenerS2C getServerMessageQoSEventListener()
	{
		return serverCoreHandler.getServerMessageQoSEventListener();
	}
	public void setServerMessageQoSEventListener(MessageQoSEventListenerS2C serverMessageQoSEventListener)
	{
		this.serverCoreHandler.setServerMessageQoSEventListener(serverMessageQoSEventListener);
	}

	public ServerCoreHandler getServerCoreHandler()
	{
		return serverCoreHandler;
	}
}
