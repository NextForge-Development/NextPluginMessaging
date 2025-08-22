package gg.nextforge.pluginmessage.api;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * Represents a message envelope for the bus system.
 * <br>
 * This class encapsulates the metadata and data of a message that is sent
 * through the bus. It includes fields for version, channel, source, target,
 * timestamp, unique identifier, and the actual data of the message.
 * <br>
 * The envelope is used to structure messages in a way that allows for
 * efficient routing and processing within the bus system. It provides a
 * standardized format for messages, ensuring that all necessary information
 * is included when sending and receiving messages.
 * <br>
 * The `v` field represents the version of the message format, allowing for
 * compatibility with different versions of the bus system. The `channel`
 * field indicates the communication channel over which the message is sent,
 * while `source` and `target` specify the origin and destination of the message,
 * respectively. The `timestamp` field records the time when the message was
 * created, and the `id` field provides a unique identifier for the message.
 * The `data` field contains the actual content of the message, which can be
 * in any format, such as JSON, binary data, or serialized objects.
 * <br>
 * This class is immutable and uses the Builder pattern for construction,
 * ensuring that all fields are set at the time of creation. It also includes
 * getter methods for accessing the fields, making it easy to retrieve
 * the message information when processing messages in the bus system.
 * <br>
 * @author FrogTheDev
 * @since 1.0
 */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Envelope {
    String v;
    String channel;
    String source;
    String target;   // optional
    long   timestamp;
    UUID   id;
    byte[] data;
}
