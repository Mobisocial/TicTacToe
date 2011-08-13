package mobisocial.tictactoe;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mobisocial.socialkit.SocialKit.Dungbeetle;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TicTacToeActivity extends Activity {
    private static final String TAG = "ttt";

    Dungbeetle mDungBeetle;
    private Board mBoard;
    private Dungbeetle.User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // All app code is in Board.
        mBoard = new Board();

        
        
        // Other demos
        if (Dungbeetle.isDungbeetleIntent(getIntent())) {
            mDungBeetle = Dungbeetle.getInstance(getIntent());
            mDungBeetle.setLaunchModes(Dungbeetle.LAUNCH_TWO_PLAYERS);

            mDungBeetle.getThread().getMembers(); // List of all known people
            mDungBeetle.getThread().getJunction(); // Message-passing without persistence.

            // sendMessage(...);
            // synState(...);
        }
    }

    class Board implements View.OnClickListener {
        List<Button> mmSquares;

        public Board() {
            mmSquares.add((Button)findViewById(R.id.s0));
            mmSquares.add((Button)findViewById(R.id.s2));
            mmSquares.add((Button)findViewById(R.id.s3));
            mmSquares.add((Button)findViewById(R.id.s4));
            mmSquares.add((Button)findViewById(R.id.s5));
            mmSquares.add((Button)findViewById(R.id.s6));
            mmSquares.add((Button)findViewById(R.id.s7));
            mmSquares.add((Button)findViewById(R.id.s8));
            for (int i = 0; i < 9; i++) {
                mmSquares.get(i).setOnClickListener(this);
            }

            parseDb(mDungBeetle.getThread().getApplicationState());
        }

        private void parseDb(JSONObject state) {
            mmSquares = new ArrayList<Button>();
            if (!state.has("s")) {
                return; // empty board initialized.
            }

            JSONArray s = state.optJSONArray("s");
            for (int i = 0; i < 9; i++) {
                mmSquares.get(i).setText(s.optString(i));
            }
        }

        private JSONObject getApplicationState() {
            JSONObject o = new JSONObject();
            JSONArray s = new JSONArray();
            try {
                // TODO: load state to s
                o.put("s", s);
            } catch (JSONException e) {
                Log.wtf(TAG, "Failed to get board state", e);
            }
            return o;
        }

        @Override
        public void onClick(View v) {
            mmSquares.get(0).setText(mUser.getAttribute("token"));
            v.getTag(R.id.s0);
            mDungBeetle.getThread().setApplicationState(getApplicationState());
        }
    }

    
}
