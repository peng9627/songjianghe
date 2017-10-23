package mahjong.constant;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by pengyi
 * Date : 17-9-6.
 * desc:
 */
public class Constant {

    public static String apiUrl;
    public static String userInfoUrl;
    public static String userListUrl;
    public static String gamerecordCreateUrl;
    public static String moneyDetailedCreate;

    public static int readyTimeout;
    public static int playCardTimeout;
    public static int dissolve;
    public static int messageTimeout;

    public static int channelPoolCount;
    public static String logicServiceIp;
    public static int logicServicePort;
    public static String notifyRoomItem;
    public static String notifyRoomList;

    public static void init() {
        BufferedInputStream in = null;
        try {
            Properties prop = new Properties();
            prop.load(Constant.class.getResourceAsStream("/config.properties"));

            apiUrl = prop.getProperty("apiUrl");
            userInfoUrl = prop.getProperty("userInfoUrl");
            userListUrl = prop.getProperty("userListUrl");
            gamerecordCreateUrl = prop.getProperty("gamerecordCreateUrl");
            moneyDetailedCreate = prop.getProperty("moneyDetailedCreate");
            readyTimeout = Integer.parseInt(prop.getProperty("readyTimeout"));
            playCardTimeout = Integer.parseInt(prop.getProperty("playCardTimeout"));
            dissolve = Integer.parseInt(prop.getProperty("dissolve"));
            messageTimeout = Integer.parseInt(prop.getProperty("messageTimeout"));
            channelPoolCount = Integer.parseInt(prop.getProperty("channelPoolCount"));
            logicServiceIp = prop.getProperty("logicServiceIp");
            logicServicePort = Integer.parseInt(prop.getProperty("logicServicePort"));
            notifyRoomItem = prop.getProperty("notifyRoomItem");
            notifyRoomList = prop.getProperty("notifyRoomList");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
