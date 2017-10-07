package mahjong.channelPool;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import mahjong.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pengyi
 * Date : 17-9-29.
 * desc:
 */
public class ChannelPool {

    private static Logger logger = LoggerFactory.getLogger(ChannelPool.class);//日志类

    public static ConcurrentHashMap<Integer, ChannelInfo> channelMap = new ConcurrentHashMap<Integer, ChannelInfo>();

    private static ChannelPool instance = new ChannelPool();

    private ChannelPool() {
    }

    public static ChannelPool getInstance() {
        if (instance == null) {
            synchronized (ChannelPool.class) {
                if (instance == null) {
                    instance = new ChannelPool();
                }
            }
        }
        return instance;
    }

    static {
        instance.initChannel(true);
    }

    /**
     * @param isAllReInit 是否全部重新初始化
     * @return void
     * @Desc 初始化链接池
     */
    public void initChannel(boolean isAllReInit) {
        for (int i = 0; i < Constant.channelPoolCount; i++) {

            if (isAllReInit) {
                channelMap.put(i, setChannelInfo(i, true, false));
            } else {
                if (channelMap.get(i) == null || channelMap.get(i).isClosed()) {
                    channelMap.put(i, setChannelInfo(i, true, false));
                }
            }
        }
    }

    /**
     * @param key
     * @param isFree
     * @param isClosed
     * @return ChannelInfo
     * @Desc 设置channelInfo值
     */
    private static ChannelInfo setChannelInfo(Integer key, boolean isFree, boolean isClosed) {
        ChannelInfo channelInfo = new ChannelInfo();
        ManagedChannel channel = createChannel();
        channelInfo.setFree(isFree);
        channelInfo.setChannel(channel);
        channelInfo.setChannelId(key);
        channelInfo.setClosed(isClosed);
        return channelInfo;
    }

    /**
     * @return ChannelInfo
     * @Desc 获取名字服务器链接
     */
    public synchronized ChannelInfo getChannelInfo() {

        ChannelInfo channelInfo = null;

        if (channelMap.size() < Constant.channelPoolCount) {
            initChannel(false);
        }

        if (channelMap.size() > 0) {
            for (Map.Entry<Integer, ChannelInfo> entry : channelMap.entrySet()) {
                channelInfo = entry.getValue();
                if (channelInfo.isFree() && !channelInfo.getChannel().isShutdown()) {
                    channelInfo.setFree(false);
                    return channelInfo;
                }
            }
        }

        channelInfo = setChannelInfo(-1, true, true);
        return channelInfo;

    }

    /**
     * 释放channel
     *
     * @param channelId
     */
    public static void distoryChannel(Integer channelId) {

        ChannelInfo channelInfo = channelMap.get(channelId);
        channelInfo.setFree(true);

    }

    /**
     * @param channelInfo void
     * @Desc 释放channel
     */
    public static void distoryChannel(ChannelInfo channelInfo) {

        if (channelInfo == null) return;

        if (!channelInfo.isClosed()) {
            distoryChannel(channelInfo.getChannelId());
            return;
        }

        if (channelInfo.getChannel() != null) {
            channelInfo.getChannel().shutdown();
        }
        channelInfo = null;
    }

    /**
     * @return Channel
     * @Desc 创建channel
     */
    public static ManagedChannel createChannel() {

        ManagedChannel channel = null;
        channel = ManagedChannelBuilder.forAddress(Constant.logicServiceIp, Constant.logicServicePort)
                .usePlaintext(true)
                .build();
        return channel;
    }
}
