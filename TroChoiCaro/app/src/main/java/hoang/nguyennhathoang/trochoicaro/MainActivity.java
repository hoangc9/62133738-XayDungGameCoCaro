package hoang.nguyennhathoang.trochoicaro;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    private TextView tvTitle, tvScore, tvStatus;
    private Button btnNewGame, btnToggleMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView       = findViewById(R.id.gameView);
        tvTitle        = findViewById(R.id.tvTitle);
        tvScore        = findViewById(R.id.tvScore);
        tvStatus       = findViewById(R.id.tvStatus);
        btnNewGame     = findViewById(R.id.btnNewGame);
        btnToggleMode  = findViewById(R.id.btnToggleMode);

        gameView.setStatusCallback(s -> runOnUiThread(() -> tvStatus.setText(s)));
        gameView.setScoreCallback (s -> runOnUiThread(() -> tvScore.setText(s)));

        btnNewGame.setOnClickListener(v -> gameView.newGame());

        btnToggleMode.setOnClickListener(v -> {
            gameView.toggleMode();
            updateModeBtn();
        });
    }

    private void updateModeBtn() {
        if (gameView.isAiMode()) {
            btnToggleMode.setText("🤖  vs Máy");
        } else {
            btnToggleMode.setText("👥  2 Người");
        }
    }
}