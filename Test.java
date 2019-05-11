import java.util.*;
import java.io.*;

public class Test {
    public static void main(String[] args) {
        // first just vizualize
        // then add horizontal folds
        // then add diagonal folds
        Cell[][] front = new Cell[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                boolean[] neighbors = new boolean[] {(j%2==1), (j>1), (i%2==1), (i>1)};
                front[i][j] = new Cell(null, neighbors);
            }
        }
        drawBoard(front);
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
        Character fillLetter = null;
        boolean[] neighbors; // degrees [0,90,180,270]
        public Cell(Character fillLetter, boolean[] neighbors) {
            this.fillLetter = fillLetter;
            this.neighbors  = neighbors;
        }
        int oppositeNeighbor(int neighbor) {
            return (neighbor + 2) % 4;
        }

        char[][] draw() {
            char[][] ret = new char[4][6];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 6; j++) {                
                    ret[i][j] = ' ';
                }
            }
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
