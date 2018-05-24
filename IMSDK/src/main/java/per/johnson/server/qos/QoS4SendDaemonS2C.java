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
 * QoS4SendDaemonS2C.java at 2017-12-9 11:24:33, code by Jack Jiang.
 * You can contact author with jack.jiang@52im.net or jb2011@163.com.
 */
package per.johnson.server.qos;

public class QoS4SendDaemonS2C extends QoS4SendDaemonRoot
{
	private static QoS4SendDaemonS2C instance = null;
	
	public static QoS4SendDaemonS2C getInstance()
	{
		if(instance == null) {
			synchronized (QoS4SendDaemonS2C.class) {
				if (instance == null)
					instance = new QoS4SendDaemonS2C();
			}
		}
		return instance;
	}
	
	private QoS4SendDaemonS2C()
	{
		super(0 
			, 0 
			, -1
			, true
			, "-本机QoS");
	}
}
