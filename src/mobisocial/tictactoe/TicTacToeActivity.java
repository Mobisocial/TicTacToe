package mobisocial.tictactoe;

import java.util.ArrayList;
import java.util.List;

import mobisocial.socialkit.Obj;
import mobisocial.socialkit.User;
import mobisocial.socialkit.musubi.FeedObserver;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.musubi.multiplayer.FeedRenderable;
import mobisocial.socialkit.musubi.multiplayer.TurnBasedMultiplayer;
import mobisocial.socialkit.musubi.multiplayer.TurnBasedMultiplayer.StateObserver;

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
    private TurnBasedMultiplayer mMultiplayer;

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
        mMultiplayer = new TurnBasedMultiplayer(mMusubi, getIntent());
        mMultiplayer.setStateObserver(mStateObserver);

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
        User user = mMultiplayer.getUser(mMultiplayer.getGlobalMemberCursor());
        if (mMultiplayer.isMyTurn()) {
            status = "Your turn.";
        } else {
            status = user.getName()
                    + "'s turn.";
        }
        ((TextView)findViewById(R.id.status)).setText(status);
        ((ImageView)findViewById(R.id.image)).setImageBitmap(user.getPicture());

        if (state == null || !state.has("s")) {
            clearBoard();
            return; // empty board initialized.
        }

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
            mMultiplayer.takeTurn(getState(), getBoardRendering());
        }
    };


    public void clearBoard() {
        for (int i = 0; i < 9; i++) {
            mmSquares.get(i).setText(BLANK);
        }
    }

    public FeedRenderable getBoardRendering() {
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
        html.append("</tr></table></body></div>");
        //html.append("<p>" + status + "</p>");
        html.append("</html>");
        return FeedRenderable.fromHtml(html.toString());
    }

    private StateObserver mStateObserver = new StateObserver() {
        @Override
        public void onUpdate(JSONObject obj) {
            Log.d(TAG, "TTT GOT STATE " + obj);
            render(obj);
        }
    };

    private View.OnClickListener mClearAll = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            if (mMultiplayer.isMyTurn()) {
                clearBoard();
                mMultiplayer.takeTurn(getState(), getBoardRendering());
            }
        }
    };
}