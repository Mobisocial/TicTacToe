package mobisocial.tictactoe;

import java.util.ArrayList;
import java.util.List;

import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.DbUser;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.musubi.multiplayer.FeedRenderable;
import mobisocial.socialkit.musubi.multiplayer.TurnBasedMultiplayer;

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
import android.widget.Toast;

public class TicTacToeActivity extends Activity {
    private static final String TAG = "ttt";

    private String mToken;
    private Musubi mMusubi;
    private TTTMultiplayer mMultiplayer;

    private Button mTokenButton;
    private final List<Button> mmSquares = new ArrayList<Button>();

    private final String BLANK = "  ";

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
        mMusubi = Musubi.getInstance(this);
        mMultiplayer = new TTTMultiplayer(mMusubi.getObj());

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
            mmSquares.get(i).setTag(R.id.s0, i);
        }
        ((Button)findViewById(R.id.clear)).setOnClickListener(mClearAll);

        mToken = (mMultiplayer.getLocalMemberIndex() == 0) ? "X" : "O";
        render(mMultiplayer.getLatestState());
    }

    /**
     * Extracts the board state from the given object
     * and renders it to screen.
     */
    private void render(JSONObject state) {
        mTokenButton.setText(mToken);
        String status;
        DbUser user = mMultiplayer.getUser(mMultiplayer.getGlobalMemberCursor());
        if (mMultiplayer.isMyTurn()) {
            status = "Your turn.";
        } else {
            status = user.getName()
                    + "'s turn.";
        }
        ((TextView)findViewById(R.id.status)).setText(status);
        ((ImageView)findViewById(R.id.image)).setImageBitmap(user.getPicture());

        JSONArray s = state.optJSONArray("s");
        for (int i = 0; i < 9; i++) {
            mmSquares.get(i).setText(s.optString(i));
        }            
    }

    /**
     * Computes the local view of the application state,
     * which is stored within the UI itself.
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

    private JSONObject getEmptyState() {
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
    }

    private View.OnClickListener mBoardClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // This call isn't necessary, since takeTurn() checks internally.
            // But it seems like a good thing to do.
            if (!mMultiplayer.isMyTurn()) {
                return;
            }

            Button square = mmSquares.get((Integer)v.getTag(R.id.s0));
            if (!square.getText().equals(BLANK)) {
                return;
            }

            square.setText(mToken);
            mMultiplayer.takeTurn(getState());
        }
    };


    public void clearBoard() {
        for (int i = 0; i < 9; i++) {
            mmSquares.get(i).setText(BLANK);
        }
    }

    private View.OnClickListener mClearAll = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            mMultiplayer.takeTurnOutOfOrder(mMultiplayer.membersJsonArray(), 0, getEmptyState());
        }
    };

    private class TTTMultiplayer extends TurnBasedMultiplayer {
        public TTTMultiplayer(DbObj objContext) {
            super(objContext);
        }

        @Override
        protected JSONObject getInitialState() {
            JSONObject wrapper = new JSONObject();
            JSONArray spaces = new JSONArray();
            for (int i = 0; i < 9; i++) {
                spaces.put(BLANK);
            }
            try {
                wrapper.put("s", spaces);
            } catch (JSONException e) {}
            return wrapper;
        };

        @Override
        protected void onStateUpdate(JSONObject state) {
            render(state);
        }

        @Override
        protected FeedRenderable getFeedView(JSONObject state) {
            try {
            JSONArray squares = state.getJSONArray("s");
            StringBuilder html = new StringBuilder("<html><head><style>");
            html.append("td { border:1px solid black; min-width:18px; }");
            html.append("table { background-color:#FC6; padding:8px;}");
            html.append("</style></head>");
            html.append("<body><div><table><tr>");
            html.append("<td>&nbsp;").append(squares.get(0)).append("</td>");
            html.append("<td>&nbsp;").append(squares.get(1)).append("</td>");
            html.append("<td>&nbsp;").append(squares.get(2)).append("</td>");
            html.append("</tr><tr>");
            html.append("<td>&nbsp;").append(squares.get(3)).append("</td>");
            html.append("<td>&nbsp;").append(squares.get(4)).append("</td>");
            html.append("<td>&nbsp;").append(squares.get(5)).append("</td>");
            html.append("</tr><tr>");
            html.append("<td>&nbsp;").append(squares.get(6)).append("</td>");
            html.append("<td>&nbsp;").append(squares.get(7)).append("</td>");
            html.append("<td>&nbsp;").append(squares.get(8)).append("</td>");
            html.append("</tr></table></body></div>");
            html.append("</html>");
            return FeedRenderable.fromHtml(html.toString());
            } catch (JSONException e) {
                Log.e(TAG, "Error getting renderable state");
                return FeedRenderable.fromText("[TicTacToe rendering error]");
            }
        }
    }
}