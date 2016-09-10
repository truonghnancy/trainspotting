import TSim.*;
import java.util.concurrent.Semaphore;
import java.lang.InterruptedException;
import java.util.*;

public class Lab1 {
	static int STOP = -1;
	static int DONOTHING = -2;
	static Boolean toUpper = true;
	static Boolean toLower = false;
	ArrayList<Semaphore> semaphores;
	private Hashtable<Integer, SensorMapping> sensorToSemaphore;

	public Lab1(Integer speed1, Integer speed2) {
		TSimInterface tsi = TSimInterface.getInstance();


		tsi.setDebug(false);
		semaphores = new ArrayList<Semaphore>();
		for(int i = 0; i < 9; i++) {
			semaphores.add(new Semaphore(1));
		}

		sensorToSemaphore = new Hashtable<Integer, SensorMapping>();
		sensorToSemaphore.put(707,  new SensorMapping(new int[] {DONOTHING}, new int[] {2}));
		sensorToSemaphore.put(907,  new SensorMapping(new int[] {2}, new int[] {DONOTHING}));
		sensorToSemaphore.put(808,  new SensorMapping(new int[] {2}, new int[] {DONOTHING}));
		sensorToSemaphore.put(806,  new SensorMapping(new int[] {DONOTHING}, new int[] {2}));
		sensorToSemaphore.put(1607, new SensorMapping(new int[] {DONOTHING}, new int[] {3}));
		sensorToSemaphore.put(1708, new SensorMapping(new int[] {DONOTHING}, new int[] {3}));
		sensorToSemaphore.put(1807, new SensorMapping(new int[] {0,1}, new int[] {DONOTHING}));
		sensorToSemaphore.put(1609, new SensorMapping(new int[] {DONOTHING}, new int[] {4,5}));
		sensorToSemaphore.put(1510, new SensorMapping(new int[] {3}, new int[] {DONOTHING}));
		sensorToSemaphore.put(1409, new SensorMapping(new int[] {3}, new int[] {DONOTHING}));
		sensorToSemaphore.put(509,  new SensorMapping(new int[] {DONOTHING}, new int[] {6}));
		sensorToSemaphore.put(410,  new SensorMapping(new int[] {DONOTHING}, new int[] {6}));
		sensorToSemaphore.put(309,  new SensorMapping(new int[] {4,5}, new int[] {DONOTHING}));
		sensorToSemaphore.put(211,  new SensorMapping(new int[] {DONOTHING}, new int[] {7,8}));
		sensorToSemaphore.put(411,  new SensorMapping(new int[] {6}, new int[] {DONOTHING}));
		sensorToSemaphore.put(312,  new SensorMapping(new int[] {6}, new int[] {DONOTHING}));

		sensorToSemaphore.put(1403,  new SensorMapping(new int[] {STOP}, new int[] {STOP}));
		sensorToSemaphore.put(1405,  new SensorMapping(new int[] {STOP}, new int[] {STOP}));
		sensorToSemaphore.put(1411,  new SensorMapping(new int[] {STOP}, new int[] {STOP}));
		sensorToSemaphore.put(1413,  new SensorMapping(new int[] {STOP}, new int[] {STOP}));

		try {
			TrainThread train1 = new TrainThread(1, speed1, semaphores, true, sensorToSemaphore, 0);
			TrainThread train2 = new TrainThread(2, speed2, semaphores, false, sensorToSemaphore, 8);
			
			train1.start();
			train2.start();
			train1.join();
			train2.join();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public int getNextSwitch(int sensCoords) {
		switch(sensCoords) {
		case 211:
		case 411:
		case 312:
			return 311;
		case 309:
		case 509:
		case 410:
			return 409;
		case 1409:
		case 1609:
		case 1508:
		case 1510:
			return 1509;
		case 1607:
		case 1708:
		case 1807:
			return 1707;
		}
		return 0;
	}

	public int getSwitchDirection(int switchCoords, int sensorCoords, int nextSem) {
		if (switchCoords == 311 && sensorCoords == 411) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 311 && nextSem == 8) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 409 && sensorCoords == 509) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 409 && nextSem == 5) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 1509 && sensorCoords == 1510) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 1509 && nextSem == 4) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 1707 && sensorCoords == 1708) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 1707 && nextSem == 1) return TSimInterface.SWITCH_LEFT;

		return TSimInterface.SWITCH_RIGHT;
	}

	class TrainThread extends Thread {
		private int speed;
		private int trainNumber;
		private ArrayList<Semaphore> semaphores;
		private TSimInterface tsi;
		private boolean toLower;
		private Hashtable<Integer, SensorMapping> sensorToSemaphore;
		int currentSemaphore;


		TrainThread(int trainNumber, int speed, ArrayList<Semaphore> semaphores, boolean toLower, Hashtable<Integer, SensorMapping> sensorToSemaphore, int currentSemaphore) {
			this.trainNumber = trainNumber;
			this.speed = speed;
			this.semaphores = semaphores;
			this.tsi = TSimInterface.getInstance();
			this.toLower = toLower;
			this.sensorToSemaphore = sensorToSemaphore;
			this.currentSemaphore = currentSemaphore;

			System.out.println("hello from train " + trainNumber);
		}
		
		public void run() {
			try {
				boolean firstRound = true;
				semaphores.get(currentSemaphore).acquire();
				System.out.println(trainNumber + ": I have acquired "+ currentSemaphore);
				tsi.setSpeed(trainNumber, speed);
				int lastSemaphore = -1;
				int secondLastSemaphore = -1;
				while (true) {
					SensorEvent sensorEvent = tsi.getSensor(trainNumber);
					int x = sensorEvent.getXpos();
					int y = sensorEvent.getYpos();
					SensorMapping sMap = sensorToSemaphore.get(x*100+y);
					int[] nextSemaphores = sMap.getSemaphore(toLower);
					
					if (sensorEvent.getStatus() == SensorEvent.INACTIVE) {
						if (nextSemaphores[0] == Lab1.DONOTHING) {
							if (lastSemaphore != -1) {
								if (currentSemaphore != 2) {
									semaphores.get(lastSemaphore).release();
									System.out.println(trainNumber + ": released " + lastSemaphore);
									if (lastSemaphore == 2) {
										semaphores.get(secondLastSemaphore).release();
										System.out.println(trainNumber + ": released " + secondLastSemaphore);
									}
								} else {
									System.out.println(trainNumber + ": not releasing " + lastSemaphore);
								}
							} else {
								System.out.println("lastSemaphore == -1");
							}
						}
					}
					if (sensorEvent.getStatus() == SensorEvent.ACTIVE) {
						if (nextSemaphores[0] == Lab1.STOP) {
							System.out.println("FirstRound = " + firstRound);
							if (!firstRound) {
								tsi.setSpeed(trainNumber, 0);
								sleep(1500);
								speed = -1*speed;
								tsi.setSpeed(trainNumber, speed);
								toLower = !toLower;
								toUpper = !toUpper;
							} else {
								firstRound = false;
							}
						} else if (nextSemaphores[0] == Lab1.DONOTHING) {
						} else {
							boolean acquired = false;
							for (int i = 0; i < nextSemaphores.length && !acquired; i++) {
								if (semaphores.get(nextSemaphores[i]).tryAcquire()) {
									acquired = true;
									System.out.println(trainNumber + ": I have acquired " + nextSemaphores[i]);
									secondLastSemaphore = lastSemaphore;
									lastSemaphore = currentSemaphore;
									currentSemaphore = nextSemaphores[i];
									int nextSemaphore = nextSemaphores[i];
									int switchCoords = getNextSwitch(x*100+y);
									int switchDir = getSwitchDirection(switchCoords, x*100+y, nextSemaphore);
									if (switchCoords != 0) {
										tsi.setSwitch(switchCoords / 100, switchCoords % 100, switchDir);
									}
								}
							}
							if (!acquired) {
								tsi.setSpeed(trainNumber, 0);
								
								semaphores.get(nextSemaphores[0]).acquire();
								currentSemaphore = nextSemaphores[0];
								
								int nextSemaphore = nextSemaphores[0];
								int switchCoords = getNextSwitch(x*100+y);
								int switchDir = getSwitchDirection(switchCoords, x*100+y, nextSemaphore);
								if (switchCoords != 0) {
									tsi.setSwitch(switchCoords / 100, switchCoords % 100, switchDir);
								}
								
								tsi.setSpeed(trainNumber, speed);
							}
						}
					}
				}
			} catch (CommandException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}

class SensorMapping {
	// if either of these two is negative, train has to come to a stop
	int[] acquireIfToUpper;
	int[] acquireIfToLower;
	int curSemaphore;
	
	public SensorMapping(int[] acquireIfToUpper, int[] acquireIfToLower) {
		this.acquireIfToLower = acquireIfToLower;
		this.acquireIfToUpper = acquireIfToUpper;
	}
	public int[] getSemaphore(boolean toLower) {
		if (toLower) {
			return acquireIfToLower;
		} else {
			return acquireIfToUpper;
		}
	}
}