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
 * ServerToolKits.java at 2017-12-9 11:24:33, code by Jack Jiang.
 * You can contact author with jack.jiang@52im.net or jb2011@163.com.
 */
package per.johnson.server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.Arrays;

import per.johnson.server.ServerCoreHandler;
import per.johnson.server.ServerLauncher;
import per.johnson.server.processor.OnlineProcessor;
import per.johnson.server.protocal.CharsetHelper;
import per.johnson.server.protocal.ProtocalOuterClass.Protocal;
import per.johnson.server.protocal.ProtocalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerToolKits
{
	private static Logger logger = LoggerFactory.getLogger(ServerCoreHandler.class);
	
    public static void setSenseMode(SenseMode mode)
    {
    	int expire = 0;
    	
    	switch(mode)
    	{
    		case MODE_3S:
    			// 误叛容忍度为丢3个包
    			expire = 3 * 3 + 1;
    			break;
    		case MODE_10S:
    			// 误叛容忍度为丢2个包
    			expire = 10 * 2 + 1;
        		break;
    		case MODE_30S:
    			// 误叛容忍度为丢2个包
    			expire = 30 * 2 + 2;
        		break;
    		case MODE_60S:
    			// 误叛容忍度为丢2个包
    			expire = 60 * 2 + 2;
        		break;
    		case MODE_120S:
    			// 误叛容忍度为丢2个包
    			expire = 120 * 2 + 2;
        		break;
    	}
    	
    	if(expire > 0)
    		ServerLauncher.SESSION_RECYCLER_EXPIRE = expire;
    }
    
	public static String clientInfoToString(Channel session)
	{
		SocketAddress remoteAddress = session.remoteAddress();
		String s1 = remoteAddress.toString();
		StringBuilder sb = new StringBuilder()
		.append("{uid:")
		.append(OnlineProcessor.getUserIdFromSession(session))
		.append("}")
		.append(s1);
		return sb.toString();
	}
	@Deprecated
	public static String fromIOBuffer_JSON(ByteBuf buffer) throws Exception 
	{
		byte[] req = new byte[buffer.readableBytes()];
		buffer.readBytes(req);
		String jsonStr = new String(req, CharsetHelper.DECODE_CHARSET);
		return jsonStr;
	}


	public static Protocal fromIOBuffer(ByteBuf buffer) throws Exception
	{
		byte[] req ;
		if(buffer.hasArray()){
		    byte[] data = buffer.array();
		    int offset = buffer.arrayOffset();  //0
		    int length = buffer.readableBytes(); // 6
            req =  Arrays.copyOfRange(data,offset,offset+length);
        }else{
		    int length = buffer.readableBytes();
		    req = new byte[length];
		    buffer.getBytes(buffer.readerIndex(),req);
        }
		return Protocal.parseFrom(req);
	}
    
    public enum SenseMode
    {
    	/** 
    	 * 对应于客户端的3秒心跳模式：此模式的用户非正常掉线超时时长为“3 * 3 + 1”秒。
    	 * <p>
    	 * 客户端心跳丢包容忍度为3个包。此模式为当前所有预设模式中体验最好，但
    	 * 客户端可能会大幅提升耗电量和心跳包的总流量。 
    	 */
    	MODE_3S,
    	
    	/** 
    	 * 对应于客户端的10秒心跳模式：此模式的用户非正常掉线超时时长为“10 * 2 + 1”秒。 
    	 * <p>
    	 * 客户端心跳丢包容忍度为2个包。
    	 */
    	MODE_10S,
    	
    	/** 
    	 * 对应于客户端的30秒心跳模式：此模式的用户非正常掉线超时时长为“30 * 2 + 2”秒。
    	 * <p>
    	 * 客户端心跳丢包容忍度为2个包。
    	 */
    	MODE_30S,
    	
    	/** 
    	 * 对应于客户端的60秒心跳模式：此模式的用户非正常掉线超时时长为“60 * 2 + 2”秒。
    	 * <p>
    	 * 客户端心跳丢包容忍度为2个包。
    	 */
    	MODE_60S,
    	
    	/** 
    	 * 对应于客户端的120秒心跳模式：此模式的用户非正常掉线超时时长为“120 * 2 + 2”秒。 
    	 * <p>
    	 * 客户端心跳丢包容忍度为2个包。
    	 */
    	MODE_120S
    }
}
