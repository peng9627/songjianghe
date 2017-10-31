package mahjong.timeout;


import mahjong.constant.Constant;
import mahjong.entrance.MahjongTcpService;

import java.util.Date;

/**
 * Created by pengyi
 * Date : 17-9-13.
 * desc:
 */
public class MessageTimeout extends Thread {

    private Date lastMessageDate;
    private int userId;

    public MessageTimeout(Date lastMessageDate, int userId) {
        this.lastMessageDate = lastMessageDate;
        this.userId = userId;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                wait(Constant.messageTimeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (MahjongTcpService.userClients.containsKey(userId)
                && 0 == MahjongTcpService.userClients.get(userId).lastMessageDate.compareTo(lastMessageDate)) {
            MahjongTcpService.userClients.get(userId).close();
        }
    }
}
