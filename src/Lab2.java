import TSim.*;
import java.util.concurrent.Semaphore;
import java.lang.InterruptedException;
import java.util.*;

public class Lab2 {
	static int STOP = -1;
	static int ACQUIRENOTHING = -2;
	ArrayList<Semaphore> semaphores;
	private Hashtable<Integer, SensorMapping> sensorToSemaphore;

	public Lab2(Integer speed1, Integer speed2) {
		TSimInterface tsi = TSimInterface.getInstance();


		tsi.setDebug(false);
		semaphores = new ArrayList<Semaphore>();
		for(int i = 0; i < 9; i++) {
			semaphores.add(new Semaphore(1));
		}

		sensorToSemaphore = new Hashtable<Integer, SensorMapping>();
		sensorToSemaphore.put(607,  new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {2}));
		sensorToSemaphore.put(1007,  new SensorMapping(new int[] {2}, new int[] {ACQUIRENOTHING}));
		sensorToSemaphore.put(908,  new SensorMapping(new int[] {2}, new int[] {ACQUIRENOTHING}));
		sensorToSemaphore.put(805,  new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {2}));
		sensorToSemaphore.put(1507, new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {3}));
		sensorToSemaphore.put(1608, new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {3}));
		sensorToSemaphore.put(1907, new SensorMapping(new int[] {0,1}, new int[] {ACQUIRENOTHING}));
		sensorToSemaphore.put(1709, new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {4,5}));
		sensorToSemaphore.put(1410, new SensorMapping(new int[] {3}, new int[] {ACQUIRENOTHING}));
		sensorToSemaphore.put(1309, new SensorMapping(new int[] {3}, new int[] {ACQUIRENOTHING}));
		sensorToSemaphore.put(609,  new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {6}));
		sensorToSemaphore.put(510,  new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {6}));
		sensorToSemaphore.put(209,  new SensorMapping(new int[] {4,5}, new int[] {ACQUIRENOTHING}));
		sensorToSemaphore.put(111,  new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {7,8}));
		sensorToSemaphore.put(511,  new SensorMapping(new int[] {6}, new int[] {ACQUIRENOTHING}));
		sensorToSemaphore.put(313,  new SensorMapping(new int[] {6}, new int[] {ACQUIRENOTHING}));

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
		case 111:
		case 511:
		case 313:
			return 311;
		case 209:
		case 609:
		case 510:
			return 409;
		case 1309:
		case 1709:
		case 1508:
		case 1410:
			return 1509;
		case 1507:
		case 1608:
		case 1907:
			return 1707;
		}
		return 0;
	}

	public int getSwitchDirection(int switchCoords, int sensorCoords, int nextSem) {
		if (switchCoords == 311 && sensorCoords == 511) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 311 && nextSem == 8) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 409 && sensorCoords == 609) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 409 && nextSem == 5) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 1509 && sensorCoords == 1410) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 1509 && nextSem == 4) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 1707 && sensorCoords == 1608) return TSimInterface.SWITCH_LEFT;
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
		int lastSemaphore = -1;
		int secondLastSemaphore = -1;


		TrainThread(int trainNumber, int speed, ArrayList<Semaphore> semaphores, boolean toLower, Hashtable<Integer, SensorMapping> sensorToSemaphore, int currentSemaphore) {
			this.trainNumber = trainNumber;
			this.speed = speed;
			this.semaphores = semaphores;
			this.tsi = TSimInterface.getInstance();
			this.toLower = toLower;
			this.sensorToSemaphore = sensorToSemaphore;
			this.currentSemaphore = currentSemaphore;
		}
		
		public void run() {
			try {
				boolean firstRound = true;
				semaphores.get(currentSemaphore).acquire();
				tsi.setSpeed(trainNumber, speed);
				while (true) {
					SensorEvent sensorEvent = tsi.getSensor(trainNumber);
					int x = sensorEvent.getXpos();
					int y = sensorEvent.getYpos();
					SensorMapping sMap = sensorToSemaphore.get(x*100+y);
					int[] nextSemaphores = sMap.getSemaphore(toLower);
					
					if (sensorEvent.getStatus() == SensorEvent.INACTIVE) {
						if (nextSemaphores[0] == Lab2.ACQUIRENOTHING) {
							if (lastSemaphore != -1) {
								if (currentSemaphore != 2) {
									if (lastSemaphore == 2) {
										semaphores.get(secondLastSemaphore).release();
									} else {
										semaphores.get(lastSemaphore).release();
									}
								} else {
									semaphores.get(currentSemaphore).release();
								}
							}
						}
					}
					if (sensorEvent.getStatus() == SensorEvent.ACTIVE) {
						if (nextSemaphores[0] == Lab2.STOP) {
							if (!firstRound) {
								tsi.setSpeed(trainNumber, 0);
								if (currentSemaphore == 2) {
									int cache = currentSemaphore;
									currentSemaphore = lastSemaphore;
									lastSemaphore = cache;
								}
								sleep(1500);
								speed = -1*speed;
								tsi.setSpeed(trainNumber, speed);
								toLower = !toLower;
							} else {
								firstRound = false;
							}
						} else if (nextSemaphores[0] != Lab2.ACQUIRENOTHING) {
							boolean succesfullyAcquired = false;
							for (int i = 0; i < nextSemaphores.length && !succesfullyAcquired; i++) {
								if (semaphores.get(nextSemaphores[i]).tryAcquire()) {
									succesfullyAcquired = true;
									secondLastSemaphore = lastSemaphore;
									lastSemaphore = currentSemaphore;
									currentSemaphore = nextSemaphores[i];
									int switchCoords = getNextSwitch(x*100+y);
									int switchDir = getSwitchDirection(switchCoords, x*100+y, currentSemaphore);
									if (switchCoords != 0) {
										tsi.setSwitch(switchCoords / 100, switchCoords % 100, switchDir);
									}
								}
							}
							if (!succesfullyAcquired) {
								tsi.setSpeed(trainNumber, 0);
								semaphores.get(nextSemaphores[0]).acquire();
								secondLastSemaphore = lastSemaphore;
								lastSemaphore = currentSemaphore;
								currentSemaphore = nextSemaphores[0];
								
								int switchCoords = getNextSwitch(x*100+y);
								int switchDir = getSwitchDirection(switchCoords, x*100+y, currentSemaphore);
								if (switchCoords != 0) {
									tsi.setSwitch(switchCoords / 100, switchCoords % 100, switchDir);
								}
								
								tsi.setSpeed(trainNumber, speed);
							}
						}
					}
				}
			} catch (CommandException e1) {
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
	int[] acquireIfToUpper;
	int[] acquireIfToLower;
	
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