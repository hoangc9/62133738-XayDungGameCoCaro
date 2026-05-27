package hoang.nguyennhathoang.trochoicaro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View {
    // ban co 15x15
    private static final int BOARD_SIZE = 15;
    private int[][] board;
    private int currentPlayer = 1; // khai bao nguoi choi
    private boolean gameOver = false;
    private boolean aiMode = false; // che do choi
    private int[] scores = {0, 0}; // diem so
    private int[] lastMove = null;
    private int[][] winLine = null;

    private Paint boardPaint, linePaint, starPaint; // ve ban co
    private Paint shadowPaint, highlightPaint, winLinePaint; // ve cac diem dac biet
    private Paint coordPaint, borderPaint; // ve ria ban co

    private float cellSize;
    private float offsetX, offsetY;
    // khai bao trang thai tro choi: luot choi, ket qua
    public interface StatusCallback { void onStatus(String s); }
    // khai bao diem so
    public interface ScoreCallback  { void onScore(String s);  }

    private StatusCallback statusCallback;
    private ScoreCallback  scoreCallback;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        // khoi tao ban co
        board = new int[BOARD_SIZE][BOARD_SIZE];
        // nen ban co
        boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // vien o co
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#7A4A1E"));
        linePaint.setStrokeWidth(1.5f);
        linePaint.setStyle(Paint.Style.STROKE);
        // diem quan co
        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setColor(Color.parseColor("#5C3010"));
        starPaint.setStyle(Paint.Style.FILL);
        // bong quan co
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.parseColor("#55000000"));
        shadowPaint.setStyle(Paint.Style.FILL);
        // highlight nuoc di cuoi cung
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.parseColor("#FFDD00"));
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(3.5f);
        // duong chien thang
        winLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        winLinePaint.setColor(Color.parseColor("#FF3333"));
        winLinePaint.setStyle(Paint.Style.STROKE);
        winLinePaint.setStrokeWidth(6f);
        winLinePaint.setStrokeCap(Paint.Cap.ROUND);
        // toa do ria ban co
        coordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        coordPaint.setColor(Color.parseColor("#8B5E3C"));
        coordPaint.setTextAlign(Paint.Align.CENTER);
        // vien ngoai ban co
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#7A4A1E"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = Math.min(w, h) * 0.065f;
        cellSize  = (Math.min(w, h) - 2f * padding) / (BOARD_SIZE - 1);
        // canh giua ban co
        offsetX   = (w - cellSize * (BOARD_SIZE - 1)) / 2f;
        offsetY   = (h - cellSize * (BOARD_SIZE - 1)) / 2f;
        // chieu rong o co
        coordPaint.setTextSize(cellSize * 0.28f);
    }
    // khai bao ham ve ban co
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas); // nen ban co
        drawGrid(canvas); // vien o co
        drawStarPoints(canvas); // diem sao
        drawCoords(canvas); // toa do
        drawStones(canvas); // quan co
        // neu chien thang, sẽ vẽ đường thang len o co chien thang
        if (winLine != null) drawWinLine(canvas);
    }
    // khai bao ham ve nen ban co
    private void drawBoard(Canvas canvas) {
        float pad = cellSize * 0.65f;

        RectF rect = new RectF(
                offsetX - pad,
                offsetY - pad,
                offsetX + (BOARD_SIZE - 1) * cellSize + pad,
                offsetY + (BOARD_SIZE - 1) * cellSize + pad
        );
        Paint outerShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        outerShadow.setColor(Color.parseColor("#66000000"));
        canvas.drawRoundRect(
                new RectF(
                        rect.left + 8,
                        rect.top + 8,
                        rect.right + 8,
                        rect.bottom + 8
                ),
                18,
                18,
                outerShadow
        );
        LinearGradient grad = new LinearGradient(
                rect.left,
                rect.top,
                rect.right,
                rect.bottom,
                Color.parseColor("#DFA05A"),
                Color.parseColor("#B8692E"),
                Shader.TileMode.CLAMP
        );
        boardPaint.setShader(grad);
        canvas.drawRoundRect(rect, 18, 18, boardPaint);
        boardPaint.setShader(null);
        // vien ngoai ban co
        canvas.drawRoundRect(rect, 18, 18, borderPaint);
    }
    // goi ham ve luoi ban co
    private void drawGrid(Canvas canvas) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            float x = offsetX + i * cellSize; // toa do cot i
            float y = offsetY + i * cellSize; // toa do hang i
            // duong ke ngang
            canvas.drawLine(
                    offsetX,
                    y,
                    offsetX + (BOARD_SIZE - 1) * cellSize,
                    y,
                    linePaint
            );
            // duong ke doc
            canvas.drawLine(
                    x,
                    offsetY,
                    x,
                    offsetY + (BOARD_SIZE - 1) * cellSize,
                    linePaint
            );
        }
    }
    private void drawStarPoints(Canvas canvas) {
        int[] pts = {3, 7, 11};

        for (int r : pts)
            for (int c : pts)
                canvas.drawCircle(
                        offsetX + c * cellSize,
                        offsetY + r * cellSize,
                        5.5f,
                        starPaint
                );
    }
    // goi ham ve ki tu toa do ban co
    private void drawCoords(Canvas canvas) {
        // ki tu theo cot
        String cols = "ABCDEFGHJKLMNOP";
        // can doc
        float textH = coordPaint.getTextSize();
        for (int i = 0; i < BOARD_SIZE; i++) {
            float x = offsetX + i * cellSize; // toa do x cot i
            float y = offsetY + i * cellSize; // toa do y hang i
            canvas.drawText(
                    String.valueOf(cols.charAt(i)),
                    x,
                    offsetY - cellSize * 0.38f, // ki tu cach 0,38 o so voi hang
                    coordPaint
            );
            canvas.drawText(
                    String.valueOf(BOARD_SIZE - i),
                    offsetX - cellSize * 0.48f, // ki tu lui sang trai 0,48 o
                    y + textH * 0.35f, // can giua
                    coordPaint
            );
        }
    }
    // ham duyet ban co
    private void drawStones(Canvas canvas) {
        for (int r = 0; r < BOARD_SIZE; r++)
            for (int c = 0; c < BOARD_SIZE; c++)
                if (board[r][c] != 0)
                    drawStone(canvas, r, c, board[r][c]);
    }
    // ham ve quan co
    private void drawStone(Canvas canvas, int row, int col, int player) {
        float cx = offsetX + col * cellSize;
        float cy = offsetY + row * cellSize;
        float radius = cellSize * 0.44f;
        canvas.drawCircle(
                cx + radius * 0.13f,
                cy + radius * 0.18f,
                radius,
                shadowPaint
        );
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        if (player == 1) {
            RadialGradient g = new RadialGradient(
                    cx - radius * 0.3f,
                    cy - radius * 0.3f,
                    radius * 1.3f,
                    Color.parseColor("#666666"),
                    Color.parseColor("#0D0D0D"),
                    Shader.TileMode.CLAMP
            );
            paint.setShader(g);
        } else {
            RadialGradient g = new RadialGradient(
                    cx - radius * 0.3f,
                    cy - radius * 0.35f,
                    radius * 1.3f,
                    Color.WHITE,
                    Color.parseColor("#C0C0C0"),
                    Shader.TileMode.CLAMP
            );
            paint.setShader(g);
        }
        canvas.drawCircle(cx, cy, radius, paint);
        if (player == 2) {
            Paint outline = new Paint(Paint.ANTI_ALIAS_FLAG);
            outline.setColor(Color.parseColor("#AAAAAA"));
            outline.setStyle(Paint.Style.STROKE);
            outline.setStrokeWidth(1.2f);
            canvas.drawCircle(cx, cy, radius, outline);
        }
        Paint shine = new Paint(Paint.ANTI_ALIAS_FLAG);
        shine.setStyle(Paint.Style.FILL);
        shine.setColor(
                Color.argb(player == 1 ? 55 : 170, 255, 255, 255)
        );
        canvas.drawCircle(
                cx - radius * 0.28f,
                cy - radius * 0.28f,
                radius * 0.28f,
                shine
        );
        if (lastMove != null &&
                lastMove[0] == row &&
                lastMove[1] == col) {
            canvas.drawCircle(
                    cx,
                    cy,
                    radius * 0.36f,
                    highlightPaint
            );
        }
    }
    // ham ve duong chien thang
    private void drawWinLine(Canvas canvas) {
        if (winLine == null || winLine.length < 2) return; // kiem tra du lieu thang
        // tinh toa doa diem dau
        float x1 = offsetX + winLine[0][1] * cellSize;
        float y1 = offsetY + winLine[0][0] * cellSize;
        // tinh toa do diem cuoi
        float x2 = offsetX + winLine[winLine.length - 1][1] * cellSize;
        float y2 = offsetY + winLine[winLine.length - 1][0] * cellSize;
        // ve duong chien thang
        canvas.drawLine(x1, y1, x2, y2, winLinePaint);
    }
    @Override
    // ham xu ly cham man hinh
    public boolean onTouchEvent(MotionEvent event) {
        // lay hanh dong cham
        if (event.getAction() != MotionEvent.ACTION_UP) return true; // nhac tay ra khoi man hinh
        // kiem tra game da ket thuc chua, neu ket thuc thi khong cho danh nua
        if (gameOver) return true;
        // kiem tra co dang choi voi may khong
        if (aiMode && currentPlayer == 2) return true;
        int col = Math.round((event.getX() - offsetX) / cellSize);
        int row = Math.round((event.getY() - offsetY) / cellSize);
        if (row < 0 || row >= BOARD_SIZE ||
                col < 0 || col >= BOARD_SIZE) return true;
        if (board[row][col] != 0) return true;
        placeStone(row, col);
        if (aiMode && !gameOver) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                int[] move = aiMove();
                // goi ham AI
                if (move != null)
                    placeStone(move[0], move[1]);

            }, 280);
        }
        return true;
    }
    // ham dat quan co len ban co
    private void placeStone(int row, int col) {
        // gan quan co vao ban co
        board[row][col] = currentPlayer;
        // luu nuoc di cuoi cung
        lastMove = new int[]{row, col};
        // goi ham ham kiem tra win
        int[][] wl = checkWinLine(row, col);
        if (wl != null) {
            winLine = wl;
            gameOver = true;
            scores[currentPlayer - 1]++;
            String winner =
                    (currentPlayer == 1)
                            ? "⚫ Đen"
                            : "⚪ Trắng";
            // hien thi nguoi chien thang
            emitStatus(winner + " thắng! 🎉");
            emitScore(
                    "⚫ " + scores[0] +
                            "  —  " +
                            scores[1] + " ⚪"
            );
        // neu khong thang thi kiem tra hoa
        } else if (isBoardFull()) {
            gameOver = true;
            emitStatus("Hòa! 🤝");
        } else {
            currentPlayer = 3 - currentPlayer;
            String next =
                    (currentPlayer == 1)
                            ? "⚫ Đen"
                            : "⚪ Trắng";
            emitStatus("Lượt: " + next);
        }
        invalidate();
    }
    // ham kiem tra nuoc di cuoi co chien thanh hay khong
    private int[][] checkWinLine(int row, int col) {

        int player = board[row][col];

        int[][] dirs = {
                {0, 1},
                {1, 0},
                {1, 1},
                {1, -1}
        };

        for (int[] d : dirs) {

            List<int[]> line = new ArrayList<>();

            line.add(new int[]{row, col});

            for (int sign : new int[]{1, -1}) {

                for (int i = 1; i <= 4; i++) {

                    int r = row + sign * d[0] * i;
                    int c = col + sign * d[1] * i;

                    if (r < 0 || r >= BOARD_SIZE ||
                            c < 0 || c >= BOARD_SIZE) break;

                    if (board[r][c] != player) break;

                    if (sign == 1)
                        line.add(new int[]{r, c});
                    else
                        line.add(0, new int[]{r, c});
                }
            }

            if (line.size() >= 5)
                return line.subList(0, 5)
                        .toArray(new int[0][]);
        }

        return null;
    }

    private boolean checkWin(int r, int c) {
        return checkWinLine(r, c) != null;
    }

    private boolean isBoardFull() {

        for (int r = 0; r < BOARD_SIZE; r++)
            for (int c = 0; c < BOARD_SIZE; c++)
                if (board[r][c] == 0)
                    return false;

        return true;
    }

    private int[] aiMove() {

        for (int r = 0; r < BOARD_SIZE; r++)
            for (int c = 0; c < BOARD_SIZE; c++)
                if (board[r][c] == 0) {

                    board[r][c] = 2;

                    boolean w = checkWin(r, c);

                    board[r][c] = 0;

                    if (w)
                        return new int[]{r, c};
                }

        for (int r = 0; r < BOARD_SIZE; r++)
            for (int c = 0; c < BOARD_SIZE; c++)
                if (board[r][c] == 0) {

                    board[r][c] = 1;

                    boolean w = checkWin(r, c);

                    board[r][c] = 0;

                    if (w)
                        return new int[]{r, c};
                }

        int best = -1;

        int[] bestMove = null;

        for (int r = 0; r < BOARD_SIZE; r++)
            for (int c = 0; c < BOARD_SIZE; c++)
                if (board[r][c] == 0) {

                    int score =
                            scoreCell(r, c, 2) * 2
                                    + scoreCell(r, c, 1)
                                    + Math.max(
                                    0,
                                    10 - Math.abs(r - 7) - Math.abs(c - 7)
                            );

                    if (score > best) {

                        best = score;

                        bestMove = new int[]{r, c};
                    }
                }

        return bestMove;
    }

    private int scoreCell(int row, int col, int player) {

        board[row][col] = player;

        int score = 0;

        int[][] dirs = {
                {0, 1},
                {1, 0},
                {1, 1},
                {1, -1}
        };

        for (int[] d : dirs) {

            int cnt = 1;

            for (int s : new int[]{1, -1})

                for (int i = 1; i <= 4; i++) {

                    int r = row + s * d[0] * i;
                    int c = col + s * d[1] * i;

                    if (r < 0 || r >= BOARD_SIZE ||
                            c < 0 || c >= BOARD_SIZE) break;

                    if (board[r][c] != player) break;

                    cnt++;
                }

            score += cnt * cnt;
        }

        board[row][col] = 0;

        return score;
    }

    public void newGame() {

        board = new int[BOARD_SIZE][BOARD_SIZE];

        currentPlayer = 1;

        gameOver = false;

        lastMove = null;

        winLine = null;

        emitStatus("Lượt: ⚫ Đen");

        invalidate();
    }

    public void toggleMode() {

        aiMode = !aiMode;

        newGame();
    }

    public boolean isAiMode() {
        return aiMode;
    }

    public void setStatusCallback(StatusCallback cb) {

        statusCallback = cb;

        cb.onStatus("Lượt: ⚫ Đen");
    }

    public void setScoreCallback(ScoreCallback cb) {

        scoreCallback = cb;

        cb.onScore("⚫ 0  —  0 ⚪");
    }

    private void emitStatus(String s) {

        if (statusCallback != null)
            statusCallback.onStatus(s);
    }

    private void emitScore(String s) {

        if (scoreCallback != null)
            scoreCallback.onScore(s);
    }
}