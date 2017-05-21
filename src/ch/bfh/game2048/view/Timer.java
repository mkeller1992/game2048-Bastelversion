package ch.bfh.game2048.view;

import java.util.Observable;

public class Timer extends Observable implements Runnable {

	long millisElapsed;
	Thread timerThread; 

	long lastMillis;
	
	public Timer() {

		this.millisElapsed = 0;
		lastMillis = System.currentTimeMillis();
		start();
	}

	public void start(){
		lastMillis = System.currentTimeMillis();
		timerThread = new Thread(this);
		timerThread.setDaemon(true);
		timerThread.start();
	}
		
	public long getMillisElapsed() {
		return millisElapsed;
	}


	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(100);
				increaseMillis();
			} catch (InterruptedException e) {
				increaseMillis();
				Thread.currentThread().interrupt();
			}
		}
	}
	
	private void increaseMillis(){
		long diff = System.currentTimeMillis() - lastMillis;
		millisElapsed+= diff;
		lastMillis = System.currentTimeMillis();
		this.setChanged();
		this.notifyObservers();		
	}
	
	public void stop(){
		timerThread.interrupt();
		
	}
}
