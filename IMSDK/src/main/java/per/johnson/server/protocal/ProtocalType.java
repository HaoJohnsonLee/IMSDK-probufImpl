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
 * ProtocalType.java at 2017-12-9 11:24:33, code by Jack Jiang.
 * You can contact author with jack.jiang@52im.net or jb2011@163.com.
 */
package per.johnson.server.protocal;

/**
 * 一些状态码
 */
public interface ProtocalType
{
	//------------------------------------------------------- from client
    interface C
	{
	    /** 来自客户端 登陆Sign*/
		int FROM_CLIENT_TYPE_OF_LOGIN = 0;
        /** 来自客户端 保活Sign*/
		int FROM_CLIENT_TYPE_OF_KEEP$ALIVE = 1;
        /** 来自客户端 一般数据Sign*/
		int FROM_CLIENT_TYPE_OF_COMMON$DATA = 2;
        /** 来自客户端 登出Sign*/
		int FROM_CLIENT_TYPE_OF_LOGOUT = 3;

        /** 来自客户端 接收到消息应答Sign*/
		int FROM_CLIENT_TYPE_OF_RECIVED = 4;
        /** 来自客户端 ECHO*/
		int FROM_CLIENT_TYPE_OF_ECHO = 5;
	}
	
	//------------------------------------------------------- from server
    interface S
	{
	    /** 来自服务端 登陆应答*/
		int FROM_SERVER_TYPE_OF_RESPONSE$LOGIN = 50;
        /** 来自服务端 保活应答Sign*/
		int FROM_SERVER_TYPE_OF_RESPONSE$KEEP$ALIVE = 51;
        /** 来自服务端 出错应答*/
		int FROM_SERVER_TYPE_OF_RESPONSE$FOR$ERROR = 52;
        /** 来自服务端 ECHO应答*/
		int FROM_SERVER_TYPE_OF_RESPONSE$ECHO = 53;
	}
	//------------------------------------------------------ typeu S C 通用，描述内容类型
	interface C_S{
        int P2P_CHAT = 1;
        int GROUP_CHAT = 2;
    }
}
