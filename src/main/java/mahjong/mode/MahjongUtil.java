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


    public static List<Integer> get_dui(List<Integer> cardList) {
        List<Integer> cards = new ArrayList<>();
        cards.addAll(cardList);
        List<Integer> dui_arr = new ArrayList<>();
        if (cards.size() >= 2) {
            cards.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1.compareTo(o2);
                }
            });
            for (int i = 0; i < cards.size() - 1; i++) {
                if (cards.get(i).intValue() == cards.get(i + 1).intValue()) {
                    dui_arr.add(cards.get(i));
                    dui_arr.add(cards.get(i));
                    i++;
                }
            }
        }
        return dui_arr;
    }

    public static List<Integer> get_san(List<Integer> cardList) {
        List<Integer> cards = new ArrayList<>();
        cards.addAll(cardList);
        List<Integer> san_arr = new ArrayList<>();
        if (cards.size() >= 3) {
            cards.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1.compareTo(o2);
                }
            });
            for (int i = 0; i < cards.size() - 2; i++) {
                if (cards.get(i).intValue() == cards.get(i + 2).intValue()) {
                    san_arr.add(cards.get(i));
                    san_arr.add(cards.get(i));
                    san_arr.add(cards.get(i));
                    i += 2;
                }
            }
        }
        return san_arr;
    }

    public static List<Integer> get_si(List<Integer> cardList) {
        List<Integer> cards = new ArrayList<>();
        cards.addAll(cardList);
        List<Integer> san_arr = new ArrayList<>();
        if (cards.size() >= 4) {
            cards.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1.compareTo(o2);
                }
            });
            for (int i = 0; i < cards.size() - 3; i++) {
                if (cards.get(i).intValue() == cards.get(i + 3).intValue()) {
                    san_arr.add(cards.get(i));
                    san_arr.add(cards.get(i));
                    san_arr.add(cards.get(i));
                    san_arr.add(cards.get(i));
                    i += 3;
                }
            }
        }
        return san_arr;
    }

    public static List<Integer> get_shun(List<Integer> cardList) {
        List<Integer> cards = new ArrayList<>();
        cards.addAll(cardList);
        cards.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        List<Integer> sun_arr = new ArrayList<>();
        List<Integer> temp = new ArrayList<>();
        temp.addAll(cards);
        while (temp.size() > 2) {
            boolean find = false;
            for (int i = 0; i < temp.size() - 2; i++) {
                int start = temp.get(i);
                if (temp.get(i) < 30) {
                    if (temp.contains(start + 1) && temp.contains(start + 2)) {
                        sun_arr.add(start);
                        sun_arr.add(start + 1);
                        sun_arr.add(start + 2);
                        temp.remove(Integer.valueOf(start));
                        temp.remove(Integer.valueOf(start + 1));
                        temp.remove(Integer.valueOf(start + 2));
                        find = true;
                        break;
                    }
                } else if (temp.get(i) < 36) {//中发白
                    if (temp.contains(31) && temp.contains(33) && temp.contains(35)) {
                        sun_arr.add(31);
                        sun_arr.add(33);
                        sun_arr.add(35);
                        temp.remove(Integer.valueOf(31));
                        temp.remove(Integer.valueOf(33));
                        temp.remove(Integer.valueOf(35));
                        find = true;
                        break;
                    }
                } else {//东南西北
                    int fengSize = 0;
                    if (temp.contains(41)) {
                        fengSize++;
                    }
                    if (temp.contains(43)) {
                        fengSize++;
                    }
                    if (temp.contains(45)) {
                        fengSize++;
                    }
                    if (temp.contains(47)) {
                        fengSize++;
                    }

                    if (fengSize >= 3) {
                        fengSize = 0;
                        if (temp.contains(41)) {
                            sun_arr.add(41);
                            temp.remove(Integer.valueOf(41));
                            fengSize++;
                        }
                        if (temp.contains(43)) {
                            sun_arr.add(43);
                            temp.remove(Integer.valueOf(43));
                            fengSize++;
                        }
                        if (temp.contains(45)) {
                            sun_arr.add(45);
                            temp.remove(Integer.valueOf(45));
                            fengSize++;
                        }
                        if (fengSize == 3) {
                            find = true;
                            break;
                        }
                        if (temp.contains(47)) {
                            sun_arr.add(45);
                            temp.remove(Integer.valueOf(45));
                            find = true;
                            break;
                        }
                    }

                }
            }
            if (!find) {
                break;
            }
        }
        return sun_arr;
    }
}
