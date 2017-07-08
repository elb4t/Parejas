package es.elb4t.parejas;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;

import java.util.ArrayList;
import java.util.Random;

import static es.elb4t.parejas.Partida.mGoogleApiClient;

/**
 * Created by eloy on 6/7/17.
 */

public class Menu extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnInvitationReceivedListener {
    private Button btnJugar;
    private static final int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;
    private com.google.android.gms.common.SignInButton btnConectar;
    private Button btnDesconectar;
    private Button btnPartidasGuardadas;
    private Button btnPartidaEnTiempoReal;
    private Button btnPartidaPorTurnos;
    String mIncomingInvitationId = null;
    final static int RC_SELECT_PLAYERS = 10000;
    private Button btnInvitar;
    private Button btnMarcadores;
    final static int REQUEST_LEADERBOARD = 100;
    private Button btnLogros;
    final static int REQUEST_ACHIEVEMENTS = 101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        btnJugar = (Button) findViewById(R.id.btnJugar);
        btnConectar = (com.google.android.gms.common.SignInButton) findViewById(R.id.sign_in_button);
        btnConectar.setOnClickListener(btnConectar_Click);
        btnDesconectar = (Button) findViewById(R.id.sign_out_button);
        btnDesconectar.setOnClickListener(btnDesconectar_Click);
        btnPartidasGuardadas = (Button) findViewById(R.id.btnPartidasGuardadas);
        btnPartidaEnTiempoReal = (Button) findViewById(R.id.btnPartidaEnTiempoReal);
        btnInvitar = (Button) findViewById(R.id.btnInvitar);
        btnPartidaPorTurnos = (Button) findViewById(R.id.btnPartidaPorTurnos);
        btnMarcadores = (Button) findViewById(R.id.btnMarcadores);
        btnLogros = (Button) findViewById(R.id.btnLogros);
        Partida.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .build();
        SharedPreferences prefs = getSharedPreferences("Parejas", MODE_PRIVATE);
        int conectado = prefs.getInt("conectado", 0);
        if (conectado != 0) {
            Partida.mGoogleApiClient.connect();
        }
    }

    public void btnJugar_Click(View v) {
        Partida.tipoPartida = "LOCAL";
        nuevoJuego(4, 4);
        Intent intent = new Intent(this, Juego.class);
        startActivity(intent);
    }

    View.OnClickListener btnConectar_Click = new View.OnClickListener() {
        public void onClick(View v) {
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
    };
    View.OnClickListener btnDesconectar_Click = new View.OnClickListener() {
        public void onClick(View v) {
            mSignInClicked = false;
            Games.signOut(mGoogleApiClient);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            SharedPreferences.Editor editor = getSharedPreferences("Parejas",
                    MODE_PRIVATE).edit();
            editor.putInt("conectado", 0);
            editor.commit();
        }
    };

    public void btnPartidasGuardadas_Click(View v) {
        Partida.tipoPartida = "GUARDADA";
        nuevoJuego(4, 4);
        Intent intent = new Intent(this, Juego.class);
        startActivity(intent);
    }

    public void btnPartidaEnTiempoReal_Click(View v) {
        Partida.tipoPartida = "REAL";
        nuevoJuego(4, 4);
        Intent intent = new Intent(this, Juego.class);
        startActivity(intent);
    }

    public void btnInvitar_Click(View v) {
        final int NUMERO_MINIMO_OPONENTES = 1, NUMERO_MAXIMO_OPONENTES = 1;
        Intent intent =
                Games.TurnBasedMultiplayer.getSelectOpponentsIntent(Partida.mGoogleApiClient,
                        NUMERO_MINIMO_OPONENTES, NUMERO_MAXIMO_OPONENTES, true);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    public void btnPartidaPorTurnos_Click(View v) {
        Partida.tipoPartida = "TURNO";
        nuevoJuego(4, 4);
        Intent intent = new Intent(this, Juego.class);
        startActivity(intent);
    }

    public void btnMarcadores_Click(View v) {
        startActivityForResult(
                Games.Leaderboards.getAllLeaderboardsIntent(Partida.mGoogleApiClient), REQUEST_LEADERBOARD);
    }

    public void btnLogros_Click(View v) {
        startActivityForResult(Games.Achievements.getAchievementsIntent(
                Partida.mGoogleApiClient), REQUEST_ACHIEVEMENTS);
    }

    private void nuevoJuego(int col, int fil) {
        Partida.turno = 1;
        Partida.puntosJ1 = Partida.puntosJ2 = 0;
        Partida.FILAS = fil;
        Partida.COLUMNAS = col;
        Partida.casillas = new int[Partida.COLUMNAS][Partida.FILAS];
        try {
            int size = Partida.FILAS * Partida.COLUMNAS;
            ArrayList<Integer> list = new ArrayList<Integer>();
            for (int i = 0; i < size; i++) {
                list.add(new Integer(i));
            }
            Random r = new Random();
            for (int i = size - 1; i >= 0; i--) {
                int t = 0;
                if (i > 0) {
                    t = r.nextInt(i);
                }
                t = list.remove(t).intValue();
                Partida.casillas[i % Partida.COLUMNAS][i / Partida.COLUMNAS] = 1 + (t % (size / 2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            return;
        }
        if (mSignInClicked) {
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN,
                    "Hubo un error al conectar, por favor, inténtalo más tarde.")) {
                mResolvingConnectionFailure = false;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        Log.e("PAREJAS", "activity result:---  " + responseCode);
        switch (requestCode) {
            case RC_SIGN_IN:
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (responseCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                    SharedPreferences.Editor editor = getSharedPreferences("Parejas",
                            MODE_PRIVATE).edit();
                    editor.putInt("conectado", 1);
                    editor.commit();
                } else {
                    BaseGameUtils.showActivityResultError(this, requestCode, responseCode, R.string.unknown_error);
                }
                break;
            case RC_SELECT_PLAYERS:
                if (responseCode != Activity.RESULT_OK) {
                    return;
                }
                final ArrayList<String> invitees = intent.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
                Bundle autoMatchCriteria = null;
                int minAutoMatchPlayers = intent.getIntExtra(
                        Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
                int maxAutoMatchPlayers = intent.getIntExtra(
                        Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
                if (minAutoMatchPlayers > 0) {
                    autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
                } else {
                    autoMatchCriteria = null;
                }
                TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                        .addInvitedPlayers(invitees)
                        .setAutoMatchCriteria(autoMatchCriteria).build();
                Games.TurnBasedMultiplayer.createMatch(Partida.mGoogleApiClient, tbmc);
                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        mIncomingInvitationId = invitation.getInvitationId();
    }

    @Override
    public void onInvitationRemoved(String invitationId) {
        if (mIncomingInvitationId.equals(invitationId) && mIncomingInvitationId != null) {
            mIncomingInvitationId = null;
        }
    }
}
