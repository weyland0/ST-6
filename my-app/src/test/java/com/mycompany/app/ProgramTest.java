package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Тесты логики игры «Крестики-нолики» и вспомогательных классов из {@link Program}.
 */
public class ProgramTest {

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void gameConstructorInitializesBoardAndPlayers() {
        Game g = new Game();
        assertEquals(State.PLAYING, g.state);
        assertEquals('X', g.player1.symbol);
        assertEquals('O', g.player2.symbol);
        for (int i = 0; i < 9; i++) {
            assertEquals(' ', g.board[i]);
        }
    }

    @Test
    void checkStateDetectsXWinRow() {
        Game g = new Game();
        g.symbol = 'X';
        char[] b = "XXX      ".toCharArray();
        assertEquals(State.XWIN, g.checkState(b));
    }

    @Test
    void checkStateDetectsOWinColumn() {
        Game g = new Game();
        g.symbol = 'O';
        char[] b = "O  O  O  ".toCharArray();
        assertEquals(State.OWIN, g.checkState(b));
    }

    @Test
    void checkStateDetectsDiagonalWin() {
        Game g = new Game();
        g.symbol = 'X';
        // Главная диагональ: клетки 0, 4, 8
        char[] b = "X   X   X".toCharArray();
        assertEquals(State.XWIN, g.checkState(b));
    }

    @Test
    void checkStateDetectsAntiDiagonalWin() {
        Game g = new Game();
        g.symbol = 'O';
        char[] b = "  O O O  ".toCharArray();
        assertEquals(State.OWIN, g.checkState(b));
    }

    @Test
    void checkStatePlayingWhenEmptyCellsAndNoWin() {
        Game g = new Game();
        g.symbol = 'X';
        char[] b = "X O      ".toCharArray();
        assertEquals(State.PLAYING, g.checkState(b));
    }

    @Test
    void checkStateDrawWhenBoardFullAndNoWinForSymbol() {
        Game g = new Game();
        g.symbol = 'X';
        // Заполненная доска без тройки для текущего symbol (X O X / O O X / X X O)
        char[] b = "XOXOOXXXO".toCharArray();
        assertEquals(State.DRAW, g.checkState(b));
    }

    @Test
    void generateMovesListsOnlyEmptyCells() {
        Game g = new Game();
        char[] b = "X O X    ".toCharArray();
        ArrayList<Integer> moves = new ArrayList<>();
        g.generateMoves(b, moves);
        List<Integer> expected = Arrays.asList(1, 3, 5, 6, 7, 8);
        assertEquals(expected, moves);
    }

    @Test
    void evaluatePositionReturnsInfWhenPlayerWins() {
        Game g = new Game();
        g.symbol = 'X';
        char[] b = "XXX      ".toCharArray();
        assertEquals(Game.INF, g.evaluatePosition(b, g.player1));
        assertEquals(-Game.INF, g.evaluatePosition(b, g.player2));
    }

    @Test
    void evaluatePositionReturnsInfWhenPlayerOWins() {
        Game g = new Game();
        g.symbol = 'O';
        char[] b = "OOO      ".toCharArray();
        assertEquals(Game.INF, g.evaluatePosition(b, g.player2));
        assertEquals(-Game.INF, g.evaluatePosition(b, g.player1));
    }

    @Test
    void evaluatePositionReturnsZeroOnDraw() {
        Game g = new Game();
        g.symbol = 'X';
        char[] b = "XOXOOXXXO".toCharArray();
        assertEquals(0, g.evaluatePosition(b, g.player1));
    }

    @Test
    void evaluatePositionReturnsMinusOneWhenGameContinues() {
        Game g = new Game();
        g.symbol = 'X';
        char[] b = "X        ".toCharArray();
        assertEquals(-1, g.evaluatePosition(b, g.player1));
    }

    @Test
    void miniMaxReturnsLegalMoveOnEmptyBoard() {
        Game g = new Game();
        g.cplayer = g.player1;
        int move = g.MiniMax(Arrays.copyOf(g.board, 9), g.player2);
        assertTrue(move >= 1 && move <= 9);
    }

    @Test
    void miniMaxReturnsMoveIntoEmptyCell_partialBoard() {
        Game g = new Game();
        char[] b = "XX       ".toCharArray();
        g.symbol = 'O';
        int move = g.MiniMax(b, g.player2);
        assertTrue(move >= 1 && move <= 9, "ход в диапазоне 1..9");
        assertEquals(' ', b[move - 1], "доска после MiniMax должна быть восстановлена, клетка изначально пустая");
    }

    @Test
    void miniMaxReturnsMoveIntoEmptyCell_whenOWinPossible() {
        Game g = new Game();
        char[] b = "OO X     ".toCharArray();
        g.symbol = 'O';
        int move = g.MiniMax(b, g.player2);
        assertTrue(move >= 1 && move <= 9);
        assertEquals(' ', b[move - 1]);
    }

    @Test
    void miniMaxExploresAlmostFullBoard() {
        Game g = new Game();
        char[] b = "XXOOXX   ".toCharArray();
        g.symbol = 'O';
        int move = g.MiniMax(b, g.player2);
        assertTrue(move >= 1 && move <= 9);
        assertTrue(b[move - 1] == ' ');
        // свободны клетки 6, 7, 8 (1-based 7, 8, 9)
        assertTrue(move == 7 || move == 8 || move == 9);
    }

    @Test
    void utilityPrintCharBoard() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream prev = System.out;
        System.setOut(new PrintStream(buf));
        try {
            Utility.print("123456789".toCharArray());
        } finally {
            System.setOut(prev);
        }
        String out = buf.toString().replace("\r\n", "\n");
        assertTrue(out.contains("1-"));
        assertTrue(out.contains("9-"));
    }

    @Test
    void utilityPrintIntBoard() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream prev = System.out;
        System.setOut(new PrintStream(buf));
        try {
            Utility.print(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8});
        } finally {
            System.setOut(prev);
        }
        assertTrue(buf.toString().contains("0-"));
    }

    @Test
    void utilityPrintMoveList() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream prev = System.out;
        System.setOut(new PrintStream(buf));
        try {
            ArrayList<Integer> m = new ArrayList<>(Arrays.asList(2, 4, 6));
            Utility.print(m);
        } finally {
            System.setOut(prev);
        }
        assertTrue(buf.toString().contains("2-"));
        assertTrue(buf.toString().contains("6-"));
    }

    @Test
    void ticTacToeCellGeometryAndMarker() {
        TicTacToeCell cell = new TicTacToeCell(4, 1, 2);
        assertEquals(4, cell.getNum());
        assertEquals(1, cell.getCol());
        assertEquals(2, cell.getRow());
        assertEquals(' ', cell.getMarker());
        cell.setMarker("X");
        assertEquals('X', cell.getMarker());
        assertEquals("X", cell.getText());
        assertFalse(cell.isEnabled());
    }

    @Test
    void ticTacToePanelCreatesCellsAndInitialGame() {
        TicTacToePanel panel = new TicTacToePanel(new java.awt.GridLayout(3, 3));
        assertEquals(9, panel.getComponentCount());
        assertNotNull(panel);
        JButton b0 = (JButton) panel.getComponent(0);
        b0.doClick();
        assertNotNull(b0.getText());
    }

    @Test
    void ticTacToePanelOneHumanMoveDoesNotFinishGame() throws Exception {
        TicTacToePanel panel = new TicTacToePanel(new java.awt.GridLayout(3, 3));
        JButton humanCell = (JButton) panel.getComponent(4);
        humanCell.doClick();
        Field gameField = TicTacToePanel.class.getDeclaredField("game");
        gameField.setAccessible(true);
        Game g = (Game) gameField.get(panel);
        assertEquals(State.PLAYING, g.state);
    }
}
