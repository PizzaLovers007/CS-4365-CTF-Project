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

    /**
     * W = wall
     * . = open space
     * H = home base
     * E = enemy base
     */
    static char[][] board;
    static txp150530Agent attacker;
    static txp150530Agent defender;
    static int homeBaseRow, homeBaseCol;
    static int enemyBaseRow, enemyBaseCol;
	static int maxSteps;

    // Both
    int rowPos, colPos;
    boolean didAssignStrategy;
    boolean isDefender;
    boolean baseOnLeft;
    boolean didPlaceInitialMine;
    ArrayList<Boolean> preKnownWalls = new ArrayList<>();  // Used when board size is not known
	int agentSteps; //Keeps track of the number of steps

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
                        board[preKnownWalls.size()][size-1] = 'E';
                    } else {
                        board[preKnownWalls.size()][size-1] = 'H';
                        board[preKnownWalls.size()][0] = 'E';
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

					agentSteps++;
                    return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
                } else {
					agentSteps++;
                    return AgentAction.DO_NOTHING;
                }
            }
			
			agentSteps++;
            return AgentAction.MOVE_SOUTH;
        } else if (!defDidWaitForAtt) {
			agentSteps++;
            return AgentAction.DO_NOTHING;
        } else if (!didPlaceInitialMine) {
            board[rowPos][colPos] = 'M';
            didPlaceInitialMine = true;
			agentSteps++;
            return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
        } else if (!defDidMoveToFlagFront) {
            if (baseOnLeft) {
                return pathTo(homeBaseRow, homeBaseCol + 1);
            } else {
                return pathTo(homeBaseRow, homeBaseCol - 1);
            }
        }
		agentSteps++;
        return AgentAction.DO_NOTHING;
    }

	/**
	 * Gets the type of move the attacker is going to make
	 * @param env Interface for the agent environment
	 * @return integer of move type
	 */
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
                            board[i][0] = '.';
                        } else {
                            board[i][size - 2] = defender.preKnownWalls.get(i) ? 'W' : '.';
                            board[i][size - 1] = '.';
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
                            board[size - i - 1][0] = '.';
                        } else {
                            board[size - i - 1][size - 2] = preKnownWalls.get(i) ? 'W' : '.';
                            board[size - i - 1][size - 1] = '.';
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
            board[rowPos][colPos] = 'M';
            didPlaceInitialMine = true;
            return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
        } else if (!attDidMoveToEnemyBase) {
            return pathTo(enemyBaseRow, enemyBaseCol);
        }

        return AgentAction.DO_NOTHING;
    }

    private int pathTo(int destR, int destC) {
        int[][] best = new int[board.length][board.length];
        for (int i = 0; i < best.length; i++){
            Arrays.fill(best[i], 1000);
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
            if (inBounds(curr.r-1, curr.c, board) && board[curr.r-1][curr.c] != 'W'
                    && board[curr.r-1][curr.c] != 'M' && board[curr.r-1][curr.c] != 'H') {
                State next = new State(curr.r-1, curr.c, curr.count+1);
                next.prev = curr;
                queue.add(next);
            }
            if (inBounds(curr.r+1, curr.c, board) && board[curr.r+1][curr.c] != 'W'
                    && board[curr.r+1][curr.c] != 'M' && board[curr.r+1][curr.c] != 'H') {
                State next = new State(curr.r+1, curr.c, curr.count+1);
                next.prev = curr;
                queue.add(next);
            }
            if (inBounds(curr.r, curr.c-1, board) && board[curr.r][curr.c-1] != 'W'
                    && board[curr.r][curr.c-1] != 'M' && board[curr.r][curr.c-1] != 'H') {
                State next = new State(curr.r, curr.c-1, curr.count+1);
                next.prev = curr;
                queue.add(next);
            }
            if (inBounds(curr.r, curr.c+1, board) && board[curr.r][curr.c+1] != 'W'
                    && board[curr.r][curr.c+1] != 'M' && board[curr.r][curr.c+1] != 'H') {
                State next = new State(curr.r, curr.c+1, curr.count+1);
                next.prev = curr;
                queue.add(next);
            }
        }
        if (endState == null) {
            return AgentAction.DO_NOTHING;
        }
        while (endState.prev != null && endState.prev.prev != null) {
            endState = endState.prev;
        }
        if (endState.r > startState.r) {
            return AgentAction.MOVE_SOUTH;
        } else if (endState.r < startState.r) {
            return AgentAction.MOVE_NORTH;
        } else if (endState.c < startState.c) {
            return AgentAction.MOVE_WEST;
        } else {
            return AgentAction.MOVE_EAST;
        }
    }

    private boolean inBounds(int r, int c, char[][] mat) {
        return r >= 0 && r < mat.length && c >= 0 && c < mat[r].length;
    }
	
	private void update(AgentEnvironment env) {
		if(env.isObstacleNorthImmediate()) {
			board[rowPos][colPos-1] = 'W';
		}
		else {
			board[rowPos][colPos-1] = '.';
		}
		if(env.isObstacleSouthImmediate()) {
			board[rowPos][colPos+1] = 'W';
		}
		else {
			board[rowPos][colPos+1] = '.';
		}
		if(env.isObstacleEastImmediate()) {
			board[rowPos-1][colPos] = 'W';
		}
		else {
			board[rowPos-1][colPos] = '.';
		}
		if(env.isObstacleWestImmediate()) {
			board[rowPos+1][colPos] = 'W';
		}
		else {
			board[rowPos+1][colPos] = '.';
		}
	}
	
	private void hasDied(AgentEnvironment env) {
		if(baseOnLeft) {
			if(isDefender) {
				if(env.isBaseSouth(AgentEnvironment.OUR_TEAM, false) && env.isObstacleNorthImmediate() && env.isObstacleWestImmediate()) {
					rowPos = 0;
					colPos = 0;
				}
			}
			else {
				if(env.isBaseNorth(AgentEnvironment.OUR_TEAM, false) && env.isObstacleSouthImmediate() && env.isObstacleWestImmediate()) {
					rowPos = board.length - 1;
					colPos = 0;
				}
			}
		}
		else {
			if(isDefender) {
				if(env.isBaseSouth(AgentEnvironment.OUR_TEAM, false) &&env.isObstacleNorthImmediate() && env.isObstacleEastImmediate()) {
					rowPos = 0;
					colPos = board.length - 1;
				}
			}
			else {
				if(env.isBaseNorth(AgentEnvironment.OUR_TEAM, false) && env.isObstacleSouthImmediate() && env.isObstacleEastImmediate()) {
					rowPos = board.length - 1;
					colPos = board.length - 1;
				}
			}
		}
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
    }
}
