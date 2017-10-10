package mahjong.mode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Author pengyi
 * Date 17-2-14.
 */

public class MahjongUtil {

    public static Integer checkGang(List<Integer> cards) {
        List<Integer> cardList = new ArrayList<>();
        cardList.addAll(cards);
        cardList.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        for (int i = 0; i < cardList.size() - 3; i++) {
            if (cardList.get(i).intValue() == cardList.get(i + 3)) {
                return cardList.get(i);
            }
        }
        return null;
    }

    public static Integer checkBaGang(List<Integer> cards, List<Integer> cardList) {
        for (Integer card : cardList) {
            for (Integer card1 : cards) {
                if (card.intValue() == card1) {
                    return card;
                }
            }
        }
        return null;
    }

    public static Integer checkXFGang(List<Integer> cards, List<Integer> cardList) {
        if (Card.containAll(cardList, Card.getAllSameColor(3)) && Card.hasSameColor(cards, 3)) {
            return 31;
        } else if (Card.containAll(cardList, Card.getAllSameColor(4)) && Card.hasSameColor(cards, 4)) {
            return 41;
        }
        if (Card.containAll(cards, Card.getAllSameColor(3))) {
            return 31;
        } else if (Card.containAll(cards, Card.getAllSameColor(4))) {
            return 41;
        }
        return null;
    }
}
