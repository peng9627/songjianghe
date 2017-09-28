package mahjong.constant;

/**
 * Created by pengyi
 * Date : 17-9-6.
 * desc:
 */
public class Constant {

    public static String apiUrl = "http://127.0.0.1:9999/api";
    public static String userInfoUrl = "/user/info";
    public static String userListUrl = "/user/list";
    public static String gamerecordCreateUrl = "/gamerecord/create";
    public static String moneyDetailedCreate = "/money_detailed/create";

    public static int readyTimeout = 10000;
    public static int playCardTimeout = 18000;
    public static int dissolve = 180000;
    public static int messageTimeout = 300000;
    //TODO 少一个0
    public static int matchEliminateScoreTimeout = 30000;
    public static int matchEliminateScore = 100;

}
