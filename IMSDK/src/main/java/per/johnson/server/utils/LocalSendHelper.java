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
 * LocalSendHelper.java at 2017-12-9 11:24:33, code by Jack Jiang.
 * You can contact author with jack.jiang@52im.net or jb2011@163.com.
 */
package per.johnson.server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import per.johnson.server.ServerCoreHandler;
import per.johnson.server.processor.OnlineProcessor;
import per.johnson.server.protocal.ErrorCode;
import per.johnson.server.protocal.ProtocalOuterClass.Protocal;
import per.johnson.server.protocal.ProtocalFactory;
import per.johnson.server.qos.QoS4SendDaemonS2C;
import per.johnson.server.bridge.QoS4SendDaemonB2C;
import per.johnson.server.netty.MBObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalSendHelper
{
	private static Logger logger = LoggerFactory.getLogger(ServerCoreHandler.class);
	
	public static void sendData(String from_user_id, String to_user_id
			, String dataContent, MBObserver resultObserver) throws Exception
    {
    	sendData(from_user_id, to_user_id, dataContent, true, null, -1, resultObserver);
    }
	
	public static void sendData(String from_user_id, String to_user_id, String dataContent
			, int typeu, MBObserver resultObserver) throws Exception 
    {
    	sendData(from_user_id, to_user_id, dataContent, true, null, typeu, resultObserver);
    }
	
	public static void sendData(String from_user_id, String to_user_id, String dataContent
			, boolean QoS, int typeu, MBObserver resultObserver) throws Exception 
    {
    	sendData(from_user_id, to_user_id, dataContent, QoS, null, typeu, resultObserver);
    }
	
	public static void sendData(String from_user_id, String to_user_id, String dataContent
			, boolean QoS, String fingerPrint, MBObserver resultObserver) throws Exception 
    {
    	sendData(from_user_id, to_user_id, dataContent, QoS, fingerPrint, -1, resultObserver);
    }
	
	public static void sendData(String from_user_id, String to_user_id, String dataContent
			, boolean QoS, String fingerPrint, int typeu, MBObserver resultObserver) throws Exception 
    {
    	sendData(
    		ProtocalFactory.createCommonData(dataContent, from_user_id, to_user_id, QoS, fingerPrint, typeu)
    		, resultObserver);
    }
    
    public static void sendData(Protocal p, MBObserver resultObserver) throws Exception
    {
    	if(p != null)
    	{
    		if(!"0".equals(p.getTo()))
    			sendData(OnlineProcessor.getInstance().getOnlineSession(p.getTo()), p, resultObserver);
    		else
    		{
    			logger.warn("[IMCORE-netty]【注意】此Protocal对象中的接收方是服务器(user_id==0)（而此方法本来就是由Server调用，自已发自已不可能！），数据发送没有继续！"+p.toString());

    			if(resultObserver != null)
    				resultObserver.update(false, p);
    		}
    	}
    	else
    	{
    		if(resultObserver != null)
    			resultObserver.update(false, p);
    	}
    }
    
    public static void sendData(final Channel session
    		, final Protocal p, final MBObserver resultObserver) throws Exception 
    {
		if(session == null)
		{
			logger.info("[IMCORE-netty]toSession==null >> id="+p.getFrom()+"的用户尝试发给客户端"+p.getTo()
					+"的消息：str="+p.getDataContent()+"因接收方的id已不在线，此次实时发送没有继续(此消息应考虑作离线处理哦).");
		}
		else
		{
			if(session.isActive())
			{
		    	if(p != null)
		    	{
		    		final byte[] res = p.toBytes();
		    		ByteBuf to = Unpooled.copiedBuffer(res);
		    		ChannelFuture cf = session.writeAndFlush(to);//.sync();
		    		
		    		cf.addListener((ChannelFutureListener) future -> {
                        if( future.isSuccess())
                        {
//		 		    			logger.info("[IMCORE-netty] >> 给客户端："+ServerToolKits.clientInfoToString(session)
//		 		    					+"的数据->"+p.toGsonString()+",已成功发出["+res.length+"].");

                            if("0".equals(p.getFrom()))
                            {
                                if(p.getQoS() && !QoS4SendDaemonS2C.getInstance().exist(p.getFp()))
                                    QoS4SendDaemonS2C.getInstance().put(p);
                            }
                            else if(p.getBridge())
                            {
                                if(p.getQoS() && !QoS4SendDaemonB2C.getInstance().exist(p.getFp()))
                                    QoS4SendDaemonB2C.getInstance().put(p);
                            }
                        }
                        else
                        {
                            logger.warn("[IMCORE-netty]给客户端："+ServerToolKits.clientInfoToString(session)+"的数据->"+p.toString()+",发送失败！["+res.length+"](此消息应考虑作离线处理哦).");
                        }

                        if(resultObserver != null)
                            resultObserver.update(future.isSuccess(), p);
                    });
		    		
		    		// ## Bug FIX: 20171226 by JS, 上述数据的发送结果直接通过ChannelFutureListener就能知道，
		    		//            如果此处不return，则会走到最后的resultObserver.update(false, null);，就会
		    		//            出现一个发送方法的结果回调先是失败（错误地走到下面去了），一个是成功（真正的listener结果）
		    		return;
		    		// ## Bug FIX: 20171226 by JS END
		    	}
		    	else
		    	{
		    		logger.warn("[IMCORE-netty]客户端id="+p.getFrom()+"要发给客户端"+p.getTo()
							+"的实时消息：str="+p.getDataContent()+"没有继续(此消息应考虑作离线处理哦).");
		    	}
			}
		}
		
		if(resultObserver != null)
			resultObserver.update(false, p);
    }
    
	public static void replyDataForUnlogined(Channel session, Protocal p
			, MBObserver resultObserver) throws Exception
	{
		logger.warn("[IMCORE-netty]>> 客户端"+ServerToolKits.clientInfoToString(session)+"尚未登陆，"+p.getDataContent()+"处理未继续.");
		Protocal perror = ProtocalFactory.createPErrorResponse(
				ErrorCode.ForS.RESPONSE_FOR_UNLOGIN, p.toString(), "-1"); // 尚未登陆则user_id就不存在了,用-1表示吧，目前此情形下该参数无意义
		sendData(session, perror, resultObserver);
	}

	public static void replyDelegateRecievedBack(Channel session, Protocal pFromClient
			, MBObserver resultObserver) throws Exception
	{
		if(pFromClient.getQoS() && pFromClient.getFp() != null)
		{
			Protocal receivedBackP = ProtocalFactory.createRecivedBack(
					pFromClient.getTo()
					, pFromClient.getFrom()
					, pFromClient.getFp());

			sendData(session, receivedBackP, resultObserver);
		}
		else
		{
			logger.warn("[IMCORE-netty]收到"+pFromClient.getFrom()
					+"发过来需要QoS的包，但它的指纹码却为null！无法发伪应答包！");
			
			if(resultObserver != null)
				resultObserver.update(false, pFromClient);
		}
	}
}
