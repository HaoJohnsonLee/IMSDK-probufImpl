package per.johnson.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.johnson.server.event.MessageQoSEventListenerS2C;
import per.johnson.server.protocal.ProtocalOuterClass.Protocal;

import java.util.ArrayList;

/**
 * 由开发者通过服务端消息发送接口发出的消息的消息送达相关事件（由S2C模式（即Server to Client）
 * 下QoS机制通知上来的）在此MessageQoSEventListenerS2C子类中实现即可
 */
public class MessageQoSEventS2CListnerImpl implements MessageQoSEventListenerS2C {
    private static Logger logger = LoggerFactory.getLogger(MessageQoSEventS2CListnerImpl.class);

    @Override
    public void messagesLost(ArrayList<Protocal> lostMessages)
    {
        logger.debug("【DEBUG_QoS_S2C事件】收到系统的未实时送达事件通知，当前共有"
                +lostMessages.size()+"个包QoS保证机制结束，判定为【无法实时送达】！");
    }

    @Override
    public void messagesBeReceived(String theFingerPrint)
    {
        if(theFingerPrint != null)
        {
            logger.debug("【DEBUG_QoS_S2C事件】收到对方已收到消息事件的通知，fp="+theFingerPrint);
        }
    }
}
