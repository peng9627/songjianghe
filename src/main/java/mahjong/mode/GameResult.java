package mahjong.mode;

import java.util.List;

/**
 * Author pengyi
 * Date 17-3-21.
 */
public class GameResult {

    private List<ScoreType> scoreTypes;
    private Integer card;
    private int score;
    private int userId;
    private int fan;

    public GameResult() {
    }

    public GameResult(List<ScoreType> scoreTypes, Integer card, int score) {
        this(scoreTypes, card, score, 0);
    }

    public GameResult(List<ScoreType> scoreTypes, Integer card, int score, int userId) {
        this(scoreTypes, card, score, userId, 0);
    }

    public GameResult(List<ScoreType> scoreTypes, Integer card, int score, int userId, int fan) {
        this.scoreTypes = scoreTypes;
        this.card = card;
        this.score = score;
        this.userId = userId;
        this.fan = fan;
    }

    public List<ScoreType> getScoreTypes() {
        return scoreTypes;
    }

    public void setScoreTypes(List<ScoreType> scoreTypes) {
        this.scoreTypes = scoreTypes;
    }

    public Integer getCard() {
        return card;
    }

    public void setCard(Integer card) {
        this.card = card;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFan() {
        return fan;
    }

    public void setFan(int fan) {
        this.fan = fan;
    }
}
