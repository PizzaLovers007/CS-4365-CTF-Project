package ctf.agent;


import ctf.common.AgentEnvironment;

import ctf.common.AgentAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 *
 */
public class txp150530sxp150830CopyAgent extends Agent {

    /**
     * W = wall
     * . = open space
     * H = home base
     * E = enemy base
     */
    static char[][] board;
    static txp150530sxp150830CopyAgent attacker;
    static txp150530sxp150830CopyAgent defender;
    static int homeBaseRow, homeBaseCol;
    static int enemyBaseRow, enemyBaseCol;
    static int maxSteps;

    // Both
    int rowPos, colPos;
    boolean didAssignStrategy;
    boolean isDefender;
    boolean baseOnLeft;
    boolean didPlaceInitialMine;
    boolean hunting;
    boolean ignoreMines;
    boolean justPlantedMine;
    ArrayList<Boolean> preKnownWalls = new ArrayList<>();  // Used when board size is not known
    int agentSteps; //Keeps track of the number of steps

    // Defender
    // Move down -> wait for attacker -> place mine -> move to front of flag -> wait until flag gets taken
    boolean defDidMoveDown, defDidWaitForAtt, defDidMoveToFlagFront;
    boolean defWasBlocked;

    // Attacker
    // Move up -> wait for defender -> place mine -> move to enemy base -> run back while placing mines
    boolean attDidMoveUp, attDidWaitForDef, attDidMoveToEnemyBase, attDidPlaceMineLast;
    boolean attStuck;
    LinkedList<int[]> previous = new LinkedList<>();

    public txp150530sxp150830CopyAgent() {
        // Dereference the board when the game is reset, prevents old data
        board = null;
        attacker = null;
        defender = null;
    }

    /**
     * Gets the type of move the agent is going to take
     * @param env Interface for the agent environment
     * @return integer of move type
     */
    public int getMove(AgentEnvironment env) {
        if (!didAssignStrategy) {
            assignStrat(env);
        }

        if (isDefender) {
            if (board != null) {
                for (char[] ar : board) {
                    System.out.println(ar);
                }
                System.out.println();
            } else {
                System.out.println("board null");
            }
            return defGetMove(env);
        } else {
            return attGetMove(env);
        }
    }


    /**
     * Assigns the agent a strategy depending on where it is originally placed
     * @param env Interface for the agent environment
     */
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

    /**
     * Gets the type of move the defender is going to make
     * @param env Interface for the agent environment
     * @return integer of move type
     */
    public int defGetMove(AgentEnvironment env) {
        System.out.printf("%s %s %s %s%n", defDidMoveDown, didPlaceInitialMine, defDidWaitForAtt, defDidMoveToFlagFront);
        agentSteps++;

        if (board != null && agentSteps >= maxSteps/2) {
            hunting = true;
        }

        if (env.isAgentNorth(AgentEnvironment.OUR_TEAM, true)) {
            if (!env.isObstacleEastImmediate() && !env.isBaseEast(AgentEnvironment.OUR_TEAM, true)) {
                colPos++;
                return AgentAction.MOVE_EAST;
            } else if (!env.isObstacleSouthImmediate() && !env.isBaseSouth(AgentEnvironment.OUR_TEAM, true)) {
                rowPos++;
                return AgentAction.MOVE_SOUTH;
            } else if (!env.isObstacleWestImmediate() && !env.isBaseWest(AgentEnvironment.OUR_TEAM, true)) {
                colPos--;
                return AgentAction.MOVE_WEST;
            } else if (!justPlantedMine) {
                justPlantedMine = true;
                return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
            }
            return AgentAction.DO_NOTHING;
        } else if (env.isAgentSouth(AgentEnvironment.OUR_TEAM, true)) {
            if (!env.isObstacleEastImmediate() && !env.isBaseEast(AgentEnvironment.OUR_TEAM, true)) {
                colPos++;
                return AgentAction.MOVE_EAST;
            } else if (!env.isObstacleNorthImmediate() && !env.isBaseNorth(AgentEnvironment.OUR_TEAM, true)) {
                rowPos--;
                return AgentAction.MOVE_NORTH;
            } else if (!env.isObstacleWestImmediate() && !env.isBaseWest(AgentEnvironment.OUR_TEAM, true)) {
                colPos--;
                return AgentAction.MOVE_WEST;
            } else if (!justPlantedMine) {
                justPlantedMine = true;
                return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
            }
            return AgentAction.DO_NOTHING;
        } else if (env.isAgentEast(AgentEnvironment.OUR_TEAM, true)) {
            if (!env.isObstacleNorthImmediate() && !env.isBaseNorth(AgentEnvironment.OUR_TEAM, true)) {
                rowPos--;
                return AgentAction.MOVE_NORTH;
            } else if (!env.isObstacleSouthImmediate() && !env.isBaseSouth(AgentEnvironment.OUR_TEAM, true)) {
                rowPos++;
                return AgentAction.MOVE_SOUTH;
            } else if (!env.isObstacleWestImmediate() && !env.isBaseWest(AgentEnvironment.OUR_TEAM, true)) {
                colPos--;
                return AgentAction.MOVE_WEST;
            } else if (!justPlantedMine) {
                justPlantedMine = true;
                return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
            }
            return AgentAction.DO_NOTHING;
        } else if (env.isAgentWest(AgentEnvironment.OUR_TEAM, true)) {
            if (!env.isObstacleEastImmediate() && !env.isBaseEast(AgentEnvironment.OUR_TEAM, true)) {
                colPos++;
                return AgentAction.MOVE_EAST;
            } else if (!env.isObstacleSouthImmediate() && !env.isBaseSouth(AgentEnvironment.OUR_TEAM, true)) {
                rowPos++;
                return AgentAction.MOVE_SOUTH;
            } else if (!env.isObstacleNorthImmediate() && !env.isBaseNorth(AgentEnvironment.OUR_TEAM, true)) {
                rowPos--;
                return AgentAction.MOVE_NORTH;
            } else if (!justPlantedMine) {
                justPlantedMine = true;
                return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
            }
            return AgentAction.DO_NOTHING;
        }

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
                    maxSteps = size * size * 2;
                    for (char[] ar : board) {
                        Arrays.fill(ar, '?');
                    }
                    for (int i = 0; i < preKnownWalls.size(); i++) {
                        if (baseOnLeft) {
                            board[i][1] = preKnownWalls.get(i) ? 'W' : '.';
                            board[i][0] = '.';
                        } else {
                            board[i][size-2] = preKnownWalls.get(i) ? 'W' : '.';
                            board[i][size-1] = '.';
                        }
                    }
                    if (baseOnLeft) {
                        board[preKnownWalls.size()][0] = 'H';
                        board[preKnownWalls.size()][size-1] = 'F';
                    } else {
                        board[preKnownWalls.size()][size-1] = 'H';
                        board[preKnownWalls.size()][0] = 'F';
                    }
                    for (int i = 0; i < attacker.preKnownWalls.size(); i++) {
                        if (baseOnLeft) {
                            board[size-i-1][1] = attacker.preKnownWalls.get(i) ? 'W' : '.';
                            board[size-i-1][0] = '.';
                        } else {
                            board[size-i-1][size-2] = attacker.preKnownWalls.get(i) ? 'W' : '.';
                            board[size-i-1][size-1] = '.';
                        }
                    }
                    attacker.attDidWaitForDef = true;
                    defDidWaitForAtt = true;
                    didPlaceInitialMine = true;

                    rowPos = preKnownWalls.size()-1;
                    attacker.rowPos = board.length-attacker.preKnownWalls.size();
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
                        enemyBaseCol = board.length-1;
                    } else {
                        homeBaseCol = board.length-1;
                        enemyBaseCol = 0;
                    }

                    board[rowPos][colPos] = 'M';
                    justPlantedMine = true;
                    return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
                } else {
                    return AgentAction.DO_NOTHING;
                }
            }

            return AgentAction.MOVE_SOUTH;
        }

        if (hasDied(env)) {
            System.out.println("Defender died!");
            defDidMoveToFlagFront = false;
        }
        update(env);
        if (!validate(env)) {
            if (!justPlantedMine) {
                justPlantedMine = true;
                return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
            }
            justPlantedMine = false;
            return AgentAction.DO_NOTHING;
        }

        previous.add(new int[]{rowPos, colPos});
        if (previous.size() > board.length) {
            previous.remove();
        }
        if (!defDidWaitForAtt) {
            return AgentAction.DO_NOTHING;
        } else if (!didPlaceInitialMine) {
            board[rowPos][colPos] = 'M';
            didPlaceInitialMine = true;
            justPlantedMine = true;
            return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
        } else if (env.hasFlag()) {
            return tryMove(env, homeBaseRow, homeBaseCol);
        } else if (env.hasFlag(AgentEnvironment.ENEMY_TEAM)) {
            ignoreMines = false;
            if (board[enemyBaseRow][enemyBaseCol] != 'M') {
                return tryMove(env, enemyBaseRow, enemyBaseCol);
            } else if (baseOnLeft) {
                return tryMove(env, homeBaseRow, homeBaseCol+1);
            } else {
                return tryMove(env, homeBaseRow, homeBaseCol-1);
            }
        } else if (env.hasFlag(AgentEnvironment.OUR_TEAM)) {
            ignoreMines = true;
            if (board[homeBaseRow-1][homeBaseCol] == 'M') {
                return tryMove(env, homeBaseRow - 1, homeBaseCol);
            }
            ignoreMines = false;
//            return tryMove(env, enemyBaseRow, enemyBaseCol);
            int move;
            if (baseOnLeft) {
                move = tryMove(env, homeBaseRow, homeBaseCol+1);
                if (rowPos == homeBaseRow && colPos == homeBaseCol+1) {
                    return move;
                }
            } else {
                move = tryMove(env, homeBaseRow, homeBaseCol-1);
                if (rowPos == homeBaseRow && colPos == homeBaseCol-1) {
                    return move;
                }
            }
            if (move == AgentAction.DO_NOTHING) {
                return tryMove(env, Math.max(homeBaseRow+2, board.length-1), homeBaseCol);
            }
            return move;
        } else if (!defDidMoveToFlagFront) {
            int move;
            if (baseOnLeft) {
                move = tryMove(env, homeBaseRow, homeBaseCol + 1);
                if (rowPos == homeBaseRow && colPos == homeBaseCol+1) {
                    defDidMoveToFlagFront = true;
                    return move;
                }
            } else {
                move = tryMove(env, homeBaseRow, homeBaseCol - 1);
                if (rowPos == homeBaseRow && colPos == homeBaseCol-1) {
                    defDidMoveToFlagFront = true;
                    return move;
                }
            }
            if (move == AgentAction.DO_NOTHING) {
                hunting = true;
                defDidMoveToFlagFront = true;
                defWasBlocked = true;
            } else {
                return move;
            }
        } else if (!hunting) {
            if (attacker.attStuck) {
                return tryMove(env, enemyBaseRow, enemyBaseCol);
            }
            if (env.isAgentNorth(AgentEnvironment.ENEMY_TEAM, true)
                    || env.isAgentEast(AgentEnvironment.ENEMY_TEAM, true)
                    || env.isAgentSouth(AgentEnvironment.ENEMY_TEAM, true)
                    || env.isAgentWest(AgentEnvironment.ENEMY_TEAM, true)) {
                justPlantedMine = true;
                return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
            }
            if (baseOnLeft) {
                return tryMove(env, homeBaseRow, homeBaseCol + 1);
            } else {
                return tryMove(env, homeBaseRow, homeBaseCol - 1);
            }
        }

        if (env.isAgentNorth(AgentEnvironment.ENEMY_TEAM, true)) {
            rowPos--;
            return AgentAction.MOVE_NORTH;
        } else if (env.isAgentSouth(AgentEnvironment.ENEMY_TEAM, true)) {
            rowPos++;
            return AgentAction.MOVE_SOUTH;
        } else if (env.isAgentEast(AgentEnvironment.ENEMY_TEAM, true)) {
            colPos++;
            return AgentAction.MOVE_EAST;
        } else if (env.isAgentWest(AgentEnvironment.ENEMY_TEAM, true)) {
            colPos--;
            return AgentAction.MOVE_WEST;
        } else if (env.hasFlag()) {
            return tryMove(env, homeBaseRow, homeBaseCol);
        } else {
            return tryMove(env, enemyBaseRow, enemyBaseCol);
        }
    }

    /**
     * Gets the type of move the attacker is going to make
     * @param env Interface for the agent environment
     * @return integer of move type
     */
    public int attGetMove(AgentEnvironment env) {
        System.out.printf("%s %s %s %s%n", attDidMoveUp, attDidWaitForDef, didPlaceInitialMine, attDidMoveToEnemyBase);
        agentSteps++;

        if (env.hasFlag(AgentEnvironment.OUR_TEAM) && !env.hasFlag()) {
            if (env.isAgentNorth(AgentEnvironment.OUR_TEAM, true)) {
                if (!env.isObstacleEastImmediate() && !env.isBaseEast(AgentEnvironment.OUR_TEAM, true)) {
                    colPos++;
                    return AgentAction.MOVE_EAST;
                } else if (!env.isObstacleSouthImmediate() && !env.isBaseSouth(AgentEnvironment.OUR_TEAM, true)) {
                    rowPos++;
                    return AgentAction.MOVE_SOUTH;
                } else if (!env.isObstacleWestImmediate() && !env.isBaseWest(AgentEnvironment.OUR_TEAM, true)) {
                    colPos--;
                    return AgentAction.MOVE_WEST;
                } else if (!justPlantedMine) {
                    justPlantedMine = true;
                    return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
                }
                return AgentAction.DO_NOTHING;
            } else if (env.isAgentSouth(AgentEnvironment.OUR_TEAM, true)) {
                if (!env.isObstacleEastImmediate() && !env.isBaseEast(AgentEnvironment.OUR_TEAM, true)) {
                    colPos++;
                    return AgentAction.MOVE_EAST;
                } else if (!env.isObstacleNorthImmediate() && !env.isBaseNorth(AgentEnvironment.OUR_TEAM, true)) {
                    rowPos--;
                    return AgentAction.MOVE_NORTH;
                } else if (!env.isObstacleWestImmediate() && !env.isBaseWest(AgentEnvironment.OUR_TEAM, true)) {
                    colPos--;
                    return AgentAction.MOVE_WEST;
                } else if (!justPlantedMine) {
                    justPlantedMine = true;
                    return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
                }
                return AgentAction.DO_NOTHING;
            } else if (env.isAgentEast(AgentEnvironment.OUR_TEAM, true)) {
                if (!env.isObstacleNorthImmediate() && !env.isBaseNorth(AgentEnvironment.OUR_TEAM, true)) {
                    rowPos--;
                    return AgentAction.MOVE_NORTH;
                } else if (!env.isObstacleSouthImmediate() && !env.isBaseSouth(AgentEnvironment.OUR_TEAM, true)) {
                    rowPos++;
                    return AgentAction.MOVE_SOUTH;
                } else if (!env.isObstacleWestImmediate() && !env.isBaseWest(AgentEnvironment.OUR_TEAM, true)) {
                    colPos--;
                    return AgentAction.MOVE_WEST;
                } else if (!justPlantedMine) {
                    justPlantedMine = true;
                    return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
                }
                return AgentAction.DO_NOTHING;
            } else if (env.isAgentWest(AgentEnvironment.OUR_TEAM, true)) {
                if (!env.isObstacleEastImmediate() && !env.isBaseEast(AgentEnvironment.OUR_TEAM, true)) {
                    colPos++;
                    return AgentAction.MOVE_EAST;
                } else if (!env.isObstacleSouthImmediate() && !env.isBaseSouth(AgentEnvironment.OUR_TEAM, true)) {
                    rowPos++;
                    return AgentAction.MOVE_SOUTH;
                } else if (!env.isObstacleNorthImmediate() && !env.isBaseNorth(AgentEnvironment.OUR_TEAM, true)) {
                    rowPos--;
                    return AgentAction.MOVE_NORTH;
                } else if (!justPlantedMine) {
                    justPlantedMine = true;
                    return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
                }
                return AgentAction.DO_NOTHING;
            }
        }

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
                    maxSteps = size * size * 2;
                    for (char[] ar : board) {
                        Arrays.fill(ar, '?');
                    }
                    for (int i = 0; i < defender.preKnownWalls.size(); i++) {
                        if (baseOnLeft) {
                            board[i][1] = defender.preKnownWalls.get(i) ? 'W' : '.';
                            board[i][0] = '.';
                        } else {
                            board[i][size - 2] = defender.preKnownWalls.get(i) ? 'W' : '.';
                            board[i][size - 1] = '.';
                        }
                    }
                    if (baseOnLeft) {
                        board[defender.preKnownWalls.size()][0] = 'H';
                        board[defender.preKnownWalls.size()][size - 1] = 'F';
                    } else {
                        board[defender.preKnownWalls.size()][size - 1] = 'H';
                        board[defender.preKnownWalls.size()][0] = 'F';
                    }
                    for (int i = 0; i < preKnownWalls.size(); i++) {
                        if (baseOnLeft) {
                            board[size - i - 1][1] = preKnownWalls.get(i) ? 'W' : '.';
                            board[size - i - 1][0] = '.';
                        } else {
                            board[size - i - 1][size - 2] = preKnownWalls.get(i) ? 'W' : '.';
                            board[size - i - 1][size - 1] = '.';
                        }
                    }
                    attDidWaitForDef = true;
                    defender.defDidWaitForAtt = true;
                    didPlaceInitialMine = true;

                    defender.rowPos = defender.preKnownWalls.size()-1;
                    rowPos = board.length - preKnownWalls.size();
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
                        enemyBaseCol = board.length-1;
                    } else {
                        homeBaseCol = board.length - 1;
                        enemyBaseCol = 0;
                    }

                    board[rowPos][colPos] = 'M';
                    justPlantedMine = true;
                    return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
                } else {
                    return AgentAction.DO_NOTHING;
                }
            }

            return AgentAction.MOVE_NORTH;
        }

        if (hasDied(env)) {
            System.out.println("Attacker died!");
            attDidMoveToEnemyBase = false;
            defender.defDidMoveToFlagFront = false;
            previous.clear();
        }
        update(env);
        if (!validate(env)) {
            if (!justPlantedMine) {
                justPlantedMine = true;
                return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
            }
            justPlantedMine = false;
            return AgentAction.DO_NOTHING;
        }

        if (!attDidMoveToEnemyBase) {
            previous.add(new int[]{rowPos, colPos});
            if (previous.size() > board.length/3) {
                previous.remove();
            }
        }
        if (!attDidWaitForDef) {
            return AgentAction.DO_NOTHING;
        } else if (!didPlaceInitialMine) {
            board[rowPos][colPos] = 'M';
            didPlaceInitialMine = true;
            justPlantedMine = true;
            return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
        } else if (!attDidMoveToEnemyBase) {
            int move = tryMove(env, enemyBaseRow, enemyBaseCol);
            if (rowPos == enemyBaseRow && colPos == enemyBaseCol) {
                attDidMoveToEnemyBase = true;
                attDidPlaceMineLast = false;
                return move;
            }
            if (move == AgentAction.DO_NOTHING) {
                attStuck = true;
                return move;
            }
            attStuck = false;
            return move;
        } else if (!attDidPlaceMineLast) {
            board[rowPos][colPos] = 'M';
            attDidPlaceMineLast = true;
            justPlantedMine = true;
            return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
        } else {
            if (!env.hasFlag()) {
                attDidMoveToEnemyBase = false;
                attDidPlaceMineLast = false;
                return AgentAction.DO_NOTHING;
            }
            int move;
            if (previous.isEmpty()) {
                move = tryMove(env, homeBaseRow, homeBaseCol);
                if (move == AgentAction.DO_NOTHING) {
                    move = tryMove(env, Math.max(0, homeBaseRow - 2), homeBaseCol);
                    if (move == AgentAction.DO_NOTHING) {
                        attStuck = true;
                    }
                }
            } else {
                int[] lastMove = previous.removeLast();
                if (lastMove[0] < rowPos && !env.isAgentNorth(AgentEnvironment.OUR_TEAM, true)) {
                    rowPos--;
                    move = AgentAction.MOVE_NORTH;
                } else if (lastMove[0] > rowPos && !env.isAgentSouth(AgentEnvironment.OUR_TEAM, true)) {
                    rowPos++;
                    move = AgentAction.MOVE_SOUTH;
                } else if (lastMove[1] < colPos && !env.isAgentWest(AgentEnvironment.OUR_TEAM, true)) {
                    colPos--;
                    move = AgentAction.MOVE_WEST;
                } else if (lastMove[1] > colPos && !env.isAgentEast(AgentEnvironment.OUR_TEAM, true)) {
                    colPos++;
                    move = AgentAction.MOVE_EAST;
                } else {
                    move = AgentAction.DO_NOTHING;
                }
                attDidPlaceMineLast = false;
            }
            attStuck = false;
            return move;
        }
    }

    private int tryMove(AgentEnvironment env, int destR, int destC) {
        char defTemp = board[defender.rowPos][defender.colPos];
        char attTemp = board[attacker.rowPos][attacker.colPos];
        board[attacker.rowPos][attacker.colPos] = 'P';
        board[defender.rowPos][defender.colPos] = 'P';
        int e1r = -1, e1c = -1, e2r = -1, e2c = -1;
        char e1temp = '.', e2temp = '.';
        if (env.isAgentNorth(AgentEnvironment.ENEMY_TEAM, true)) {
            System.out.println("Found enemy 1");
            e1r = rowPos-1;
            e1c = colPos;
            e1temp = board[e1r][e1c];
        }
        if (env.isAgentSouth(AgentEnvironment.ENEMY_TEAM, true)) {
            if (e1r == -1) {
                System.out.println("Found enemy 1");
                e1r = rowPos+1;
                e1c = colPos;
                e1temp = board[e1r][e1c];
            } else {
                System.out.println("Found enemy 2");
                e2r = rowPos+1;
                e2c = colPos;
                e2temp = board[e2r][e2c];
            }
        }
        if (env.isAgentEast(AgentEnvironment.ENEMY_TEAM, true)) {
            if (e1r == -1) {
                System.out.println("Found enemy 1");
                e1r = rowPos;
                e1c = colPos+1;
                e1temp = board[e1r][e1c];
            } else {
                System.out.println("Found enemy 2");
                e2r = rowPos;
                e2c = colPos+1;
                e2temp = board[e2r][e2c];
            }
        }
        if (env.isAgentWest(AgentEnvironment.ENEMY_TEAM, true)) {
            if (e1r == -1) {
                System.out.println("Found enemy 1");
                e1r = rowPos;
                e1c = colPos-1;
                e1temp = board[e1r][e1c];
            } else {
                System.out.println("Found enemy 2");
                e2r = rowPos;
                e2c = colPos-1;
                e2temp = board[e2r][e2c];
            }
        }
        if (e1r != -1) {
            board[e1r][e1c] = 'E';
        }
        if (e2r != -1) {
            board[e2r][e2c] = 'E';
        }
        int move = pathTo(destR, destC, env.hasFlag());
        if (e1r != -1) {
//            board[e1r][e1c] = e1temp;
        }
        if (e2r != -1) {
//            board[e2r][e2c] = e2temp;
        }
        board[attacker.rowPos][attacker.colPos] = attTemp;
        board[defender.rowPos][defender.colPos] = defTemp;

        if (move == AgentAction.MOVE_NORTH) {
            if (env.isAgentNorth(AgentEnvironment.OUR_TEAM, true)) {
                return AgentAction.DO_NOTHING;
            } else {
                rowPos--;
            }
        } else if (move == AgentAction.MOVE_EAST) {
            if (env.isAgentEast(AgentEnvironment.OUR_TEAM, true)) {
                return AgentAction.DO_NOTHING;
            } else {
                colPos++;
            }
        } else if (move == AgentAction.MOVE_SOUTH) {
            if (env.isAgentSouth(AgentEnvironment.OUR_TEAM, true)) {
                return AgentAction.DO_NOTHING;
            } else {
                rowPos++;
            }
        } else if (move == AgentAction.MOVE_WEST) {
            if (env.isAgentWest(AgentEnvironment.OUR_TEAM, true)) {
                return AgentAction.DO_NOTHING;
            } else {
                colPos--;
            }
        }

        return move;
    }

    private int pathTo(int destR, int destC, boolean hasFlag) {
        int[][] best = new int[board.length][board.length];
        for (int i = 0; i < best.length; i++){
            Arrays.fill(best[i], 1000000);
        }
        State startState = new State(rowPos, colPos, 0);
        State endState = null;
        PriorityQueue<State> queue = new PriorityQueue<>();
        queue.add(startState);
        while (!queue.isEmpty()) {
            State curr = queue.remove();
            if (curr.count >= best[curr.r][curr.c]) {
                continue;
            }
            best[curr.r][curr.c] = curr.count;
            if (curr.r == destR && curr.c == destC) {
                endState = curr;
            }
            if (inBounds(curr.r, curr.c+1, board) && board[curr.r][curr.c+1] != 'W'
                    && (hasFlag || curr.r != homeBaseRow || curr.c+1 != homeBaseCol)) {
                int add = 1;
                if (!hunting && board[curr.r][curr.c+1] == 'E') {
                    add = 100;
                }
                if (board[curr.r][curr.c+1] == 'P') {
                    add = 300;
                }
                if (!defWasBlocked && !ignoreMines && board[curr.r][curr.c+1] == 'M') {
                    add = 800;
                }
                State next = new State(curr.r, curr.c+1, curr.count+add);
                next.prev = curr;
                queue.add(next);
            }
            if (inBounds(curr.r, curr.c-1, board) && board[curr.r][curr.c-1] != 'W'
                    && (hasFlag || curr.r != homeBaseRow || curr.c-1 != homeBaseCol)) {
                int add = 1;
                if (!hunting && board[curr.r][curr.c-1] == 'E') {
                    add = 100;
                }
                if (board[curr.r][curr.c-1] == 'P') {
                    add = 300;
                }
                if (!defWasBlocked && !ignoreMines && board[curr.r][curr.c-1] == 'M') {
                    add = 800;
                }
                State next = new State(curr.r, curr.c-1, curr.count+add);
                next.prev = curr;
                queue.add(next);
            }
            if (inBounds(curr.r+1, curr.c, board) && board[curr.r+1][curr.c] != 'W'
                    && (hasFlag || curr.r+1 != homeBaseRow || curr.c != homeBaseCol)) {
                int add = 1;
                if (!hunting && board[curr.r+1][curr.c] == 'E') {
                    add = 100;
                }
                if (board[curr.r+1][curr.c] == 'P') {
                    add = 300;
                }
                if (!defWasBlocked && !ignoreMines && board[curr.r+1][curr.c] == 'M') {
                    add = 800;
                }
                State next = new State(curr.r+1, curr.c, curr.count+add);
                next.prev = curr;
                queue.add(next);
            }
            if (inBounds(curr.r-1, curr.c, board) && board[curr.r-1][curr.c] != 'W'
                    && (hasFlag || curr.r-1 != homeBaseRow || curr.c != homeBaseCol)) {
                int add = 1;
                if (!hunting && board[curr.r-1][curr.c] == 'E') {
                    add = 100;
                }
                if (board[curr.r-1][curr.c] == 'P') {
                    add = 300;
                }
                if (!defWasBlocked && !ignoreMines && board[curr.r-1][curr.c] == 'M') {
                    add = 800;
                }
                State next = new State(curr.r-1, curr.c, curr.count+add);
                next.prev = curr;
                queue.add(next);
            }
        }
        if (endState == null || endState.prev == null) {
            System.out.println("nowhere");
            return AgentAction.DO_NOTHING;
        }
        while (endState.prev.prev != null) {
            endState = endState.prev;
        }
        if (endState.r > startState.r) {
            System.out.println("south");
            return AgentAction.MOVE_SOUTH;
        } else if (endState.r < startState.r) {
            System.out.println("north");
            return AgentAction.MOVE_NORTH;
        } else if (endState.c < startState.c) {
            System.out.println("west");
            return AgentAction.MOVE_WEST;
        } else {
            System.out.println("east");
            return AgentAction.MOVE_EAST;
        }
    }

    private boolean inBounds(int r, int c, char[][] mat) {
        return r >= 0 && r < mat.length && c >= 0 && c < mat[r].length;
    }

    private void update(AgentEnvironment env) {
        if (inBounds(rowPos, colPos-1, board)
                && (board[rowPos][colPos-1] == '?' || board[rowPos][colPos-1] == 'E')) {
            if (env.isObstacleWestImmediate()) {
                board[rowPos][colPos - 1] = 'W';
            } else {
                board[rowPos][colPos - 1] = '.';
            }
        }
        if (inBounds(rowPos, colPos+1, board)
                && (board[rowPos][colPos+1] == '?' || board[rowPos][colPos+1] == 'E')) {
            if (env.isObstacleEastImmediate()) {
                board[rowPos][colPos + 1] = 'W';
            } else {
                board[rowPos][colPos + 1] = '.';
            }
        }
        if (inBounds(rowPos-1, colPos, board)
                && (board[rowPos-1][colPos] == '?' || board[rowPos-1][colPos] == 'E')) {
            if (env.isObstacleNorthImmediate()) {
                board[rowPos - 1][colPos] = 'W';
            } else {
                board[rowPos - 1][colPos] = '.';
            }
        }
        if (inBounds(rowPos+1, colPos, board)
                && (board[rowPos+1][colPos] == '?' || board[rowPos+1][colPos] == 'E')) {
            if (env.isObstacleSouthImmediate()) {
                board[rowPos + 1][colPos] = 'W';
            } else {
                board[rowPos + 1][colPos] = '.';
            }
        }
        if (!justPlantedMine) {
            board[rowPos][colPos] = '.';
        }
        if (!validate(env)) {
            System.out.println("*****");
            System.out.println("INVALID LOCATION!!!");
            System.out.println("*****");
            System.out.println("Attacker previous locs:");
            for (int[] ar : attacker.previous) {
                System.out.println(Arrays.toString(ar));
            }
            System.out.println("Curr Pos: " + attacker.rowPos + "," + attacker.colPos);
            System.out.println("Defender previous locs:");
            for (int[] ar : defender.previous) {
                System.out.println(Arrays.toString(ar));
            }
            System.out.println("Curr Pos: " + defender.rowPos + "," + defender.colPos);
            throw new RuntimeException("Invalid location");
        } else {
            justPlantedMine = false;
        }
    }

    private boolean validate(AgentEnvironment env) {
        if (inBounds(rowPos, colPos-1, board) && env.isObstacleWestImmediate()
                && board[rowPos][colPos-1] != 'W') {
            return false;
        }
        if (inBounds(rowPos, colPos+1, board) && env.isObstacleEastImmediate()
                && board[rowPos][colPos+1] != 'W') {
            return false;
        }
        if (inBounds(rowPos-1, colPos, board) && env.isObstacleNorthImmediate()
                && board[rowPos-1][colPos] != 'W') {
            return false;
        }
        if (inBounds(rowPos+1, colPos, board) && env.isObstacleSouthImmediate()
                && board[rowPos+1][colPos] != 'W') {
            return false;
        }
        return true;
    }

    private boolean hasDied(AgentEnvironment env) {
        if(baseOnLeft) {
            if(isDefender) {
                if(env.isBaseSouth(AgentEnvironment.OUR_TEAM, false) && !env.isBaseWest(AgentEnvironment.OUR_TEAM, false)
                        && env.isObstacleNorthImmediate() && env.isObstacleWestImmediate()) {
                    board[rowPos][colPos] = '.';
                    rowPos = 0;
                    colPos = 0;
                    return true;
                }
            }
            else {
                if(env.isBaseNorth(AgentEnvironment.OUR_TEAM, false) && !env.isBaseWest(AgentEnvironment.OUR_TEAM, false)
                        && env.isObstacleSouthImmediate() && env.isObstacleWestImmediate()) {
                    board[rowPos][colPos] = '.';
                    rowPos = board.length - 1;
                    colPos = 0;
                    return true;
                }
            }
        }
        else {
            if(isDefender) {
                if(env.isBaseSouth(AgentEnvironment.OUR_TEAM, false) && !env.isBaseEast(AgentEnvironment.OUR_TEAM, false)
                        && env.isObstacleNorthImmediate() && env.isObstacleEastImmediate()) {
                    board[rowPos][colPos] = '.';
                    rowPos = 0;
                    colPos = board.length - 1;
                    return true;
                }
            }
            else {
                if(env.isBaseNorth(AgentEnvironment.OUR_TEAM, false) && !env.isBaseEast(AgentEnvironment.OUR_TEAM, false)
                        && env.isObstacleSouthImmediate() && env.isObstacleEastImmediate()) {
                    board[rowPos][colPos] = '.';
                    rowPos = board.length - 1;
                    colPos = board.length - 1;
                    return true;
                }
            }
        }
        return false;
    }

    private class State implements Comparable<State> {

        int r, c, count;
        State prev;

        public State(int r, int c, int cnt) {
            this.r = r;
            this.c = c;
            this.count = cnt;
        }

        public int compareTo(State other) {
            return count - other.count;
        }

        public String toString() {
            return String.format("(%d,%d) %d", r, c, count);
        }
    }
}
