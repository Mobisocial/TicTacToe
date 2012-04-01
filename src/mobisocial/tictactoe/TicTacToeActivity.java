package mobisocial.tictactoe;

import java.util.ArrayList;
import java.util.List;

import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.musubi.multiplayer.FeedRenderable;
import mobisocial.socialkit.musubi.multiplayer.TurnBasedApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TicTacToeActivity extends Activity {
    private static final String TAG = "ttt";

    private TTTMultiplayer mMultiplayer;
    private Button mTokenButton;
    private final List<Button> mmSquares = new ArrayList<Button>();

    private static final String BLANK = "  ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mTokenButton = (Button)findViewById(R.id.token);

        // Set up the game's backend:
        Musubi musubi = new Musubi(this);
        mMultiplayer = new TTTMultiplayer(musubi, musubi.objFromIntent(getIntent()));

        // Bind UI to actions:
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
            mmSquares.get(i).setOnClickListener(mBoardClickedListener);
            mmSquares.get(i).setTag(i);
        }
        findViewById(R.id.clear).setOnClickListener(mClearAll);

        // Display the game's current state:
        render(mMultiplayer.getLatestState());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMultiplayer.enableStateUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMultiplayer.disableStateUpdates();
    }

    /**
     * Extracts the board state from the given json object and
     * renders it to screen.
     */
    private void render(JSONObject state) {
        mTokenButton.setText(mMultiplayer.getPlayerToken());
        String status;
        DbIdentity user = mMultiplayer.getUser(mMultiplayer.getGlobalMemberCursor());
        if (mMultiplayer.isMyTurn()) {
            status = "Your turn.";
        } else {
            status = user.getName() + "'s turn.";
        }

        ((TextView)findViewById(R.id.status)).setText(status);
        ((ImageView)findViewById(R.id.image)).setImageBitmap(user.getPicture());

        // The game state is completely stored in the UI as the labels of our buttons:
        JSONArray s = state.optJSONArray("s");
        for (int i = 0; i < 9; i++) {
            mmSquares.get(i).setText(s.optString(i));
        }            
    }

    /**
     * Computes the local view of the application state
     * and returns it as a JSON object.
     */
    private JSONObject getState() {
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

    private View.OnClickListener mBoardClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // This call isn't necessary, since takeTurn() checks internally. But it doesn't hurt.
            if (!mMultiplayer.isMyTurn()) {
                return;
            }

            Button square = mmSquares.get((Integer)v.getTag());
            if (!square.getText().equals(BLANK)) {
                return;
            }
            square.setText(mMultiplayer.getPlayerToken());
            mMultiplayer.takeTurn(getState());
        }
    };

    private View.OnClickListener mClearAll = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            mMultiplayer.takeTurnOutOfOrder(
                    mMultiplayer.membersJsonArray(), 0, getInitialState());
        }
    };

    private class TTTMultiplayer extends TurnBasedApp {
        private final String mToken;

        public TTTMultiplayer(Musubi musubi, DbObj objContext) {
            super(musubi, objContext);
            // First player is X, second is O. getLocalMemberIndex() returns
            // the player corresponding to this device:
            mToken = (getLocalMemberIndex() == 0) ? "X" : "O";

        }

        public String getPlayerToken() {
            return mToken;
        }

        @Override
        protected void onStateUpdate(JSONObject state) {
            render(state);
        }

        @Override
        protected FeedRenderable getFeedView(JSONObject state) {
            try {
                JSONArray squares = state.getJSONArray("s");
                StringBuilder html = new StringBuilder("<html><head><style>")
                    .append("td { min-width:18px; }")
                    .append("table { padding:8px; border-collapse: collapse;}")
                    .append(".left { border-right:1px solid black; }")
                    .append(".right { border-left:1px solid black; }")
                    .append(".top { border-bottom:1px solid black; }")
                    .append(".bottom { border-top:1px solid black; }")
                    .append("</style></head>")
                    .append("<body><div><table><tr>")
                        .append("<td class=\"left top\">&nbsp;")
                        .append(squares.getString(0)).append("</td>")
                        .append("<td class=\"top\">&nbsp;")
                        .append(squares.getString(1)).append("</td>")
                        .append("<td class=\"right top\">&nbsp;")
                        .append(squares.getString(2)).append("</td>")
                    .append("</tr><tr>")
                        .append("<td class=\"left\">&nbsp;")
                        .append(squares.getString(3)).append("</td>")
                        .append("<td class=\"\">&nbsp;")
                        .append(squares.getString(4)).append("</td>")
                        .append("<td class=\"right\">&nbsp;")
                        .append(squares.getString(5)).append("</td>")
                    .append("</tr><tr>")
                        .append("<td class=\"left bottom\">&nbsp;")
                        .append(squares.getString(6)).append("</td>")
                        .append("<td class=\"bottom\">&nbsp;")
                        .append(squares.getString(7)).append("</td>")
                        .append("<td class=\"right bottom\">&nbsp;")
                        .append(squares.getString(8)).append("</td>")
                    .append("</tr></table></body></div>")
                    .append("</html>");
                return FeedRenderable.fromHtml(html.toString());
            } catch (JSONException e) {
                Log.wtf(TAG, "Error getting renderable state");
                return FeedRenderable.fromText("[TicTacToe rendering error]");
            }
        }
    }

    public static JSONObject getInitialState() {
        JSONObject o = new JSONObject();
        JSONArray s = new JSONArray();
        try {
            for (int i = 0; i < 9; i++) {
                s.put(BLANK);
            }
            o.put("s", s);
        } catch (JSONException e) {
            Log.wtf(TAG, "Failed to get board state", e);
        }
        return o;
    };
}