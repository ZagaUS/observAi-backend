package com.zaga.kafka.consumer;


import org.apache.kafka.common.header.Headers;

import com.zaga.entity.kepler.KeplerMetric;

import io.smallrye.common.annotation.Identifier;
import io.smallrye.reactive.messaging.kafka.DeserializationFailureHandler;
import jakarta.enterprise.context.ApplicationScoped;


/**
 * A deserialization failure handler for metric messages from Kafka.
 */


@ApplicationScoped
@Identifier("kepler-failure-fallback")
public class KeplerDeserializerFailureHandler   implements DeserializationFailureHandler<KeplerMetric> {

  /**
   * Handles deserialization failures for metric messages.
   *
   * @param topic        The topic from which the message was consumed.
   * @param isKey        Whether the message is a key.
   * @param deserializer The deserializer being used.
   * @param data         The raw message data.
   * @param exception    The exception that occurred during deserialization.
   * @param headers      The headers associated with the message.
   * @return Returns null to skip the problematic message and
   * continue processing the next message.
   */

    @Override
  public KeplerMetric handleDeserializationFailure(
    final String topic,
    final boolean isKey,
    final String deserializer,
    final byte[] data,
    final Exception exception,
    final Headers headers
  ) {
    // Log the deserialization failure with the relevant information
    System.err.println("Deserialization failed for message in topic: " + topic);
    System.err.println("Exception: " + exception.getMessage());

    // Return null to skip the problematic message and
    // continue processing the next message.
    return null;
  }
}
