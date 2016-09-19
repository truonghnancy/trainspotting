import TSim.*;
import java.lang.InterruptedException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Lab2 {
	static int STOP = -1;
	static int ACQUIRENOTHING = -2;
	ArrayList<Monitor> monitors;
	private Hashtable<Integer, SensorMapping> sensorToMonitor;

	public Lab2(Integer speed1, Integer speed2) {
		TSimInterface tsi = TSimInterface.getInstance();


		tsi.setDebug(false);
		
		monitors = new ArrayList<Monitor>();
		for(int i = 0; i < 9; i++) {
			monitors.add(new Monitor((i==0||i==8)));
		}

		sensorToMonitor = new Hashtable<Integer, SensorMapping>();
		sensorToMonitor.put(607,  new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {2}));
		sensorToMonitor.put(1007,  new SensorMapping(new int[] {2}, new int[] {ACQUIRENOTHING}));
		sensorToMonitor.put(908,  new SensorMapping(new int[] {2}, new int[] {ACQUIRENOTHING}));
		sensorToMonitor.put(805,  new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {2}));
		sensorToMonitor.put(1507, new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {3}));
		sensorToMonitor.put(1608, new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {3}));
		sensorToMonitor.put(1907, new SensorMapping(new int[] {0,1}, new int[] {ACQUIRENOTHING}));
		sensorToMonitor.put(1709, new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {4,5}));
		sensorToMonitor.put(1410, new SensorMapping(new int[] {3}, new int[] {ACQUIRENOTHING}));
		sensorToMonitor.put(1309, new SensorMapping(new int[] {3}, new int[] {ACQUIRENOTHING}));
		sensorToMonitor.put(609,  new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {6}));
		sensorToMonitor.put(510,  new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {6}));
		sensorToMonitor.put(209,  new SensorMapping(new int[] {4,5}, new int[] {ACQUIRENOTHING}));
		sensorToMonitor.put(111,  new SensorMapping(new int[] {ACQUIRENOTHING}, new int[] {7,8}));
		sensorToMonitor.put(511,  new SensorMapping(new int[] {6}, new int[] {ACQUIRENOTHING}));
		sensorToMonitor.put(313,  new SensorMapping(new int[] {6}, new int[] {ACQUIRENOTHING}));

		sensorToMonitor.put(1403,  new SensorMapping(new int[] {STOP}, new int[] {STOP}));
		sensorToMonitor.put(1405,  new SensorMapping(new int[] {STOP}, new int[] {STOP}));
		sensorToMonitor.put(1411,  new SensorMapping(new int[] {STOP}, new int[] {STOP}));
		sensorToMonitor.put(1413,  new SensorMapping(new int[] {STOP}, new int[] {STOP}));

		try {
			TrainThread train1 = new TrainThread(1, speed1, monitors, true, sensorToMonitor, 0);
			TrainThread train2 = new TrainThread(2, speed2, monitors, false, sensorToMonitor, 8);
			
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

	public int getSwitchDirection(int switchCoords, int sensorCoords, int nextMon) {
		if (switchCoords == 311 && sensorCoords == 511) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 311 && nextMon == 8) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 409 && sensorCoords == 609) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 409 && nextMon == 5) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 1509 && sensorCoords == 1410) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 1509 && nextMon == 4) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 1707 && sensorCoords == 1608) return TSimInterface.SWITCH_LEFT;
		if (switchCoords == 1707 && nextMon == 1) return TSimInterface.SWITCH_LEFT;

		return TSimInterface.SWITCH_RIGHT;
	}

	class TrainThread extends Thread {
		private int speed;
		private int trainNumber;
		private ArrayList<Monitor> monitors;
		private TSimInterface tsi;
		private boolean toLower;
		private Hashtable<Integer, SensorMapping> sensorToMonitor;
		int currentMonitor;
		int lastMonitor = -1;
		int secondLastMonitor = -1;


		TrainThread(int trainNumber, int speed, ArrayList<Monitor> monitors, boolean toLower, Hashtable<Integer, SensorMapping> sensorToMonitor, int currentMonitor) {
			this.trainNumber = trainNumber;
			this.speed = speed;
			this.monitors = monitors;
			this.tsi = TSimInterface.getInstance();
			this.toLower = toLower;
			this.sensorToMonitor = sensorToMonitor;
			this.currentMonitor = currentMonitor;
		}
		
		public void run() {
			try {
				boolean firstRound = true;
				monitors.get(currentMonitor).enter();
				tsi.setSpeed(trainNumber, speed);
				while (true) {
					SensorEvent sensorEvent = tsi.getSensor(trainNumber);
					int x = sensorEvent.getXpos();
					int y = sensorEvent.getYpos();
					SensorMapping sMap = sensorToMonitor.get(x*100+y);
					int[] nextMonitors = sMap.getMonitor(toLower);
					
					if (sensorEvent.getStatus() == SensorEvent.INACTIVE) {
						if (nextMonitors[0] == Lab2.ACQUIRENOTHING) {
							if (lastMonitor != -1) {
								if (currentMonitor != 2) {
									if (lastMonitor == 2) {
										monitors.get(secondLastMonitor).leave();
									} else {
										monitors.get(lastMonitor).leave();
									}
								} else {
									monitors.get(currentMonitor).leave();
								}
							}
						}
					}
					if (sensorEvent.getStatus() == SensorEvent.ACTIVE) {
						if (nextMonitors[0] == Lab2.STOP) {
							if (!firstRound) {
								tsi.setSpeed(trainNumber, 0);
								if (currentMonitor == 2) {
									int cache = currentMonitor;
									currentMonitor = lastMonitor;
									lastMonitor = cache;
								}
								sleep(1500);
								speed = -1*speed;
								tsi.setSpeed(trainNumber, speed);
								toLower = !toLower;
							} else {
								firstRound = false;
							}
						} else if (nextMonitors[0] != Lab2.ACQUIRENOTHING) {
							boolean succesfullyAcquired = false;
							int nextMonitor = nextMonitors[0];
							if (nextMonitors.length == 2 && monitors.get(nextMonitors[0]).isInUse()) {
								nextMonitor = nextMonitors[1];
							}
									tsi.setSpeed(trainNumber, 0);
									monitors.get(nextMonitor).enter();
									secondLastMonitor = lastMonitor;
									lastMonitor = currentMonitor;
									currentMonitor = nextMonitor;
									int switchCoords = getNextSwitch(x*100+y);
									int switchDir = getSwitchDirection(switchCoords, x*100+y, currentMonitor);
									if (switchCoords != 0) {
										tsi.setSwitch(switchCoords / 100, switchCoords % 100, switchDir);
									}
									tsi.setSpeed(trainNumber, speed);
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
	public int[] getMonitor(boolean toLower) {
		if (toLower) {
			return acquireIfToLower;
		} else {
			return acquireIfToUpper;
		}
	}
}


class Monitor {
	private final Lock lock;
	private final Condition notInUse;
	private boolean isInUse;
	
	public Monitor(boolean inUse) {
		lock = new ReentrantLock();
		notInUse = lock.newCondition();
		isInUse = false;
	}
	
	public void enter() {
		// await the notInUse
		lock.lock();
		if (isInUse) {
			try {
				notInUse.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		isInUse = true;
		lock.unlock();
	}
	
	public void leave() {
		// signal the notInUse
		lock.lock();
		isInUse = false;
		notInUse.signal();
		lock.unlock();
	}
	
	public boolean isInUse() {
		return isInUse;
	}
}