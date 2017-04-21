package ctf.agent;


import ctf.common.AgentEnvironment;
import ctf.agent.Agent;

import ctf.common.AgentAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

/**
 *
 */
public class txp150530Agent extends Agent {

    static char[][] board;
    static txp150530Agent attacker;
    static txp150530Agent defender;
    static int homeBaseRow, homeBaseCol;
    static int enemyBaseRow, enemyBaseCol;

    // Both
    int rowPos, colPos;
    boolean didAssignStrategy;
    boolean isDefender;
    boolean baseOnLeft;
    boolean didPlaceInitialMine;
    ArrayList<Boolean> preKnownWalls = new ArrayList<>();  // Used when board size is not known

    // Defender
    // Move down -> wait for attacker -> place mine -> move to front of flag -> wait until flag gets taken
    boolean defDidMoveDown, defDidWaitForAtt, defDidMoveToFlagFront, defIsChasing;

    // Attacker
    // Move up -> wait for defender -> place mine -> move to enemy base -> run back while placing mines
    boolean attDidMoveUp, attDidWaitForDef, attDidMoveToEnemyBase;

    public txp150530Agent() {
        // Dereference the board when the game is reset, prevents old data
        board = null;
    }

    public int getMove(AgentEnvironment env) {
        if (!didAssignStrategy) {
            assignStrat(env);
        }

        if (isDefender) {
            return defGetMove(env);
        } else {
            return attGetMove(env);
        }
    }

    private void assignStrat(AgentEnvironment env) {
        if (env.isAgentSouth(AgentEnvironment.OUR_TEAM, false)) {
            isDefender = true;
            defender = this;
        } else {
            attacker = this;
        }
        didAssignStrategy = true;
        if (env.isBaseEast(AgentEnvironment.ENEMY_TEAM, false)) {
            baseOnLeft = true;
        }
    }

    public int defGetMove(AgentEnvironment env) {
        if (!defDidMoveDown) {
            // Get if object directly to left/right
            if (baseOnLeft) {
                preKnownWalls.add(env.isObstacleEastImmediate());
            } else {
                preKnownWalls.add(env.isObstacleWestImmediate());
            }

            if (env.isBaseSouth(AgentEnvironment.OUR_TEAM, true)) {
                defDidMoveDown = true;
                if (attacker.attDidMoveUp) {
                    System.out.println("Creating board from defender move");
                    int size = preKnownWalls.size() + attacker.preKnownWalls.size() + 1;
                    board = new char[size][size];
                    for (char[] ar : board) {
                        Arrays.fill(ar, '?');
                    }
                    for (int i = 0; i < preKnownWalls.size(); i++) {
                        if (baseOnLeft) {
                            board[i][1] = preKnownWalls.get(i) ? 'W' : '.';
                        } else {
                            board[i][size-2] = preKnownWalls.get(i) ? 'W' : '.';
                        }
                    }
                    if (baseOnLeft) {
                        board[preKnownWalls.size()][0] = 'H';
                        board[preKnownWalls.size()][size-1] = 'E';
                    } else {
                        board[preKnownWalls.size()][size-1] = 'H';
                        board[preKnownWalls.size()][0] = 'E';
                    }
                    for (int i = 0; i < attacker.preKnownWalls.size(); i++) {
                        if (baseOnLeft) {
                            board[size-i-1][1] = attacker.preKnownWalls.get(i) ? 'W' : '.';
                        } else {
                            board[size-i-1][size-2] = attacker.preKnownWalls.get(i) ? 'W' : '.';
                        }
                    }
                    attacker.attDidWaitForDef = true;
                    defDidWaitForAtt = true;
                    didPlaceInitialMine = true;

                    rowPos = preKnownWalls.size();
                    attacker.rowPos = board.length-1-attacker.preKnownWalls.size();
                    if (baseOnLeft) {
                        colPos = 0;
                        attacker.colPos = 0;
                    } else {
                        colPos = board.length-1;
                        attacker.colPos = board.length-1;
                    }

                    homeBaseRow = preKnownWalls.size();
                    enemyBaseRow = preKnownWalls.size();
                    if (baseOnLeft) {
                        homeBaseCol = 0;
                        enemyBaseCol = 0;
                    } else {
                        homeBaseCol = board.length-1;
                        enemyBaseCol = board.length-1;
                    }

                    return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
                } else {
                    return AgentAction.DO_NOTHING;
                }
            }

            return AgentAction.MOVE_SOUTH;
        } else if (!defDidWaitForAtt) {
            return AgentAction.DO_NOTHING;
        } else if (!didPlaceInitialMine) {
            didPlaceInitialMine = true;
            return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
        } else if (!defDidMoveToFlagFront) {

        }
        return AgentAction.DO_NOTHING;
    }

    public int attGetMove(AgentEnvironment env) {
        if (!attDidMoveUp) {
            if (baseOnLeft) {
                preKnownWalls.add(env.isObstacleEastImmediate());
            } else {
                preKnownWalls.add(env.isObstacleWestImmediate());
            }

            if (env.isBaseNorth(AgentEnvironment.OUR_TEAM, true)) {
                attDidMoveUp = true;
                if (defender.defDidMoveDown) {
                    System.out.println("Creating board from attacker move");
                    int size = defender.preKnownWalls.size() + preKnownWalls.size() + 1;
                    board = new char[size][size];
                    for (char[] ar : board) {
                        Arrays.fill(ar, '?');
                    }
                    for (int i = 0; i < defender.preKnownWalls.size(); i++) {
                        if (baseOnLeft) {
                            board[i][1] = defender.preKnownWalls.get(i) ? 'W' : '.';
                        } else {
                            board[i][size - 2] = defender.preKnownWalls.get(i) ? 'W' : '.';
                        }
                    }
                    if (baseOnLeft) {
                        board[defender.preKnownWalls.size()][0] = 'H';
                        board[defender.preKnownWalls.size()][size - 1] = 'E';
                    } else {
                        board[defender.preKnownWalls.size()][size - 1] = 'H';
                        board[defender.preKnownWalls.size()][0] = 'E';
                    }
                    for (int i = 0; i < preKnownWalls.size(); i++) {
                        if (baseOnLeft) {
                            board[size - i - 1][1] = preKnownWalls.get(i) ? 'W' : '.';
                        } else {
                            board[size - i - 1][size - 2] = preKnownWalls.get(i) ? 'W' : '.';
                        }
                    }
                    attDidWaitForDef = true;
                    defender.defDidWaitForAtt = true;
                    didPlaceInitialMine = true;

                    defender.rowPos = defender.preKnownWalls.size();
                    rowPos = board.length - 1 - preKnownWalls.size();
                    if (baseOnLeft) {
                        colPos = 0;
                        defender.colPos = 0;
                    } else {
                        colPos = board.length - 1;
                        defender.colPos = board.length - 1;
                    }

                    homeBaseRow = defender.preKnownWalls.size();
                    enemyBaseRow = defender.preKnownWalls.size();
                    if (baseOnLeft) {
                        homeBaseCol = 0;
                        enemyBaseCol = 0;
                    } else {
                        homeBaseCol = board.length - 1;
                        enemyBaseCol = board.length - 1;
                    }

                    return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
                } else {
                    return AgentAction.DO_NOTHING;
                }
            }

            return AgentAction.MOVE_NORTH;
        } else if (!attDidWaitForDef) {
            return AgentAction.DO_NOTHING;
        } else if (!didPlaceInitialMine) {
            didPlaceInitialMine = true;
            return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
        } else if (!attDidMoveToEnemyBase) {

        }

        return AgentAction.DO_NOTHING;
    }

    private int pathTo(int r, int c, int destR, int destC) {
        State startState = new State(r, c, 0);
        PriorityQueue<State> queue = new PriorityQueue<>();
        queue.add(startState);
        while (!queue.isEmpty()) {
            State curr = queue.remove();
//            if (curr.)
        }
        return 1;
    }

    private class State implements Comparable<State> {

        int r, c, count;

        public State(int r, int c, int cnt) {
            this.r = r;
            this.c = c;
            this.count = cnt;
        }

        public int compareTo(State other) {
            return count - other.count;
        }
    }
}
