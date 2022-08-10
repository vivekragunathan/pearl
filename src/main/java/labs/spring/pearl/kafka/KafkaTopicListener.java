package labs.spring.pearl.kafka;

import labs.spring.pearl.utils.NonEmptyList;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static labs.spring.pearl.utils.Utils.runIf;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

/**
 * <a href="https://www.confluent.io/blog/kafka-consumer-multi-threaded-messaging/">...</a>
 * <a href="https://github.com/inovatrend/mtc-demo/blob/master/src/main/java/com/inovatrend/mtcdemo/MultithreadedKafkaConsumer.java"></a>
 * <p/>
 * <a href="https://www.confluent.io/blog/introducing-confluent-parallel-message-processing-client/">...</a>
 * <a href="https://www.confluent.io/resources/kafka-summit-2020/kafkaconsumer-decoupling-consumption-and-processing-for-better-resource-utilization/">...</a>
 */
public class KafkaTopicListener<K, V> implements Runnable, ConsumerRebalanceListener {

  private static final Logger   logger       = LoggerFactory.getLogger(KafkaTopicListener.class);
  private static final Duration pollDuration = Duration.of(100, ChronoUnit.MILLIS);

  private static final long COMMIT_INTERVAL_MS = 5000;

  private final List<String>                                 topics;
  private final KafkaConsumer<K, V>                          consumer;
  private final ExecutorService                              executor        = Executors.newFixedThreadPool(8);
  private final Map<TopicPartition, ProcessRecordTask<K, V>> activeTasks     = new HashMap<>();
  private final Map<TopicPartition, OffsetAndMetadata>       offsetsToCommit = new HashMap<>();
  private final AtomicBoolean                                stopped         = new AtomicBoolean(false);
  private       long                                         lastCommitTime  = System.currentTimeMillis();

  public KafkaTopicListener(
    String bootstrapServers,
    String groupId,
    NonEmptyList<String> topics,
    Deserializer<K> keyDeserializer,
    Deserializer<V> valueDeserializer
  ) {
    this.topics = topics;

    this.consumer = new KafkaConsumer<>(
      new HashMap<>() {{
        put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        put(GROUP_ID_CONFIG, groupId);
        put(ENABLE_AUTO_COMMIT_CONFIG, false);
        put(AUTO_OFFSET_RESET_CONFIG, "earliest");
      }},
      keyDeserializer,
      valueDeserializer
    );
  }

  @Override
  public void run() {
    try {
      consumer.subscribe(this.topics, this);

      while (!stopped.get()) {
        var records = consumer.poll(pollDuration);
        handleFetchedRecords(records);
        checkActiveTasks();
        commitOffsets();
      }
    } catch (WakeupException ex) {
      if (!stopped.get())
        throw ex;
    } finally {
      consumer.close();
    }
  }

  @Override
  public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

  }

  @Override
  public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
    consumer.resume(partitions);
  }

  private void handleFetchedRecords(ConsumerRecords<K, V> records) {
    if (!records.isEmpty()) {
      var partitionsToPause = new ArrayList<TopicPartition>();

      records.partitions().forEach(partition -> {
        var task = new ProcessRecordTask<>(records.records(partition));
        partitionsToPause.add(partition);
        executor.submit(task);
        activeTasks.put(partition, task);
      });

      consumer.pause(partitionsToPause);
    }

  }

  private void commitOffsets() {
    try {
      final long currentTimeMillis = System.currentTimeMillis();
      if (currentTimeMillis - lastCommitTime > COMMIT_INTERVAL_MS) {
        if (!offsetsToCommit.isEmpty()) {
          consumer.commitSync(offsetsToCommit);
          offsetsToCommit.clear();
        }

        lastCommitTime = currentTimeMillis;
      }
    } catch (Exception ex) {
      logger.error("Failed to commit offset(s)", ex);
    }
  }

  private void checkActiveTasks() {
    var finishedTasksPartitions =
      activeTasks
        .entrySet()
        .stream()
        .map(this::processActiveTask)
        .flatMap(Collection::stream)
        .toList();

    finishedTasksPartitions.forEach(activeTasks::remove);
    consumer.resume(finishedTasksPartitions);
  }

  // private List<TopicPartition> processActiveTask(TopicPartition partition, ProcessRecordTask<K, V> task) {
  private List<TopicPartition> processActiveTask(Map.Entry<TopicPartition, ProcessRecordTask<K, V>> entry) {
    final var partition               = entry.getKey();
    final var task                    = entry.getValue();
    final var finishedTasksPartitions = new ArrayList<TopicPartition>();

    if (task.isFinished()) {
      finishedTasksPartitions.add(partition);
    }

    runIf(
      task.getCurrentOffset(),
      KafkaTopicListener::greaterThanZero,
      offset -> offsetsToCommit.put(partition, new OffsetAndMetadata(offset))
    );

    return finishedTasksPartitions;
  }

  private static Boolean greaterThanZero(long value) {
    return value > 0;
  }
}
