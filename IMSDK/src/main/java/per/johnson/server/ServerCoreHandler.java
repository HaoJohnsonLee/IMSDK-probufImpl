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
 * ServerCoreHandler.java at 2017-12-9 11:24:34, code by Jack Jiang.
 * You can contact author with jack.jiang@52im.net or jb2011@163.com.
 */
package per.johnson.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.johnson.server.event.GroupEventListener;
import per.johnson.server.event.MessageQoSEventListenerS2C;
import per.johnson.server.event.ServerEventListener;
import per.johnson.server.processor.BridgeProcessor;
import per.johnson.server.processor.LogicProcessor;
import per.johnson.server.processor.OnlineProcessor;
import per.johnson.server.protocal.ProtocalOuterClass.Protocal;
import per.johnson.server.protocal.ProtocalType;
import per.johnson.server.utils.LocalSendHelper;
import per.johnson.server.utils.ServerToolKits;

import java.util.List;

public class ServerCoreHandler {
    private static Logger logger = LoggerFactory.getLogger(ServerCoreHandler.class);

    protected GroupEventListener groupEventListener = null;
    protected ServerEventListener serverEventListener = null;
    protected MessageQoSEventListenerS2C serverMessageQoSEventListener = null;

    protected LogicProcessor logicProcessor = null;

    protected BridgeProcessor bridgeProcessor = null;

    public ServerCoreHandler() {
        logicProcessor = this.createLogicProcessor();
        if (ServerLauncher.bridgeEnabled)
            bridgeProcessor = this.createBridgeProcessor();
    }

    protected LogicProcessor createLogicProcessor() {
        return new LogicProcessor(this);
    }

    protected BridgeProcessor createBridgeProcessor() {
        BridgeProcessor bp = new BridgeProcessor() {
            protected void realtimeC2CSuccessCallback(Protocal p) {
                serverEventListener.onTransBuffer_C2C_CallBack(
                        p.getTo(), p.getFrom(), p.getDataContent(), p.getFp(), p.getTypeu());
            }

            @Override
            protected boolean offlineC2CProcessCallback(Protocal p) {
                return serverEventListener.onTransBuffer_C2C_RealTimeSendFaild_CallBack(
                        p.getTo(), p.getFrom(), p.getDataContent(), p.getFp(), p.getTypeu());
            }
        };
        return bp;
    }

    public void lazyStartupBridgeProcessor() {
        if (ServerLauncher.bridgeEnabled && bridgeProcessor != null) {
            bridgeProcessor.start();
        }
    }

    public void exceptionCaught(Channel session, Throwable cause) throws Exception {
        logger.debug("[IMCORE-netty]此客户端的Channel抛出了exceptionCaught，原因是：" + cause.getMessage() + "，可以提前close掉了哦！", cause);
        session.close();
    }

    public void messageReceived(Channel session, ByteBuf bytebuf) throws Exception {
        Protocal pFromClient = ServerToolKits.fromIOBuffer(bytebuf);
        String remoteAddress = ServerToolKits.clientInfoToString(session);
//    	logger.info("---------------------------------------------------------");
//    	logger.info("[IMCORE-netty] << 收到客户端"+remoteAddress+"的消息:::"+pFromClient.toGsonString());

        switch (pFromClient.getType()) {
            case ProtocalType.C.FROM_CLIENT_TYPE_OF_RECIVED: {
                logger.info("[IMCORE-netty]<< 收到客户端" + remoteAddress + "的ACK应答包发送请求.");
                if (!OnlineProcessor.isLogined(session)) {
                    LocalSendHelper.replyDataForUnlogined(session, pFromClient, null);
                    return;
                }
                logicProcessor.processACK(pFromClient, remoteAddress);
                break;
            }
            case ProtocalType.C.FROM_CLIENT_TYPE_OF_COMMON$DATA: {
                logger.info("[IMCORE-netty]<< 收到客户端" + remoteAddress + "的通用数据发送请求.");
                if (serverEventListener != null) {
                    if (!OnlineProcessor.isLogined(session)) {
                        LocalSendHelper.replyDataForUnlogined(session, pFromClient, null);
                        return;
                    }
                    //判断typeu
                    //0 必然不是群聊类型
                    if ("0".equals(pFromClient.getTo()))
                        logicProcessor.processC2SMessage(session, pFromClient, remoteAddress);
                    else { //判断typeu = 1 + groupId
                        if (pFromClient.getTypeu() == ProtocalType.C_S.P2P_CHAT) {
                            logicProcessor.processC2CMessage(bridgeProcessor, session
                                    , pFromClient, remoteAddress);
                        } else if (pFromClient.getTypeu() > 1) { // 群聊类型   1 + to(groupId) = typeu
                            //TODO 分发消息
                            if (groupEventListener == null) {
                                logger.error("请实现GroupEventListener接口添加对群聊的支持");
                            } else {
                                int groupId = pFromClient.getTypeu() - 1;
                                List<Integer> ids = groupEventListener.getGroupMembers(groupId);
                                if(ids == null||ids.size()==0){
                                    logger.error("该群聊为空 groupID: " +groupId);
                                    return;
                                }
                                for (int id : ids) { //构造分发数据 群聊数据不需要Qos服务
                                    Protocal protocal = Protocal.newBuilder(pFromClient).setTo(String.valueOf(id)).setQoS(false).build();
                                    logicProcessor.processC2CMessage(bridgeProcessor, session, protocal, remoteAddress);
                                }
                            }
                        } else {
                            logger.debug("IMCORE-netty<<收到客户端" + remoteAddress + "通用数据请求，但是是无法识别的typeu");
                        }
                    }
                } else {
                    logger.warn("[IMCORE-netty]<< 收到客户端" + remoteAddress + "的通用数据传输消息，但回调对象是null，回调无法继续.");
                }
                break;
            }
            case ProtocalType.C.FROM_CLIENT_TYPE_OF_KEEP$ALIVE: {
                if (!OnlineProcessor.isLogined(session)) {
                    LocalSendHelper.replyDataForUnlogined(session, pFromClient, null);
                    return;
                } else
                    logicProcessor.processKeepAlive(session, pFromClient, remoteAddress);

                break;
            }
            case ProtocalType.C.FROM_CLIENT_TYPE_OF_LOGIN: {
                logicProcessor.processLogin(session, pFromClient, remoteAddress);
                break;
            }
            case ProtocalType.C.FROM_CLIENT_TYPE_OF_LOGOUT: {
                logger.info("[IMCORE-netty]<< 收到客户端" + remoteAddress + "的退出登陆请求.");
                session.close();
                break;
            }
            case ProtocalType.C.FROM_CLIENT_TYPE_OF_ECHO: {
                Protocal toProto = Protocal.newBuilder(pFromClient).setType(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$ECHO).build();
                LocalSendHelper.sendData(session, toProto, null);
                break;
            }
            default: {
                logger.warn("[IMCORE-netty]【注意】收到的客户端" + remoteAddress + "消息类型：" + pFromClient.getType() + "，但目前该类型服务端不支持解析和处理！");
                break;
            }
        }
    }

    public void sessionClosed(Channel session) throws Exception {
        String user_id = OnlineProcessor.getUserIdFromSession(session);

        Channel sessionInOnlinelist = OnlineProcessor.getInstance().getOnlineSession(user_id);

        logger.info("[IMCORE-netty]" + ServerToolKits.clientInfoToString(session) + "的会话已关闭(user_id=" + user_id + ")了...");

        if (user_id != null) {
            if (sessionInOnlinelist != null && session != null && session == sessionInOnlinelist) {
                OnlineProcessor.getInstance().removeUser(user_id);

                if (serverEventListener != null)
                    serverEventListener.onUserLogoutAction_CallBack(user_id, null, session);
                else
                    logger.debug("[IMCORE-netty]>> 会话" + ServerToolKits.clientInfoToString(session)
                            + "被系统close了，但回调对象是null，没有进行回调通知.");
            } else {
                logger.warn("[IMCORE-netty]【2】【注意】会话" + ServerToolKits.clientInfoToString(session)
                        + "不在在线列表中，意味着它是被客户端弃用的，本次忽略这条关闭事件即可！");
            }
        } else {
            logger.warn("[IMCORE-netty]【注意】会话" + ServerToolKits.clientInfoToString(session) + "被系统close了，但它里面没有存放user_id，这个会话是何时建立的？");
        }
    }

    public void sessionCreated(Channel session) throws Exception {
        logger.info("[IMCORE-netty]与" + ServerToolKits.clientInfoToString(session) + "的会话建立(channelActive)了...");
    }

    public ServerEventListener getServerEventListener() {
        return serverEventListener;
    }

    void setServerEventListener(ServerEventListener serverEventListener) {
        this.serverEventListener = serverEventListener;
    }

    public MessageQoSEventListenerS2C getServerMessageQoSEventListener() {
        return serverMessageQoSEventListener;
    }

    void setServerMessageQoSEventListener(MessageQoSEventListenerS2C serverMessageQoSEventListener) {
        this.serverMessageQoSEventListener = serverMessageQoSEventListener;
    }

    public BridgeProcessor getBridgeProcessor() {
        return bridgeProcessor;
    }
}
