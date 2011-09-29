package mobisocial.tictactoe;

import java.util.ArrayList;
import java.util.List;

import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.musubi.Musubi.Multiplayer;
import mobisocial.socialkit.musubi.Musubi.StateObserver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TicTacToeActivity extends Activity {
    private static final String TAG = "ttt";

    Musubi mMusubi;
    private Board mBoard;
    private String mToken;
    private Button mTokenButton;
    private Multiplayer mMultiplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mTokenButton = (Button)findViewById(R.id.token);

        if (!Musubi.isMusubiIntent(getIntent())) {
            Toast.makeText(this, "Please launch with 2-players!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // All app code is in Board.
        mMusubi = Musubi.getInstance(this, getIntent());
        mMultiplayer = new Multiplayer(this, getIntent());
        mMultiplayer.setStateObserver(mStateObserver);

        mToken = (mMultiplayer.getLocalMemberIndex() == 0) ? "X" : "O";
        mBoard = new Board();
        
        mBoard.render(mMusubi.getFeed().getLatestState());
    }

    class Board implements View.OnClickListener {
        private final List<Button> mmSquares = new ArrayList<Button>();

        public Board() {
            // TODO: It is more efficient to bind each individual
            // view object to the SocialKit. Can just give root view?
            mmSquares.add((Button)findViewById(R.id.s0));
            mmSquares.add((Button)findViewById(R.id.s1));
            mmSquares.add((Button)findViewById(R.id.s2));
            mmSquares.add((Button)findViewById(R.id.s3));
            mmSquares.add((Button)findViewById(R.id.s4));
            mmSquares.add((Button)findViewById(R.id.s5));
            mmSquares.add((Button)findViewById(R.id.s6));
            mmSquares.add((Button)findViewById(R.id.s7));
            mmSquares.add((Button)findViewById(R.id.s8));
            for (int i = 0; i < 9; i++) {
                mmSquares.get(i).setOnClickListener(this);
                mmSquares.get(i).setTag(R.id.s0, i);
            }
            ((Button)findViewById(R.id.clear)).setOnClickListener(mClearAll);
        }

        private synchronized void render(JSONObject state) {
            if (state == null || !state.has("s")) {
                return; // empty board initialized.
            }

            mTokenButton.setText(mToken);
            JSONArray s = state.optJSONArray("s");
            for (int i = 0; i < 9; i++) {
                mmSquares.get(i).setText(s.optString(i));
            }
        }

        private JSONObject getApplicationState() {
            JSONObject o = new JSONObject();
            JSONArray s = new JSONArray();
            try {
                for (Button b : mmSquares) {
                    s.put(b.getText());
                }
                o.put("s", s);
            } catch (JSONException e) {
                Log.wtf(TAG, "Failed to get board state", e);
            }
            return o;
        }

        @Override
        public void onClick(View v) {
            if (!mMultiplayer.isMyTurn()) {
                return;
            }

            mmSquares.get((Integer)v.getTag(R.id.s0)).setText(mToken);
            pushUpdate();
        }

        public void onClickClear() {
            for (int i = 0; i < 9; i++) {
                mmSquares.get(i).setText("  ");
            }
            pushUpdate();
        }

        public void pushUpdate() {
            //mDungBeetle.getFeed().setApplicationState(getApplicationState(), getSnapshotText());
            mMultiplayer.takeTurn(getApplicationState(), getSnapshotHtml());
        }

        public String getSnapshotText() {
            StringBuilder snapshot = new StringBuilder();
            snapshot.append(" " + mmSquares.get(0).getText() +
                    " | " + mmSquares.get(1).getText() + " | " + mmSquares.get(2).getText());
            snapshot.append("\n------------\n");
            snapshot.append(" " + mmSquares.get(3).getText() +
                    " | " + mmSquares.get(4).getText() + " | " + mmSquares.get(5).getText());
            snapshot.append("\n------------\n");
            snapshot.append(" " + mmSquares.get(6).getText() +
                    " | " + mmSquares.get(7).getText() + " | " + mmSquares.get(8).getText());
            return snapshot.toString();
        }

        public String getSnapshotHtml() {
            StringBuilder html = new StringBuilder("<html><head><style>");
            html.append("td { border:1px solid black; min-width:18px; }");
            html.append("table { background-color:#FC6; padding:8px;}");
            html.append("</style></head>");
            html.append("<body><div><table><tr>");
            html.append("<td>&nbsp;").append(mmSquares.get(0).getText()).append("</td>");
            html.append("<td>&nbsp;").append(mmSquares.get(1).getText()).append("</td>");
            html.append("<td>&nbsp;").append(mmSquares.get(2).getText()).append("</td>");
            html.append("</tr><tr>");
            html.append("<td>&nbsp;").append(mmSquares.get(3).getText()).append("</td>");
            html.append("<td>&nbsp;").append(mmSquares.get(4).getText()).append("</td>");
            html.append("<td>&nbsp;").append(mmSquares.get(5).getText()).append("</td>");
            html.append("</tr><tr>");
            html.append("<td>&nbsp;").append(mmSquares.get(6).getText()).append("</td>");
            html.append("<td>&nbsp;").append(mmSquares.get(7).getText()).append("</td>");
            html.append("<td>&nbsp;").append(mmSquares.get(8).getText()).append("</td>");
            html.append("</tr></table></body></div></html>");
            return html.toString();
        }
    }

    private StateObserver mStateObserver = new StateObserver() {
        @Override
        public void onUpdate(JSONObject state) {
            mBoard.render(state);
        }
    };

    @SuppressWarnings("unused")
    private void toast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TicTacToeActivity.this, text, 500).show();
            }
        });
    }

    private View.OnClickListener mClearAll = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            mBoard.onClickClear();
        }
    };
}