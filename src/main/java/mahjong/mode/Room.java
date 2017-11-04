package mahjong.mode;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.protobuf.InvalidProtocolBufferException;
import mahjong.channelPool.ChannelInfo;
import mahjong.channelPool.ChannelPool;
import mahjong.constant.Constant;
import mahjong.entrance.MahjongTcpService;
import mahjong.mode.proto.*;
import mahjong.redis.RedisService;
import mahjong.timeout.OperationTimeout;
import mahjong.timeout.PlayCardTimeout;
import mahjong.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Author pengyi
 * Date 17-3-7.
 */
public class Room {

    private Logger logger = LoggerFactory.getLogger(Room.class);

    private String roomNo;  //桌号
    private List<Seat> seats = new ArrayList<>();//座位
    private List<Integer> seatNos;
    private int operationSeatNo;
    private List<OperationHistory> historyList = new ArrayList<>();
    private List<Integer> surplusCards;//剩余的牌
    private GameStatus gameStatus;

    private int lastOperation;

    private int banker;//庄家
    private int gameTimes; //游戏局数
    private int count;//人数
    private boolean normal; //true正常结算 false点炮包自己
    private boolean singleFan; //true1翻封，false 16翻
    private int gameRules;////游戏规则  高位到低位顺序（一炮多响，旋风杠，飘，允许相同ip，代开房）
    private Integer[] dice;//骰子
    private List<Record> recordList = new ArrayList<>();//战绩
    private int gameCount;

    private int roomOwner;

    private Date startDate;

    private boolean gangMo;

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public List<Integer> getSeatNos() {
        return seatNos;
    }

    public void setSeatNos(List<Integer> seatNos) {
        this.seatNos = seatNos;
    }

    public int getOperationSeatNo() {
        return operationSeatNo;
    }

    public void setOperationSeatNo(int operationSeatNo) {
        this.operationSeatNo = operationSeatNo;
    }

    public List<OperationHistory> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<OperationHistory> historyList) {
        this.historyList = historyList;
    }

    public List<Integer> getSurplusCards() {
        return surplusCards;
    }

    public void setSurplusCards(List<Integer> surplusCards) {
        this.surplusCards = surplusCards;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public int getLastOperation() {
        return lastOperation;
    }

    public void setLastOperation(int lastOperation) {
        this.lastOperation = lastOperation;
    }

    public int getBanker() {
        return banker;
    }

    public void setBanker(int banker) {
        this.banker = banker;
    }

    public int getGameTimes() {
        return gameTimes;
    }

    public void setGameTimes(int gameTimes) {
        this.gameTimes = gameTimes;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isNormal() {
        return normal;
    }

    public void setNormal(boolean normal) {
        this.normal = normal;
    }

    public boolean isSingleFan() {
        return singleFan;
    }

    public void setSingleFan(boolean singleFan) {
        this.singleFan = singleFan;
    }

    public int getGameRules() {
        return gameRules;
    }

    public void setGameRules(int gameRules) {
        this.gameRules = gameRules;
    }

    public Integer[] getDice() {
        return dice;
    }

    public void setDice(Integer[] dice) {
        this.dice = dice;
    }

    public List<Record> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<Record> recordList) {
        this.recordList = recordList;
    }

    public int getGameCount() {
        return gameCount;
    }

    public void setGameCount(int gameCount) {
        this.gameCount = gameCount;
    }

    public int getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(int roomOwner) {
        this.roomOwner = roomOwner;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void addSeat(User user, int score) {
        Seat seat = new Seat();
        seat.setRobot(false);
        seat.setReady(false);
        seat.setAreaString(user.getArea());
        seat.setHead(user.getHead());
        seat.setNickname(user.getNickname());
        seat.setSex(user.getSex().equals("1"));
        seat.setScore(score);
        seat.setIp(user.getLastLoginIp());
        seat.setGameCount(user.getGameCount());
        seat.setSeatNo(seatNos.get(0));
        seatNos.remove(0);
        seat.setUserId(user.getUserId());
        seats.add(seat);
    }

    public boolean isGangMo() {
        return gangMo;
    }

    public void setGangMo(boolean gangMo) {
        this.gangMo = gangMo;
    }

    public void dealCard() {
        startDate = new Date();
        surplusCards = Card.getAllCard();

        StringBuilder stringBuilder = new StringBuilder();
        for (Seat seat1 : seats) {
            stringBuilder.append(",").append(seat1.getUserId());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userIds", stringBuilder.toString().substring(1));
        ApiResponse<List<User>> usersResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.userListUrl, jsonObject.toJSONString()),
                new TypeReference<ApiResponse<List<User>>>() {
                });
        if (0 == usersResponse.getCode()) {
            List<User> users = usersResponse.getData();
            Map<Integer, User> userMap = new HashMap<>();
            for (User user : users) {
                userMap.put(user.getUserId(), user);
            }

            //卖马 发牌
            for (Seat seat : seats) {
                seat.setReady(false);
                List<Integer> cardList = new ArrayList<>();
                int cardIndex;

                switch (userMap.get(seat.getUserId()).getCardType()) {
                    case 0:
                        for (int i = 0; i < 13; i++) {
                            cardIndex = (int) (Math.random() * surplusCards.size());
                            cardList.add(surplusCards.get(cardIndex));
                            surplusCards.remove(cardIndex);
                        }
                        break;
                    case 1:
                        List<Integer> dui = MahjongUtil.get_dui(surplusCards);
                        int index = (int) (Math.random() * (dui.size() / 2));
                        cardList.add(dui.get(2 * index));
                        cardList.add(dui.get(2 * index + 1));
                        Card.remove(surplusCards, dui.get(2 * index));
                        Card.remove(surplusCards, dui.get(2 * index + 1));
                        switch ((int) (Math.random() * 3)) {
                            case 0:
                                List<Integer> san = MahjongUtil.get_san(surplusCards);
                                index = (int) (Math.random() * (san.size() / 3));
                                cardList.add(san.get(3 * index));
                                cardList.add(san.get(3 * index + 1));
                                cardList.add(san.get(3 * index + 2));
                                Card.remove(surplusCards, san.get(3 * index));
                                Card.remove(surplusCards, san.get(3 * index + 1));
                                Card.remove(surplusCards, san.get(3 * index + 2));

                                for (int i = 0; i < 2; i++) {
                                    List<Integer> shun = MahjongUtil.get_shun(surplusCards);
                                    index = (int) (Math.random() * (shun.size() / 3));
                                    cardList.add(shun.get(3 * index));
                                    cardList.add(shun.get(3 * index + 1));
                                    cardList.add(shun.get(3 * index + 2));
                                    Card.remove(surplusCards, shun.get(3 * index));
                                    Card.remove(surplusCards, shun.get(3 * index + 1));
                                    Card.remove(surplusCards, shun.get(3 * index + 2));
                                }
                                break;
                            case 1:
                                List<Integer> si = MahjongUtil.get_si(surplusCards);
                                index = (int) (Math.random() * (si.size() / 4));
                                cardList.add(si.get(4 * index));
                                cardList.add(si.get(4 * index + 1));
                                cardList.add(si.get(4 * index + 2));
                                cardList.add(si.get(4 * index + 3));
                                Card.remove(surplusCards, si.get(4 * index));
                                Card.remove(surplusCards, si.get(4 * index + 1));
                                Card.remove(surplusCards, si.get(4 * index + 2));
                                Card.remove(surplusCards, si.get(4 * index + 3));


                                List<Integer> shun = MahjongUtil.get_shun(surplusCards);
                                index = (int) (Math.random() * (shun.size() / 3));
                                cardList.add(shun.get(3 * index));
                                cardList.add(shun.get(3 * index + 1));
                                cardList.add(shun.get(3 * index + 2));
                                Card.remove(surplusCards, shun.get(3 * index));
                                Card.remove(surplusCards, shun.get(3 * index + 1));
                                Card.remove(surplusCards, shun.get(3 * index + 2));
                                break;
                            case 2:
                                for (int i = 0; i < 3; i++) {
                                    san = MahjongUtil.get_san(surplusCards);
                                    index = (int) (Math.random() * (san.size() / 3));
                                    cardList.add(san.get(3 * index));
                                    cardList.add(san.get(3 * index + 1));
                                    cardList.add(san.get(3 * index + 2));
                                    Card.remove(surplusCards, san.get(3 * index));
                                    Card.remove(surplusCards, san.get(3 * index + 1));
                                    Card.remove(surplusCards, san.get(3 * index + 2));
                                }
                                break;
                        }
                        break;
                    case 2:
                        switch ((int) (Math.random() * 5)) {
                            case 0:
                                List<Integer> temps = new ArrayList<>();
                                while (null == temps || temps.size() < 13) {
                                    temps = Card.getAllSameColor(surplusCards, (int) (Math.random() * 3));
                                }
                                for (int i = 0; i < 13; i++) {
                                    cardIndex = (int) (Math.random() * temps.size());
                                    cardList.add(temps.get(cardIndex));
                                    surplusCards.remove(temps.get(cardIndex));
                                    temps.remove(cardIndex);
                                }
                                break;
                            case 1:
                                for (int i = 0; i < 5; i++) {
                                    dui = MahjongUtil.get_dui(surplusCards);
                                    index = (int) (Math.random() * (dui.size() / 2));
                                    cardList.add(dui.get(2 * index));
                                    cardList.add(dui.get(2 * index + 1));
                                    Card.remove(surplusCards, dui.get(2 * index));
                                    Card.remove(surplusCards, dui.get(2 * index + 1));
                                }
                                break;
                            case 2:
                                if (0 < Card.containSize(surplusCards, 41)) {
                                    cardList.add(41);
                                    surplusCards.remove(Integer.valueOf(41));
                                }
                                if (0 < Card.containSize(surplusCards, 43)) {
                                    cardList.add(43);
                                    surplusCards.remove(Integer.valueOf(43));
                                }
                                if (0 < Card.containSize(surplusCards, 45)) {
                                    cardList.add(45);
                                    surplusCards.remove(Integer.valueOf(45));
                                }
                                if (0 < Card.containSize(surplusCards, 47)) {
                                    cardList.add(47);
                                    surplusCards.remove(Integer.valueOf(47));
                                }

                                temps = new ArrayList<>();
                                while (null == temps || temps.size() < 13 - cardList.size()) {
                                    temps = Card.getAllSameColor(surplusCards, (int) (Math.random() * 3));
                                }
                                while (cardList.size() < 13) {
                                    cardIndex = (int) (Math.random() * temps.size());
                                    cardList.add(temps.get(cardIndex));
                                    surplusCards.remove(temps.get(cardIndex));
                                    temps.remove(cardIndex);
                                }
                                break;
                            case 3:

                                for (int i = 0; i < 2; i++) {
                                    dui = MahjongUtil.get_dui(surplusCards);
                                    index = (int) (Math.random() * (dui.size() / 2));
                                    cardList.add(dui.get(2 * index));
                                    cardList.add(dui.get(2 * index + 1));
                                    Card.remove(surplusCards, dui.get(2 * index));
                                    Card.remove(surplusCards, dui.get(2 * index + 1));
                                }

                                for (int i = 0; i < 3; i++) {
                                    List<Integer> san = MahjongUtil.get_san(surplusCards);
                                    index = (int) (Math.random() * (san.size() / 3));
                                    cardList.add(san.get(3 * index));
                                    cardList.add(san.get(3 * index + 1));
                                    cardList.add(san.get(3 * index + 2));
                                    Card.remove(surplusCards, san.get(3 * index));
                                    Card.remove(surplusCards, san.get(3 * index + 1));
                                    Card.remove(surplusCards, san.get(3 * index + 2));
                                }
                                break;
                            case 4:
                                if (0 < Card.containSize(surplusCards, 31)) {
                                    cardList.add(31);
                                    surplusCards.remove(Integer.valueOf(31));
                                }
                                if (0 < Card.containSize(surplusCards, 33)) {
                                    cardList.add(33);
                                    surplusCards.remove(Integer.valueOf(33));
                                }
                                if (0 < Card.containSize(surplusCards, 35)) {
                                    cardList.add(35);
                                    surplusCards.remove(Integer.valueOf(35));
                                }
                                temps = new ArrayList<>();
                                while (null == temps || temps.size() < 13 - cardList.size()) {
                                    temps = Card.getAllSameColor(surplusCards, (int) (Math.random() * 3));
                                }
                                while (cardList.size() < 13) {
                                    cardIndex = (int) (Math.random() * temps.size());
                                    cardList.add(temps.get(cardIndex));
                                    surplusCards.remove(temps.get(cardIndex));
                                    temps.remove(cardIndex);
                                }
                                break;
                        }
                        break;
                    case 3:
                        switch ((int) (Math.random() * 4)) {
                            case 0:
                                dui = MahjongUtil.get_dui(surplusCards);
                                index = (int) (Math.random() * (dui.size() / 2));
                                cardList.add(dui.get(2 * index));
                                cardList.add(dui.get(2 * index + 1));
                                Card.remove(surplusCards, dui.get(2 * index));
                                Card.remove(surplusCards, dui.get(2 * index + 1));

                                for (int i = 0; i < 2; i++) {
                                    List<Integer> san = MahjongUtil.get_san(surplusCards);
                                    index = (int) (Math.random() * (san.size() / 3));
                                    cardList.add(san.get(3 * index));
                                    cardList.add(san.get(3 * index + 1));
                                    cardList.add(san.get(3 * index + 2));
                                    Card.remove(surplusCards, san.get(3 * index));
                                    Card.remove(surplusCards, san.get(3 * index + 1));
                                    Card.remove(surplusCards, san.get(3 * index + 2));
                                }
                                break;
                            case 1:
                                if (0 < Card.containSize(surplusCards, 31)) {
                                    cardList.add(31);
                                    surplusCards.remove(Integer.valueOf(31));
                                }
                                if (0 < Card.containSize(surplusCards, 33)) {
                                    cardList.add(33);
                                    surplusCards.remove(Integer.valueOf(33));
                                }
                                if (0 < Card.containSize(surplusCards, 35)) {
                                    cardList.add(35);
                                    surplusCards.remove(Integer.valueOf(35));
                                }
                                for (int i = 0; i < 2; i++) {
                                    List<Integer> shun = MahjongUtil.get_shun(surplusCards);
                                    index = (int) (Math.random() * (shun.size() / 3));
                                    cardList.add(shun.get(3 * index));
                                    cardList.add(shun.get(3 * index + 1));
                                    cardList.add(shun.get(3 * index + 2));
                                    Card.remove(surplusCards, shun.get(3 * index));
                                    Card.remove(surplusCards, shun.get(3 * index + 1));
                                    Card.remove(surplusCards, shun.get(3 * index + 2));
                                }
                                break;
                            case 2:
                                if (0 < Card.containSize(surplusCards, 41)) {
                                    cardList.add(41);
                                    surplusCards.remove(Integer.valueOf(41));
                                }
                                if (0 < Card.containSize(surplusCards, 43)) {
                                    cardList.add(43);
                                    surplusCards.remove(Integer.valueOf(43));
                                }
                                if (0 < Card.containSize(surplusCards, 45)) {
                                    cardList.add(45);
                                    surplusCards.remove(Integer.valueOf(45));
                                }
                                if (0 < Card.containSize(surplusCards, 47)) {
                                    cardList.add(47);
                                    surplusCards.remove(Integer.valueOf(47));
                                }
                                List<Integer> san = MahjongUtil.get_san(surplusCards);
                                index = (int) (Math.random() * (san.size() / 3));
                                cardList.add(san.get(3 * index));
                                cardList.add(san.get(3 * index + 1));
                                cardList.add(san.get(3 * index + 2));
                                Card.remove(surplusCards, san.get(3 * index));
                                Card.remove(surplusCards, san.get(3 * index + 1));
                                Card.remove(surplusCards, san.get(3 * index + 2));
                                break;
                            case 3:
                                dui = MahjongUtil.get_dui(surplusCards);
                                index = (int) (Math.random() * (dui.size() / 2));
                                cardList.add(dui.get(2 * index));
                                cardList.add(dui.get(2 * index + 1));
                                Card.remove(surplusCards, dui.get(2 * index));
                                Card.remove(surplusCards, dui.get(2 * index + 1));

                                List<Integer> si = MahjongUtil.get_si(surplusCards);
                                index = (int) (Math.random() * (si.size() / 4));
                                cardList.add(si.get(4 * index));
                                cardList.add(si.get(4 * index + 1));
                                cardList.add(si.get(4 * index + 2));
                                cardList.add(si.get(4 * index + 3));
                                Card.remove(surplusCards, si.get(4 * index));
                                Card.remove(surplusCards, si.get(4 * index + 1));
                                Card.remove(surplusCards, si.get(4 * index + 2));
                                Card.remove(surplusCards, si.get(4 * index + 3));

                                san = MahjongUtil.get_san(surplusCards);
                                index = (int) (Math.random() * (san.size() / 3));
                                cardList.add(san.get(3 * index));
                                cardList.add(san.get(3 * index + 1));
                                cardList.add(san.get(3 * index + 2));
                                Card.remove(surplusCards, san.get(3 * index));
                                Card.remove(surplusCards, san.get(3 * index + 1));
                                Card.remove(surplusCards, san.get(3 * index + 2));
                                break;
                        }
                        break;
                    case 4:
                        switch ((int) (Math.random() * 5)) {
                            case 0:
                                dui = MahjongUtil.get_dui(surplusCards);
                                index = (int) (Math.random() * (dui.size() / 2));
                                cardList.add(dui.get(2 * index));
                                cardList.add(dui.get(2 * index + 1));
                                Card.remove(surplusCards, dui.get(2 * index));
                                Card.remove(surplusCards, dui.get(2 * index + 1));

                                List<Integer> san = MahjongUtil.get_san(surplusCards);
                                index = (int) (Math.random() * (san.size() / 3));
                                cardList.add(san.get(3 * index));
                                cardList.add(san.get(3 * index + 1));
                                cardList.add(san.get(3 * index + 2));
                                Card.remove(surplusCards, san.get(3 * index));
                                Card.remove(surplusCards, san.get(3 * index + 1));
                                Card.remove(surplusCards, san.get(3 * index + 2));
                                break;

                            case 1:
                                san = MahjongUtil.get_san(surplusCards);
                                index = (int) (Math.random() * (san.size() / 3));
                                cardList.add(san.get(3 * index));
                                cardList.add(san.get(3 * index + 1));
                                cardList.add(san.get(3 * index + 2));
                                Card.remove(surplusCards, san.get(3 * index));
                                Card.remove(surplusCards, san.get(3 * index + 1));
                                Card.remove(surplusCards, san.get(3 * index + 2));

                                List<Integer> shun = MahjongUtil.get_shun(surplusCards);
                                index = (int) (Math.random() * (shun.size() / 3));
                                cardList.add(shun.get(3 * index));
                                cardList.add(shun.get(3 * index + 1));
                                cardList.add(shun.get(3 * index + 2));
                                Card.remove(surplusCards, shun.get(3 * index));
                                Card.remove(surplusCards, shun.get(3 * index + 1));
                                Card.remove(surplusCards, shun.get(3 * index + 2));
                                break;
                            case 2:
                                if (0 < Card.containSize(surplusCards, 41)) {
                                    cardList.add(41);
                                    surplusCards.remove(Integer.valueOf(41));
                                }
                                if (0 < Card.containSize(surplusCards, 43)) {
                                    cardList.add(43);
                                    surplusCards.remove(Integer.valueOf(43));
                                }
                                if (0 < Card.containSize(surplusCards, 45)) {
                                    cardList.add(45);
                                    surplusCards.remove(Integer.valueOf(45));
                                }
                                if (0 < Card.containSize(surplusCards, 47)) {
                                    cardList.add(47);
                                    surplusCards.remove(Integer.valueOf(47));
                                }
                                dui = MahjongUtil.get_dui(surplusCards);
                                index = (int) (Math.random() * (dui.size() / 2));
                                cardList.add(dui.get(2 * index));
                                cardList.add(dui.get(2 * index + 1));
                                Card.remove(surplusCards, dui.get(2 * index));
                                Card.remove(surplusCards, dui.get(2 * index + 1));
                                break;
                            case 3:
                                if (0 < Card.containSize(surplusCards, 31)) {
                                    cardList.add(31);
                                    surplusCards.remove(Integer.valueOf(31));
                                }
                                if (0 < Card.containSize(surplusCards, 33)) {
                                    cardList.add(33);
                                    surplusCards.remove(Integer.valueOf(33));
                                }
                                if (0 < Card.containSize(surplusCards, 35)) {
                                    cardList.add(35);
                                    surplusCards.remove(Integer.valueOf(35));
                                }
                                shun = MahjongUtil.get_shun(surplusCards);
                                index = (int) (Math.random() * (shun.size() / 3));
                                cardList.add(shun.get(3 * index));
                                cardList.add(shun.get(3 * index + 1));
                                cardList.add(shun.get(3 * index + 2));
                                Card.remove(surplusCards, shun.get(3 * index));
                                Card.remove(surplusCards, shun.get(3 * index + 1));
                                Card.remove(surplusCards, shun.get(3 * index + 2));
                                break;
                            case 4:
                                if (0 < Card.containSize(surplusCards, 31)) {
                                    cardList.add(31);
                                    surplusCards.remove(Integer.valueOf(31));
                                }
                                if (0 < Card.containSize(surplusCards, 33)) {
                                    cardList.add(33);
                                    surplusCards.remove(Integer.valueOf(33));
                                }
                                if (0 < Card.containSize(surplusCards, 35)) {
                                    cardList.add(35);
                                    surplusCards.remove(Integer.valueOf(35));
                                }
                                san = MahjongUtil.get_san(surplusCards);
                                index = (int) (Math.random() * (san.size() / 3));
                                cardList.add(san.get(3 * index));
                                cardList.add(san.get(3 * index + 1));
                                cardList.add(san.get(3 * index + 2));
                                Card.remove(surplusCards, san.get(3 * index));
                                Card.remove(surplusCards, san.get(3 * index + 1));
                                Card.remove(surplusCards, san.get(3 * index + 2));
                                break;
                        }
                        break;
                }

                while (cardList.size() < 13) {
                    cardIndex = (int) (Math.random() * surplusCards.size());
                    cardList.add(surplusCards.get(cardIndex));
                    surplusCards.remove(cardIndex);
                }

                seat.setCards(cardList);
                seat.setInitialCards(cardList);

                CalculateData.Builder calculateData = CalculateData.newBuilder().setAllocid(3)
                        .setPlayer(majongPlayerData(seat));
                SjApplyCalculateData.Builder sjApplyCalculateData = SjApplyCalculateData.newBuilder().setXuanfeng(true);
                if (1 == (gameRules >> 1) % 2) {
                    if (0 < Card.containSize(seat.getXfGangCards(), 31)) {
                        sjApplyCalculateData.addXflist(1);
                    }
                    if (0 < Card.containSize(seat.getXfGangCards(), 41)) {
                        sjApplyCalculateData.addXflist(2);
                    }
                }
                if (singleFan) {
                    sjApplyCalculateData.setYifan(true);
                }
                calculateData.setAdjunct(sjApplyCalculateData.build().toByteString());
                calculateData.setAdjunct(sjApplyCalculateData.build().toByteString());
                ChannelInfo channelInfo = ChannelPool.getInstance().getChannelInfo();
                MajongCalculateGrpc.MajongCalculateBlockingStub blockingStub = MajongCalculateGrpc.newBlockingStub(channelInfo.getChannel());
                CalculateResult calculateResult = blockingStub.calculate(calculateData.build());
                ChannelPool.distoryChannel(channelInfo);

                seat.getCanChi().clear();
                seat.getCanChi().addAll(calculateResult.getChiList());
                seat.getCanPeng().clear();
                seat.getCanPeng().addAll(calculateResult.getPengList());
                seat.getCanGang().clear();
                seat.getCanGang().addAll(calculateResult.getGangList());
                seat.getCanHu().clear();
                seat.getCanHu().addAll(calculateResult.getHuList());
                seat.getCanZimo().clear();
                seat.getCanZimo().addAll(calculateResult.getZimoList());
                try {
                    seat.getCanXfGang().addAll(SjPlayerSettleData.parseFrom(calculateResult.getAdjunct()).getXflistList());
                } catch (InvalidProtocolBufferException e) {
                    logger.error(e.toString(), e);
                }

                if (seat.getUserId() == banker) {
                    operationSeatNo = seat.getSeatNo();
                    cardIndex = (int) (Math.random() * surplusCards.size());
                    seat.getCards().add(surplusCards.get(cardIndex));
                    surplusCards.remove(cardIndex);
                }
            }
        }
    }

    public int getNextSeat() {
        int next = operationSeatNo;
        if (count == next) {
            next = 1;
        } else {
            next += 1;
        }
        return next;
    }

    private void clear(Map<Integer, Integer> huCard) {
        Record record = new Record();
        record.setDice(dice);
        record.setBanker(banker);
        record.setStartDate(startDate);
        record.setGameCount(gameCount);
        List<SeatRecord> seatRecords = new ArrayList<>();
        seats.forEach(seat -> {
            SeatRecord seatRecord = new SeatRecord();
            seatRecord.setUserId(seat.getUserId());
            seatRecord.setNickname(seat.getNickname());
            seatRecord.setHead(seat.getHead());
            seatRecord.setCardResult(seat.getCardResult());
            seatRecord.getMingGangResult().addAll(seat.getMingGangResult());
            seatRecord.getAnGangResult().addAll(seat.getAnGangResult());
            seatRecord.getXfGangResult().addAll(seat.getXfGangResult());
            seatRecord.getInitialCards().addAll(seat.getInitialCards());
            seatRecord.getCards().addAll(seat.getCards());
            seatRecord.getPengCards().addAll(seat.getPengCards());
            seatRecord.getChiCards().addAll(seat.getChiCards());
            seatRecord.getAnGangCards().addAll(seat.getAnGangCards());
            seatRecord.getMingGangCards().addAll(seat.getMingGangCards());
            seatRecord.getXfGangCards().addAll(seat.getXfGangCards());
            seatRecord.setScore(seat.getScore());
            seatRecord.setSex(seat.isSex());
            seatRecord.setIp(seat.getIp());
            seatRecord.setSeatNo(seat.getSeatNo());
            if (null != huCard && huCard.containsKey(seatRecord.getUserId())) {
                seatRecord.setHuCard(huCard.get(seatRecord.getUserId()));
            }
            final int[] winOrLose = {0};
            seat.getMingGangResult().forEach(gameResult -> winOrLose[0] += gameResult.getScore());
            seat.getAnGangResult().forEach(gameResult -> winOrLose[0] += gameResult.getScore());
            if (null != seat.getCardResult()) {
                winOrLose[0] += seat.getCardResult().getScore();
            }
            int xfGangScore = 0;
            for (GameResult gameResult : seat.getXfGangResult()) {
                xfGangScore += gameResult.getScore();
            }
            seatRecord.setGangHuScore(seat.getGangHuScore());
            seatRecord.setWinOrLose(winOrLose[0] + xfGangScore + seat.getGangHuScore());
            seatRecords.add(seatRecord);
        });
        record.setSeatRecordList(seatRecords);
        record.getHistoryList().addAll(historyList);
        recordList.add(record);

        historyList.clear();
        surplusCards.clear();
        gameStatus = GameStatus.READYING;
        lastOperation = 0;
        dice = null;
        seats.forEach(Seat::clear);
        startDate = new Date();
        gangMo = false;
    }

    public void getCard(GameBase.BaseConnection.Builder response, int seatNo, RedisService redisService, boolean biMo) {
        if (biMo) {
            gangMo = true;
        }
        if (0 == surplusCards.size() || (!gangMo && 14 == surplusCards.size())) {
            gameOver(response, redisService, 0, null);
            return;
        }
        GameBase.BaseAction.Builder actionResponse = GameBase.BaseAction.newBuilder().setOperationId(GameBase.ActionId.GET_CARD);
        operationSeatNo = seatNo;
        int cardIndex = (int) (Math.random() * surplusCards.size());
        Integer card1 = surplusCards.get(cardIndex);
        surplusCards.remove(cardIndex);
        final Integer[] username = new Integer[1];
        seats.stream().filter(seat -> seat.getSeatNo() == seatNo).forEach(seat -> username[0] = seat.getUserId());
        actionResponse.setID(username[0]);

        historyList.add(new OperationHistory(username[0], OperationHistoryType.GET_CARD, card1));
        Mahjong.CardsData.Builder builder1 = Mahjong.CardsData.newBuilder();
        builder1.addCards(card1);
        Seat operationSeat = null;
        for (Seat seat : seats) {
            if (seat.getSeatNo() == seatNo) {
                seat.getCards().add(card1);
                operationSeat = seat;
                actionResponse.setData(builder1.build().toByteString());
            } else {
                actionResponse.clearData();
            }
            if (MahjongTcpService.userClients.containsKey(seat.getUserId())) {
                response.setOperationType(GameBase.OperationType.ACTION).setData(actionResponse.build().toByteString());
                MahjongTcpService.userClients.get(seat.getUserId()).send(response.build(), seat.getUserId());
            }
        }

        GameBase.RoundResponse roundResponse = GameBase.RoundResponse.newBuilder().setID(username[0]).build();
        response.setOperationType(GameBase.OperationType.ROUND).setData(roundResponse.toByteString());
        seats.stream().filter(seat -> MahjongTcpService.userClients.containsKey(seat.getUserId()))
                .forEach(seat -> MahjongTcpService.userClients.get(seat.getUserId()).send(response.build(), seat.getUserId()));

        checkSelfGetCard(response, operationSeat, redisService, card1);
    }

    /**
     * 游戏结束
     *
     * @param response
     * @param redisService
     */
    public void gameOver(GameBase.BaseConnection.Builder response, RedisService redisService, int card, SettleResult settleResult) {
        Map<Integer, Integer> huCard = new HashMap<>();
        if (null != settleResult) {
            try {
                SjSettleResult sjSettleResult = SjSettleResult.parseFrom(settleResult.getResults());
                for (SjSingleSettleResult sjSingleSettleResult : sjSettleResult.getResultsList()) {
                    for (Seat seat : seats) {
                        if (seat.getUserId() == sjSingleSettleResult.getPlayerId()) {
                            List<ScoreType> scoreTypes = new ArrayList<>();
                            for (SjSettlePatterns sjSettlePatterns : sjSingleSettleResult.getPatternsList()) {
                                scoreTypes.add(ScoreType.valueOf(sjSettlePatterns.name()));
                            }
                            seat.setGangHuScore(sjSingleSettleResult.getGangHuScore());
                            seat.setCardResult(new GameResult(scoreTypes, card, sjSingleSettleResult.getTotalScore() - sjSingleSettleResult.getGangScore() - sjSingleSettleResult.getGangHuScore(), 0, sjSingleSettleResult.getFan()));
                            break;
                        }
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                logger.error(e.toString(), e);
            }
        }

        Mahjong.MahjongResultResponse.Builder resultResponse = Mahjong.MahjongResultResponse.newBuilder();
        resultResponse.setDateTime(new Date().getTime());

        List<Integer> winSeats = new ArrayList<>();
        List<Integer> loseSeats = new ArrayList<>();
        for (Seat seat : seats) {
            if (null != seat.getCardResult()) {
                if (seat.getCardResult().getScore() > 0) {
                    winSeats.add(seat.getUserId());
                } else if (seat.getCardResult().getScore() < 0) {
                    loseSeats.add(seat.getUserId());
                }
            }
        }
        for (Seat seat : seats) {
            Mahjong.MahjongUserResult.Builder userResult = Mahjong.MahjongUserResult.newBuilder();
            userResult.setID(seat.getUserId());
            userResult.addAllCards(seat.getCards());
            userResult.addAllChiCards(seat.getChiCards());
            userResult.addAllPengCards(seat.getPengCards());
            userResult.addAllAnGangCards(seat.getAnGangCards());
            userResult.addAllMingGangCards(seat.getMingGangCards());
            userResult.addAllXuanfengGangCards(seat.getXfGangCards());
            if (null != seat.getCardResult()) {
                userResult.setCardScore(seat.getCardResult().getScore());
                userResult.setFan(seat.getCardResult().getFan());
                if (seat.getCardResult().getScore() > 0) {
                    userResult.setHuCard(card);
                    huCard.put(seat.getUserId(), card);
                    if (1 == (gameRules >> 2) % 2) {
                        userResult.setPiao(10 * loseSeats.size());
                    }
                } else if (seat.getCardResult().getScore() < 0 && 1 == (gameRules >> 2) % 2) {
                    userResult.setPiao(-10 * winSeats.size());
                }
                for (ScoreType scoreType : seat.getCardResult().getScoreTypes()) {
                    userResult.addScoreTypes(Mahjong.ScoreType.forNumber(scoreType.ordinal() - 3));
                }
            }
            int mingGangScore = 0;
            for (GameResult gameResult : seat.getMingGangResult()) {
                mingGangScore += gameResult.getScore();
            }
            int anGangScore = 0;
            for (GameResult gameResult : seat.getAnGangResult()) {
                anGangScore += gameResult.getScore();
            }
            int xfGangScore = 0;
            for (GameResult gameResult : seat.getXfGangResult()) {
                xfGangScore += gameResult.getScore();
            }
            userResult.setGangScore(mingGangScore + anGangScore + xfGangScore + seat.getGangHuScore());
            resultResponse.addUserResult(userResult);
        }

        int tempBanker = 0;
        for (Seat seat : seats) {
            if (seat.getUserId() == banker) {
                seat.setWin(true);
                break;
            }
        }

        if (1 == winSeats.size() && winSeats.get(0) == banker) {
            tempBanker = winSeats.get(0);

//        } else if (1 == loseSeats.size()) {
//            tempBanker = loseSeats.get(0);
        } else {
            for (int i = 0; i < seats.size(); i++) {
                if (seats.get(i).getUserId() == banker) {
                    if (i == seats.size() - 1) {
                        tempBanker = seats.get(0).getUserId();
                    } else {
                        tempBanker = seats.get(i + 1).getUserId();
                    }
                    break;
                }
            }
        }

        if (banker == tempBanker) {
            for (Seat seat : seats) {
                if (seat.getUserId() == tempBanker) {
                    seat.setLianzhuang(seat.getLianzhuang() + 1);
                    break;
                }
            }
        }

        for (Mahjong.MahjongUserResult.Builder userResult : resultResponse.getUserResultBuilderList()) {
            int win = userResult.getCardScore() + userResult.getGangScore();
            userResult.setWinOrLose(win);
            for (Seat seat : seats) {
                if (seat.getUserId() == userResult.getID()) {
                    seat.setScore(seat.getScore() + win);
                    userResult.setScore(seat.getScore());
                    break;
                }
            }
        }

        boolean over = false;
        if (gameCount == gameTimes) {
            if (count != 2) {
                boolean allWin = true;
                for (Seat seat : seats) {
                    if (!seat.isWin()) {
                        allWin = false;
                    }
                }
                if (allWin) {
                    over = true;
                }
            } else {
                over = true;
            }
        }

        if (over) {
            resultResponse.setOver(over);
        }
        response.setOperationType(GameBase.OperationType.RESULT).setData(resultResponse.build().toByteString());
        seats.stream().filter(seat -> MahjongTcpService.userClients.containsKey(seat.getUserId()))
                .forEach(seat -> MahjongTcpService.userClients.get(seat.getUserId()).send(response.build(), seat.getUserId()));
        clear(huCard);
        banker = tempBanker;
        //结束房间
        if (over) {
            roomOver(response, redisService);
        }
    }

    public void roomOver(GameBase.BaseConnection.Builder response, RedisService redisService) {
        JSONObject jsonObject = new JSONObject();
        if (0 == gameStatus.compareTo(GameStatus.WAITING)) {
            jsonObject.clear();
            jsonObject.put("flowType", 1);
            switch (gameTimes) {
                case 2:
                case 12:
                    jsonObject.put("money", 1);
                    break;
                case 4:
                case 24:
                    jsonObject.put("money", 2);
                    break;
                case 8:
                case 48:
                    jsonObject.put("money", 4);
                    break;
            }
            jsonObject.put("description", "开房间退回" + roomNo);
            jsonObject.put("userId", roomOwner);
            ApiResponse moneyDetail = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.moneyDetailedCreate, jsonObject.toJSONString()), new TypeReference<ApiResponse<User>>() {
            });
            if (0 != moneyDetail.getCode()) {
                LoggerFactory.getLogger(this.getClass()).error(Constant.apiUrl + Constant.moneyDetailedCreate + "?" + jsonObject.toJSONString());
            }
        }

        if (0 != recordList.size()) {
            Mahjong.MahjongBalanceResponse.Builder balance = Mahjong.MahjongBalanceResponse.newBuilder();
            balance.setDateTime(new Date().getTime());
            for (Seat seat : seats) {
                Mahjong.MahjongSeatGameBalance.Builder seatGameOver = Mahjong.MahjongSeatGameBalance.newBuilder()
                        .setID(seat.getUserId()).setMinggang(seat.getMinggang()).setAngang(seat.getAngang())
                        .setZimoCount(seat.getZimoCount()).setHuCount(seat.getHuCount()).setLianzhuang(seat.getLianzhuang())
                        .setDianpaoCount(seat.getDianpaoCount()).setWinOrLose(seat.getScore());
                balance.addGameBalance(seatGameOver);
            }
            for (Seat seat : seats) {
                if (MahjongTcpService.userClients.containsKey(seat.getUserId())) {
                    response.setOperationType(GameBase.OperationType.BALANCE).setData(balance.build().toByteString());
                    MahjongTcpService.userClients.get(seat.getUserId()).send(response.build(), seat.getUserId());
                }
            }
        }
        StringBuilder people = new StringBuilder();

        GameBase.OverResponse.Builder over = GameBase.OverResponse.newBuilder();
        for (Seat seat : seats) {
            people.append(",").append(seat.getUserId());
            redisService.delete("reconnect" + seat.getUserId());
            if (MahjongTcpService.userClients.containsKey(seat.getUserId())) {
                String uuid = UUID.randomUUID().toString().replace("-", "");
                while (redisService.exists(uuid)) {
                    uuid = UUID.randomUUID().toString().replace("-", "");
                }
                redisService.addCache("backkey" + uuid, seat.getUserId() + "", 1800);
                over.setBackKey(uuid);
                over.setDateTime(new Date().getTime());

                response.setOperationType(GameBase.OperationType.OVER).setData(over.build().toByteString());
                MahjongTcpService.userClients.get(seat.getUserId()).send(response.build(), seat.getUserId());
            }
        }

        if (0 != recordList.size()) {
            List<TotalScore> totalScores = new ArrayList<>();
            for (Seat seat : seats) {
                TotalScore totalScore = new TotalScore();
                totalScore.setHead(seat.getHead());
                totalScore.setNickname(seat.getNickname());
                totalScore.setUserId(seat.getUserId());
                totalScore.setScore(seat.getScore());
                totalScores.add(totalScore);
            }
            SerializerFeature[] features = new SerializerFeature[]{SerializerFeature.WriteNullListAsEmpty,
                    SerializerFeature.WriteMapNullValue, SerializerFeature.DisableCircularReferenceDetect,
                    SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullNumberAsZero,
                    SerializerFeature.WriteNullBooleanAsFalse};
            int feature = SerializerFeature.config(JSON.DEFAULT_GENERATE_FEATURE, SerializerFeature.WriteEnumUsingName, false);
            jsonObject.clear();
            jsonObject.put("gameType", 4);
            jsonObject.put("roomOwner", roomOwner);
            jsonObject.put("people", people.toString().substring(1));
            jsonObject.put("gameTotal", gameTimes);
            jsonObject.put("gameCount", gameCount);
            jsonObject.put("peopleCount", count);
            jsonObject.put("roomNo", Integer.parseInt(roomNo));
            JSONObject gameRule = new JSONObject();
            gameRule.put("gameRule", gameRules);
            gameRule.put("normal", normal);
            gameRule.put("singleFan", singleFan);
            jsonObject.put("gameRule", gameRule.toJSONString());
            jsonObject.put("gameData", JSON.toJSONString(recordList, feature, features).getBytes());
            jsonObject.put("scoreData", JSON.toJSONString(totalScores, feature, features).getBytes());

            ApiResponse apiResponse = JSON.parseObject(HttpUtil.urlConnectionByRsa(Constant.apiUrl + Constant.gamerecordCreateUrl, jsonObject.toJSONString()), ApiResponse.class);
            if (0 != apiResponse.getCode()) {
                LoggerFactory.getLogger(this.getClass()).error(Constant.apiUrl + Constant.gamerecordCreateUrl + "?" + jsonObject.toJSONString());
            }
        }

        //删除该桌
        redisService.delete("room" + roomNo);
        redisService.delete("room_type" + roomNo);

        if (1 == (gameRules >> 4) % 2) {
            SerializerFeature[] features = new SerializerFeature[]{SerializerFeature.WriteNullListAsEmpty,
                    SerializerFeature.WriteMapNullValue, SerializerFeature.DisableCircularReferenceDetect,
                    SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullNumberAsZero,
                    SerializerFeature.WriteNullBooleanAsFalse};
            int ss = SerializerFeature.config(JSON.DEFAULT_GENERATE_FEATURE, SerializerFeature.WriteEnumUsingName, false);
            SocketRequest socketRequest = new SocketRequest();
            socketRequest.setUserId(roomOwner);
            HttpUtil.urlConnectionByRsa(Constant.notifyRoomList, JSON.toJSONString(socketRequest, ss, features));
        }
        roomNo = null;
    }

    /**
     * 摸牌后检测是否可以自摸、暗杠、扒杠、旋风杠
     *
     * @param seat 座位
     * @param card 摸的那张牌
     */
    public void checkSelfGetCard(GameBase.BaseConnection.Builder response, Seat seat, RedisService redisService, int card) {
        GameBase.AskResponse.Builder builder = GameBase.AskResponse.newBuilder();
        if (1 == Card.containSize(seat.getCanZimo(), card)) {
            builder.addOperationId(GameBase.ActionId.HU);
            if (redisService.exists("room_match" + roomNo)) {
                new OperationTimeout(seat.getUserId(), roomNo, historyList.size(), gameCount, redisService, true).start();
            }
        }
        //暗杠
        if (null != MahjongUtil.checkGang(seat.getCards()) && 0 < surplusCards.size()) {
            builder.addOperationId(GameBase.ActionId.AN_GANG);
        }
        //扒杠
        if (null != MahjongUtil.checkBaGang(seat.getCards(), seat.getPengCards()) && 0 < surplusCards.size()) {
            builder.addOperationId(GameBase.ActionId.BA_GANG);
        }
        if (null != MahjongUtil.checkXFGang(seat.getCards(), seat.getXfGangCards()) && 1 == (gameRules >> 1) % 2 && 0 < surplusCards.size()) {
            builder.addOperationId(GameBase.ActionId.XF_GANG);
        }
        if (0 != builder.getOperationIdCount()) {
            if (redisService.exists("room_match" + roomNo) && !builder.getOperationIdList().contains(GameBase.ActionId.HU)) {
                new OperationTimeout(seat.getUserId(), roomNo, historyList.size(), gameCount, redisService, false).start();
            }
            if (MahjongTcpService.userClients.containsKey(seat.getUserId())) {
                response.clear();
                response.setOperationType(GameBase.OperationType.ASK).setData(builder.build().toByteString());
                MahjongTcpService.userClients.get(seat.getUserId()).send(response.build(), seat.getUserId());
            }
        } else {
            if (redisService.exists("room_match" + roomNo)) {
                new PlayCardTimeout(seat.getUserId(), roomNo, historyList.size(), gameCount, redisService).start();
            }
        }
    }

    /**
     * 和牌
     *
     * @param userId
     * @param response
     * @param redisService
     */
    public void hu(int userId, GameBase.BaseConnection.Builder response, RedisService redisService) {

        OperationHistory operationHistory = null;
        if (historyList.size() > 0) {
            operationHistory = historyList.get(historyList.size() - 1);
        }
        Seat operationSeat = null;
        for (Seat seat : seats) {
            if (null != operationHistory && seat.getUserId() == operationHistory.getUserId()) {
                operationSeat = seat;
                break;
            }
        }
        if (null == operationSeat) {
            for (Seat seat : seats) {
                if (seat.getUserId() == userId) {
                    operationSeat = seat;
                    break;
                }
            }
        }

        int card = operationSeat.getCards().get(operationSeat.getCards().size() - 1);
        boolean gangkai = false;
        //检查是自摸还是点炮,自摸输家是其它三家
        if (1 == Card.containSize(operationSeat.getCanZimo(), card) && operationSeat.getUserId() == userId && operationSeat.getSeatNo() == operationSeatNo) {

            if (0 < historyList.size()) {
                if (0 != historyList.get(historyList.size() - 1).getHistoryType().compareTo(OperationHistoryType.GET_CARD)
                        || historyList.get(historyList.size() - 1).getUserId() != userId) {
                    return;
                }
            }
            historyList.add(new OperationHistory(operationSeat.getUserId(), OperationHistoryType.HU, card));

            if (historyList.size() > 2) {
                if ((0 == historyList.get(historyList.size() - 3).getHistoryType().compareTo(OperationHistoryType.DIAN_GANG)
                        || 0 == historyList.get(historyList.size() - 3).getHistoryType().compareTo(OperationHistoryType.AN_GANG)
                        || 0 == historyList.get(historyList.size() - 3).getHistoryType().compareTo(OperationHistoryType.BA_GANG)
                        || 0 == historyList.get(historyList.size() - 3).getHistoryType().compareTo(OperationHistoryType.XF_GANG))
                        && 0 == historyList.get(historyList.size() - 2).getHistoryType().compareTo(OperationHistoryType.GET_CARD)) {
                    gangkai = true;
                }
            }

            SettleData.Builder settleData = SettleData.newBuilder();
            settleData.setAllocId(3);
            settleData.setBanker(banker);
            settleData.setAdjunct(SjApplySettleData.newBuilder().setNormal(normal).setPiao(1 == (gameRules >> 2) % 2 ? 10 : 0).setTop(singleFan ? 1 : 4).build().toByteString());
            for (Seat seat : seats) {
                SettlePlayerData.Builder settlePlayerData = SettlePlayerData.newBuilder();
                settlePlayerData.setAdjunct(SjPlayerSettleData.newBuilder().addAllXflist(seat.getXfGangCards()).build().toByteString());
                if (seat.getUserId() == userId) {
                    settlePlayerData.setMajong(seat.getCards().get(seat.getCards().size() - 1));
                    if (gangkai) {
                        settlePlayerData.setSettle(SettleType.GANG_HUA);
                    } else {
                        settlePlayerData.setSettle(SettleType.ZI_MO);
                    }
                } else {
                    if (gangkai) {
                        settlePlayerData.setSettle(SettleType.BGANG_HUA);
                    } else {
                        settlePlayerData.setSettle(SettleType.BZI_MO);
                    }

                }
                settlePlayerData.setPlayer(majongPlayerData(seat));
                settleData.addPlayerList(settlePlayerData);
            }

            ChannelInfo channelInfo = ChannelPool.getInstance().getChannelInfo();
            MajongCalculateGrpc.MajongCalculateBlockingStub blockingStub = MajongCalculateGrpc.newBlockingStub(channelInfo.getChannel());
            SettleResult settleResult = blockingStub.settle(settleData.build());
            ChannelPool.distoryChannel(channelInfo);

            operationSeat.setZimoCount(operationSeat.getZimoCount() + 1);

            response.setOperationType(GameBase.OperationType.ACTION).setData(GameBase.BaseAction.newBuilder().setOperationId(GameBase.ActionId.HU)
                    .setID(operationSeat.getUserId()).setData(Mahjong.CardsData.newBuilder().addCards(card)
                            .build().toByteString()).build().toByteString());
            seats.stream().filter(seat1 -> MahjongTcpService.userClients.containsKey(seat1.getUserId()))
                    .forEach(seat1 -> MahjongTcpService.userClients.get(seat1.getUserId()).send(response.build(), seat1.getUserId()));
            Card.remove(operationSeat.getCards(), card);
            gameOver(response, redisService, card, settleResult);
            return;
        }

        if (null == operationHistory) {
            return;
        }

        card = operationHistory.getCards().get(0);
        if (historyList.size() > 3) {
            if ((0 == historyList.get(historyList.size() - 4).getHistoryType().compareTo(OperationHistoryType.DIAN_GANG)
                    || 0 == historyList.get(historyList.size() - 4).getHistoryType().compareTo(OperationHistoryType.AN_GANG)
                    || 0 == historyList.get(historyList.size() - 4).getHistoryType().compareTo(OperationHistoryType.BA_GANG)
                    || 0 == historyList.get(historyList.size() - 4).getHistoryType().compareTo(OperationHistoryType.XF_GANG)
                    || 0 == historyList.get(historyList.size() - 3).getHistoryType().compareTo(OperationHistoryType.XF_GANG))
                    && 0 == historyList.get(historyList.size() - 2).getHistoryType().compareTo(OperationHistoryType.PLAY_CARD)) {
                gangkai = true;
            }
        }

        if (singleFan && 0 < surplusCards.size()) {
            for (Seat seat : seats) {
                if (3 == Card.containSize(seat.getCards(), card) && seat.getUserId() != operationHistory.getUserId() && seat.getOperation() == 2) {
                    Card.remove(seat.getCards(), card);
                    Card.remove(seat.getCards(), card);
                    Card.remove(seat.getCards(), card);
                    seat.getMingGangCards().add(card);
                    seat.getMingGangCards().add(card);
                    seat.getMingGangCards().add(card);
                    seat.getMingGangCards().add(card);

                    //添加结算
                    List<ScoreType> scoreTypes = new ArrayList<>();
                    scoreTypes.add(ScoreType.DIAN_GANG);

                    int[] loseScore = {0};
                    int finalCard = card;
                    seats.stream().filter(seat1 -> seat1.getSeatNo() == operationSeatNo)
                            .forEach(seat1 -> {
                                if (seat1.getUserId() == banker || seat.getUserId() == banker) {
                                    seat1.getMingGangResult().add(new GameResult(scoreTypes, finalCard, -2));
                                    loseScore[0] += 2;
                                } else {
                                    seat1.getMingGangResult().add(new GameResult(scoreTypes, finalCard, -1));
                                    loseScore[0] += 1;
                                }
                            });
                    seat.getMingGangResult().add(new GameResult(scoreTypes, card, loseScore[0], operationSeat.getUserId()));

                    seat.setMinggang(seat.getMinggang() + 1);
                    historyList.add(new OperationHistory(userId, OperationHistoryType.DIAN_GANG, card));

                    operationSeat.getPlayedCards().remove(operationSeat.getPlayedCards().size() - 1);

                    GameBase.BaseAction.Builder actionResponse = GameBase.BaseAction.newBuilder().setID(seat.getUserId()).setOperationId(GameBase.ActionId.DIAN_GANG).setData(Mahjong.CardsData.newBuilder()
                            .addCards(card).build().toByteString());
                    response.setOperationType(GameBase.OperationType.ACTION).setData(actionResponse.build().toByteString());
                    seats.stream().filter(seat1 -> MahjongTcpService.userClients.containsKey(seat1.getUserId()))
                            .forEach(seat1 -> MahjongTcpService.userClients.get(seat1.getUserId()).send(response.build(), seat1.getUserId()));

                    CalculateData.Builder calculateData = CalculateData.newBuilder().setAllocid(3)
                            .setPlayer(majongPlayerData(seat));
                    SjApplyCalculateData.Builder sjApplyCalculateData = SjApplyCalculateData.newBuilder().setXuanfeng(true);
                    if (1 == (gameRules >> 1) % 2) {
                        if (0 < Card.containSize(seat.getXfGangCards(), 31)) {
                            sjApplyCalculateData.addXflist(1);
                        }
                        if (0 < Card.containSize(seat.getXfGangCards(), 41)) {
                            sjApplyCalculateData.addXflist(2);
                        }
                    }
                    sjApplyCalculateData.setYifan(true);
                    calculateData.setAdjunct(sjApplyCalculateData.build().toByteString());

                    ChannelInfo channelInfo = ChannelPool.getInstance().getChannelInfo();
                    MajongCalculateGrpc.MajongCalculateBlockingStub blockingStub = MajongCalculateGrpc.newBlockingStub(channelInfo.getChannel());
                    CalculateResult calculateResult = blockingStub.calculate(calculateData.build());
                    ChannelPool.distoryChannel(channelInfo);

                    seat.getCanChi().clear();
                    seat.getCanChi().addAll(calculateResult.getChiList());
                    seat.getCanPeng().clear();
                    seat.getCanPeng().addAll(calculateResult.getPengList());
                    seat.getCanGang().clear();
                    seat.getCanGang().addAll(calculateResult.getGangList());
                    seat.getCanHu().clear();
                    seat.getCanHu().addAll(calculateResult.getHuList());
                    seat.getCanZimo().clear();
                    seat.getCanZimo().addAll(calculateResult.getZimoList());
                    try {
                        seat.getCanXfGang().addAll(SjPlayerSettleData.parseFrom(calculateResult.getAdjunct()).getXflistList());
                    } catch (InvalidProtocolBufferException e) {
                        logger.error(e.toString(), e);
                    }
                }
            }
        }
        boolean hu = false;
        SettleData.Builder settleData = SettleData.newBuilder();
        settleData.setAllocId(3);
        settleData.setBanker(banker);
        settleData.setAdjunct(SjApplySettleData.newBuilder().setNormal(normal).setPiao(1 == (gameRules >> 2) % 2 ? 10 : 0).setTop(singleFan ? 1 : 4).build().toByteString());

        List<Seat> arraySeats = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat.getSeatNo() > operationSeatNo) {
                arraySeats.add(seat);
            }
        }
        for (Seat seat : seats) {
            if (seat.getSeatNo() <= operationSeatNo) {
                arraySeats.add(seat);
            }
        }

        for (Seat seat : arraySeats) {
            SettlePlayerData.Builder settlePlayerData = SettlePlayerData.newBuilder();
            settlePlayerData.setAdjunct(SjPlayerSettleData.newBuilder().addAllXflist(seat.getXfGangCards()).build().toByteString());
            settlePlayerData.setPlayer(majongPlayerData(seat));

            if (1 == Card.containSize(seat.getCanHu(), card) && seat.getUserId() != operationHistory.getUserId() && (!hu || 1 == gameRules % 2) && seat.getOperation() == 1) {
                settlePlayerData.setMajong(card);
                if (gangkai) {
                    settlePlayerData.setSettle(SettleType.HU_GANG_PAO);
                } else {
                    settlePlayerData.setSettle(SettleType.HU_PAO);
                }
                seat.setHuCount(seat.getHuCount() + 1);
                if (!hu) {
                    if (0 == operationHistory.getHistoryType().compareTo(OperationHistoryType.BA_GANG)) {
                        for (Seat seat1 : seats) {
                            if (seat1.getUserId() == operationHistory.getUserId()) {
                                for (Seat seat2 : seats) {
                                    seat2.getMingGangResult().remove(seat2.getMingGangResult().size() - 1);
                                }
                                seat1.getMingGangCards().remove(seat1.getMingGangCards().size() - 1);
                                seat1.getMingGangCards().remove(seat1.getMingGangCards().size() - 1);
                                seat1.getMingGangCards().remove(seat1.getMingGangCards().size() - 1);
                                seat1.getMingGangCards().remove(seat1.getMingGangCards().size() - 1);
                                seat1.getPengCards().add(operationHistory.getCards().get(0));
                                seat1.getPengCards().add(operationHistory.getCards().get(0));
                                seat1.getPengCards().add(operationHistory.getCards().get(0));
                                break;
                            }
                        }
                    } else if (0 == operationHistory.getHistoryType().compareTo(OperationHistoryType.XF_GANG)) {
                        for (Seat seat1 : seats) {
                            if (seat1.getUserId() == operationHistory.getUserId()) {
                                for (Seat seat2 : seats) {
                                    seat2.getXfGangResult().remove(seat2.getXfGangResult().size() - 1);
                                }
                                seat1.getXfGangCards().remove(seat1.getXfGangCards().size() - 1);
                                break;
                            }
                        }
                    }
                }
                if (0 == operationHistory.getHistoryType().compareTo(OperationHistoryType.BA_GANG)
                        || 0 == operationHistory.getHistoryType().compareTo(OperationHistoryType.XF_GANG)) {
                    historyList.add(new OperationHistory(operationSeat.getUserId(), OperationHistoryType.QIANG_GANG_HU, card));
                    response.setOperationType(GameBase.OperationType.ACTION).setData(GameBase.BaseAction.newBuilder().setOperationId(GameBase.ActionId.QIANG_GANG_HU)
                            .setID(seat.getUserId()).setData(Mahjong.CardsData.newBuilder().addCards(seat.getCards().size() - 1)
                                    .build().toByteString()).build().toByteString());
                } else {
                    historyList.add(new OperationHistory(operationSeat.getUserId(), OperationHistoryType.HU, card));
                    response.setOperationType(GameBase.OperationType.ACTION).setData(GameBase.BaseAction.newBuilder().setOperationId(GameBase.ActionId.HU)
                            .setID(seat.getUserId()).setData(Mahjong.CardsData.newBuilder().addCards(seat.getCards().size() - 1)
                                    .build().toByteString()).build().toByteString());
                }
                seats.stream().filter(seat1 -> MahjongTcpService.userClients.containsKey(seat1.getUserId()))
                        .forEach(seat1 -> MahjongTcpService.userClients.get(seat1.getUserId()).send(response.build(), seat1.getUserId()));
                hu = true;
            } else {
                if (seat.getSeatNo() == operationSeatNo) {
                    if (gangkai) {
                        settlePlayerData.setSettle(SettleType.GANG_PAO);
                    } else {
                        settlePlayerData.setSettle(SettleType.FANG_PAO);
                    }
                } else {
                    settlePlayerData.setSettle(SettleType.PING_JU);
                }
            }
            settleData.addPlayerList(settlePlayerData);
        }
        if (hu) {
            operationSeat.setDianpaoCount(operationSeat.getDianpaoCount() + 1);
            ChannelInfo channelInfo = ChannelPool.getInstance().getChannelInfo();
            MajongCalculateGrpc.MajongCalculateBlockingStub blockingStub = MajongCalculateGrpc.newBlockingStub(channelInfo.getChannel());
            SettleResult settleResult = blockingStub.settle(settleData.build());
            ChannelPool.distoryChannel(channelInfo);
            //胡牌
            gameOver(response, redisService, card, settleResult);
        }
    }

    /**
     * 暗杠或者扒杠
     *
     * @param actionResponse
     * @param card
     * @param response
     * @param redisService
     */

    public void selfGang(GameBase.BaseAction.Builder actionResponse, Integer card, GameBase.BaseConnection.Builder response, RedisService redisService, int userId) {
        //碰或者杠
        seats.stream().filter(seat -> seat.getSeatNo() == operationSeatNo).forEach(seat -> {
            if (4 == Card.containSize(seat.getCards(), card)) {//暗杠
                Card.remove(seat.getCards(), card);
                Card.remove(seat.getCards(), card);
                Card.remove(seat.getCards(), card);
                Card.remove(seat.getCards(), card);

                seat.getAnGangCards().add(card);
                seat.getAnGangCards().add(card);
                seat.getAnGangCards().add(card);
                seat.getAnGangCards().add(card);

                List<ScoreType> scoreTypes = new ArrayList<>();
                scoreTypes.add(ScoreType.AN_GANG);

                int[] loseScore = {0};
                int score = 4;
                if (card % 10 == 1 && card < 30) {
                    score = 8;
                }
                if (singleFan) {
                    if (seat.getUserId() == banker) {
                        score = 2;
                    } else {
                        score = 1;
                    }
                    int finalScore = score;
                    seats.stream().filter(seat1 -> seat1.getSeatNo() != seat.getSeatNo())
                            .forEach(seat1 -> {
                                if (seat1.getUserId() == banker) {
                                    seat1.getAnGangResult().add(new GameResult(scoreTypes, card, -2));
                                    loseScore[0] += 2;
                                } else {
                                    seat1.getAnGangResult().add(new GameResult(scoreTypes, card, -finalScore));
                                    loseScore[0] += finalScore;
                                }
                            });
                } else {
                    int finalScore = score;
                    seats.stream().filter(seat1 -> seat1.getSeatNo() != seat.getSeatNo())
                            .forEach(seat1 -> {
                                seat1.getAnGangResult().add(new GameResult(scoreTypes, card, -finalScore));
                                loseScore[0] += finalScore;
                            });
                }

                seat.getAnGangResult().add(new GameResult(scoreTypes, card, loseScore[0]));

                seat.setAngang(seat.getAngang() + 1);
                historyList.add(new OperationHistory(seat.getUserId(), OperationHistoryType.AN_GANG, card));

                seats.stream().filter(seat1 -> MahjongTcpService.userClients.containsKey(seat1.getUserId()))
                        .forEach(seat1 -> {
                            if (seat1.getUserId() == seat.getUserId()) {
                                actionResponse.setOperationId(GameBase.ActionId.AN_GANG).setData(Mahjong.CardsData.newBuilder().addCards(card).build().toByteString());
                                response.setOperationType(GameBase.OperationType.ACTION).setData(actionResponse.build().toByteString());
                            } else {
                                actionResponse.setOperationId(GameBase.ActionId.AN_GANG).setData(Mahjong.CardsData.newBuilder().addCards(0).build().toByteString());
                                response.setOperationType(GameBase.OperationType.ACTION).setData(actionResponse.build().toByteString());
                            }
                            MahjongTcpService.userClients.get(seat1.getUserId()).send(response.build(), seat1.getUserId());
                        });


                CalculateData.Builder calculateData = CalculateData.newBuilder().setAllocid(3)
                        .setPlayer(majongPlayerData(seat));
                SjApplyCalculateData.Builder sjApplyCalculateData = SjApplyCalculateData.newBuilder().setXuanfeng(true);
                if (1 == (gameRules >> 1) % 2) {
                    if (0 < Card.containSize(seat.getXfGangCards(), 31)) {
                        sjApplyCalculateData.addXflist(1);
                    }
                    if (0 < Card.containSize(seat.getXfGangCards(), 41)) {
                        sjApplyCalculateData.addXflist(2);
                    }
                }
                if (singleFan) {
                    sjApplyCalculateData.setYifan(true);
                }
                calculateData.setAdjunct(sjApplyCalculateData.build().toByteString());

                ChannelInfo channelInfo = ChannelPool.getInstance().getChannelInfo();
                MajongCalculateGrpc.MajongCalculateBlockingStub blockingStub = MajongCalculateGrpc.newBlockingStub(channelInfo.getChannel());
                CalculateResult calculateResult = blockingStub.calculate(calculateData.build());
                ChannelPool.distoryChannel(channelInfo);

                seat.getCanChi().clear();
                seat.getCanChi().addAll(calculateResult.getChiList());
                seat.getCanPeng().clear();
                seat.getCanPeng().addAll(calculateResult.getPengList());
                seat.getCanGang().clear();
                seat.getCanGang().addAll(calculateResult.getGangList());
                seat.getCanHu().clear();
                seat.getCanHu().addAll(calculateResult.getHuList());
                seat.getCanZimo().clear();
                seat.getCanZimo().addAll(calculateResult.getZimoList());
                try {
                    seat.getCanXfGang().addAll(SjPlayerSettleData.parseFrom(calculateResult.getAdjunct()).getXflistList());
                } catch (InvalidProtocolBufferException e) {
                    logger.error(e.toString(), e);
                }


                getCard(response, seat.getSeatNo(), redisService, true);
            } else if (0 < Card.containSize(seat.getPengCards(), card) && 1 == Card.containSize(seat.getCards(), card)) {//扒杠
                Card.remove(seat.getCards(), card);
                Card.remove(seat.getPengCards(), card);
                Card.remove(seat.getPengCards(), card);
                Card.remove(seat.getPengCards(), card);

                seat.getMingGangCards().add(card);
                seat.getMingGangCards().add(card);
                seat.getMingGangCards().add(card);
                seat.getMingGangCards().add(card);

                List<ScoreType> scoreTypes = new ArrayList<>();
                scoreTypes.add(ScoreType.BA_GANG);

                int[] loseScore = {0};
                int score = 2;
                if (card % 10 == 1 && card < 30) {
                    score = 4;
                }
                if (singleFan) {
                    if (seat.getUserId() == banker) {
                        score = 2;
                    } else {
                        score = 1;
                    }
                    int finalScore = score;
                    seats.stream().filter(seat1 -> seat1.getSeatNo() != seat.getSeatNo())
                            .forEach(seat1 -> {
                                if (seat1.getUserId() == banker) {
                                    seat1.getMingGangResult().add(new GameResult(scoreTypes, card, -2));
                                    loseScore[0] += 2;
                                } else {
                                    seat1.getMingGangResult().add(new GameResult(scoreTypes, card, -finalScore));
                                    loseScore[0] += finalScore;
                                }
                            });
                } else {
                    int finalScore = score;
                    seats.stream().filter(seat1 -> seat1.getSeatNo() != seat.getSeatNo())
                            .forEach(seat1 -> {
                                seat1.getMingGangResult().add(new GameResult(scoreTypes, card, -finalScore));
                                loseScore[0] += finalScore;
                            });
                }

                seat.getMingGangResult().add(new GameResult(scoreTypes, card, loseScore[0]));

                seat.setMinggang(seat.getMinggang() + 1);
                historyList.add(new OperationHistory(seat.getUserId(), OperationHistoryType.BA_GANG, card));

                actionResponse.setOperationId(GameBase.ActionId.BA_GANG).setData(Mahjong.CardsData.newBuilder().addCards(card).build().toByteString());
                response.setOperationType(GameBase.OperationType.ACTION).setData(actionResponse.build().toByteString());
                seats.stream().filter(seat1 -> MahjongTcpService.userClients.containsKey(seat1.getUserId()))
                        .forEach(seat1 -> MahjongTcpService.userClients.get(seat1.getUserId()).send(response.build(), seat1.getUserId()));

                CalculateData.Builder calculateData = CalculateData.newBuilder().setAllocid(3)
                        .setPlayer(majongPlayerData(seat));
                SjApplyCalculateData.Builder sjApplyCalculateData = SjApplyCalculateData.newBuilder().setXuanfeng(true);
                if (1 == (gameRules >> 1) % 2) {
                    if (0 < Card.containSize(seat.getXfGangCards(), 31)) {
                        sjApplyCalculateData.addXflist(1);
                    }
                    if (0 < Card.containSize(seat.getXfGangCards(), 41)) {
                        sjApplyCalculateData.addXflist(2);
                    }
                }
                if (singleFan) {
                    sjApplyCalculateData.setYifan(true);
                }
                calculateData.setAdjunct(sjApplyCalculateData.build().toByteString());
                ChannelInfo channelInfo = ChannelPool.getInstance().getChannelInfo();
                MajongCalculateGrpc.MajongCalculateBlockingStub blockingStub = MajongCalculateGrpc.newBlockingStub(channelInfo.getChannel());
                CalculateResult calculateResult = blockingStub.calculate(calculateData.build());
                ChannelPool.distoryChannel(channelInfo);

                seat.getCanChi().clear();
                seat.getCanChi().addAll(calculateResult.getChiList());
                seat.getCanPeng().clear();
                seat.getCanPeng().addAll(calculateResult.getPengList());
                seat.getCanGang().clear();
                seat.getCanGang().addAll(calculateResult.getGangList());
                seat.getCanHu().clear();
                seat.getCanHu().addAll(calculateResult.getHuList());
                seat.getCanZimo().clear();
                seat.getCanZimo().addAll(calculateResult.getZimoList());
                try {
                    seat.getCanXfGang().addAll(SjPlayerSettleData.parseFrom(calculateResult.getAdjunct()).getXflistList());
                } catch (InvalidProtocolBufferException e) {
                    logger.error(e.toString(), e);
                }

//                getCard(response, seat.getSeatNo(), redisService);
                checkCard(response, redisService, seat.getSeatNo());
            }
        });
    }

    /**
     * 旋风杠
     *
     * @param actionResponse
     * @param cardList
     * @param response
     * @param redisService
     * @param userId
     */
    public void xuanfengGang(GameBase.BaseAction.Builder actionResponse, List<Integer> cardList, GameBase.BaseConnection.Builder response, RedisService redisService, int userId) {
        //旋风杠
        seats.stream().filter(seat -> seat.getSeatNo() == operationSeatNo).forEach(seat -> {
            if (null != MahjongUtil.checkXFGang(seat.getCards(), seat.getXfGangCards())) {

                if (3 == cardList.size() && 3 != Card.getAllSameColor(3).size()) {
                    return;
                }
                if (4 == cardList.size() && 4 != Card.getAllSameColor(4).size()) {
                    return;
                }
                if (!Card.containAll(seat.getCards(), cardList)) {
                    return;
                }

                Card.removeAll(seat.getCards(), cardList);
                seat.getXfGangCards().addAll(cardList);

                List<ScoreType> scoreTypes = new ArrayList<>();
                scoreTypes.add(ScoreType.XUANFENGGANG);

                final int[] loseScore = {0};
                int score = 2;
                if (4 == cardList.size()) {
                    score = 4;
                }

                int finalScore = score;
                seats.stream().filter(seat1 -> seat1.getSeatNo() != seat.getSeatNo())
                        .forEach(seat1 -> {
                            seat1.getXfGangResult().add(new GameResult(scoreTypes, cardList.get(0), -finalScore));
                            loseScore[0] += finalScore;
                        });
                seat.getXfGangResult().add(new GameResult(scoreTypes, cardList.get(0), loseScore[0]));

                historyList.add(new OperationHistory(seat.getUserId(), OperationHistoryType.XF_GANG, cardList));

                actionResponse.setOperationId(GameBase.ActionId.XF_GANG).setData(Mahjong.CardsData.newBuilder().addAllCards(cardList).build().toByteString());
                response.setOperationType(GameBase.OperationType.ACTION).setData(actionResponse.build().toByteString());
                seats.stream().filter(seat1 -> MahjongTcpService.userClients.containsKey(seat1.getUserId()))
                        .forEach(seat1 -> MahjongTcpService.userClients.get(seat1.getUserId()).send(response.build(), seat1.getUserId()));

                if (3 != cardList.size()) {

                    CalculateData.Builder calculateData = CalculateData.newBuilder().setAllocid(3)
                            .setPlayer(majongPlayerData(seat));
                    SjApplyCalculateData.Builder sjApplyCalculateData = SjApplyCalculateData.newBuilder().setXuanfeng(true);
                    if (1 == (gameRules >> 1) % 2) {
                        if (0 < Card.containSize(seat.getXfGangCards(), 31)) {
                            sjApplyCalculateData.addXflist(1);
                        }
                        if (0 < Card.containSize(seat.getXfGangCards(), 41)) {
                            sjApplyCalculateData.addXflist(2);
                        }
                    }
                    if (singleFan) {
                        sjApplyCalculateData.setYifan(true);
                    }
                    calculateData.setAdjunct(sjApplyCalculateData.build().toByteString());

                    ChannelInfo channelInfo = ChannelPool.getInstance().getChannelInfo();
                    MajongCalculateGrpc.MajongCalculateBlockingStub blockingStub = MajongCalculateGrpc.newBlockingStub(channelInfo.getChannel());
                    CalculateResult calculateResult = blockingStub.calculate(calculateData.build());
                    ChannelPool.distoryChannel(channelInfo);

                    seat.getCanChi().clear();
                    seat.getCanChi().addAll(calculateResult.getChiList());
                    seat.getCanPeng().clear();
                    seat.getCanPeng().addAll(calculateResult.getPengList());
                    seat.getCanGang().clear();
                    seat.getCanGang().addAll(calculateResult.getGangList());
                    seat.getCanHu().clear();
                    seat.getCanHu().addAll(calculateResult.getHuList());
                    seat.getCanZimo().clear();
                    seat.getCanZimo().addAll(calculateResult.getZimoList());
                    try {
                        seat.getCanXfGang().addAll(SjPlayerSettleData.parseFrom(calculateResult.getAdjunct()).getXflistList());
                    } catch (InvalidProtocolBufferException e) {
                        logger.error(e.toString(), e);
                    }
//                    getCard(response, seat.getSeatNo(), redisService);
                } else {
                    GameBase.AskResponse.Builder builder = GameBase.AskResponse.newBuilder();
                    //暗杠
                    if (null != MahjongUtil.checkGang(seat.getCards()) && 0 < surplusCards.size()) {
                        builder.addOperationId(GameBase.ActionId.AN_GANG);
                    }
                    //扒杠
                    if (null != MahjongUtil.checkBaGang(seat.getCards(), seat.getPengCards()) && 0 < surplusCards.size()) {
                        builder.addOperationId(GameBase.ActionId.BA_GANG);
                    }
                    if (null != MahjongUtil.checkXFGang(seat.getCards(), seat.getXfGangCards()) && 1 == (gameRules >> 1) % 2) {
                        builder.addOperationId(GameBase.ActionId.XF_GANG);
                    }
                    if (0 != builder.getOperationIdCount()) {
                        if (MahjongTcpService.userClients.containsKey(seat.getUserId())) {
                            response.clear();
                            response.setOperationType(GameBase.OperationType.ASK).setData(builder.build().toByteString());
                            MahjongTcpService.userClients.get(seat.getUserId()).send(response.build(), seat.getUserId());
                        }
                    } else {
                        if (redisService.exists("room_match" + roomNo)) {
                            new PlayCardTimeout(seat.getUserId(), roomNo, historyList.size(), gameCount, redisService).start();
                        }
                    }
                }
                if (1 == cardList.size()) {
                    checkCard(response, redisService, seat.getSeatNo());
                } else if (4 == cardList.size()) {
                    getCard(response, seat.getSeatNo(), redisService, true);
                }
            }
        });
    }

    /**
     * 出牌后或者杠后检查是否有人能胡、杠、碰
     *
     * @param response
     * @param redisService
     */

    public void checkCard(GameBase.BaseConnection.Builder response, RedisService redisService, int mopai) {
        seats.forEach(seat1 -> {
            seat1.setOperation(0);
            seat1.getChiTemp().clear();
        });

        OperationHistory operationHistory = null;
        if (historyList.size() > 0) {
            operationHistory = historyList.get(historyList.size() - 1);
        }
        int card = operationHistory.getCards().get(0);
        GameBase.AskResponse.Builder builder = GameBase.AskResponse.newBuilder();
        //先检查胡，胡优先
        boolean cannotOperation = false;
        for (Seat seat : seats) {
            if (seat.getUserId() != operationHistory.getUserId()) {
                builder.clearOperationId();
                List<Integer> temp = new ArrayList<>();
                temp.addAll(seat.getCards());

                if (0 == operationHistory.getHistoryType().compareTo(OperationHistoryType.PLAY_CARD)) {
                    //检测吃
                    //下家
                    if (seat.getSeatNo() == getSeat(operationSeatNo, 1)) {
                        if (1 == Card.containSize(seat.getCanChi(), card)) {
                            builder.addOperationId(GameBase.ActionId.CHI);
                        }
                    }
                }

                //当前玩家手里有几张牌，3张可碰可杠，两张只能碰
                int containSize = Card.containSize(temp, card);
                if (3 == containSize && 0 < surplusCards.size()) {
                    builder.addOperationId(GameBase.ActionId.PENG);
                    if (0 < surplusCards.size()) {
                        builder.addOperationId(GameBase.ActionId.DIAN_GANG);
                    }
                } else if (2 == containSize) {
                    builder.addOperationId(GameBase.ActionId.PENG);
                }
                //当前玩家是否可以胡牌
                if (1 == Card.containSize(seat.getCanHu(), card)) {
                    builder.addOperationId(GameBase.ActionId.HU);
                }
                if (0 != builder.getOperationIdCount()) {
                    if (redisService.exists("room_match" + roomNo)) {
                        new OperationTimeout(seat.getUserId(), roomNo, historyList.size(), gameCount, redisService, false).start();
                    }
                    if (MahjongTcpService.userClients.containsKey(seat.getUserId())) {
                        response.setOperationType(GameBase.OperationType.ASK).setData(builder.build().toByteString());
                        MahjongTcpService.userClients.get(seat.getUserId()).send(response.build(), seat.getUserId());
                    }
                    cannotOperation = true;
                }
            }
        }

        if (!cannotOperation && 0 != mopai) {
            //如果没有人可以胡、碰、杠，游戏继续，下家摸牌；
            getCard(response, mopai, redisService, true);
        }
    }

    private int getSeat(int seat, int count) {
        seat += count;
        if (seat > this.count) {
            seat = seat % this.count;
        }
        return seat;
    }

    /**
     * 重连时检查出牌后是否有人能胡、杠、碰
     */
    public void checkSeatCan(OperationHistory operationHistory, GameBase.BaseConnection.Builder response, int userId) {
        GameBase.AskResponse.Builder builder = GameBase.AskResponse.newBuilder();
        int card = operationHistory.getCards().get(0);
        //先检查胡，胡优先
        seats.stream().filter(seat -> seat.getUserId() == userId).forEach(seat -> {
            builder.clearOperationId();
            List<Integer> temp = new ArrayList<>();
            temp.addAll(seat.getCards());
            if (0 == operationHistory.getHistoryType().compareTo(OperationHistoryType.PLAY_CARD)) {
                //检测吃
                //下家
                if (seat.getSeatNo() == getSeat(operationSeatNo, 1)) {
                    if (1 == Card.containSize(seat.getCanChi(), card)) {
                        builder.addOperationId(GameBase.ActionId.CHI);
                    }
                }
            }
            //当前玩家手里有几张牌，3张可碰可杠，两张只能碰
            int containSize = Card.containSize(temp, card);
            if (3 == containSize && 0 < surplusCards.size()) {
                builder.addOperationId(GameBase.ActionId.PENG);
                if (0 < surplusCards.size()) {
                    builder.addOperationId(GameBase.ActionId.DIAN_GANG);
                }
            } else if (2 == containSize) {
                builder.addOperationId(GameBase.ActionId.PENG);
            }
            //当前玩家是否可以胡牌
            if (1 == Card.containSize(seat.getCanHu(), card)) {
                builder.addOperationId(GameBase.ActionId.HU);
            }
            if (0 != builder.getOperationIdCount()) {
                if (MahjongTcpService.userClients.containsKey(seat.getUserId())) {
                    response.setOperationType(GameBase.OperationType.ASK).setData(builder.build().toByteString());
                    MahjongTcpService.userClients.get(seat.getUserId()).send(response.build(), seat.getUserId());
                }
            }
        });
    }

    /**
     * 检查是否还需要操作
     */
    public boolean passedChecked() {
        //找到那张牌
        final boolean[] hasNoOperation = {false};
        OperationHistory operationHistory = null;
        if (historyList.size() > 0) {
            operationHistory = historyList.get(historyList.size() - 1);
        } else {
            return false;
        }
        final int card = operationHistory.getCards().get(0);

        int operationUserId = operationHistory.getUserId();
        //先检查胡，胡优先
        seats.stream().filter(seat -> seat.getUserId() != operationUserId).forEach(seat -> {
            List<Integer> temp = new ArrayList<>();
            temp.addAll(seat.getCards());

            //当前玩家是否可以胡牌
            if (1 == Card.containSize(seat.getCanHu(), card) && seat.getOperation() != 4) {
                hasNoOperation[0] = true;
            }

            //当前玩家手里有几张牌，3张可碰可杠，两张只能碰
            int containSize = Card.containSize(temp, card);
            if (3 == containSize && 0 < surplusCards.size() && seat.getOperation() != 4) {
                hasNoOperation[0] = true;
            } else if (2 <= containSize && seat.getOperation() != 4) {
                hasNoOperation[0] = true;
            }

            if (1 == Card.containSize(seat.getCanChi(), card) && 0 != seat.getChiTemp().size()) {
                hasNoOperation[0] = true;
            }
        });

        return hasNoOperation[0];
    }

    /**
     * 当有人吃后，再次检查是否还有人胡、碰、杠
     */
    public boolean checkCanChi(int seatNo) {
        //找到那张牌
        final Integer[] card = new Integer[1];
        seats.stream().filter(seat -> seat.getSeatNo() == operationSeatNo)
                .forEach(seat -> card[0] = seat.getPlayedCards().get(seat.getPlayedCards().size() - 1));
        final boolean[] canOperation = {true};
        seats.stream().filter(seat -> seat.getSeatNo() != operationSeatNo).forEach(seat -> {
            List<Integer> temp = new ArrayList<>();
            temp.addAll(seat.getCards());
            //当前玩家手里有几张牌，3张可碰可杠，两张只能碰
            int containSize = Card.containSize(temp, card[0]);
            if (3 == containSize && 0 < surplusCards.size() && seat.getOperation() == 0 && 0 == seat.getChiTemp().size()) {
                canOperation[0] = false;
                return;
            } else if (2 == containSize && seat.getOperation() == 0 && 0 == seat.getChiTemp().size()) {
                canOperation[0] = false;
                return;
            }
            //当前玩家是否可以胡牌
//            if ((MahjongUtil.hu(temp, bao) || MahjongUtil.fei(temp, bao) && dianpao) && seat.getOperation() == 0 && 0 == seat.getChiTemp().size()) {
            if (1 == Card.containSize(seat.getCanHu(), card[0]) && (seat.getOperation() == 0 || seat.getOperation() == 1) && 0 == seat.getChiTemp().size()) {
                canOperation[0] = false;
                return;
            }
            if (0 == seatNo) {
                return;
            }
//            //下家
//            if (seat.getSeatNo() == getSeat(operationSeatNo, 1)) {
//                if (1 == Card.containSize(seat.getCanChi(), card[0]) && 4 != seat.getOperation()) {
//                    canOperation[0] = false;
//                }
//            }
        });

        return canOperation[0];
    }

    /**
     * 检查是否可以胡
     *
     * @return
     */
    public boolean checkCanHu(int userId) {
        OperationHistory operationHistory = null;
        if (historyList.size() > 0) {
            operationHistory = historyList.get(historyList.size() - 1);
        } else {
            return true;
        }
        final int card = operationHistory.getCards().get(0);

        int operationUserId = 0;
        for (Seat seat : seats) {
            if (seat.getUserId() == operationHistory.getUserId()) {
                operationUserId = seat.getUserId();
            }
        }
        boolean canHu = true;
        if (1 == gameRules % 2) {
            for (Seat seat : seats) {
                if (1 == Card.containSize(seat.getCanHu(), card) && seat.getOperation() == 0) {
                    canHu = false;
                    break;
                }
            }
        } else {
            List<Seat> arraySeats = new ArrayList<>();
            for (Seat seat : seats) {
                if (seat.getSeatNo() > operationUserId) {
                    arraySeats.add(seat);
                }
            }
            for (Seat seat : seats) {
                if (seat.getSeatNo() <= operationUserId) {
                    arraySeats.add(seat);
                }
            }
            for (Seat seat : arraySeats) {
                if (seat.getUserId() == userId) {
                    break;
                }
                if (1 == Card.containSize(seat.getCanHu(), card) && seat.getOperation() == 0) {
                    canHu = false;
                    break;
                }
            }
        }
        if (singleFan && 0 < surplusCards.size()) {
            for (Seat seat : seats) {
                if (3 == Card.containSize(seat.getCards(), card) && seat.getUserId() != operationHistory.getUserId()
                        && seat.getOperation() == 0 && 0 == seat.getChiTemp().size()) {
                    canHu = false;
                }
            }
        }
        return canHu;
    }

    /**
     * 当有人碰、杠后，再次检查是否还有人胡
     */
    public boolean checkCanPeng() {
        OperationHistory operationHistory = null;
        if (historyList.size() > 0) {
            operationHistory = historyList.get(historyList.size() - 1);
        } else {
            return false;
        }
        final int card = operationHistory.getCards().get(0);
        int operationUserId = operationHistory.getUserId();
        final boolean[] canOperation = {true};
        //先检查胡，胡优先
        seats.stream().filter(seat -> seat.getUserId() != operationUserId).forEach(seat -> {
            //当前玩家是否可以胡牌
            if (1 == Card.containSize(seat.getCanHu(), card) && (seat.getOperation() == 0 || seat.getOperation() == 1) && 0 == seat.getChiTemp().size()) {
                canOperation[0] = false;
            }
        });

        return canOperation[0];
    }

    /**
     * 检测单个玩家是否可以碰或者港
     *
     * @param actionResponse
     * @param response
     * @param redisService
     * @param userId
     */
    public void operation(GameBase.BaseAction.Builder actionResponse, GameBase.BaseConnection.Builder response, RedisService redisService, int userId) {
        //找到那张牌
        Seat operationSeat = null;
        OperationHistory operationHistory = null;
        if (historyList.size() > 0) {
            operationHistory = historyList.get(historyList.size() - 1);
        }
        final int card = operationHistory.getCards().get(0);

        for (Seat seat : seats) {
            if (seat.getUserId() == operationHistory.getUserId()) {
                operationSeat = seat;
                break;
            }
        }
        for (Seat seat : seats) {
            if (seat.getSeatNo() != operationSeatNo) {
                List<Integer> temp = new ArrayList<>();
                temp.addAll(seat.getCards());

                //当前玩家手里有几张牌，3张可碰可杠，两张只能碰
                int containSize = Card.containSize(temp, card);
                if (3 == containSize && 0 < surplusCards.size() && seat.getOperation() == 2) {//杠牌
                    Card.remove(seat.getCards(), card);
                    Card.remove(seat.getCards(), card);
                    Card.remove(seat.getCards(), card);
                    seat.getMingGangCards().add(card);
                    seat.getMingGangCards().add(card);
                    seat.getMingGangCards().add(card);
                    seat.getMingGangCards().add(card);

                    //添加结算
                    List<ScoreType> scoreTypes = new ArrayList<>();
                    scoreTypes.add(ScoreType.DIAN_GANG);

                    int[] loseScore = {0};
                    int score = 2;
                    if (card % 10 == 1 && card < 30) {
                        score = 4;
                    }
                    if (singleFan) {
                        seats.stream().filter(seat1 -> seat1.getSeatNo() == operationSeatNo)
                                .forEach(seat1 -> {
                                    if (seat1.getUserId() == banker || seat.getUserId() == banker) {
                                        seat1.getMingGangResult().add(new GameResult(scoreTypes, card, -2));
                                        loseScore[0] += 2;
                                    } else {
                                        seat1.getMingGangResult().add(new GameResult(scoreTypes, card, -1));
                                        loseScore[0] += 1;
                                    }
                                });
                    } else {
                        int finalScore = score;
                        seats.stream().filter(seat1 -> seat1.getSeatNo() != seat.getSeatNo())
                                .forEach(seat1 -> {
                                    seat1.getMingGangResult().add(new GameResult(scoreTypes, card, -finalScore));
                                    loseScore[0] += finalScore;
                                });
                    }
                    seat.getMingGangResult().add(new GameResult(scoreTypes, card, loseScore[0], operationSeat.getUserId()));

                    seat.setMinggang(seat.getMinggang() + 1);
                    historyList.add(new OperationHistory(userId, OperationHistoryType.DIAN_GANG, card));

                    operationSeat.getPlayedCards().remove(operationSeat.getPlayedCards().size() - 1);

                    actionResponse.setID(seat.getUserId()).setOperationId(GameBase.ActionId.DIAN_GANG).setData(Mahjong.CardsData.newBuilder()
                            .addCards(card).build().toByteString());
                    response.setOperationType(GameBase.OperationType.ACTION).setData(actionResponse.build().toByteString());
                    seats.stream().filter(seat1 -> MahjongTcpService.userClients.containsKey(seat1.getUserId()))
                            .forEach(seat1 -> MahjongTcpService.userClients.get(seat1.getUserId()).send(response.build(), seat1.getUserId()));

                    CalculateData.Builder calculateData = CalculateData.newBuilder().setAllocid(3)
                            .setPlayer(majongPlayerData(seat));
                    SjApplyCalculateData.Builder sjApplyCalculateData = SjApplyCalculateData.newBuilder().setXuanfeng(true);
                    if (1 == (gameRules >> 1) % 2) {
                        if (0 < Card.containSize(seat.getXfGangCards(), 31)) {
                            sjApplyCalculateData.addXflist(1);
                        }
                        if (0 < Card.containSize(seat.getXfGangCards(), 41)) {
                            sjApplyCalculateData.addXflist(2);
                        }
                    }
                    if (singleFan) {
                        sjApplyCalculateData.setYifan(true);
                    }
                    calculateData.setAdjunct(sjApplyCalculateData.build().toByteString());

                    ChannelInfo channelInfo = ChannelPool.getInstance().getChannelInfo();
                    MajongCalculateGrpc.MajongCalculateBlockingStub blockingStub = MajongCalculateGrpc.newBlockingStub(channelInfo.getChannel());
                    CalculateResult calculateResult = blockingStub.calculate(calculateData.build());
                    ChannelPool.distoryChannel(channelInfo);

                    seat.getCanChi().clear();
                    seat.getCanChi().addAll(calculateResult.getChiList());
                    seat.getCanPeng().clear();
                    seat.getCanPeng().addAll(calculateResult.getPengList());
                    seat.getCanGang().clear();
                    seat.getCanGang().addAll(calculateResult.getGangList());
                    seat.getCanHu().clear();
                    seat.getCanHu().addAll(calculateResult.getHuList());
                    seat.getCanZimo().clear();
                    seat.getCanZimo().addAll(calculateResult.getZimoList());
                    try {
                        seat.getCanXfGang().addAll(SjPlayerSettleData.parseFrom(calculateResult.getAdjunct()).getXflistList());
                    } catch (InvalidProtocolBufferException e) {
                        logger.error(e.toString(), e);
                    }

                    //点杠后需要摸牌
                    getCard(response, seat.getSeatNo(), redisService, true);
                    return;
                } else if (2 <= containSize && seat.getOperation() == 3) {//碰
                    Card.remove(seat.getCards(), card);
                    Card.remove(seat.getCards(), card);
                    seat.getPengCards().add(card);
                    seat.getPengCards().add(card);
                    seat.getPengCards().add(card);
                    operationSeatNo = seat.getSeatNo();
                    if (0 == operationHistory.getHistoryType().compareTo(OperationHistoryType.XF_GANG)) {
                        for (Seat seat1 : seats) {
                            seat1.getXfGangResult().remove(seat1.getXfGangResult().size() - 1);
                        }
                        historyList.add(new OperationHistory(userId, OperationHistoryType.QIANG_GANG_PENG, card));
                        operationSeat.getXfGangCards().remove(operationSeat.getXfGangCards().size() - 1);
                        actionResponse.setID(seat.getUserId()).setOperationId(GameBase.ActionId.QIANG_GANG_PENG).setData(Mahjong.CardsData.newBuilder().addCards(card).build().toByteString());
                    } else {
                        historyList.add(new OperationHistory(userId, OperationHistoryType.PENG, card));
                        operationSeat.getPlayedCards().remove(operationSeat.getPlayedCards().size() - 1);
                        actionResponse.setID(seat.getUserId()).setOperationId(GameBase.ActionId.PENG).setData(Mahjong.CardsData.newBuilder().addCards(card).build().toByteString());
                    }
                    response.setOperationType(GameBase.OperationType.ACTION).setData(actionResponse.build().toByteString());
                    seats.stream().filter(seat1 -> MahjongTcpService.userClients.containsKey(seat1.getUserId()))
                            .forEach(seat1 -> MahjongTcpService.userClients.get(seat1.getUserId()).send(response.build(), seat1.getUserId()));

                    if (redisService.exists("room_match" + roomNo)) {
                        new PlayCardTimeout(seat.getUserId(), roomNo, historyList.size(), gameCount, redisService).start();
                    }
                    GameBase.RoundResponse roundResponse = GameBase.RoundResponse.newBuilder().setID(seat.getUserId()).build();
                    response.setOperationType(GameBase.OperationType.ROUND).setData(roundResponse.toByteString());
                    seats.stream().filter(seat1 -> MahjongTcpService.userClients.containsKey(seat1.getUserId()))
                            .forEach(seat1 -> MahjongTcpService.userClients.get(seat1.getUserId()).send(response.build(), seat1.getUserId()));
                    return;
                }
            }
        }

        int s = operationSeatNo;
        for (
                int i = 0;
                i < 3; i++)

        {
            s++;
            if (s > count) {
                s = 1;
            }
            for (Seat seat : seats) {
                if (seat.getSeatNo() == s) {
                    if (0 != seat.getChiTemp().size()) {
                        List<Integer> chiCard = new ArrayList<>();
                        Card.removeAll(seat.getCards(), seat.getChiTemp());
                        chiCard.addAll(seat.getChiTemp());
                        chiCard.sort(new Comparator<Integer>() {
                            @Override
                            public int compare(Integer o1, Integer o2) {
                                return o1.compareTo(o2);
                            }
                        });
                        chiCard.add(1, card);

                        seat.getChiCards().addAll(chiCard);

                        operationSeatNo = seat.getSeatNo();
                        historyList.add(new OperationHistory(seat.getUserId(), OperationHistoryType.CHI, chiCard));

                        operationSeat.getPlayedCards().remove(operationSeat.getPlayedCards().size() - 1);
                        actionResponse.setID(seat.getUserId());
                        actionResponse.setOperationId(GameBase.ActionId.CHI).setData(Mahjong.CardsData.newBuilder().addAllCards(chiCard).build().toByteString());
                        response.setOperationType(GameBase.OperationType.ACTION).setData(actionResponse.build().toByteString());
                        seats.stream().filter(seat1 -> MahjongTcpService.userClients.containsKey(seat1.getUserId()))
                                .forEach(seat1 -> MahjongTcpService.userClients.get(seat1.getUserId()).send(response.build(), seat1.getUserId()));
                        GameBase.RoundResponse roundResponse = GameBase.RoundResponse.newBuilder().setID(seat.getUserId()).build();
                        response.setOperationType(GameBase.OperationType.ROUND).setData(roundResponse.toByteString());
                        seats.stream().filter(seat1 -> MahjongTcpService.userClients.containsKey(seat1.getUserId()))
                                .forEach(seat1 -> MahjongTcpService.userClients.get(seat1.getUserId()).send(response.build(), seat1.getUserId()));
                        return;
                    }
                    break;
                }
            }
        }

    }

    public void start(GameBase.BaseConnection.Builder response, RedisService redisService) {
        if (2 == count) {
            gameCount = gameCount + 1;
        } else {
            boolean allWin = true;
            for (Seat seat : seats) {
                if (!seat.isWin()) {
                    allWin = false;
                }
            }
            if (allWin) {
                for (Seat seat : seats) {
                    seat.setWin(false);
                }
                gameCount = gameCount + 1;
            }
            if (0 == gameCount) {
                gameCount = 1;
            }
        }
        boolean hasBanker = false;
        for (Seat seat : seats) {
            if (seat.getUserId() == banker) {
                hasBanker = true;
                break;
            }
        }
        if (!hasBanker) {
            banker = seats.get(0).getUserId();
        }
        gameStatus = GameStatus.PLAYING;
        dealCard();
        //骰子
        int dice1 = new Random().nextInt(6) + 1;
        int dice2 = new Random().nextInt(6) + 1;
        dice = new Integer[]{dice1, dice2};
        Mahjong.MahjongStartResponse.Builder dealCard = Mahjong.MahjongStartResponse.newBuilder();
        dealCard.setBanker(banker).addDice(dice1).addDice(dice2).setGameCount(gameCount);
        dealCard.setSurplusCardsSize(surplusCards.size());
        response.setOperationType(GameBase.OperationType.START);
        for (Seat seat : seats) {
            if (MahjongTcpService.userClients.containsKey(seat.getUserId())) {
                dealCard.clearCards();
                dealCard.addAllCards(seat.getCards());
                response.setData(dealCard.build().toByteString());
                MahjongTcpService.userClients.get(seat.getUserId()).send(response.build(), seat.getUserId());
            }
        }

        Seat operationSeat = null;
        GameBase.RoundResponse roundResponse = GameBase.RoundResponse.newBuilder().setID(banker).build();
        response.setOperationType(GameBase.OperationType.ROUND).setData(roundResponse.toByteString());

        if (redisService.exists("room_match" + roomNo)) {
            new PlayCardTimeout(banker, roomNo, historyList.size(), gameCount, redisService).start();
        }
        for (Seat seat : seats) {
            if (operationSeatNo == seat.getSeatNo()) {
                operationSeat = seat;
            }
            if (MahjongTcpService.userClients.containsKey(seat.getUserId())) {
                MahjongTcpService.userClients.get(seat.getUserId()).send(response.build(), seat.getUserId());
            }
        }

        checkSelfGetCard(response, operationSeat, redisService, operationSeat.getCards().get(operationSeat.getCards().size() - 1));
    }

    public void playCard(Integer card, int userId, GameBase.BaseAction.Builder actionResponse, GameBase.BaseConnection.Builder response, RedisService redisService) {
        actionResponse.setID(userId);
        for (Seat seat : seats) {
            if (seat.getUserId() == userId) {
                if (operationSeatNo == seat.getSeatNo() && (lastOperation != userId || (0 < historyList.size() && 0 != historyList.get(historyList.size() - 1).getHistoryType().compareTo(OperationHistoryType.PLAY_CARD)))) {
                    if (seat.getCards().contains(card)) {
                        seat.getCards().remove(card);
                        if (null == seat.getPlayedCards()) {
                            seat.setPlayedCards(new ArrayList<>());
                        }
                        seat.getPlayedCards().add(card);
                        Mahjong.CardsData.Builder builder = Mahjong.CardsData.newBuilder().addCards(card);

                        actionResponse.setOperationId(GameBase.ActionId.PLAY_CARD).setData(builder.build().toByteString());

                        response.setOperationType(GameBase.OperationType.ACTION).setData(actionResponse.build().toByteString());
                        lastOperation = userId;
                        historyList.add(new OperationHistory(userId, OperationHistoryType.PLAY_CARD, card));
                        seats.stream().filter(seat1 -> MahjongTcpService.userClients.containsKey(seat1.getUserId()))
                                .forEach(seat1 -> MahjongTcpService.userClients.get(seat1.getUserId()).send(response.build(), seat1.getUserId()));

                        CalculateData.Builder calculateData = CalculateData.newBuilder().setAllocid(3)
                                .setPlayer(majongPlayerData(seat));
                        SjApplyCalculateData.Builder sjApplyCalculateData = SjApplyCalculateData.newBuilder().setXuanfeng(true);
                        if (1 == (gameRules >> 1) % 2) {
                            if (0 < Card.containSize(seat.getXfGangCards(), 31)) {
                                sjApplyCalculateData.addXflist(1);
                            }
                            if (0 < Card.containSize(seat.getXfGangCards(), 41)) {
                                sjApplyCalculateData.addXflist(2);
                            }
                        }
                        if (singleFan) {
                            sjApplyCalculateData.setYifan(true);
                        }
                        calculateData.setAdjunct(sjApplyCalculateData.build().toByteString());

                        ChannelInfo channelInfo = ChannelPool.getInstance().getChannelInfo();
                        MajongCalculateGrpc.MajongCalculateBlockingStub blockingStub = MajongCalculateGrpc.
                                newBlockingStub(ChannelPool.getInstance().getChannelInfo().getChannel());
                        CalculateResult calculateResult = blockingStub.calculate(calculateData.build());
                        ChannelPool.distoryChannel(channelInfo);

                        seat.getCanChi().clear();
                        seat.getCanChi().addAll(calculateResult.getChiList());
                        seat.getCanPeng().clear();
                        seat.getCanPeng().addAll(calculateResult.getPengList());
                        seat.getCanGang().clear();
                        seat.getCanGang().addAll(calculateResult.getGangList());
                        seat.getCanHu().clear();
                        seat.getCanHu().addAll(calculateResult.getHuList());
                        seat.getCanZimo().clear();
                        seat.getCanZimo().addAll(calculateResult.getZimoList());
                        try {
                            seat.getCanXfGang().addAll(SjPlayerSettleData.parseFrom(calculateResult.getAdjunct()).getXflistList());
                        } catch (InvalidProtocolBufferException e) {
                            logger.error(e.toString(), e);
                        }

                        //先检查其它三家牌，是否有人能胡、杠、碰
                        checkCard(response, redisService, getNextSeat());
                    } else {
                        System.out.println("用户手中没有此牌" + userId);
                    }
                } else {
                    System.out.println("不该当前玩家操作" + userId);
                }
                break;
            }
        }
    }

    private MajongPlayerData.Builder majongPlayerData(Seat seat) {
        MajongPlayerData.Builder majongPlayerData = MajongPlayerData.newBuilder().setPlayerId(seat.getUserId()).addAllHandlist(seat.getCards())
                .addAllChi(seat.getChiCards()).addAllPeng(seat.getPengCards());
        for (GameResult gameResult : seat.getMingGangResult()) {
            if (0 < gameResult.getScore()) {
                GangData.Builder gangData = GangData.newBuilder().setGangvalue(gameResult.getCard());
                if (gameResult.getUserId() != 0) {
                    gangData.setType(GangType.MGANG);
                    gangData.setFighter(gameResult.getUserId());
                } else {
                    gangData.setType(GangType.BGANG);
                }
                majongPlayerData.addGang(gangData);
            }
        }
        for (GameResult gameResult : seat.getAnGangResult()) {
            if (0 < gameResult.getScore()) {
                majongPlayerData.addGang(GangData.newBuilder().setGangvalue(gameResult.getCard()).setType(GangType.AGANG));
            }
        }
        return majongPlayerData;
    }

    public void sendRoomInfo(GameBase.RoomCardIntoResponse.Builder roomCardIntoResponseBuilder, GameBase.BaseConnection.Builder response, int userId) {
        Songjianghe.SongjiangheMahjongIntoResponse.Builder intoResponseBuilder = Songjianghe.SongjiangheMahjongIntoResponse.newBuilder();
        intoResponseBuilder.setCount(count);
        intoResponseBuilder.setGameTimes(gameTimes);
        intoResponseBuilder.setGameRules(gameRules);
        intoResponseBuilder.setSingleFan(singleFan);
        intoResponseBuilder.setNormal(normal);
        roomCardIntoResponseBuilder.setError(GameBase.ErrorCode.SUCCESS);
        roomCardIntoResponseBuilder.setData(intoResponseBuilder.build().toByteString());
        response.setOperationType(GameBase.OperationType.ROOM_INFO).setData(roomCardIntoResponseBuilder.build().toByteString());
        if (MahjongTcpService.userClients.containsKey(userId)) {
            MahjongTcpService.userClients.get(userId).send(response.build(), userId);
        }
    }

    public void sendSeatInfo(GameBase.BaseConnection.Builder response) {
        GameBase.RoomSeatsInfo.Builder roomSeatsInfo = GameBase.RoomSeatsInfo.newBuilder();
        for (Seat seat1 : seats) {
            GameBase.SeatResponse.Builder seatResponse = GameBase.SeatResponse.newBuilder();
            seatResponse.setSeatNo(seat1.getSeatNo());
            seatResponse.setID(seat1.getUserId());
            seatResponse.setScore(seat1.getScore());
            seatResponse.setReady(seat1.isReady());
            seatResponse.setNickname(seat1.getNickname());
            seatResponse.setHead(seat1.getHead());
            seatResponse.setSex(seat1.isSex());
            seatResponse.setOffline(seat1.isRobot());
            seatResponse.setIsRobot(seat1.isRobot());
            seatResponse.setIp(seat1.getIp());
            seatResponse.setGameCount(seat1.getGameCount());
            roomSeatsInfo.addSeats(seatResponse.build());
        }
        response.setOperationType(GameBase.OperationType.SEAT_INFO).setData(roomSeatsInfo.build().toByteString());
        for (Seat seat : seats) {
            if (MahjongTcpService.userClients.containsKey(seat.getUserId())) {
                MahjongTcpService.userClients.get(seat.getUserId()).send(response.build(), seat.getUserId());
            }
        }
    }
}
