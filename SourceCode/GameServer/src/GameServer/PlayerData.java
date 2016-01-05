/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GameServer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author developer
 */
public class PlayerData {

    Log m_Log = new Log("PlayerData");
    public ArrayList<State> CardState = new ArrayList();
    public HashMap<String, String> StepRecord = new HashMap();
    private ArrayList<Integer> m_SelectCard = new ArrayList();

    public String CardStateToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CardState.size(); i++) {
            State state = CardState.get(i);
            sb.append(String.format("{\"Card\":\"%s\",\"Img\":\"%s\",\"Open\":%d,\"Click\":%d,\"Content\":\"%s\"},", state.Card, state.Img, state.Open, state.Click, state.Content));
        }
        return String.format("[%s]", sb.toString().substring(0, sb.length() - 1));
    }

    public boolean LegalStep(int step) {
        if (CardState.get(step).Open == 1) {
            m_Log.Writeln(String.format("%s fail, Step : %d is already %s", "LegalStep", step, "Open"));
            m_Log.Writeln(String.format("CardStateToString :  %s", CardStateToString()));
            return false;
        }
        if (CardState.get(step).Click == 1) {
            m_Log.Writeln(String.format("%s fail, Step : %d is already %s", "LegalStep", step, "Click"));
            m_Log.Writeln(String.format("CardStateToString :  %s", CardStateToString()));
            return false;
        }
        return true;
    }

    public void Clean() {
        StepRecord.clear();
        m_SelectCard.clear();
    }

    public void ApplyStep(int step) {
        CardState.get(step).Open = 1;
        CardState.get(step).Click = 1;
        m_SelectCard.add(step);
        if (m_SelectCard.size() >= 2) {
            State cardState1 = CardState.get(m_SelectCard.get(0));
            State cardState2 = CardState.get(m_SelectCard.get(1));
            if (cardState1.Content.equals(cardState2.Content)) {
                CardState.get(m_SelectCard.get(0)).Click = 0;
                CardState.get(m_SelectCard.get(1)).Click = 0;
                m_Log.Writeln(String.format("O Success, Step1 : %02d, Step2 : %02d", m_SelectCard.get(0), m_SelectCard.get(1)));
            } else {
                CardState.get(m_SelectCard.get(0)).Click = 0;
                CardState.get(m_SelectCard.get(0)).Open = 0;
                CardState.get(m_SelectCard.get(1)).Click = 0;
                CardState.get(m_SelectCard.get(1)).Open = 0;
                m_Log.Writeln(String.format("X Fail, Step1 : %02d, Step2 : %02d", m_SelectCard.get(0), m_SelectCard.get(1)));
            }
            m_Log.Writeln(String.format("CardStateToString :  %s", CardStateToString()));
            m_SelectCard.clear();
        }
    }

    public boolean ParseState(String strState) {
        try {
            Clean();
            CardState = new ArrayList();
            for (String token : strState.split("\\[|\\]|\\{|\\}")) {
                if (token.isEmpty() || token.endsWith(",")) {
                    continue;
                }
                String[] args = token.replaceAll("\"", "").split(",|:");
                State state = new State();
                state.Card = args[1];
                state.Img = args[3];
                state.Open = Integer.parseInt(args[5]);
                state.Click = Integer.parseInt(args[7]);
                state.Content = args[9];
                CardState.add(state);
                if (state.Click == 1) {
                    m_SelectCard.add(Integer.parseInt(state.Card));
                }
            }
            return true;
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "ParseState", e.getMessage()));
            return false;
        }
    }
}
