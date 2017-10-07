package mahjong.channelPool;

import io.grpc.ManagedChannel;

/**
 * Created by pengyi
 * Date : 17-9-29.
 * desc:
 */
public class ChannelInfo {

    private ManagedChannel channel;         //通道
    private boolean isFree;                 //是否空闲 （是：true  否：false）
    private Integer channelId;              //通道 id
    private boolean isClosed;               //是否为可关闭链接 （是：true  否：false）

    public ManagedChannel getChannel() {
        return channel;
    }

    public void setChannel(ManagedChannel channel) {
        this.channel = channel;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean isFree) {
        this.isFree = isFree;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean isClosed) {
        this.isClosed = isClosed;
    }

}
