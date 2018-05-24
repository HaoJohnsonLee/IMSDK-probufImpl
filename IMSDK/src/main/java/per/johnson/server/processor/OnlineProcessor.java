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
 * OnlineProcessor.java at 2017-12-9 11:24:33, code by Jack Jiang.
 * You can contact author with jack.jiang@52im.net or jb2011@163.com.
 */
package per.johnson.server.processor;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnlineProcessor
{
	public final static String USER_ID_IN_SESSION_ATTRIBUTE = "__user_id__";
	public static final AttributeKey<String> USER_ID_IN_SESSION_ATTRIBUTE_ATTR = AttributeKey.newInstance(USER_ID_IN_SESSION_ATTRIBUTE);
	
	public static boolean DEBUG = false;
	private static Logger logger = LoggerFactory.getLogger(OnlineProcessor.class); 
	private static OnlineProcessor instance = null;
	
	private ConcurrentHashMap<String, Channel> onlineSessions = new ConcurrentHashMap<>();
	
	public static OnlineProcessor getInstance()
	{
		if(instance == null) {
			synchronized (OnlineProcessor.class) {
				if(instance==null)
					instance = new OnlineProcessor();
			}
		}
		return instance;
	}
	
	private OnlineProcessor()
	{
	}
	
	public void putUser(String user_id, Channel session)
	{
		if(onlineSessions.containsKey(user_id))
		{
			logger.debug("[IMCORE]【注意】用户id="+user_id+"已经在在线列表中了，session也是同一个吗？"
					+(onlineSessions.get(user_id).hashCode() == session.hashCode()));
		}
		
		onlineSessions.put(user_id, session);
		
		__printOnline();// just for debug
	}
	
	public void __printOnline()
	{
		logger.debug("【@】当前在线用户共("+onlineSessions.size()+")人------------------->");
		if(DEBUG)
		{
			for(String key : onlineSessions.keySet())
				logger.debug("      > user_id="+key+",session="+onlineSessions.get(key).remoteAddress());
		}
	}
	
	public boolean removeUser(String user_id)
	{
		synchronized(onlineSessions)
		{
			if(!onlineSessions.containsKey(user_id))
			{
				logger.warn("[IMCORE]！用户id="+user_id+"不存在在线列表中，本次removeUser没有继续.");
				__printOnline();// just for debug
				return false;
			}
			else
				return (onlineSessions.remove(user_id) != null);
		}
	}
	
	public Channel getOnlineSession(String user_id)
	{
		return onlineSessions.get(user_id);
	}
	
	public ConcurrentHashMap<String, Channel> getOnlineSessions()
	{
		return onlineSessions;
	}

	public static boolean isLogined(Channel session)
	{
		return session != null && getUserIdFromSession(session) != null;
	}
	
	public static String getUserIdFromSession(Channel session)
	{
		Object attr = null;
		if(session != null)
		{
			attr = session.attr(USER_ID_IN_SESSION_ATTRIBUTE_ATTR).get();
			if(attr != null)
				return (String)attr;
		}
		return null;
	}
	
	public static boolean isOnline(String userId)
	{
		return OnlineProcessor.getInstance().getOnlineSession(userId) != null;
	}
}
