package gg.nextforge.pluginmessage.api;


import reactor.util.annotation.NonNull;

/**
 * Interface for encoding and decoding messages in the bus system.
 * <br>
 * This interface defines methods for encoding a message into a byte array
 * and decoding a byte array back into a message object. It is used to
 * facilitate the transmission of messages over the bus, allowing for
 * serialization and deserialization of message data.
 * <br>
 * Implementing classes should provide the specific logic for encoding and
 * decoding messages of a particular type, ensuring that the data can be
 * correctly transformed between its in-memory representation and its
 * serialized form.
 *
 * @param <T> the type of message that this codec handles, which must extend
 *            {@link BusMessage}.
 * @author FrogTheDev
 * @since 1.0
 */
public interface MessageCodec<T extends BusMessage> {

    /**
     * Encodes a message into a byte array.
     * <br>
     * This method takes a message object and converts it into a byte array
     * representation. The byte array can then be transmitted over the bus or
     * stored for later use. The encoding process should ensure that all
     * necessary data from the message is captured in the byte array format.
     * <br>
     * Implementations should handle any exceptions that may occur during the
     * encoding process, such as serialization errors or data conversion issues.
     * @param msg the message to encode
     * @return a byte array representing the encoded message. The byte array
     *         should not be null, but may be empty if the message has no data.
     * @throws Exception if an error occurs during encoding, such as serialization failure.
     */
    @NonNull
    byte[] encode(T msg) throws Exception;

    /**
     * Decodes a byte array into a message object.
     * <br>
     * This method takes a byte array and converts it back into a message object.
     * The byte array should contain all the necessary data to reconstruct the
     * original message. The decoding process should ensure that the data is
     * correctly interpreted and that the resulting message object is valid.
     * <br>
     * Implementations should handle any exceptions that may occur during the
     * decoding process, such as deserialization errors or data format issues.
     * @param data the byte array to decode. It should not be null, but may be empty if
     *             the message has no data.
     * @return the decoded message object. The returned object should not be null,
     * @throws Exception if an error occurs during decoding, such as deserialization failure.
     */
    @NonNull
    T decode(@NonNull byte[] data) throws Exception;

    /**
     * Returns the channel associated with this codec.
     * <br>
     * This method provides the channel name that this codec is associated with.
     * The channel is used to identify the specific communication channel over
     * which messages are sent and received. It is typically a string that
     * represents a unique identifier for the channel.
     * <br>
     * Implementations should return a non-null string that represents the
     * channel name. This channel name is used to route messages to the correct
     * codec when processing messages in the bus system.
     * <br>
     * Example channel names could be "chat", "notifications", or "commands".
     * <br>
     * Implementations should ensure that the channel name is unique within the
     * context of the bus system to avoid conflicts with other codecs.
     * <br>
     * @see CodecRegistry
     * @return the channel name associated with this codec. It must not be null.
     */
    @NonNull
    String channel();

    /**
     * Returns the type of message that this codec handles.
     * <br>
     * This method provides the class type of the message that this codec is
     * designed to encode and decode. It is used to ensure that the codec is
     * applied to the correct message type when processing messages in the bus
     * system.
     * <br>
     * Implementations should return the class type of the
     * message that this codec handles, which must extend
     * {@link BusMessage}.
     * @return the class type of the message that this codec handles.
     */
    @NonNull
    Class<T> type();
}
