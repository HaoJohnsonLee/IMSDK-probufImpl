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
 * BridgeProcessor.java at 2017-12-9 11:24:33, code by Jack Jiang.
 * You can contact author with jack.jiang@52im.net or jb2011@163.com.
 */
package per.johnson.server.processor;


import per.johnson.server.protocal.ProtocalOuterClass.Protocal;
import per.johnson.server.protocal.ProtocalFactory;
import per.johnson.server.utils.LocalSendHelper;
import per.johnson.server.bridge.MQProvider;
import per.johnson.server.netty.MBObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 用作异构平台IM消息交互的桥接服务提供者。
 */
public abstract class BridgeProcessor extends MQProvider
{
	private static Logger logger = LoggerFactory.getLogger(BridgeProcessor.class);  
	
	public final static String IMMQ_DECODE_CHARSET = "UTF-8";
	
	public static String IMMQ_URI = "amqp://js:19844713@192.168.1.190";
	/** 消息中转队列：Web端IM转发至APP端的消息，本类是此队列的消费者，表示从Web端读取消息*/
	public static String IMMQ_QUEUE_WEB2APP = "q_web2app";
	/** 消息中转队列：APP端IM转发至Web端的消息，本类是此队列的生产者，表示将消息发送至Web端*/
	public static String IMMQ_QUEUE_APP2WEB = "q_app2web";
	
	public BridgeProcessor()
	{
		super(IMMQ_URI, IMMQ_QUEUE_APP2WEB, IMMQ_QUEUE_WEB2APP, "IMMQ", false);
	}
	
	@Override
	protected boolean work(byte[] contentBody)
	{
		try
		{
			Protocal p =Protocal.parseFrom(contentBody);
			logger.info("[IMCORE-桥接↓] - [startWorker()中] 收到异构服务器的原始 msg："+p.toString()+", 即时进行解析并桥接转发（给接收者）...");
			//final Protocal p = ProtocalFactory.parse(msg, Protocal.class);
			//p.setQoS(true);
			//p.setBridge(true);
			
			MBObserver sendResultObserver = (sendOK, extraObj) -> {
                if(sendOK)
                {
                    realtimeC2CSuccessCallback(p);
                    logger.info("[IMCORE-桥接↓] - "+p.getFrom()+"发给"+p.getTo()
                            +"的指纹为"+p.getFp()+"的消息转发成功！【第一阶段APP+WEB跨机通信算法】");
                }
                else
                {
                    logger.info("[IMCORE-桥接↓]>> 客户端"+p.getFrom()+"发送给"+p.getTo()+"的桥接数据尝试实时发送没有成功("
                            +p.getTo()+"不在线)，将交给应用层进行离线存储哦... 【第一阶段APP+WEB跨机通信算法】");
                    boolean offlineProcessedOK = offlineC2CProcessCallback(p);
                    if(offlineProcessedOK)
                    {
                        logger.debug("[IMCORE-桥接↓]>> 向"+p.getFrom()+"发送"+p.getFp()
                                    +"的消息【离线处理】成功,from="+p.getTo()+". 【第一阶段APP+WEB跨机通信算法】");
                    }
                    else
                    {
                        logger.warn("[IMCORE-桥接↓]>> 客户端"+p.getFrom()+"发送给"+p.getTo()+"的桥接数据传输消息尝试实时发送没有成功，但上层应用层没有成" +
                                "功(或者完全没有)进行离线存储，此消息将被服务端丢弃！ 【第一阶段APP+WEB跨机通信算法】");
                    }
                }
            };
			LocalSendHelper.sendData(p, sendResultObserver);
			return true;
		}
		catch (Exception e)
		{
			logger.warn("[IMCORE-桥接↓] - [startWorker()中] work()方法出错，本条错误消息被记录：" +
					""+e.getMessage(), e);
			return true;
		}
	}

	/** 在线实时将消息桥接转发成功后被调用的回调方法。*/
	protected abstract void realtimeC2CSuccessCallback(Protocal p);
	/** 无法在线实时发送成功的消息将通过本回调方法进行处理*/
	protected abstract boolean offlineC2CProcessCallback(Protocal p);
	
}
