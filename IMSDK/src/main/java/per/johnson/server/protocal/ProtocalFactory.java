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
 * ProtocalFactory.java at 2017-12-9 11:24:33, code by Jack Jiang.
 * You can contact author with jack.jiang@52im.net or jb2011@163.com.
 */
package per.johnson.server.protocal;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import per.johnson.server.protocal.c.PKeepAlive;
import per.johnson.server.protocal.c.PLoginInfo;
import per.johnson.server.protocal.s.PErrorResponse;
import per.johnson.server.protocal.s.PKeepAliveResponse;
import per.johnson.server.protocal.s.PLoginInfoResponse;
import per.johnson.server.protocal.ProtocalOuterClass.Protocal;

import java.util.Arrays;

/**
 * 生产Protocal
 */
public class ProtocalFactory
{
	private static String create(Object c)
	{
		return new Gson().toJson(c);
	}
	
	public static <T> T parse(byte[] fullProtocalJASOnBytes, int len, Class<T> clazz)
	{
		return parse(CharsetHelper.getString(fullProtocalJASOnBytes, len), clazz);
	}
	
	public static <T> T parse(String dataContentOfProtocal, Class<T> clazz)
	{
		return new Gson().fromJson(dataContentOfProtocal, clazz);
	}
	
	public static ProtocalOuterClass.Protocal parse(byte[] fullProtocalJASOnBytes, int len)
	{
		//return parse(fullProtocalJASOnBytes, len, ProtocalOuterClass.Protocal.class);
        try {
            return Protocal.parseFrom(Arrays.copyOf(fullProtocalJASOnBytes,len));
        } catch (InvalidProtocolBufferException e) {
            return Protocal.newBuilder().setType(-1).build();
        }
    }
	
	public static ProtocalOuterClass.Protocal createPKeepAliveResponse(String to_user_id)
	{
        //Protocal(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$KEEP$ALIVE, create(new PKeepAliveResponse()), "0", to_user_id);
        return Protocal.newBuilder()
                .setType(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$KEEP$ALIVE)
                .setDataContent(create(new PKeepAliveResponse()))
                .setFrom("0")
                .setTo(to_user_id).build();
	}
	
	public static PKeepAliveResponse parsePKeepAliveResponse(String dataContentOfProtocal)
	{
		return parse(dataContentOfProtocal, PKeepAliveResponse.class);
	}
	
	public static Protocal createPKeepAlive(String from_user_id)
	{
	    return Protocal.newBuilder()
                .setType(ProtocalType.C.FROM_CLIENT_TYPE_OF_KEEP$ALIVE)
                .setDataContent(create(new PKeepAlive()))
                .setFrom(from_user_id)
                .setTo("0").build();
	}
	
	public static PKeepAlive parsePKeepAlive(String dataContentOfProtocal)
	{
		return parse(dataContentOfProtocal, PKeepAlive.class);
	}
	
	public static Protocal createPErrorResponse(int errorCode, String errorMsg, String user_id)
	{
		//return new Protocal(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$FOR$ERROR, create(new PErrorResponse(errorCode, errorMsg)), "0", user_id);
        return Protocal.newBuilder()
                .setType(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$FOR$ERROR)
                .setDataContent(create(new PErrorResponse(errorCode, errorMsg)))
                .setFrom("0")
                .setTo(user_id).build();
	}
	
	public static PErrorResponse parsePErrorResponse(String dataContentOfProtocal)
	{
		return parse(dataContentOfProtocal, PErrorResponse.class);
	}
	
	public static Protocal createPLoginoutInfo(String user_id)
	{
		//return new Protocal(ProtocalType.C.FROM_CLIENT_TYPE_OF_LOGOUT, null, user_id, "0");
        return Protocal.newBuilder()
                .setType(ProtocalType.C.FROM_CLIENT_TYPE_OF_LOGOUT)
                .setFrom(user_id)
                .setTo("0").build();
	}
	
	public static Protocal createPLoginInfo(String userId, String token, String extra)
	{
		/*return new Protocal(ProtocalType.C.FROM_CLIENT_TYPE_OF_LOGIN
				, create(new PLoginInfo(userId, token, extra))
					, userId
					, "0");*/
		return Protocal.newBuilder().setType(ProtocalType.C.FROM_CLIENT_TYPE_OF_LOGIN)
                .setDataContent(create(new PLoginInfo(userId, token, extra)))
                .setFrom(userId)
                .setTo("0").build();
	}
	
	public static PLoginInfo parsePLoginInfo(String dataContentOfProtocal)
	{
		return parse(dataContentOfProtocal, PLoginInfo.class);
	}
	
	public static Protocal createPLoginInfoResponse(int code
			, String user_id)
	{
		//return new Protocal(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$LOGIN, create(new PLoginInfoResponse(code)), "0", user_id, true, Protocal.genFingerPrint());
		return Protocal.newBuilder().setType(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$LOGIN)
                .setDataContent(create(new PLoginInfoResponse(code)))
                .setFrom("0")
                .setTo(user_id)
                .setQoS(true)
                .setFp(Protocal.genFingerPrint()).build();
	}
	
	public static PLoginInfoResponse parsePLoginInfoResponse(String dataContentOfProtocal)
	{
		return parse(dataContentOfProtocal, PLoginInfoResponse.class);
	}
	
	public static Protocal createCommonData(String dataContent, String from_user_id, String to_user_id
			, boolean QoS, String fingerPrint)
	{
		return createCommonData(dataContent, from_user_id, to_user_id, QoS, fingerPrint, -1);
	}
	
	public static Protocal createCommonData(String dataContent, String from_user_id, String to_user_id
			, boolean QoS, String fingerPrint, int typeu)
	{
		/*return new Protocal(ProtocalType.C.FROM_CLIENT_TYPE_OF_COMMON$DATA
				, dataContent, from_user_id, to_user_id, QoS, fingerPrint, typeu);*/
		return Protocal.newBuilder().setType(ProtocalType.C.FROM_CLIENT_TYPE_OF_COMMON$DATA).setDataContent(dataContent).setFrom(from_user_id).setTo(to_user_id)
                .setQoS(QoS).setFp(fingerPrint).setTypeu(typeu).build();
	}
	
	public static Protocal createRecivedBack(String from_user_id, String to_user_id
			, String recievedMessageFingerPrint)
	{
		return createRecivedBack(from_user_id, to_user_id, recievedMessageFingerPrint, false);
	}
	
	public static Protocal createRecivedBack(String from_user_id, String to_user_id
			, String recievedMessageFingerPrint, boolean bridge)
	{
		/*Protocal p = new Protocal(ProtocalType.C.FROM_CLIENT_TYPE_OF_RECIVED
				, recievedMessageFingerPrint, from_user_id, to_user_id);
		p.setBridge(bridge);
		return p;*/
		return Protocal.newBuilder().setType(ProtocalType.C.FROM_CLIENT_TYPE_OF_RECIVED).setFp(recievedMessageFingerPrint)
                .setFrom(from_user_id).setTo(to_user_id).setBridge(bridge).build();
	}
}
