package per.johnson.server.processor;

import java.util.List;

/**
 * Created by Johnson on 2018/4/12.
 * 应用层添加对群聊的支持
 */
public interface    GroupProcessor {
    List<Integer> getMembers(String groupId);
}
