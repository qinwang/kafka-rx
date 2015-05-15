package com.cj.kafka.rx

case class Message[K, V](
  key: K = null,
  value: V,
  topic: String,
  partition: Int,
  offset: Long,
  private[rx] val consumerOffsets: OffsetMap = defaultOffsets,
  private[rx] val mergeWith: MergeWith = defaultMergeWith ) {

  val topicPartition = topic -> partition

  def commit(merge: OffsetMerge = (zkOffsets, proposedOffsets) => proposedOffsets): OffsetMap = {
    mergeWith(consumerOffsets, merge)
  }

  override def equals(other: Any) = {
    other match {
      case message: Message[K, V] =>
        message.key == key &&
        message.topic == topic &&
        message.partition == partition &&
        message.offset == offset
      case _ => false
    }
  }

  def produce: ProducedMessage[K, V, K, V] = {
    produce[K, V](key, value, partition)
  }

  def produce[v](value: v): ProducedMessage[Null, v, K, V] = {
    produce[Null, v](null, value)
  }

  def produce[k, v](key: k, value: v): ProducedMessage[k, v, K, V] = {
    new ProducedMessage(key, value, sourceMessage = this)
  }

  def produce[k, v](key: k, value: v, partition: Int): ProducedMessage[k, v, K, V] = {
    new ProducedMessage(key = key, value = value, partition=partition, this)
  }

}
