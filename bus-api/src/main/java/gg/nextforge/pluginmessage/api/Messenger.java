package gg.nextforge.pluginmessage.api;

import reactor.util.annotation.NonNull;

/**
 * Interface for handling message communication in the bus system.
 * <br>
 * This interface defines methods for registering and unregistering
 * listeners for raw and typed messages, sending messages, and managing
 * message channels. It provides a unified way to handle both raw byte
 * messages and type-safe bus messages.
 * <br>
 * Implementations of this interface should ensure thread safety and
 * proper resource management, especially in the context of closing
 * resources when no longer needed.
 *
 * @author FrogTheDev
 * @since 1.0
 */
public interface Messenger extends AutoCloseable {

    // Raw message handling

    /**
     * Registers a listener for incoming raw messages on a specific channel.
     * <br>
     * This method allows the registration of a listener that will be notified
     * when raw messages are received on the specified channel. The listener
     * will receive the channel name, source, target, and the raw byte data.
     *
     * @param channel  the name of the channel to listen on. Must not be null.
     * @param listener the listener to register. Must not be null.
     */
    void registerIncomingChannel(@NonNull String channel, @NonNull RawListener listener);

    /**
     * Unregisters a listener for incoming raw messages on a specific channel.
     * <br>
     * This method removes a previously registered listener from the specified
     * channel, stopping it from receiving further raw messages.
     *
     * @param channel  the name of the channel to stop listening on. Must not be null.
     * @param listener the listener to unregister. Must not be null.
     */
    void unregisterIncomingChannel(@NonNull String channel, @NonNull RawListener listener);

    /**
     * Registers a channel for outgoing raw messages.
     * <br>
     * This method allows the registration of a channel that can be used to
     * send raw messages. The channel must be registered before any messages
     * can be sent through it.
     *
     * @param channel the name of the channel to register. Must not be null.
     */
    void registerOutgoingChannel(@NonNull String channel);

    /**
     * Unregisters a channel for outgoing raw messages.
     * <br>
     * This method removes a previously registered channel, stopping it from
     * being used to send raw messages. Any messages sent to this channel
     * after unregistration will not be processed.
     *
     * @param channel the name of the channel to unregister. Must not be null.
     */
    void unregisterOutgoingChannel(@NonNull String channel);

    /**
     * Sends a raw message to a specific target on a given channel.
     * <br>
     * This method allows sending raw byte data to a specified target on the
     * specified channel. The data will be sent as-is without any type
     * conversion or processing.
     *
     * @param channel the name of the channel to send the message on. Must not be null.
     * @param target  the target to send the message to. Can be null if not applicable.
     * @param data    the raw byte data to send. Must not be null.
     */
    void sendRaw(@NonNull String channel, String target, @NonNull byte[] data);

    // Type safe message handling

    /**
     * Registers a handler for a specific type of bus message.
     * <br>
     * This method allows the registration of a typed listener that will be
     * notified when messages of the specified type are received. The handler
     * will receive the message, source, and target information.
     *
     * @param type    the class type of the message to listen for. Must not be null.
     * @param handler the handler to register. Must not be null.
     */
    <T extends BusMessage> void registerHandler(@NonNull Class<T> type, @NonNull TypedListener<T> handler);

    /**
     * Unregisters a handler for a specific type of bus message.
     * <br>
     * This method removes a previously registered typed listener for the
     * specified message type, stopping it from receiving further messages
     * of that type.
     *
     * @param type    the class type of the message to stop listening for. Must not be null.
     * @param handler the handler to unregister. Must not be null.
     */
    <T extends BusMessage> void unregisterHandler(@NonNull Class<T> type, @NonNull TypedListener<T> handler);

    /**
     * Sends a typed message to a specific target.
     * <br>
     * This method allows sending a message of a specific type to a specified
     * target. The message will be encoded and sent over the bus system.
     *
     * @param message the message to send. Must not be null and must be of the specified type.
     * @param target  the target to send the message to. Can be null if not applicable.
     * @param <T>     the type of the message being sent, which must extend {@link BusMessage}.
     */
    <T extends BusMessage> void send(@NonNull T message, String target);

    /**
     * Closes the messenger, releasing any resources it holds.
     * <br>
     * This method should be called when the messenger is no longer needed,
     * to ensure that all resources are properly released and any ongoing
     * operations are terminated gracefully.
     */
    @Override void close();

    /**
     * Interface for raw message listeners.
     * <br>
     * This interface defines a method to handle raw messages received on a
     * specific channel. Implementations should process the raw byte data
     * along with the channel, source, and target information.
     *
     * @author FrogTheDev
     * @since 1.0
     */
    @FunctionalInterface
    interface RawListener {

        /**
         * Called when a raw message is received.
         * <br>
         * This method is invoked with the channel name, source, target, and
         * the raw byte data of the message. Implementations should handle
         * the processing of this data as needed.
         *
         * @param channel the name of the channel on which the message was received. Must not be null.
         * @param source  the source of the message. Can be null if not applicable.
         * @param target  the target of the message. Can be null if not applicable.
         * @param data    the raw byte data of the message. Must not be null.
         */
        void onMessage(String channel, String source, String target, byte[] data);

    }

    /**
     * Interface for typed message listeners.
     * <br>
     * This interface defines a method to handle messages of a specific type
     * received on the bus. Implementations should process the message along
     * with the source and target information.
     *
     * @param <T> the type of message that this listener handles, which must extend {@link BusMessage}.
     * @author FrogTheDev
     * @since 1.0
     */
    @FunctionalInterface
    interface TypedListener<T extends BusMessage> {

        /**
         * Called when a message of the specified type is received.
         * <br>
         * This method is invoked with the message, source, and target
         * information. Implementations should handle the processing of
         * the message as needed.
         *
         * @param message the message that was received. Must not be null and must be of the specified type.
         * @param source  the source of the message. Can be null if not applicable.
         * @param target  the target of the message. Can be null if not applicable.
         */
        void onMessage(T message, String source, String target);

    }
}
