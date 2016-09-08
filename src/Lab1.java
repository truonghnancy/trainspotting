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
		sensorToSemaphore.put(1607, new SensorMapping(new int[] {DONOTHING}, new int[] {3}));
		sensorToSemaphore.put(808,  new SensorMapping(new int[] {2}, new int[] {DONOTHING}));
		sensorToSemaphore.put(806,  new SensorMapping(new int[] {DONOTHING}, new int[] {2}));
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
		
/*		public void release(int sensor, boolean toLower) {
			switch(sensor) {
			case 211:
				semaphores.get(7).release();
				semaphores.get(8).release();
				System.out.println(trainNumber + ": I have released " + 7);
				System.out.println(trainNumber + ": I have released " + 8);
				break;
			case 312:
			case 411:
				semaphores.get(6).release();
				System.out.println(trainNumber + ": I have released " + 6);
				break;
			case 309:
			case 1609:
				semaphores.get(4).release();
				semaphores.get(5).release();
				System.out.println(trainNumber + ": I have released " + 4);
				System.out.println(trainNumber + ": I have released " + 5);
				break;
			case 410:
			case 509:
				semaphores.get(6).release();
				System.out.println(trainNumber + ": I have released " + 6);
				break;
			case 1409:
			case 1510:
				semaphores.get(3).release();
				System.out.println(trainNumber + ": I have released " + 3);
				break;
			case 1807:
				semaphores.get(0).release();
				semaphores.get(1).release();
				System.out.println(trainNumber + ": I have released " + 0);
				System.out.println(trainNumber + ": I have released " + 1);
				break;
			case 1607:
			case 1708:
				semaphores.get(3).release();
				System.out.println(trainNumber + ": I have released " + 3);
				break;
			case 808:
			case 907:
				if (toLower)
					semaphores.get(2).release();
				System.out.println(trainNumber + ": I have released " + 2);
				break;
			case 806:
			case 707:
				if (!toLower)
					semaphores.get(2).release();
				System.out.println(trainNumber + ": I have released " + 2);
				break;
				
			}
		}*/

		public void run() {
			try {
				boolean firstRound = true;
				semaphores.get(currentSemaphore).acquire();
				System.out.println(trainNumber + ": I have acquired "+ currentSemaphore);
				tsi.setSpeed(trainNumber, speed);
				int lastSemaphore = -1;
				while (true) {
					SensorEvent sensorEvent = tsi.getSensor(trainNumber);
					
					int x = sensorEvent.getXpos();
					int y = sensorEvent.getYpos();
//					release(x*100+y, toLower);
					SensorMapping sMap = sensorToSemaphore.get(x*100+y);
					int[] nextSemaphores = sMap.getSemaphore(toLower);
					
					if (sensorEvent.getStatus() == SensorEvent.INACTIVE) {
						if (nextSemaphores[0] == Lab1.STOP || nextSemaphores[0] == Lab1.DONOTHING) {
							if (lastSemaphore != -1) {
								semaphores.get(lastSemaphore).release();
								System.out.println(trainNumber + ": released " + lastSemaphore);
							} else
								System.out.println("lastSemaphore == -1");
						}
					}
					if (sensorEvent.getStatus() == SensorEvent.ACTIVE) {
						if (nextSemaphores[0] == Lab1.STOP) {
							if (!firstRound) {
								tsi.setSpeed(trainNumber, 0);
								sleep(1500);
								tsi.setSpeed(trainNumber, -1*speed);
								toLower = !toLower;
								toUpper = !toUpper;
							} else {
								firstRound = false;
							}
						} else if (nextSemaphores[0] == Lab1.DONOTHING) {
						} else {
							boolean acquired = false;
							for (int i = 0; i < nextSemaphores.length && !acquired; i++) {
//								System.out.println("nextSemaphore: " + nextSemaphores[i]);
								if (semaphores.get(nextSemaphores[i]).tryAcquire()) {
									acquired = true;
									System.out.println(trainNumber + ": I have acquired! " + nextSemaphores[i]);
//									if (!((currentSemaphore == 0 || currentSemaphore == 1) && nextSemaphores[i] == 2)) {
//										semaphores.get(currentSemaphore).release();
//									}
									lastSemaphore = currentSemaphore;
									currentSemaphore = nextSemaphores[i];
									int nextSemaphore = nextSemaphores[i];
									int switchCoords = getNextSwitch(x*100+y);
									int switchDir = getSwitchDirection(switchCoords, x*100+y, nextSemaphore);
//									System.out.println("switchCoords = " + switchCoords);
									if (switchCoords != 0) {
										tsi.setSwitch(switchCoords / 100, switchCoords % 100, switchDir);
//										System.out.println("switch set");
									}
								}
							}
							if (!acquired) {
								tsi.setSpeed(trainNumber, 0);
								
								semaphores.get(nextSemaphores[0]).acquire();
//								semaphores.get(currentSemaphore).release();
//								System.out.println(trainNumber + ": released " + currentSemaphore);
								currentSemaphore = nextSemaphores[0];
								
								int nextSemaphore = nextSemaphores[0];
								int switchCoords = getNextSwitch(x*100+y);
								int switchDir = getSwitchDirection(switchCoords, x*100+y, nextSemaphore);
								System.out.println("switchCoords = " + switchCoords);
								if (switchCoords != 0) {
									tsi.setSwitch(switchCoords / 100, switchCoords % 100, switchDir);
									System.out.println("switch set");
								}
								
								tsi.setSpeed(trainNumber, speed);
								//acquire next sem
								//accelerate
							}
						}
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