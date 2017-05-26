package ch.bfh.game2048.persistence;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ch.bfh.game2048.model.GameStatistics;
import ch.bfh.game2048.model.Highscore;
import ch.bfh.game2048.model.PushObject;

public class OnlineScoreHandler extends Observable {

	private Highscore highscore;
	private DatabaseReference scoreRef;
	ScoreHandler scoreHandler;

	
	final static String APPDATA_PATH = System.getenv("APPDATA") + "/";

	public OnlineScoreHandler() {

		// Fetch the service account key JSON file contents
		try {
			
			InputStream serviceAccount = this.getClass().getResourceAsStream("credentials.json");

			// Initialize the app with a custom auth variable, limiting the server's access
			Map<String, Object> auth = new HashMap<String, Object>();
			auth.put("uid", "my-service-worker");

			FirebaseOptions options = new FirebaseOptions.Builder().setCredential(FirebaseCredentials.fromCertificate(serviceAccount)).setDatabaseUrl("https://game2048mkeller.firebaseio.com/").setDatabaseAuthVariableOverride(auth).build();
			FirebaseApp.initializeApp(options);

			// The app only has access as defined in the Security Rules
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/highscore");

			scoreRef = ref.child("scores");

			scoreRef.addValueEventListener(new ValueEventListener() {
				public void onDataChange(DataSnapshot snapshot) {

					ArrayList<GameStatistics> tempScoreArray = new ArrayList<GameStatistics>();

					for (DataSnapshot postSnapshot : snapshot.getChildren()) {
						GameStatistics score = postSnapshot.getValue(GameStatistics.class);
						tempScoreArray.add(score);
					}

					highscore.setHighscores(tempScoreArray);

					setChanged();
					notifyObservers();
				}

				@Override
				public void onCancelled(DatabaseError firebaseError) {
					System.out.println("The read failed: " + firebaseError.getMessage());

				}
			});
			
			
			scoreRef.addChildEventListener(new ChildEventListener() {
			    @Override
			    public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
			    	GameStatistics justArrivedScore = dataSnapshot.getValue(GameStatistics.class);
									
					if(System.currentTimeMillis()-justArrivedScore.getTimeOfAddingToScoreList() < 120000){
						setChanged();
						notifyObservers(new PushObject(justArrivedScore));				  	    							
					}
			    }

			    @Override
			    public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

			    @Override
			    public void onChildRemoved(DataSnapshot dataSnapshot) {}

			    @Override
			    public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

			    @Override
			    public void onCancelled(DatabaseError databaseError) {}
			});
			

		} catch (IOException e) {
		}

	}

	public DatabaseReference getScoreRef() {
		return scoreRef;
	}

	public void setHighscore(Highscore highscore) {
		this.highscore = highscore;
	}

	public void setScoreHandler(ScoreHandler scoreHandler) {
		this.scoreHandler = scoreHandler;
	}

}
