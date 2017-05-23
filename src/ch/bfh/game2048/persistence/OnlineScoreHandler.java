package ch.bfh.game2048.persistence;

import java.io.FileInputStream;
import java.io.IOException;
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
	private Config conf;

	public OnlineScoreHandler() {

		conf = Config.getInstance();

		// Fetch the service account key JSON file contents
		FileInputStream serviceAccount;
		try {
			serviceAccount = new FileInputStream("credentials2.json");

			// Initialize the app with a custom auth variable, limiting the server's access
			Map<String, Object> auth = new HashMap<String, Object>();
			auth.put("uid", "my-service-worker");

			FirebaseOptions options = new FirebaseOptions.Builder().setCredential(FirebaseCredentials.fromCertificate(serviceAccount)).setDatabaseUrl("https://game2048mkeller.firebaseio.com/").setDatabaseAuthVariableOverride(auth).build();
			FirebaseApp.initializeApp(options);

			// The app only has access as defined in the Security Rules
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/highscore");

			scoreRef = ref.child("scores");

			// GameStatistics gameStats = new GameStatistics("Matthias Keller", 8);
			// gameStats.setBoardSize(20);
			// scoreRef.push().setValue(gameStats);

			System.out.println("Done");

			scoreRef.addValueEventListener(new ValueEventListener() {
				public void onDataChange(DataSnapshot snapshot) {

					ArrayList<GameStatistics> tempScoreArray = new ArrayList<GameStatistics>();

					for (DataSnapshot postSnapshot : snapshot.getChildren()) {
						GameStatistics score = postSnapshot.getValue(GameStatistics.class);
						tempScoreArray.add(score);
					}
					// System.out.println("Print ArrayList:");
					// for (GameStatistics s : tempScoreArray) {
					// System.out.println(s.getBoardSize());
					// }

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
				    	System.out.println("New Score:"+justArrivedScore);
						
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
