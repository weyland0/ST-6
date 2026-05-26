package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ProgramTest {

    @BeforeAll
    static void runSwingTestsInHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void gameConstructorInitializesPlayersStateAndBoard() {
        Game game = new Game();

        assertNotNull(game.player1);
        assertNotNull(game.player2);
        assertEquals('X', game.player1.symbol);
        assertEquals('O', game.player2.symbol);
        assertEquals(State.PLAYING, game.state);
        assertEquals(9, game.board.length);
        for (char cell : game.board) {
            assertEquals(' ', cell);
        }
    }

    @ParameterizedTest
    @MethodSource("winningBoards")
    void checkStateDetectsEveryWinningLine(String pattern, char symbol, State expected) {
        Game game = new Game();
        game.symbol = symbol;

        assertEquals(expected, game.checkState(board(pattern)));
    }

    @Test
    void checkStateReturnsPlayingWhenThereAreEmptyCellsAndNoWinner() {
        Game game = new Game();
        game.symbol = 'X';

        assertEquals(State.PLAYING, game.checkState(board("X.O......")));
    }

    @Test
    void checkStateReturnsDrawWhenBoardIsFullAndNoWinner() {
        Game game = new Game();
        game.symbol = 'X';

        assertEquals(State.DRAW, game.checkState(board("XOXXOOOXX")));
    }

    @Test
    void evaluatePositionReturnsPositiveValueForCurrentPlayersWin() {
        Game game = new Game();
        game.symbol = 'X';

        assertEquals(Game.INF, game.evaluatePosition(board("XXX......"), game.player1));
    }

    @Test
    void evaluatePositionReturnsNegativeValueForCurrentPlayersLoss() {
        Game game = new Game();
        game.symbol = 'X';

        assertEquals(-Game.INF, game.evaluatePosition(board("XXX......"), game.player2));
    }

    @Test
    void evaluatePositionReturnsPositiveValueForOPlayerWin() {
        Game game = new Game();
        game.symbol = 'O';

        assertEquals(Game.INF, game.evaluatePosition(board("OOO......"), game.player2));
    }

    @Test
    void evaluatePositionReturnsZeroForDraw() {
        Game game = new Game();
        game.symbol = 'O';

        assertEquals(0, game.evaluatePosition(board("XOXXOOOXX"), game.player1));
    }

    @Test
    void evaluatePositionReturnsMinusOneForUnfinishedGame() {
        Game game = new Game();
        game.symbol = 'X';

        assertEquals(-1, game.evaluatePosition(board("X.O......"), game.player1));
    }

    @Test
    void generateMovesAddsAllEmptyCellIndexes() {
        Game game = new Game();
        ArrayList<Integer> moves = new ArrayList<>();

        game.generateMoves(board("X.O..OXX."), moves);

        assertEquals(List.of(1, 3, 4, 8), moves);
    }

    @Test
    void generateMovesLeavesListEmptyForFullBoard() {
        Game game = new Game();
        ArrayList<Integer> moves = new ArrayList<>();

        game.generateMoves(board("XOXXOOOXX"), moves);

        assertTrue(moves.isEmpty());
    }

    @Test
    void minMoveReturnsTerminalEvaluationImmediately() {
        Game game = new Game();
        game.symbol = 'X';

        assertEquals(Game.INF, game.MinMove(board("XXX......"), game.player1));
    }

    @Test
    void maxMoveReturnsTerminalEvaluationImmediately() {
        Game game = new Game();
        game.symbol = 'X';

        assertEquals(-Game.INF, game.MaxMove(board("XXX......"), game.player2));
    }

    @Test
    void maxMoveCanFindWinningMoveForX() {
        Game game = new Game();
        game.symbol = 'X';

        assertEquals(Game.INF, game.MaxMove(board("XX.OOXOXO"), game.player1));
        assertTrue(game.q > 0);
    }

    @Test
    void maxMoveCanFindWinningMoveForO() {
        Game game = new Game();
        game.symbol = 'O';

        assertEquals(Game.INF, game.MaxMove(board("OO.XXOXOX"), game.player2));
        assertTrue(game.q > 0);
    }

    @Test
    void minMoveCanFindOpponentWinningMoveAgainstX() {
        Game game = new Game();
        game.symbol = 'X';

        assertEquals(-Game.INF, game.MinMove(board("OO.XXOXOX"), game.player1));
        assertTrue(game.q > 0);
    }

    @Test
    void minMoveCanFindOpponentWinningMoveAgainstO() {
        Game game = new Game();
        game.symbol = 'O';

        assertEquals(-Game.INF, game.MinMove(board("XX.OOXOXO"), game.player2));
        assertTrue(game.q > 0);
    }

    @Test
    void miniMaxChoosesImmediateWinningMoveForOAndRestoresBoard() {
        Game game = new Game();
        char[] board = board("OO.XX.X..");
        char[] original = board.clone();

        int move = game.MiniMax(board, game.player2);

        assertEquals(3, move);
        assertArrayEquals(original, board);
        assertEquals(0, game.q);
    }

    @Test
    void miniMaxChoosesOnlyAvailableMoveAndRestoresBoard() {
        Game game = new Game();
        char[] board = board("XOXOOXXO.");
        char[] original = board.clone();

        int move = game.MiniMax(board, game.player1);

        assertEquals(9, move);
        assertArrayEquals(original, board);
        assertEquals(0, game.q);
    }

    @Test
    void miniMaxReturnsZeroWhenThereAreNoAvailableMoves() {
        Game game = new Game();

        assertEquals(0, game.MiniMax(board("XOXXOOOXX"), game.player1));
        assertEquals(0, game.q);
    }

    @Test
    void ticTacToeCellStoresCoordinatesNumberAndMarker() {
        TicTacToeCell cell = new TicTacToeCell(5, 2, 1);

        assertEquals(5, cell.getNum());
        assertEquals(1, cell.getRow());
        assertEquals(2, cell.getCol());
        assertEquals(' ', cell.getMarker());
        assertEquals(" ", cell.getText());
        assertTrue(cell.isEnabled());

        cell.setMarker("X");

        assertEquals('X', cell.getMarker());
        assertEquals("X", cell.getText());
        assertFalse(cell.isEnabled());
        assertEquals("Arial", cell.getFont().getName());
    }

    @Test
    void utilityPrintsCharBoard() {
        String output = captureOutput(() -> Utility.print(board("XO.......")));

        assertTrue(output.contains("X-O- - - - - - - -"));
    }

    @Test
    void utilityPrintsIntBoard() {
        int[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9};

        String output = captureOutput(() -> Utility.print(values));

        assertTrue(output.contains("1-2-3-4-5-6-7-8-9-"));
    }

    @Test
    void utilityPrintsMoveList() {
        ArrayList<Integer> moves = new ArrayList<>(List.of(2, 4, 8));

        String output = captureOutput(() -> Utility.print(moves));

        assertTrue(output.contains("2-4-8-"));
    }

    @Test
    void ticTacToePanelConstructorCreatesNineCellsAndInitialGame() throws Exception {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));

        assertEquals(9, panel.getComponentCount());
        TicTacToeCell[] cells = getField(panel, "cells", TicTacToeCell[].class);
        for (int i = 0; i < cells.length; i++) {
            assertSame(cells[i], panel.getComponent(i));
            assertEquals(i, cells[i].getNum());
            assertEquals(' ', cells[i].getMarker());
            assertTrue(Arrays.asList(cells[i].getActionListeners()).contains(panel));
        }

        Game game = getField(panel, "game", Game.class);
        assertSame(game.player1, game.cplayer);
        assertEquals('X', game.cplayer.symbol);
    }

    @Test
    void actionPerformedPlacesHumanAndComputerMovesWithoutFinishingGame() throws Exception {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));
        TicTacToeCell[] cells = getField(panel, "cells", TicTacToeCell[].class);
        Game game = getField(panel, "game", Game.class);

        panel.actionPerformed(new ActionEvent(cells[0], ActionEvent.ACTION_PERFORMED, "click"));

        assertEquals('X', cells[0].getMarker());
        assertEquals(1, countMarkers(cells, 'X'));
        assertEquals(1, countMarkers(cells, 'O'));
        assertEquals(State.PLAYING, game.state);
        assertSame(game.player1, game.cplayer);
    }

    @Test
    void actionPerformedEntersXWinBranch() throws Exception {
        TicTacToePanel panel = panelWithCompletedBoard("XXX......", 'X');
        Game game = getField(panel, "game", Game.class);

        assertThrows(HeadlessException.class,
                () -> panel.actionPerformed(new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, "finish")));

        assertEquals(State.XWIN, game.state);
    }

    @Test
    void actionPerformedEntersOWinBranch() throws Exception {
        TicTacToePanel panel = panelWithCompletedBoard("OOO......", 'O');
        Game game = getField(panel, "game", Game.class);

        assertThrows(HeadlessException.class,
                () -> panel.actionPerformed(new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, "finish")));

        assertEquals(State.OWIN, game.state);
    }

    @Test
    void actionPerformedEntersDrawBranch() throws Exception {
        TicTacToePanel panel = panelWithCompletedBoard("XOXXOOOXX", 'X');
        Game game = getField(panel, "game", Game.class);

        assertThrows(HeadlessException.class,
                () -> panel.actionPerformed(new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, "finish")));

        assertEquals(State.DRAW, game.state);
    }

    @Test
    void programMainAttemptsToOpenFrameInHeadlessMode() {
        assertThrows(HeadlessException.class, () -> Program.main(new String[0]));
    }

    @Test
    void publicStaticWritersCanBeAssigned() {
        Program.fileWriter = null;
        Program.printWriter = null;

        assertDoesNotThrow(() -> {
            Program.fileWriter = null;
            Program.printWriter = new PrintWriter(new ByteArrayOutputStream());
            Program.printWriter.close();
            Program.printWriter = null;
        });
    }

    private static Stream<Arguments> winningBoards() {
        String[] xWins = {
            "XXX......",
            "...XXX...",
            "......XXX",
            "X..X..X..",
            ".X..X..X.",
            "..X..X..X",
            "X...X...X",
            "..X.X.X.."
        };

        Stream<Arguments> xArguments = Arrays.stream(xWins)
                .map(pattern -> Arguments.of(pattern, 'X', State.XWIN));
        Stream<Arguments> oArguments = Arrays.stream(xWins)
                .map(pattern -> pattern.replace('X', 'O'))
                .map(pattern -> Arguments.of(pattern, 'O', State.OWIN));
        return Stream.concat(xArguments, oArguments);
    }

    private static TicTacToePanel panelWithCompletedBoard(String pattern, char symbolForStateCheck) throws Exception {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));
        TicTacToeCell[] cells = getField(panel, "cells", TicTacToeCell[].class);
        Game game = getField(panel, "game", Game.class);
        char[] values = board(pattern);

        for (int i = 0; i < values.length; i++) {
            if (values[i] != ' ') {
                cells[i].setMarker(Character.toString(values[i]));
            }
        }
        game.player1.symbol = symbolForStateCheck;
        game.cplayer = game.player2;
        return panel;
    }

    private static long countMarkers(TicTacToeCell[] cells, char marker) {
        return Arrays.stream(cells)
                .filter(cell -> cell.getMarker() == marker)
                .count();
    }

    private static String captureOutput(Runnable runnable) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        try {
            runnable.run();
        } finally {
            System.setOut(original);
        }
        return output.toString();
    }

    private static char[] board(String pattern) {
        assertEquals(9, pattern.length());
        char[] board = new char[9];
        for (int i = 0; i < pattern.length(); i++) {
            board[i] = pattern.charAt(i) == '.' ? ' ' : pattern.charAt(i);
        }
        return board;
    }

    private static <T> T getField(Object target, String fieldName, Class<T> fieldType) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return fieldType.cast(field.get(target));
    }
}
