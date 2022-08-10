package labs.spring.pearl.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;


public class ProcessRecordTask<K, V> implements Runnable {


  private final List<ConsumerRecord<K, V>> records;

  public ProcessRecordTask(List<ConsumerRecord<K, V>> records) {
    this.records = records;
  }

  @Override
  public void run() {

  }

  public boolean isFinished() {
    return false;
  }

  public long getCurrentOffset() {
    return 0;
  }
}
