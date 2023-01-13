package com.capacitor.community.googlegameservices;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataBuffer;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.android.gms.common.api.Result;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;

@CapacitorPlugin(name = "GoogleGameServices")
public class GoogleGameServicesPlugin extends Plugin {

    private static final int RC_SAVED_GAMES = 9009;
    private final String TAG = "GoogleGameServices";

    // current save game - serializable to and from the saved game
    SaveGame mSaveGame = new SaveGame();
    // Client used to interact with Google Snapshots.
    private SnapshotsClient mSnapshotsClient = null;



    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Request code for listing saved games
    private static final int RC_LIST_SAVED_GAMES = 9002;

    // Request code for selecting a snapshot
    private static final int RC_SELECT_SNAPSHOT = 9003;

    // Request code for saving the game to a snapshot.
    private static final int RC_SAVE_SNAPSHOT = 9004;

    private static final int RC_LOAD_SNAPSHOT = 9005;

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient;

    private String currentSaveName = "snapshotTemp";

    // world we're currently viewing
    int mWorld = 1;
    private static final int WORLD_MIN = 1;
    private static final int WORLD_MAX = 20;
    private static final int LEVELS_PER_WORLD = 12;

    // level we're currently "playing"
    int mLevel = 0;

    // state of "playing" - used to make the back button work correctly
    boolean mInLevel = false;

    // progress dialog we display while we're loading state from the cloud
    //ProgressDialog mLoadingDialog = null;

    // star strings (we use the Unicode BLACK STAR and WHITE STAR characters -- lazy graphics!)
    final static String[] STAR_STRINGS = {
            "\u2606\u2606\u2606\u2606\u2606", // 0 stars
            "\u2605\u2606\u2606\u2606\u2606", // 1 star
            "\u2605\u2605\u2606\u2606\u2606", // 2 stars
            "\u2605\u2605\u2605\u2606\u2606", // 3 stars
            "\u2605\u2605\u2605\u2605\u2606", // 4 stars
            "\u2605\u2605\u2605\u2605\u2605", // 5 stars
    };

    // Members related to the conflict resolution chooser of Snapshots.
    final static int MAX_SNAPSHOT_RESOLVE_RETRIES = 50;

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", echo(value));
        call.resolve(ret);
    }

    private String echo(String value) {
        Log.i("Echo", value);
        return value;
    }

    @Override
    public void load() {
        super.load();
        Log.d(TAG, "lifecycle load called");

        PlayGamesSdk.initialize(getContext());
        mSnapshotsClient = PlayGames.getSnapshotsClient(getActivity());
    }

    @PluginMethod()
    public void signIn(final PluginCall call) {
        Log.d(TAG, "signIn called");
        startSignIn();
    }

    @PluginMethod()
    public void showSavedGamesUI(final PluginCall call) {
        Log.d(TAG, "showSavedGamesUI called");
        doShowSavedGamesUI(call);
    }

    @PluginMethod()
    public void saveGame(final PluginCall call) {
        Log.d(TAG, "saveGame called");

        mSaveGame.setLevelStars("LevelTwo", 2);

        // Load a snapshot.
        SnapshotMetadata snapshotMetadata =
                getActivity().getIntent().getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA);
        saveSnapshot(snapshotMetadata);
    }

    @PluginMethod()
    public void loadGame(final PluginCall call) {
        Log.d(TAG, "loadGame called..");

        // Get the SnapshotsClient from the signed in account.
        SnapshotsClient snapshotsClient =
                PlayGames.getSnapshotsClient(getActivity());

        // In the case of a conflict, the most recently modified version of this snapshot will be used.
        int conflictResolutionPolicy = SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED;

        //final String filename = useMetadata ? snapshotMetadata.getUniqueName() : currentSaveName;
        // Open the saved game using its name.
        snapshotsClient.open(currentSaveName, true, conflictResolutionPolicy)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error while opening Snapshot.", e);
                    }
                }).continueWith(new Continuation<SnapshotsClient.DataOrConflict<Snapshot>, byte[]>() {
                    @Override
                    public byte[] then(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) throws Exception {
                        Snapshot snapshot = task.getResult().getData();

                        // Opening the snapshot was a success and any conflicts have been resolved.
                        try {
                            // Extract the raw data from the snapshot.
                            byte[] result = snapshot.getSnapshotContents().readFully();
                            return result;
                        } catch (IOException e) {
                            Log.e(TAG, "Error while reading Snapshot.", e);
                        }

                        return null;
                    }
                }).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        byte[] result = task.getResult();
                        mSaveGame = new SaveGame(result);
                        Log.d(TAG, "onComplete loadGame = "+mSaveGame.toString());
                        // Dismiss progress dialog and reflect the changes in the UI when complete.
                        // ...
                    }
                });

        Log.d(TAG, "Finished loadGame with: "+mSaveGame.toString());
    }
    public static Object getObject(byte[] bytes) throws IOException,
            ClassNotFoundException {
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        ObjectInputStream oi = new ObjectInputStream(bi);
        Object obj = oi.readObject();
        bi.close();
        oi.close();
        return obj;
    }

    public static Object getObject(ByteBuffer byteBuffer)
            throws ClassNotFoundException, IOException {
        InputStream input = new ByteArrayInputStream(byteBuffer.array());
        ObjectInputStream oi = new ObjectInputStream(input);
        Object obj = oi.readObject();
        input.close();
        oi.close();
        byteBuffer.clear();
        return obj;
    }

    private void startSignIn() {
        GamesSignInClient gamesSignInClient = PlayGames.getGamesSignInClient(getActivity());

        gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {
            boolean isAuthenticated = (isAuthenticatedTask.isSuccessful() &&
                    isAuthenticatedTask.getResult().isAuthenticated());

            if (isAuthenticated) {
                // Continue with Play Games Services
                Log.d(TAG, "User authenticated with google play");
            } else {
                // Disable your integration with Play Games Services or show a
                // login button to ask players to sign-in. Clicking it should
                // call GamesSignInClient.signIn().
                Log.d(TAG, "User NOT authenticated with google play");
            }
        });
    }

    private void doShowSavedGamesUI(PluginCall call) {
        SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(getActivity());
        int maxNumberOfSavedGamesToShow = 5;

        Task<Intent> intentTask = snapshotsClient.getSelectSnapshotIntent(
                "See My Saves", true, true, maxNumberOfSavedGamesToShow);

        intentTask.addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                startActivityForResult(call, intent, RC_SAVED_GAMES);
            }
        });
    }

    private Task<SnapshotsClient.DataOrConflict<Snapshot>> waitForClosedAndOpen(final SnapshotMetadata snapshotMetadata) {

        final boolean useMetadata = snapshotMetadata != null && snapshotMetadata.getUniqueName() != null;
        if (useMetadata) {
            Log.i(TAG, "Opening snapshot using metadata: " + snapshotMetadata);
        } else {
            Log.i(TAG, "Opening snapshot using currentSaveName: " + currentSaveName);
        }

        final String filename = useMetadata ? snapshotMetadata.getUniqueName() : currentSaveName;

        return SnapshotCoordinator.getInstance()
                .waitForClosed(filename)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "There was a problem waiting for the file to close!", e);
                    }
                })
                .continueWithTask(new Continuation<Result, Task<SnapshotsClient.DataOrConflict<Snapshot>>>() {
                    @Override
                    public Task<SnapshotsClient.DataOrConflict<Snapshot>> then(@NonNull Task<Result> task) throws Exception {
                        Task<SnapshotsClient.DataOrConflict<Snapshot>> openTask = useMetadata
                                ? SnapshotCoordinator.getInstance().open(mSnapshotsClient, snapshotMetadata)
                                : SnapshotCoordinator.getInstance().open(mSnapshotsClient, filename, true);
                        return openTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "There was a problem waiting for the file to close!", e);
                            }
                        });
                    }
                });
    }

    /**
     * Loads a Snapshot from the user's synchronized storage.
     */
    void loadFromSnapshot(final SnapshotMetadata snapshotMetadata) {
        waitForClosedAndOpen(snapshotMetadata)
                .addOnSuccessListener(new OnSuccessListener<SnapshotsClient.DataOrConflict<Snapshot>>() {
                    @Override
                    public void onSuccess(SnapshotsClient.DataOrConflict<Snapshot> result) {

                        // if there is a conflict  - then resolve it.
                        Snapshot snapshot = processOpenDataOrConflict(RC_LOAD_SNAPSHOT, result, 0);

                        if (snapshot == null) {
                            Log.w(TAG, "Conflict was not resolved automatically, waiting for user to resolve.");
                        } else {
                            try {
                                readSavedGame(snapshot);
                                Log.i(TAG, "Snapshot loaded.");
                            } catch (IOException e) {
                                Log.e(TAG, "Error while reading snapshot contents: " + e.getMessage());
                            }
                        }

                        SnapshotCoordinator.getInstance().discardAndClose(mSnapshotsClient, snapshot)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "There was a problem discarding the snapshot!", e);
//                                        handleException(e, "There was a problem discarding the snapshot!");
                                    }
                                });

//                        hideAlertBar();
//                        updateUi();
                    }
                });
    }

    private void readSavedGame(Snapshot snapshot) throws IOException {
        mSaveGame = new SaveGame(snapshot.getSnapshotContents().readFully());
    }

    /**
     * Conflict resolution for when Snapshots are opened.
     *
     * @param requestCode - the request currently being processed.  This is used to forward on the
     *                    information to another activity, or to send the result intent.
     * @param result      The open snapshot result to resolve on open.
     * @param retryCount  - the current iteration of the retry.  The first retry should be 0.
     * @return The opened Snapshot on success; otherwise, returns null.
     */
    Snapshot processOpenDataOrConflict(int requestCode,
                                       SnapshotsClient.DataOrConflict<Snapshot> result,
                                       int retryCount) {

        retryCount++;

        if (!result.isConflict()) {
            return result.getData();
        }

        Log.i(TAG, "Open resulted in a conflict!");

        SnapshotsClient.SnapshotConflict conflict = result.getConflict();
        final Snapshot snapshot = conflict.getSnapshot();
        final Snapshot conflictSnapshot = conflict.getConflictingSnapshot();

        ArrayList<Snapshot> snapshotList = new ArrayList<Snapshot>(2);
        snapshotList.add(snapshot);
        snapshotList.add(conflictSnapshot);

        // Display both snapshots to the user and allow them to select the one to resolve.
        //selectSnapshotItem(requestCode, snapshotList, conflict.getConflictId(), retryCount);

        // Since we are waiting on the user for input, there is no snapshot available; return null.
        return null;
    }

    /**
     * Handles resolving the snapshot conflict asynchronously.
     *
     * @param requestCode      - the request currently being processed.  This is used to forward on the
     *                         information to another activity, or to send the result intent.
     * @param conflictId       - the id of the conflict being resolved.
     * @param retryCount       - the current iteration of the retry.  The first retry should be 0.
     * @param snapshotMetadata - the metadata of the snapshot that is selected to resolve the conflict.
     */
    private Task<SnapshotsClient.DataOrConflict<Snapshot>> resolveSnapshotConflict(final int requestCode,
                                                                                   final String conflictId,
                                                                                   final int retryCount,
                                                                                   final SnapshotMetadata snapshotMetadata) {

        Log.i(TAG, "Resolving conflict retry count = " + retryCount + " conflictid = " + conflictId);
        return waitForClosedAndOpen(snapshotMetadata)
                .continueWithTask(new Continuation<SnapshotsClient.DataOrConflict<Snapshot>, Task<SnapshotsClient.DataOrConflict<Snapshot>>>() {
                    @Override
                    public Task<SnapshotsClient.DataOrConflict<Snapshot>> then(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) throws Exception {
                        return SnapshotCoordinator.getInstance().resolveConflict(
                                        mSnapshotsClient,
                                        conflictId,
                                        task.getResult().getData())
                                .addOnCompleteListener(new OnCompleteListener<SnapshotsClient.DataOrConflict<Snapshot>>() {
                                    @Override
                                    public void onComplete(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) {
                                        if (!task.isSuccessful()) {
                                            Log.e(TAG, "There was a problem opening a file for resolving the conflict!", task.getException());
                                            return;
                                        }

                                        Snapshot snapshot = processOpenDataOrConflict(requestCode,
                                                task.getResult(),
                                                retryCount);
                                        Log.d(TAG, "resolved snapshot conflict - snapshot is " + snapshot);
                                        // if there is a snapshot returned, then pass it along to onActivityResult.
                                        // otherwise, another activity will be used to resolve the conflict so we
                                        // don't need to do anything here.
                                        if (snapshot != null) {
                                            Intent intent = new Intent("");
//                                            intent.putExtra(SelectSnapshotActivity.SNAPSHOT_METADATA, snapshot.getMetadata().freeze());
//                                            onActivityResult(requestCode, RESULT_OK, intent);
                                        }
                                    }
                                });
                    }
                });
    }
    /**
     * Prepares saving Snapshot to the user's synchronized storage, conditionally resolves errors,
     * and stores the Snapshot.
     */
    void saveSnapshot(final SnapshotMetadata snapshotMetadata) {
        waitForClosedAndOpen(snapshotMetadata)
                .addOnCompleteListener(new OnCompleteListener<SnapshotsClient.DataOrConflict<Snapshot>>() {
                    @Override
                    public void onComplete(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) {
                        SnapshotsClient.DataOrConflict<Snapshot> result = task.getResult();
                        Snapshot snapshotToWrite = processOpenDataOrConflict(RC_SAVE_SNAPSHOT, result, 0);

                        if (snapshotToWrite == null) {
                            // No snapshot available yet; waiting on the user to choose one.
                            return;
                        }

                        Log.d(TAG, "Writing data to snapshot: " + snapshotToWrite.getMetadata().getUniqueName());
                        writeSnapshot(snapshotToWrite)
                                .addOnCompleteListener(new OnCompleteListener<SnapshotMetadata>() {
                                    @Override
                                    public void onComplete(@NonNull Task<SnapshotMetadata> task) {
                                        if (task.isSuccessful()) {
                                            Log.i(TAG, "Snapshot saved!");
                                        } else {

                                            Log.e(TAG, "There was a problem writing the snapshot!", task.getException());
                                        }
                                    }
                                });
                    }
                });
    }

    /**
     * Generates metadata, takes a screenshot, and performs the write operation for saving a
     * snapshot.
     */
    private Task<SnapshotMetadata> writeSnapshot(Snapshot snapshot) {
        // Set the data payload for the snapshot.
        snapshot.getSnapshotContents().writeBytes(mSaveGame.toBytes());

        // Save the snapshot.
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                .setDescription("Modified data at: " + Calendar.getInstance().getTime())
                .build();
        return SnapshotCoordinator.getInstance().commitAndClose(mSnapshotsClient, snapshot, metadataChange);
    }

    /**
     * Prints a log message (convenience method).
     */
    void log(String message) {
        Log.d(TAG, message);
    }
}
