package mahjong.mode;

import com.alibaba.fastjson.JSON;
import mahjong.entrance.MahjongTcpService;
import mahjong.redis.RedisService;
import mahjong.timeout.ReadyTimeout;

import java.util.*;

/**
 * Created by pengyi
 * Date : 17-9-1.
 * desc:
 */
public class MatchInfo {

    private int status;
    private List<Integer> rooms;
    private Arena arena;
    private List<MatchUser> matchUsers;
    private boolean start;
    private List<MatchUser> waitUsers;
    private int matchEliminateScore;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Integer> getRooms() {
        return rooms;
    }

    public void setRooms(List<Integer> rooms) {
        this.rooms = rooms;
    }

    public Arena getArena() {
        return arena;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public List<MatchUser> getMatchUsers() {
        return matchUsers;
    }

    public void setMatchUsers(List<MatchUser> matchUsers) {
        this.matchUsers = matchUsers;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public List<MatchUser> getWaitUsers() {
        return waitUsers;
    }

    public void setWaitUsers(List<MatchUser> waitUsers) {
        this.waitUsers = waitUsers;
    }

    public int getMatchEliminateScore() {
        return matchEliminateScore;
    }

    public void setMatchEliminateScore(int matchEliminateScore) {
        this.matchEliminateScore = matchEliminateScore;
    }

    public int addRoom(String matchNo, int gameTimes, RedisService redisService, List<User> users, Map<Integer, Integer> userIdScore,
                       GameBase.BaseConnection.Builder response, GameBase.MatchData.Builder matchData) {
        Room room = new Room();
        room.setBaseScore(1);
        room.setRoomNo(roomNo(redisService));
        room.setGameTimes(gameTimes);
        room.setCount(4);
        room.setGhost(1);
        room.setGameRules(16382);
        room.setGameStatus(GameStatus.WAITING);
        room.setSeatNos(new ArrayList<>(Arrays.asList(1, 2, 3, 4)));
        room.setBanker(users.get(0).getUserId());
        GameBase.MatchResult.Builder matchResult = GameBase.MatchResult.newBuilder();
        while (4 > room.getSeats().size()) {
            User user = users.remove(0);
            room.addSeat(user, userIdScore.get(user.getUserId()));
            matchResult.setResult(1).setTotalScore(userIdScore.get(user.getUserId())).setCurrentScore(-1);
            response.setOperationType(GameBase.OperationType.MATCH_RESULT).setData(matchResult.build().toByteString());
            if (MahjongTcpService.userClients.containsKey(user.getUserId())) {
                MahjongTcpService.userClients.get(user.getUserId()).send(response.build(), user.getUserId());
                room.sendRoomInfo(GameBase.RoomCardIntoResponse.newBuilder(), response, user.getUserId());
            }
            redisService.addCache("room_match" + room.getRoomNo(), matchNo);
            redisService.addCache("reconnect" + user.getUserId(), "xingning_mahjong," + room.getRoomNo());
        }
        room.sendSeatInfo(response);

        for (Seat seat : room.getSeats()) {
            if (MahjongTcpService.userClients.containsKey(seat.getUserId())) {
                MahjongTcpService.userClients.get(seat.getUserId()).roomNo = Integer.valueOf(room.getRoomNo());
                response.setOperationType(GameBase.OperationType.MATCH_DATA).setData(matchData.build().toByteString());
                MahjongTcpService.userClients.get(seat.getUserId()).send(response.build(), seat.getUserId());
            }
        }

        new ReadyTimeout(Integer.parseInt(room.getRoomNo()), redisService, room.getGameCount()).start();
        redisService.addCache("room" + room.getRoomNo(), JSON.toJSONString(room));
        return Integer.parseInt(room.getRoomNo());
    }

    /**
     * 生成随机桌号
     *
     * @return 桌号
     */
    private String roomNo(RedisService redisService) {
        String roomNo = (new Random().nextInt(899999) + 100001) + "";
        if (redisService.exists("room" + roomNo + "")) {
            roomNo = roomNo(redisService);
        }
        return roomNo;
    }
}
