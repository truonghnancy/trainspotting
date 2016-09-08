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


		//tsi.setDebug(false);
		semaphores = new ArrayList<Semaphore>();
		for(int i = 0; i < 9; i++) {
			semaphores.add(new Semaphore(1));
		}

		sensorToSemaphore = new Hashtable<Integer, SensorMapping>();

		sensorToSemaphore.put(707, new SensorMapping(new SDT(), new SDT(), new SDT(), new SDT()));

		sensorToSemaphore.put(707,  new SensorMapping(new int[] {STOP}, new int[] {2}));
		sensorToSemaphore.put(907,  new SensorMapping(new int[] {2}, new int[] {DONOTHING}));
		sensorToSemaphore.put(1607, new SensorMapping(new int[] {DONOTHING}, new int[] {3}));
		sensorToSemaphore.put(808,  new SensorMapping(new int[] {2}, new int[] {DONOTHING}));
		sensorToSemaphore.put(806,  new SensorMapping(new int[] {STOP}, new int[] {2}));
		sensorToSemaphore.put(1708, new SensorMapping(new int[] {DONOTHING}, new int[] {3}));
		sensorToSemaphore.put(1807, new SensorMapping(new int[] {0,1}, new int[] {DONOTHING}));
		sensorToSemaphore.put(1609, new SensorMapping(new int[] {DONOTHING}, new int[] {4,5}));
		sensorToSemaphore.put(1510, new SensorMapping(new int[] {3}, new int[] {DONOTHING}));
		sensorToSemaphore.put(1409, new SensorMapping(new int[] {3}, new int[] {DONOTHING}));
		sensorToSemaphore.put(509,  new SensorMapping(new int[] {DONOTHING}, new int[] {6}));
		sensorToSemaphore.put(410,  new SensorMapping(new int[] {DONOTHING}, new int[] {6}));
		sensorToSemaphore.put(309,  new SensorMapping(new int[] {4,5}, new int[] {DONOTHING}));
		sensorToSemaphore.put(211,  new SensorMapping(new int[] {DONOTHING}, new int[] {7,8}));
		sensorToSemaphore.put(411,  new SensorMapping(new int[] {6}, new int[] {STOP}));
		sensorToSemaphore.put(312,  new SensorMapping(new int[] {6}, new int[] {STOP}));

		try {
			TrainThread train1 = new TrainThread(1, speed1, semaphores, true, sensorToSemaphore);
			TrainThread train2 = new TrainThread(2, speed2, semaphores, false, sensorToSemaphore);

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
		case 408:
			return 409;
		case 1409:
		case 1609:
		case 1508:
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


		TrainThread(int trainNumber, int speed, ArrayList<Semaphore> semaphores, boolean toLower, Hashtable<Integer, SensorMapping> sensorToSemaphore) {
			this.trainNumber = trainNumber;
			this.speed = speed;
			this.semaphores = semaphores;
			this.tsi = TSimInterface.getInstance();
			this.toLower = toLower;
			this.sensorToSemaphore = sensorToSemaphore;

			System.out.println("hello from train " + trainNumber);
		}

		public void run() {
			while (true) {
				try {
					tsi.setSpeed(trainNumber, speed);
					SensorEvent sensorEvent = tsi.getSensor(trainNumber);
					if (sensorEvent.getStatus() == SensorEvent.ACTIVE) {
						//					System.out.println(sensorEvent.toString());

						int x = sensorEvent.getXpos();
						int y = sensorEvent.getYpos();
						SensorMapping sMap = sensorToSemaphore.get(x^100+y);
						//					SemDirTuple[] nextSemDir = sMap.getSemaphore(toLower);
						//					int[] nextSemaphore = new int[]{nextSemDir[0].nextSemaphore, nextSemDir[1].nextSemaphore};
						//					int[] nextDirection = new int[]{nextSemDir[0].switchDirection, nextSemDir[1].switchDirection};

						
/*						if (nextSemaphore[0] == Lab1.STOP) {
							tsi.setSpeed(trainNumber, 0);
						} else if (nextSemaphore[0] == Lab1.DONOTHING) {

						} else {
							for (int i = 0; i < nextSemaphore.length; i++) {
								if (semaphores.get(nextSemaphore[i]).tryAcquire()) {
									int swX = sMap.nextSwitchX;
									int swY = sMap.nextSwitchY;
									tsi.setSwitch(swX, swY, nextDirection[i]);
									//tsi.setSwitch(x, y, TSimInterface.SWITCH_LEFT);
									System.out.println("switch set");
								}
							}
						}*/
						/*					
					if (semaphores.get(6).tryAcquire()) {
						tsi.setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
						System.out.println("switch set");
					} else {
						tsi.setSpeed(trainNumber, 0);
						semaphores.get(6).acquire();
						tsi.setSpeed(trainNumber, speed);

					}*/
					}
				}
				catch (CommandException e) {
					e.printStackTrace();
					System.exit(1);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
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

/*
class SensorMapping {
	// if either of these two is negative, train has to come to a stop
	SDT acquireIfToUpper1;
	SDT acquireIfToUpper2;
	SDT acquireIfToLower1;
	SDT acquireIfToLower2;
//	int[] acquireIfToUpper;
//	int[] acquireIfToLower;
//	int curSemaphore;
	int nextSwitchX;
	int nextSwitchY;

	public SensorMapping(int x, int y, SDT acquireIfToUpper1, SDT acquireIfToUpper2, SDT acquireIfToLower1,  SDT acquireIfToLower2) {
		this.nextSwitchX = x;
		this.acquireIfToLower1 = acquireIfToLower1;
		this.acquireIfToLower2 = acquireIfToLower2;
		this.acquireIfToUpper1 = acquireIfToUpper1;
		this.acquireIfToUpper2 = acquireIfToUpper2;
	}
	public SDT getSemaphore(boolean toLower, int i) {
		if (toLower) {
			if (i==1) {
				return acquireIfToLower1;
			}
			return acquireIfToLower2;
		} else {
			if (i==1) {
				return acquireIfToUpper1;
			}
			return acquireIfToUpper2;
		}
	}
}

class SDT { 
	  public final int nextSemaphore; 
	  public final int switchDirection; 
	  public SDT(int nextSemaphore, int switchDirection) { 
	    this.nextSemaphore = nextSemaphore; 
	    this.switchDirection = switchDirection; 
	  } 
	}
}*/