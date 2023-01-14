package com.capacitor.community.googlegameservices;

import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

@CapacitorPlugin(name = "GoogleGameServices")
public class GoogleGameServicesPlugin extends Plugin {

    private final String TAG = "GoogleGameServices";

    // current save game - serializable to and from the saved game
    SaveGame mSaveGame = new SaveGame();
    // Client used to interact with Google Snapshots.
    private SnapshotsClient mSnapshotsClient = null;

    // Request code for saving the game to a snapshot.
    private static final int RC_SAVE_SNAPSHOT = 9004;

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
        String title = call.getString("title");
        String data = call.getString("data");
        Log.d(TAG, "saveGame called with title: "+title+" data: "+data);

        mSaveGame.setGameObject("Game #1", "{'something':'1'}");

        SnapshotMetadata snapshotMetadata =
                getActivity().getIntent().getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA);
        saveSnapshot(snapshotMetadata);
    }

    @PluginMethod()
    public void loadGame(final PluginCall call) {
        Log.d(TAG, "loadGame called..");

        loadSnapshot();

        Log.d(TAG, "Finished loadGame with: "+mSaveGame);
    }

    private void loadSnapshot(){
        // Get the SnapshotsClient from the signed in account.
        SnapshotsClient snapshotsClient =
                PlayGames.getSnapshotsClient(getActivity());

        // In the case of a conflict, the most recently modified version of this snapshot will be used.
        int conflictResolutionPolicy = SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED;

        // Open the saved game using its name.
        snapshotsClient.open(mCurrentSaveName, true, conflictResolutionPolicy)
                .addOnFailureListener(e -> Log.e(TAG, "Error while opening Snapshot.", e)).continueWith(task -> {
                    Snapshot snapshot = task.getResult().getData();

                    // Opening the snapshot was a success and any conflicts have been resolved.
                    try {
                        // Extract the raw data from the snapshot.
                        assert snapshot != null;
                        return snapshot.getSnapshotContents().readFully();
                    } catch (IOException e) {
                        Log.e(TAG, "Error while reading Snapshot.", e);
                    }

                    return null;
                }).addOnCompleteListener(task -> {
                    byte[] result = task.getResult();
                    mSaveGame = new SaveGame(result);
                    Log.d(TAG, "onComplete loadGame = "+mSaveGame);
                    // Dismiss progress dialog and reflect the changes in the UI when complete.
                });
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


        intentTask.addOnSuccessListener(intent -> {
            Log.i(TAG,"doShowSavedGameUI onSuccess called for Intent");
            startActivityForResult(call, intent, "GoogleActivityResult");
        });

        intentTask.addOnCompleteListener(intent -> {
                Log.i(TAG,"doShowSavedGameUI onComplete called for Intent");
        });

        intentTask.addOnFailureListener(intent -> {
            Log.i(TAG,"doShowSavedGameUI onFailure called for Intent");
        });
    }
    private String mCurrentSaveName = "snapshotTemp";

    /**
     * This callback will be triggered after you call startActivityForResult from the
     * showSavedGamesUI method.
     */
    @ActivityCallback
    public void GoogleActivityResult(PluginCall call, ActivityResult result) {
        Log.i(TAG, "onActivityResult is called!");
        Intent intent = result.getData();
        if (intent != null) {
            if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)) {
                // Load a snapshot.
                SnapshotMetadata snapshotMetadata =
                        intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA);
                mCurrentSaveName = snapshotMetadata.getUniqueName();
                Log.i(TAG, "Load snapshot from saved: "+mCurrentSaveName);
                loadSnapshot();
            } else if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_NEW)) {
                // Create a new snapshot named with a unique string
                String unique = new BigInteger(281, new Random()).toString(13);
                mCurrentSaveName = "snapshotTemp-" + unique;

                Log.i(TAG, "Create snapshot from saved: "+mCurrentSaveName);
                // Create the new snapshot
                SnapshotMetadata snapshotMetadata =
                        getActivity().getIntent().getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA);
                saveSnapshot(snapshotMetadata);
            }
        }
    }
    private Task<SnapshotsClient.DataOrConflict<Snapshot>> waitForClosedAndOpen(final SnapshotMetadata snapshotMetadata) {

        final boolean useMetadata = snapshotMetadata != null && snapshotMetadata.getUniqueName() != null;
        if (useMetadata) {
            Log.i(TAG, "Opening snapshot using metadata: " + snapshotMetadata);
        } else {
            Log.i(TAG, "Opening snapshot using currentSaveName: " + mCurrentSaveName);
        }

        final String filename = useMetadata ? snapshotMetadata.getUniqueName() : mCurrentSaveName;

        return SnapshotCoordinator.getInstance()
                .waitForClosed(filename)
                .addOnFailureListener(e -> Log.e(TAG, "There was a problem waiting for the file to close!", e))
                .continueWithTask(task -> {
                    Task<SnapshotsClient.DataOrConflict<Snapshot>> openTask = useMetadata
                            ? SnapshotCoordinator.getInstance().open(mSnapshotsClient, snapshotMetadata)
                            : SnapshotCoordinator.getInstance().open(mSnapshotsClient, filename, true);
                    return openTask.addOnFailureListener(e -> Log.e(TAG, "There was a problem waiting for the file to close!", e));
                });
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
        assert conflict != null;
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
     * Prepares saving Snapshot to the user's synchronized storage, conditionally resolves errors,
     * and stores the Snapshot.
     */
    private void saveSnapshot(final SnapshotMetadata snapshotMetadata) {
        waitForClosedAndOpen(snapshotMetadata)
                .addOnCompleteListener(task -> {
                    SnapshotsClient.DataOrConflict<Snapshot> result = task.getResult();
                    Snapshot snapshotToWrite = processOpenDataOrConflict(RC_SAVE_SNAPSHOT, result, 0);

                    if (snapshotToWrite == null) {
                        // No snapshot available yet; waiting on the user to choose one.
                        return;
                    }

                    Log.d(TAG, "Writing data to snapshot: " + snapshotToWrite.getMetadata().getUniqueName());
                    writeSnapshot(snapshotToWrite)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Log.i(TAG, "Snapshot saved!");
                                } else {

                                    Log.e(TAG, "There was a problem writing the snapshot!", task1.getException());
                                }
                            });
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
                .setCoverImage()
                .setDescription("Modified data at: " + Calendar.getInstance().getTime())
                .build();
        return SnapshotCoordinator.getInstance().commitAndClose(mSnapshotsClient, snapshot, metadataChange);
    }
}
