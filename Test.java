import java.util.*;
import java.io.*;

public class Test {
    public static void main(String[] args) throws Exception {

        Scanner cmdIn = new Scanner(System.in);
        System.out.println("Enter level file name:");
        String levelFilename = cmdIn.nextLine();
        
        Scanner levelIn = new Scanner(new File(levelFilename));
        int R = levelIn.nextInt();
        int C = levelIn.nextInt();
        int Sr = levelIn.nextInt();
        int Sc = levelIn.nextInt();
        int Er = levelIn.nextInt();
        int Ec = levelIn.nextInt();

        Cell[][] front = loadBoard(levelIn, R, C);
	front[Sr][Sc].isStart = true;
        front[Er][Ec].isEnd = true;
        Cell[][] back = loadBoard(levelIn, R, C);

        GameState cur = new GameState(front, back, "");
        List<GameState> stack = new ArrayList<>();
        help();
        while (true) {
            cur.trySolve();
            drawBoard(cur.front);
            if (cur.isSolved()) {
                System.out.println("Congrats, you solved it!!");
                break;
            }
            String[] cmdParts = cmdIn.nextLine().trim().split("\\s+");
            String cmd = cmdParts[0];
            if ("exit".equals(cmd) || "e".equals(cmd)) {
                System.out.println("Exiting!");
                break;
            } else if ("help".equals(cmd) || "h".equals(cmd)) {
                help(); 
            } else if ("flip".equals(cmd) || "f".equals(cmd)) {
                cur.flip();
            } else if ("hf".equals(cmd) || "hfold".equals(cmd)) {
                boolean foldToward;
                if (cmdParts[1].equals ("up")) {
                    foldToward = true;
                } else if ("down".equals(cmdParts[1])) {
                    foldToward = false;
                } else {
                    System.out.println("Invalid hf fold dir '" + cmdParts[1] + "'");
                    help();
                    continue;
                }
                int index = Integer.parseInt(cmdParts[2]);
                stack.add(cur);
                cur = cur.hfold(foldToward, index);
            } else if ("vf".equals(cmd) || "vfold".equals(cmd)) {
                boolean foldToward; 
                if (cmdParts[1].equals("up")) {
                    foldToward = true;
                } else if ("down".equals(cmdParts[1])) {
                    foldToward = false;
                } else {
                    System.out.println("Invalid hf fold dir '" + cmdParts[1] + "'");
                    help();
                    continue;
                }
                int index = Integer.parseInt(cmdParts[2]);
                stack.add(cur);
                cur = cur.vfold(foldToward, index);
            } else if ("undo".equals(cmd) || "u".equals(cmd)) {
                if (stack.isEmpty()) {
                    System.out.println("Cannot undo any further!");
                } else {
                    System.out.println("undoing " + cur.cmd);
                    cur = stack.get(stack.size() - 1);
                    stack.remove(stack.size() - 1);
                }
            } else if ("restart".equals(cmd) || "r".equals(cmd)) {
                if (stack.isEmpty()) {
                    System.out.println("Already at beginning, cannot restart!");
                } else {
                    System.out.println("Restarting Level");
                    cur = stack.get(0);
                    stack.clear();
                }
            } else {
                System.out.println("Invalid command '" + cmd + "'");
                help();
            }
            System.out.println();
        } 
    }

    static class GameState {
        Cell[][] front;
        Cell[][] back;
        String cmd;
        boolean solved;

        public GameState(Cell[][] front, Cell[][] back, String cmd) {
            this.front = front;
            this.back = back;
            this.cmd = cmd;
            solved = false;
        }

        public GameState flip() {
            Cell[][] temp = front;
            front = back;
            back = temp;
            return this;
        }

        public void trySolve() {
            Cell start = null;
            boolean startFront = true;
            int startR=0;
            int startC=0;
            Cell end   = null; 
            boolean endFront = true;
            int endR=0;
            int endC=0;

            for (int r = 0; r < front.length; r++) {
                for (int c = 0; c < front[r].length; c++) {
                    Cell cell = front[r][c];
                    if (cell.isStart) {
                        start = cell;
                        startR = r;
                        startC = c; 
                        startFront = true;
                    }
                    if (cell.isEnd) {
                        end = cell;
                        endR = r;
                        endC = c; 
                        endFront = true;
                    }
                }
            }


            for (int r = 0; r < back.length; r++) {
                for (int c = 0; c < back[r].length; c++) {
                    Cell cell = back[r][c];
                    if (cell.isStart) {
                        start = cell;
                        startR = r;
                        startC = c; 
                        startFront = false;
                    }
                    if (cell.isEnd) {
                        end = cell;
                        endR = r;
                        endC = c; 
                        endFront = false;
                    }
                }
            }

            if (start == null && end == null) {
                System.out.println("Unsolvable, no start or end!");
                return;
            } else if (start == null) {
                System.out.println("Unsolvable, no start or end!");
                return;
            } else if (end == null) {
                System.out.println("Unsolvable, no start or end!");
                return;
            } else if (startFront != endFront) {
                return;
            }

            // TODO add jumping over edge?
            Cell[][] board = (startFront) ? front : back;
            
            Map<Cell, Cell> prevMap = new HashMap<>();
            prevMap.put(board[startR][startC], board[startR][startC]);
            Deque<int[]> queue = new ArrayDeque<>();
            queue.offerLast(new int[] {startR, startC});
 
            int[][] dirs = new int[][] {{0,1}, {-1,0}, {0,-1}, {1,0}};
            while (!queue.isEmpty()) {
                int[] pos = queue.pollFirst();
                // System.out.println("Searching from (" + pos[0] + ", " + pos[1] + ")");
                Cell cur = board[pos[0]][pos[1]];
                if (cur == end) {
                    // System.out.println("Found!");
                    solved = true;
                    // mark path
                    Cell prev = prevMap.get(cur);
                    while (prev != null && !prev.isStart) {
                        prev.markPath();
                        prev = prevMap.get(prev);
                    }
                    if (!startFront) flip();
                    return;
                }
                // find neighbors
                // System.out.println(Arrays.toString(cur.neighbors));
                for (int k = 0; k < dirs.length; k++) {
                   if (!cur.neighbors[k]) continue;
                   int newR = pos[0] + dirs[k][0];
                   int newC = pos[1] + dirs[k][1];
                   if (newR >= 0 && newR < board.length && newC >= 0 && newC < board[newR].length) {
                       Cell n = board[newR][newC];
                       // System.out.println("on board: [" + newR + "," + newC + "], neighbors: " + Arrays.toString(n.neighbors));
                       if (n.neighbors[n.oppositeNeighbor(k)] && !prevMap.containsKey(n)) {
                           prevMap.put(n, cur);
                           queue.offer(new int[] {newR, newC});
                       }
                   }
               }
            }
        }

        public boolean isSolved() {
            return solved;
        }

        private static Cell[][] cloneCells(Cell[][] cells) {
            Cell[][] newCells = new Cell[cells.length][];
            for (int i = 0; i < cells.length; i++) {
                newCells[i] = new Cell[cells[i].length];
                for (int j = 0; j < newCells[i].length; j++) {
                    newCells[i][j] = new Cell(cells[i][j]);
                }
            }
            return newCells;
        }

        public GameState clone() {
            return new GameState(cloneCells(front), cloneCells(back), cmd);
        }

        public GameState vfold(boolean towards, int index) {
            /*if (index < 0 || index >= front.length) return clone();
            Cell[][] newFront = new Cell[newR][front[0].length];
            Cell[][] newBack  = new Cell[newR][front[0].length];
            int newR = front.length - index;
            if (newR < index && towards) {
            } else if (newR < index) {
            
            } else if (towards) {
                for (int r = 0; r < index; r++) {
                    for (int c = 0 ; c < front[r].length; c++) {
                        
                    }
                }
            } else {
                
            }*/
            // TODO
            return null;
        }

        public GameState hfold(boolean towards, int index) {
            if (index <= 0 || index >= front[0].length) return clone();
            int newC = front[0].length - index;
            Cell[][] newFront = new Cell[front.length][newC];
            Cell[][] newBack = new Cell[front.length][newC];
            // tail remainder of front
            if (newC < index) {
               return clone().flip().hfold(!towards, newC);
            } else if (towards) {
                int frontLength = newC - index;
                for (int r = 0; r < front.length; r++) {
                    for (int i = 0; i < frontLength; i++) {
                        newFront[r][index+i] = new Cell(front[r][index*2+i]);
                    }
                }
                for (int r = 0; r < front.length; r++) {
                    for (int i = 0; i < index; i++) {
                        newFront[r][i] = new Cell(back[r][newC + i]);
                    }
                }
                for (int r = 0; r < back.length; r++) {
                    for (int i = 0; i < newC; i++) {
                        newBack[r][i] = new Cell(back[r][i]);
                    }
                }
            } else {
                for (int r = 0; r < front.length; r++) {
                    for (int i = 0; i < newC - index; i++) {
                        newBack[r][i] = new Cell( back[r][i]);
                    }
                }
                for (int r = 0; r < front.length; r++) {
                    for (int i = 0; i < index; i++) {
                        newBack[r][newC - index + i] = new Cell(front[r][i]);
                    }
                }
                for (int r = 0; r < front.length; r++) {
                    for (int i = 0; i < newC; i++) {
                        newFront[r][i] = new Cell(front[r][index+i]);
                    }
                }
            }

            return new GameState(newFront, newBack, "horizontal fold");
        }
    }

    static void help() {
        System.out.println("Help: ");
        System.out.println("    f,  flip    - switch whether back or front is showing");
        System.out.println("    hf, hfold   - {up,down} {index} fold paper horizontally at index in direction");
        System.out.println("    u,  undo    - undo last action TODO");
        System.out.println("    r,  restart - revert to start TODO");
        System.out.println("    h,  help    - show this help screen");
        System.out.println("    e,  exit    - quit the game");
        System.out.println();
    }

    static Cell[][] loadBoard(Scanner in, int r, int c) {
        Cell[][] ret = new Cell[r][c];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                int v = in.nextInt();
                boolean[] neighbors = new boolean[4];
                for (int n = 0; n < 4; n++) {
                    neighbors[n] = ((1<<n) & v) != 0;
                    ret[i][j] = new Cell(false, false, neighbors);
                }
            }
        }
        return ret;
    }

    static void drawBoard(Cell[][] board) {
        drawTopDivider(board[0].length);
        for (int i = 0; i < board.length; i++) {
            char[][][] row = new char[board[i].length][][];
            for (int j = 0; j < board[i].length; j++) {
                row[j] = board[i][j].draw();
            }
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < board[i].length; c++) {
                    System.out.print('|');
                    System.out.print(new String(row[c][r]));
                }
                System.out.println('|');
            }
            drawTopDivider(board[i].length);
        }
    }
    
    static void drawTopDivider(int numCells) {
        for (int i = 0; i < numCells; i++) {
            System.out.print(" ------");
        }
        System.out.println(" ");
    }

    static class Cell {
        boolean isStart;
        boolean isEnd;
        boolean onPath;
        boolean[] neighbors; // degrees [0,90,180,270]

        public Cell(boolean isStart, boolean isEnd, boolean[] neighbors) {
            this.isStart = isStart;
            this.isEnd = isEnd;
            this.neighbors  = neighbors;
            this.onPath = false;
        }

        public Cell(Cell other) {
            this.isStart = other.isStart;
            this.isEnd = other.isEnd;
            this.neighbors = Arrays.copyOf(other.neighbors, 4);
        }

        public void markPath() {
            onPath = true;
        }

        int oppositeNeighbor(int neighbor) {
            return (neighbor + 2) % 4;
        }

        private Character getFillLetter() {
            if (isStart) return 'S';
            if (isEnd) return 'E';
            if (onPath) return 'X';
            return null;
        }

        char[][] draw() {
            char[][] ret = new char[4][6];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 6; j++) {                
                    ret[i][j] = ' ';
                }
            }
            Character fillLetter = getFillLetter();
            if (fillLetter != null) {
                ret[1][2] = ret[1][3] = ret[2][2] = ret[2][3] = fillLetter;
            }
            if (neighbors[0]) {
                ret[0][5] = ret[2][5] = '_';
                if (!neighbors[1]) ret[0][4] = '_';
                if (!neighbors[3]) ret[2][4] = '_';
            } else {
                ret[1][4] = ret[2][4] = '|';
            }
            if (neighbors[1]) {
                ret[0][1] = ret[0][4] = '|';
            } else {
                ret[0][2] = ret[0][3] = '_';
            }
            if (neighbors[2]) {
                ret[0][0] = ret[2][0] = '_';
                if (!neighbors[1]) ret[0][1] = '_';
                if (!neighbors[3]) ret[2][1] = '_';
            } else {
                ret[1][1] = ret[2][1] = '|';
            }
            if (neighbors[3]) {
                ret[3][1] = ret[3][4] = '|';
            } else {
                ret[2][2] = ret[2][3] = '_';
            }
            return ret;
        }
    }




/*
 ------ ------ ------ ------
|      | |  | |_|  |_|____  |
|      | |  | |  ee  |  ss| |
|      | |  | |_ ee _|_ ss| |
|      | |  | | |  | | |  | |
 ------ ------ ------ ------
}
*/
}
