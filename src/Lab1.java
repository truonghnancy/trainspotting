import TSim.*;
import java.util.concurrent.Semaphore;
import java.lang.InterruptedException;
import java.util.*;

public class Lab1 {
    ArrayList<Semaphore> semaphores;

  public Lab1(Integer speed1, Integer speed2) {
    TSimInterface tsi = TSimInterface.getInstance();
    tsi.setDebug(false);
    semaphores = new ArrayList<Semaphore>();
    for(int i = 0; i < 9; i++) {
       semaphores.add(new Semaphore(1));
    }

    try {
      TrainThread train1 = new TrainThread(1, speed1, semaphores);
      TrainThread train2 = new TrainThread(2, speed2, semaphores);

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

   TrainThread(int trainNumber, int speed, ArrayList<Semaphore> semaphores) {
      this.trainNumber = trainNumber;
      this.speed = speed;
      this.semaphores = semaphores;
      this.tsi = TSimInterface.getInstance();

      System.out.println("hello from train " + trainNumber);
   }

   public void run() {
    while (true) {
    try {

      tsi.setSpeed(trainNumber, speed);
      SensorEvent sensorEvent = tsi.getSensor(trainNumber);
      if (sensorEvent.getStatus() == SensorEvent.ACTIVE) {
         System.out.println(sensorEvent.toString());
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
