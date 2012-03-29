package mobisocial.tictactoe;

import java.util.List;

import mobisocial.socialkit.Obj;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.musubi.multiplayer.TurnBasedApp;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class TicTacToeKickoffActivity extends Activity {
    final String type = "tictactoe";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Musubi m = Musubi.forIntent(this, getIntent());
        JSONObject initialState = TicTacToeActivity.getInitialState();
        List<DbIdentity> members = m.getFeed().getMembers();
        if (members.size() < 2) {
            Toast.makeText(this, "Not enough players for tic-tac-toe", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<DbIdentity> players = members.subList(0, 2);        
        Obj game = TurnBasedApp.newInstance(type, players, initialState);
        Uri objUri = m.getFeed().insert(game);
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setDataAndType(objUri, "vnd.musubi.obj/" + type);
        startActivity(view);
        finish();
    }
}
