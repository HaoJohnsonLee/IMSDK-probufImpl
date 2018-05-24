package per.johnson.server.event;

import java.util.List;

/**
 * Created by Johnson on 2018/4/22.
 * 群聊相关接口
 */
public interface GroupEventListener {
    List<Integer> getGroupMembers(int groupId) throws Exception;
}
