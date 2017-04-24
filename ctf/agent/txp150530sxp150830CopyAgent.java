package ctf.agent;


import ctf.common.AgentEnvironment;

import ctf.common.AgentAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class txp150530sxp150830CopyAgent extends Agent {

    /**
     * W = wall
     * . = open space
     * H = home base
     * F = enemy base
     * M = mine
     * E = enemy agent
     * P = ally agent
     * ? = Unknown
     */
    static char[][] board;  // Holds the board data
    static txp150530sxp150830CopyAgent attacker;  // Reference to the attacker agent
    static txp150530sxp150830CopyAgent defender;  // Reference to the defender agent
    static int homeBaseRow, homeBaseCol;  // Home base location
    static int enemyBaseRow, enemyBaseCol;  // Enemy base location
    static int maxSteps;  // Step limit on the map

    // Both
    int rowPos, colPos;  // Agent position
    boolean didAssignStrategy;  // True if agent has been assigned attacker/defender
    boolean isDefender;  // True if agent is the defender
    boolean baseOnLeft;  // True if home base is on the left
    boolean didPlaceInitialMine;  // True if initial mine was placed next to home base
    boolean hunting;  // True if agent is hunting other agents
    boolean ignoreMines;  // True if agent is allowed to path through mines
    boolean justPlantedMine;  // True if agent placed a mine on the last turn
    ArrayList<Boolean> preKnownWalls = new ArrayList<>();  // Used when board size is not known
    int agentSteps;  // Keeps track of the number of steps an agent has taken

    // Defender
    // Move down -> wait for attacker -> place mine -> move to front of flag -> wait until flag gets taken
    boolean defDidMoveDown, defDidWaitForAtt, defDidMoveToFlagFront;
    boolean defWasBlocked;  // True if the defender cannot stand in front of home base

    // Attacker
    // Move up -> wait for defender -> place mine -> move to enemy base -> run back while placing mines
    boolean attDidMoveUp, attDidWaitForDef, attDidMoveToEnemyBase, attDidPlaceMineLast;
    boolean attStuck;  // True if the attacker cannot path to the enemy base
    LinkedList<int[]> previous = new LinkedList<>();  // Holds previous boardsize/3 moves

    public txp150530sxp150830CopyAgent() {
        // Dereference static variables when the game is reset, prevents old data
        // from messing up future games
        board = null;
        attacker = null;
        defender = null;
    }

    /**
     * Gets the type of move the agent is going to take.
     * @param env Interface for the agent environment
     * @return integer of move type
     */
    public int getMove(AgentEnvironment env) {
        // Assign strategy
        if (!didAssignStrategy) {
            assignStrat(env);
        }

        // Get the move
        if (isDefender) {
//            if (board != null) {
//                for (char[] ar : board) {
//                    System.out.println(ar);
//                }
//                System.out.println();
//            } else {
//                System.out.println("board null");
//            }
            return defGetMove(env);
        } else {
            return attGetMove(env);
        }
    }


    /**
     * Assigns the agent a strategy depending on where it is originally placed.
     * @param env Interface for the agent environment
     */
    private void assignStrat(AgentEnvironment env) {
        // Northern agent is the defender
        if (env.isAgentSouth(AgentEnvironment.OUR_TEAM, false)) {
            isDefender = true;
            defender = this;
        } else {
            attacker = this;
        }
        didAssignStrategy = true;

        // Check if home base is on the left
        if (env.isBaseEast(AgentEnvironment.ENEMY_TEAM, false)) {
            baseOnLeft = true;
        }
    }

    /**
     * Gets the type of move the defender is going to make.
     * @param env Interface for the agent environment
     * @return integer of move type
     */
    public int defGetMove(AgentEnvironment env) {
//        System.out.printf("%s %s %s %s%n", defDidMoveDown, didPlaceInitialMine, defDidWaitForAtt, defDidMoveToFlagFront);
        agentSteps++;

        // Activate hunting mode halfway through the game
        if (board != null && agentSteps >= maxSteps/2) {
            hunting = true;
        }

        // Avoid the ally attacker to prevent blocking a flag capture
        // Blows itself up if it can't move away
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

        // Move down
        if (!defDidMoveDown) {
            // Get if object directly to left/right
            if (baseOnLeft) {
                preKnownWalls.add(env.isObstacleEastImmediate());
            } else {
                preKnownWalls.add(env.isObstacleWestImmediate());
            }

            // Check if defender is directly above home base
            if (env.isBaseSouth(AgentEnvironment.OUR_TEAM, true)) {
                defDidMoveDown = true;
                // Check if attacker is waiting
                if (attacker.attDidMoveUp) {
//                    System.out.println("Creating board from defender move");
                    // Initialize board
                    int size = preKnownWalls.size() + attacker.preKnownWalls.size() + 1;
                    board = new char[size][size];
                    maxSteps = size * size * 2;
                    for (char[] ar : board) {
                        Arrays.fill(ar, '?');
                    }

                    // Mark walls above base
                    for (int i = 0; i < preKnownWalls.size(); i++) {
                        if (baseOnLeft) {
                            board[i][1] = preKnownWalls.get(i) ? 'W' : '.';
                            board[i][0] = '.';
                        } else {
                            board[i][size-2] = preKnownWalls.get(i) ? 'W' : '.';
                            board[i][size-1] = '.';
                        }
                    }

                    // Mark home/enemy bases
                    if (baseOnLeft) {
                        board[preKnownWalls.size()][0] = 'H';
                        board[preKnownWalls.size()][size-1] = 'F';
                    } else {
                        board[preKnownWalls.size()][size-1] = 'H';
                        board[preKnownWalls.size()][0] = 'F';
                    }

                    // Mark walls below base
                    for (int i = 0; i < attacker.preKnownWalls.size(); i++) {
                        if (baseOnLeft) {
                            board[size-i-1][1] = attacker.preKnownWalls.get(i) ? 'W' : '.';
                            board[size-i-1][0] = '.';
                        } else {
                            board[size-i-1][size-2] = attacker.preKnownWalls.get(i) ? 'W' : '.';
                            board[size-i-1][size-1] = '.';
                        }
                    }

                    // Update states
                    attacker.attDidWaitForDef = true;
                    defDidWaitForAtt = true;
                    didPlaceInitialMine = true;

                    // Set attacker/defender positions
                    rowPos = preKnownWalls.size()-1;
                    attacker.rowPos = board.length-attacker.preKnownWalls.size();
                    if (baseOnLeft) {
                        colPos = 0;
                        attacker.colPos = 0;
                    } else {
                        colPos = board.length-1;
                        attacker.colPos = board.length-1;
                    }

                    // Set home/enemy base locations
                    homeBaseRow = preKnownWalls.size();
                    enemyBaseRow = preKnownWalls.size();
                    if (baseOnLeft) {
                        homeBaseCol = 0;
                        enemyBaseCol = board.length-1;
                    } else {
                        homeBaseCol = board.length-1;
                        enemyBaseCol = 0;
                    }

                    // Place initial mine
                    board[rowPos][colPos] = 'M';
                    justPlantedMine = true;
                    return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
                } else {
                    return AgentAction.DO_NOTHING;
                }
            }

            // Still haven't reached home base, continue moving south
            return AgentAction.MOVE_SOUTH;
        }

        // Update position and states if dead
        if (hasDied(env)) {
//            System.out.println("Defender died!");
            defDidMoveToFlagFront = false;
        }
        // Update neighboring cells
        update(env);
        // Kill self if calculated position doesn't match its actual position
        if (!validate(env)) {
            if (!justPlantedMine) {
                justPlantedMine = true;
                return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
            }
            justPlantedMine = false;
            return AgentAction.DO_NOTHING;
        }

        // Store previous move
        previous.add(new int[]{rowPos, colPos});
        if (previous.size() > board.length) {
            previous.remove();
        }

        // Wait for attacker
        if (!defDidWaitForAtt) {
            return AgentAction.DO_NOTHING;
        }

        // Place initial mine
        if (!didPlaceInitialMine) {
            board[rowPos][colPos] = 'M';
            didPlaceInitialMine = true;
            justPlantedMine = true;
            return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
        }

        // Move back to home base if defender has the flag
        if (env.hasFlag()) {
            return tryMove(env, homeBaseRow, homeBaseCol);
        }

        // Enemy has the flag
        if (env.hasFlag(AgentEnvironment.ENEMY_TEAM)) {
            // Move to enemy base if no mine is on the base
            ignoreMines = false;
            if (board[enemyBaseRow][enemyBaseCol] != 'M') {
                return tryMove(env, enemyBaseRow, enemyBaseCol);
            }

            // Move in front of home base otherwise
            if (baseOnLeft) {
                return tryMove(env, homeBaseRow, homeBaseCol+1);
            } else {
                return tryMove(env, homeBaseRow, homeBaseCol-1);
            }
        }

        // Attacker has the flag
        if (env.hasFlag(AgentEnvironment.OUR_TEAM)) {
            // Kill self on mine above the base (prevents attacker from being blocked from capture)
            ignoreMines = true;
            if (board[homeBaseRow-1][homeBaseCol] == 'M') {
                return tryMove(env, homeBaseRow - 1, homeBaseCol);
            }
            ignoreMines = false;

            int move;

            // Try to move in front of home base
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

            // If move is blocked, move to 2 spaces south of home base
            if (move == AgentAction.DO_NOTHING) {
                return tryMove(env, Math.max(homeBaseRow+2, board.length-1), homeBaseCol);
            }

            return move;
        }

        // Move to front of home base
        if (!defDidMoveToFlagFront) {
            int move;

            // Try to move to front of home base
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

            // If move is blocked, begin hunting
            if (move == AgentAction.DO_NOTHING) {
                hunting = true;
                defDidMoveToFlagFront = true;
                defWasBlocked = true;
            }

            return move;
        }

        // Now in front of home base
        if (!hunting) {
            // If attacker is stuck, go for the enemy flag
            if (attacker.attStuck) {
                return tryMove(env, enemyBaseRow, enemyBaseCol);
            }

            // If enemy agent is approaching, place a mine
            if (env.isAgentNorth(AgentEnvironment.ENEMY_TEAM, true)
                    || env.isAgentEast(AgentEnvironment.ENEMY_TEAM, true)
                    || env.isAgentSouth(AgentEnvironment.ENEMY_TEAM, true)
                    || env.isAgentWest(AgentEnvironment.ENEMY_TEAM, true)) {
                justPlantedMine = true;
                return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
            }

            // Move back to in front of home base if moved away
            if (baseOnLeft) {
                return tryMove(env, homeBaseRow, homeBaseCol + 1);
            } else {
                return tryMove(env, homeBaseRow, homeBaseCol - 1);
            }
        }

        // Kill neighboring enemy agents
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
        }

        // Attack enemy base
        return tryMove(env, enemyBaseRow, enemyBaseCol);
    }

    /**
     * Gets the type of move the attacker is going to make.
     * @param env Interface for the agent environment
     * @return integer of move type
     */
    public int attGetMove(AgentEnvironment env) {
//        System.out.printf("%s %s %s %s%n", attDidMoveUp, attDidWaitForDef, didPlaceInitialMine, attDidMoveToEnemyBase);
        agentSteps++;

        // Avoid defender if the defender has the flag
        // Blows itself up if can't move away
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

        // Move up
        if (!attDidMoveUp) {
            // Get if object directly to left/right
            if (baseOnLeft) {
                preKnownWalls.add(env.isObstacleEastImmediate());
            } else {
                preKnownWalls.add(env.isObstacleWestImmediate());
            }

            // Check if attacker is directly below home base
            if (env.isBaseNorth(AgentEnvironment.OUR_TEAM, true)) {
                attDidMoveUp = true;
                // Check if defender is waiting
                if (defender.defDidMoveDown) {
//                    System.out.println("Creating board from attacker move");
                    // Initialize board
                    int size = defender.preKnownWalls.size() + preKnownWalls.size() + 1;
                    board = new char[size][size];
                    maxSteps = size * size * 2;
                    for (char[] ar : board) {
                        Arrays.fill(ar, '?');
                    }

                    // Mark walls above base
                    for (int i = 0; i < defender.preKnownWalls.size(); i++) {
                        if (baseOnLeft) {
                            board[i][1] = defender.preKnownWalls.get(i) ? 'W' : '.';
                            board[i][0] = '.';
                        } else {
                            board[i][size - 2] = defender.preKnownWalls.get(i) ? 'W' : '.';
                            board[i][size - 1] = '.';
                        }
                    }

                    // Mark home/enemy bases
                    if (baseOnLeft) {
                        board[defender.preKnownWalls.size()][0] = 'H';
                        board[defender.preKnownWalls.size()][size - 1] = 'F';
                    } else {
                        board[defender.preKnownWalls.size()][size - 1] = 'H';
                        board[defender.preKnownWalls.size()][0] = 'F';
                    }

                    // Mark walls below base
                    for (int i = 0; i < preKnownWalls.size(); i++) {
                        if (baseOnLeft) {
                            board[size - i - 1][1] = preKnownWalls.get(i) ? 'W' : '.';
                            board[size - i - 1][0] = '.';
                        } else {
                            board[size - i - 1][size - 2] = preKnownWalls.get(i) ? 'W' : '.';
                            board[size - i - 1][size - 1] = '.';
                        }
                    }

                    // Updates states
                    attDidWaitForDef = true;
                    defender.defDidWaitForAtt = true;
                    didPlaceInitialMine = true;

                    // Set attacker/defender positions
                    defender.rowPos = defender.preKnownWalls.size()-1;
                    rowPos = board.length - preKnownWalls.size();
                    if (baseOnLeft) {
                        colPos = 0;
                        defender.colPos = 0;
                    } else {
                        colPos = board.length - 1;
                        defender.colPos = board.length - 1;
                    }

                    // Set home/enemy base locations
                    homeBaseRow = defender.preKnownWalls.size();
                    enemyBaseRow = defender.preKnownWalls.size();
                    if (baseOnLeft) {
                        homeBaseCol = 0;
                        enemyBaseCol = board.length-1;
                    } else {
                        homeBaseCol = board.length - 1;
                        enemyBaseCol = 0;
                    }

                    // Place initial mine
                    board[rowPos][colPos] = 'M';
                    justPlantedMine = true;
                    return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
                } else {
                    return AgentAction.DO_NOTHING;
                }
            }

            // Still haven't reached home base, continue moving north
            return AgentAction.MOVE_NORTH;
        }

        // Update position and states if died
        if (hasDied(env)) {
//            System.out.println("Attacker died!");
            attDidMoveToEnemyBase = false;
            defender.defDidMoveToFlagFront = false;
            previous.clear();
        }
        // Update neighboring cells
        update(env);
        // Kill self if calculated position is different from actual position
        if (!validate(env)) {
            if (!justPlantedMine) {
                justPlantedMine = true;
                return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
            }
            justPlantedMine = false;
            return AgentAction.DO_NOTHING;
        }

        // Keep track of previous moves
        if (!attDidMoveToEnemyBase) {
            previous.add(new int[]{rowPos, colPos});
            if (previous.size() > board.length/3) {
                previous.remove();
            }
        }

        // Wait for defender
        if (!attDidWaitForDef) {
            return AgentAction.DO_NOTHING;
        }

        // Place initial mine
        if (!didPlaceInitialMine) {
            board[rowPos][colPos] = 'M';
            didPlaceInitialMine = true;
            justPlantedMine = true;
            return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
        }

        // Move to enemy base
        if (!attDidMoveToEnemyBase) {
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
        }

        // Place mine every other turn (for boardsize/3 turns) if have flag
        if (!attDidPlaceMineLast) {
            board[rowPos][colPos] = 'M';
            attDidPlaceMineLast = true;
            justPlantedMine = true;
            return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
        }

        // Died and lost flag, return to state "move to enemy base"
        if (!env.hasFlag()) {
            attDidMoveToEnemyBase = false;
            attDidPlaceMineLast = false;
            return AgentAction.DO_NOTHING;
        }

        int move;

        // Check if still placing mines
        if (previous.isEmpty()) {
            // Done placing mines, try move to home base
            move = tryMove(env, homeBaseRow, homeBaseCol);

            // Home base path is blocked, try moving to 2 spaces north of home base
            if (move == AgentAction.DO_NOTHING) {
                move = tryMove(env, Math.max(0, homeBaseRow - 2), homeBaseCol);

                // That move is blocked too, so do nothing and mark itself stuck
                if (move == AgentAction.DO_NOTHING) {
                    attStuck = true;
                }
            }
        } else {
            // Still placing mines
            int[] lastMove = previous.removeLast();
            attDidPlaceMineLast = false;

            // Get direction of the last move
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
                attDidPlaceMineLast = true;
                move = AgentAction.DO_NOTHING;
            }
        }

        // Attacker can move, un-mark stuck
        attStuck = false;
        return move;
    }

    /**
     * Used to help update the map and see if a move is good before actually making the move.
     * @param env Interface for the agent environment
     * @param destR the row position of the destination
     * @param destC the column position of the destination
     * @return integer of move type
     */
    private int tryMove(AgentEnvironment env, int destR, int destC) {
        // Temporarily mark ally agents on the board
        char defTemp = board[defender.rowPos][defender.colPos];
        char attTemp = board[attacker.rowPos][attacker.colPos];
        board[attacker.rowPos][attacker.colPos] = 'P';
        board[defender.rowPos][defender.colPos] = 'P';
        int e1r = -1, e1c = -1, e2r = -1, e2c = -1;

        // Sees if an enemy agent is to the north
        if (env.isAgentNorth(AgentEnvironment.ENEMY_TEAM, true)) {
//            System.out.println("Found enemy 1");
            e1r = rowPos-1;
            e1c = colPos;
        }
        // Sees if an enemy agent is to the south
        if (env.isAgentSouth(AgentEnvironment.ENEMY_TEAM, true)) {
            if (e1r == -1) {
//                System.out.println("Found enemy 1");
                e1r = rowPos+1;
                e1c = colPos;
            } else {
//                System.out.println("Found enemy 2");
                e2r = rowPos+1;
                e2c = colPos;
            }
        }
        // Sees if an enemy agent is to the east
        if (env.isAgentEast(AgentEnvironment.ENEMY_TEAM, true)) {
            if (e1r == -1) {
//                System.out.println("Found enemy 1");
                e1r = rowPos;
                e1c = colPos+1;
            } else {
//                System.out.println("Found enemy 2");
                e2r = rowPos;
                e2c = colPos+1;
            }
        }
        // Sees if an enemy agent is to the west
        if (env.isAgentWest(AgentEnvironment.ENEMY_TEAM, true)) {
            if (e1r == -1) {
//                System.out.println("Found enemy 1");
                e1r = rowPos;
                e1c = colPos-1;
            } else {
//                System.out.println("Found enemy 2");
                e2r = rowPos;
                e2c = colPos-1;
            }
        }

        // Mark board with enemy agents
        if (e1r != -1) {
            board[e1r][e1c] = 'E';
        }
        if (e2r != -1) {
            board[e2r][e2c] = 'E';
        }

        // Get move on path to the destination
        int move = pathTo(destR, destC, env.hasFlag());

        // Replace characters on attacker/defender positions
        board[attacker.rowPos][attacker.colPos] = attTemp;
        board[defender.rowPos][defender.colPos] = defTemp;

        // Does not move if there is a friendly agent for a move, updates position if there is no agent
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

    /**
     * Uses the current known board to determine a path towards a destination. The
     * search algorithm is UCS.
     * @param destR the row position of the destination
     * @param destC the column position of the destination
     * @param hasFlag true if the agent has the flag
     * @return integer of move type
     */
    private int pathTo(int destR, int destC, boolean hasFlag) {
        int[][] best = new int[board.length][board.length]; // Array filled with numbers to determine shortest path to destination
        for (int i = 0; i < best.length; i++){ //Fills initially with empty space moves
            Arrays.fill(best[i], 1000000);
        }
        //Current state of the agent, start position for UCS
        State startState = new State(rowPos, colPos, 0);
        State endState = null;
        PriorityQueue<State> queue = new PriorityQueue<>();  // Queue for UCS
        queue.add(startState);

        // Search until queue is empty
        while (!queue.isEmpty()) {
            State curr = queue.remove();

            // If already better path to curr, do not expand
            if (curr.count >= best[curr.r][curr.c]) {
                continue;
            }

            // Mark curr as best path
            best[curr.r][curr.c] = curr.count;

            // If at goal, set end state to curr
            if (curr.r == destR && curr.c == destC) {
                endState = curr;
            }

            // Expand east move
            if (inBounds(curr.r, curr.c+1, board) && board[curr.r][curr.c+1] != 'W'
                    && (hasFlag || curr.r != homeBaseRow || curr.c+1 != homeBaseCol)) {
                int add = 1; // Keeps weights on less desirable paths
                if (!hunting && board[curr.r][curr.c+1] == 'E') { // Gives an enemy agent a weight of 100
                    add = 100;
                }
                if (board[curr.r][curr.c+1] == 'P') { // Gives an allied agent a weight of 300
                    add = 300;
                }
                if (!defWasBlocked && !ignoreMines && board[curr.r][curr.c+1] == 'M') { // Gives mines a weight of 800
                    add = 800;
                }
                State next = new State(curr.r, curr.c+1, curr.count+add);
                next.prev = curr;
                queue.add(next);
            }

            // Expand west move
            if (inBounds(curr.r, curr.c-1, board) && board[curr.r][curr.c-1] != 'W'
                    && (hasFlag || curr.r != homeBaseRow || curr.c-1 != homeBaseCol)) {
                int add = 1; // Keeps weights on less desirable paths
                if (!hunting && board[curr.r][curr.c-1] == 'E') { // Gives an enemy agent a weight of 100
                    add = 100;
                }
                if (board[curr.r][curr.c-1] == 'P') { // Gives an allied agent a weight of 300
                    add = 300;
                }
                if (!defWasBlocked && !ignoreMines && board[curr.r][curr.c-1] == 'M') { // Gives mines a weight of 800
                    add = 800;
                }
                State next = new State(curr.r, curr.c-1, curr.count+add);
                next.prev = curr;
                queue.add(next);
            }

            // Expand south move
            if (inBounds(curr.r+1, curr.c, board) && board[curr.r+1][curr.c] != 'W'
                    && (hasFlag || curr.r+1 != homeBaseRow || curr.c != homeBaseCol)) {
                int add = 1; // Keeps weights on less desirable paths
                if (!hunting && board[curr.r+1][curr.c] == 'E') { // Gives an enemy agent a weight of 100
                    add = 100;
                }
                if (board[curr.r+1][curr.c] == 'P') { // Gives an allied agent a weight of 300
                    add = 300;
                }
                if (!defWasBlocked && !ignoreMines && board[curr.r+1][curr.c] == 'M') { // Gives mines a weight of 800
                    add = 800;
                }
                State next = new State(curr.r+1, curr.c, curr.count+add);
                next.prev = curr;
                queue.add(next);
            }

            // Expand north move
            if (inBounds(curr.r-1, curr.c, board) && board[curr.r-1][curr.c] != 'W'
                    && (hasFlag || curr.r-1 != homeBaseRow || curr.c != homeBaseCol)) {
                int add = 1; // Keeps weights on less desirable paths
                if (!hunting && board[curr.r-1][curr.c] == 'E') { // Gives an enemy agent a weight of 100
                    add = 100;
                }
                if (board[curr.r-1][curr.c] == 'P') { // Gives an allied agent a weight of 300
                    add = 300;
                }
                if (!defWasBlocked && !ignoreMines && board[curr.r-1][curr.c] == 'M') { // Gives mines a weight of 800
                    add = 800;
                }
                State next = new State(curr.r-1, curr.c, curr.count+add);
                next.prev = curr;
                queue.add(next);
            }
        }

        // Could not find path to destination, don't move
        if (endState == null || endState.prev == null) {
//            System.out.println("nowhere");
            return AgentAction.DO_NOTHING;
        }

        // Traverse back on path until reaching the 2nd position
        while (endState.prev.prev != null) {
            endState = endState.prev;
        }

        // Return the move the agent should take to reach the 2nd position
        if (endState.r > startState.r) {
//            System.out.println("south");
            return AgentAction.MOVE_SOUTH;
        } else if (endState.r < startState.r) {
//            System.out.println("north");
            return AgentAction.MOVE_NORTH;
        } else if (endState.c < startState.c) {
//            System.out.println("west");
            return AgentAction.MOVE_WEST;
        } else {
//            System.out.println("east");
            return AgentAction.MOVE_EAST;
        }
    }

    /**
     * Checks to see if a given position is within the bounds of the board.
     * @param r int of the row position
     * @param c int of the column position
     * @param mat char matrix containing the mapped out board
     * @return integer of move type
     */
    private boolean inBounds(int r, int c, char[][] mat) {
        return r >= 0 && r < mat.length && c >= 0 && c < mat[r].length;
    }

    /**
     * Updates the map based on the surroundings of the agent.
     * @param env Interface for the agent environment
     */
    private void update(AgentEnvironment env) {
        // Update position to the west
        if (inBounds(rowPos, colPos-1, board)
                && (board[rowPos][colPos-1] == '?' || board[rowPos][colPos-1] == 'E')) {
            if (env.isObstacleWestImmediate()) {
                board[rowPos][colPos - 1] = 'W';
            } else {
                board[rowPos][colPos - 1] = '.';
            }
        }
        // Update position to the east
        if (inBounds(rowPos, colPos+1, board)
                && (board[rowPos][colPos+1] == '?' || board[rowPos][colPos+1] == 'E')) {
            if (env.isObstacleEastImmediate()) {
                board[rowPos][colPos + 1] = 'W';
            } else {
                board[rowPos][colPos + 1] = '.';
            }
        }
        // Update the position to the north
        if (inBounds(rowPos-1, colPos, board)
                && (board[rowPos-1][colPos] == '?' || board[rowPos-1][colPos] == 'E')) {
            if (env.isObstacleNorthImmediate()) {
                board[rowPos - 1][colPos] = 'W';
            } else {
                board[rowPos - 1][colPos] = '.';
            }
        }
        //Update the position to the south
        if (inBounds(rowPos+1, colPos, board)
                && (board[rowPos+1][colPos] == '?' || board[rowPos+1][colPos] == 'E')) {
            if (env.isObstacleSouthImmediate()) {
                board[rowPos + 1][colPos] = 'W';
            } else {
                board[rowPos + 1][colPos] = '.';
            }
        }
        // Mark current space as empty
        if (!justPlantedMine) {
            board[rowPos][colPos] = '.';
        }
        if (!validate(env)) {
//            System.out.println("*****");
//            System.out.println("INVALID LOCATION!!!");
//            System.out.println("*****");
//            System.out.println("Attacker previous locs:");
//            for (int[] ar : attacker.previous) {
//                System.out.println(Arrays.toString(ar));
//            }
//            System.out.println("Curr Pos: " + attacker.rowPos + "," + attacker.colPos);
//            System.out.println("Defender previous locs:");
//            for (int[] ar : defender.previous) {
//                System.out.println(Arrays.toString(ar));
//            }
//            System.out.println("Curr Pos: " + defender.rowPos + "," + defender.colPos);
//            throw new RuntimeException("Invalid location");
        } else {
            justPlantedMine = false;
        }
    }

    /**
     * Determines if the agent's actual position matches up with its supposed position.
     * @param env Interface for the agent environment
     * @return boolean if position is not valid
     */
    private boolean validate(AgentEnvironment env) {
        // Validate West
        if (inBounds(rowPos, colPos-1, board) && env.isObstacleWestImmediate()
                && board[rowPos][colPos-1] != 'W') {
            return false;
        }
        // Validate East
        if (inBounds(rowPos, colPos+1, board) && env.isObstacleEastImmediate()
                && board[rowPos][colPos+1] != 'W') {
            return false;
        }
        // Validate North
        if (inBounds(rowPos-1, colPos, board) && env.isObstacleNorthImmediate()
                && board[rowPos-1][colPos] != 'W') {
            return false;
        }
        // Validate South
        if (inBounds(rowPos+1, colPos, board) && env.isObstacleSouthImmediate()
                && board[rowPos+1][colPos] != 'W') {
            return false;
        }
        return true;
    }


    /**
     * Determines if the agent has died and resets its known position.
     * @param env Interface for the agent environment
     * @return boolean if it has died
     */
    private boolean hasDied(AgentEnvironment env) {
        // Home base on left
        if(baseOnLeft) {
            // Defender
            if(isDefender) {
                // Determines starting position by being in starting corner and above home base on same column
                if(env.isBaseSouth(AgentEnvironment.OUR_TEAM, false) && !env.isBaseWest(AgentEnvironment.OUR_TEAM, false)
                        && env.isObstacleNorthImmediate() && env.isObstacleWestImmediate()) {
                    board[rowPos][colPos] = '.';
                    rowPos = 0;
                    colPos = 0;
                    return true;
                }
            }
            // Attacker
            else {
                // Determines starting position by being in starting corner and below home base on same column
                if(env.isBaseNorth(AgentEnvironment.OUR_TEAM, false) && !env.isBaseWest(AgentEnvironment.OUR_TEAM, false)
                        && env.isObstacleSouthImmediate() && env.isObstacleWestImmediate()) {
                    board[rowPos][colPos] = '.';
                    rowPos = board.length - 1;
                    colPos = 0;
                    return true;
                }
            }
        }
        // Home base on right
        else {
            // Defender
            if(isDefender) {
                // Determines starting position by being in starting corner and above home base on same column
                if(env.isBaseSouth(AgentEnvironment.OUR_TEAM, false) && !env.isBaseEast(AgentEnvironment.OUR_TEAM, false)
                        && env.isObstacleNorthImmediate() && env.isObstacleEastImmediate()) {
                    board[rowPos][colPos] = '.';
                    rowPos = 0;
                    colPos = board.length - 1;
                    return true;
                }
            }
            // Attacker
            else {
                // Determines starting position by being in starting corner and below home base on same column
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

    /**
     * Used in the search algorithm to hold the current search data.
     */
    private class State implements Comparable<State> {

        int r, c;  // Position
        int count;  // Distance from start node
        State prev;  // Previous state

        public State(int r, int c, int cnt) {
            this.r = r;
            this.c = c;
            this.count = cnt;
        }

        // Sort by distance from start node
        public int compareTo(State other) {
            return count - other.count;
        }

        public String toString() {
            return String.format("(%d,%d) %d", r, c, count);
        }
    }
}
