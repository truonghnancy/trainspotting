import TSim.*;
import java.util.concurrent.Semaphore;
import java.lang.InterruptedException;
import java.util.*;

public class Lab1 {
	int STOP = -1;
	int DONOTHING = -2;
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
			TrainThread train1 = new TrainThread(1, speed1, semaphores, true);
			TrainThread train2 = new TrainThread(2, speed2, semaphores, false);

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
}

class TrainThread extends Thread {
	private int speed;
	private int trainNumber;
	private ArrayList<Semaphore> semaphores;
	private TSimInterface tsi;
	private boolean toLower;
//	private Hashtable<Integer, Integer[]> sensorToSemaphore;
	

	TrainThread(int trainNumber, int speed, ArrayList<Semaphore> semaphores, boolean toLower) {
		this.trainNumber = trainNumber;
		this.speed = speed;
		this.semaphores = semaphores;
		this.tsi = TSimInterface.getInstance();
		this.toLower = toLower;
		
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
					switch (x*100 + y) {
					case 707:
						// Semaphore 0, 2
						break;
					case 907:
						// Semaphore 0, 2
						break;
					case 1607:
						// Semaphore 0
						break;
					case 808:
						// Semaphore 1, 2
						break;
					case 806:
						// Semaphore 1, 2
						break;
					case 1708:
						// Semaphore 1
						break;
					case 1807:
						// Semaphore 3
						break;
					case 1609:
						// Semaphore 3
						break;
					case 1510:
						// Semaphore 4
						break;
					case 410:
						// Semaphore 4
						break;
					case 1409:
						// Semaphore 5
						break;
					case 509:
						// Semaphore 5
						break;
					case 309:
						// Semaphore 6
						break;
					case 211:
						// Semaphore 6
						break;
					case 312:
						// Semaphore 7
						break;
					case 411:
						// Semaphore 8
						if (toLower) {
							
						} else {
							if (semaphores.get(6).tryAcquire()) {
								tsi.setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
								System.out.println("switch set");
							} else {
								tsi.setSpeed(trainNumber, 0);
								semaphores.get(6).acquire();
								tsi.setSpeed(trainNumber, speed);
								
							}
						}
						break;
					}
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

class SensorMapping {
	// if either of these two is negative, train has to come to a stop
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