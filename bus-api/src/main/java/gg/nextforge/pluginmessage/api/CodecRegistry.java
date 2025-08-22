package gg.nextforge.pluginmessage.api;

import reactor.util.annotation.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for message codecs used in the bus system.
 * <br>
 * This class maintains a mapping of message types to their corresponding codecs,
 * allowing for efficient encoding and decoding of messages. It provides methods
 * to register codecs and retrieve them based on message type or channel.
 * <br>
 * Codecs are responsible for converting messages to and from byte arrays, enabling
 * serialization and deserialization of messages for transmission over the bus.
 * <br>
 * The registry is thread-safe, allowing concurrent access to codec registration
 * and retrieval operations.
 *
 * @author FrogTheDev
 * @since 1.0
 */
public final class CodecRegistry {
    private final Map<Class<?>, MessageCodec<?>> byType = new ConcurrentHashMap<>();
    private final Map<String, MessageCodec<?>> byChannel = new ConcurrentHashMap<>();

    /**
     * Registers a codec for a specific message type.
     * <br>
     * This method adds a codec to the registry, associating it with its message type
     * and channel. The codec can then be used to encode and decode messages of that type
     * when they are sent or received over the bus.
     * <br>
     * The codec must implement the {@link MessageCodec} interface and provide the necessary
     * logic for encoding and decoding messages. The type of the codec must extend {@link BusMessage},
     * ensuring that it is compatible with the bus message system.
     * <br>
     * This method is thread-safe and can be called concurrently from multiple threads.
     * <br>
     * Example usage:
     * <pre>
     *     CodecRegistry registry = new CodecRegistry();
     *     registry.register(new MyMessageCodec());
     * </pre>
     *
     * @param codec the codec to register. It must not be null and must implement
     *              {@link MessageCodec} for a type that extends {@link BusMessage}.
     *              The codec will be associated with its message type and channel.
     * @param <T>   the type of message that the codec handles, which must extend {@link BusMessage}.
     * @throws NullPointerException     if the codec is null.
     * @throws IllegalArgumentException if the codec's type is not a subclass of {@link BusMessage} or if the channel is null.
     */
    public <T extends BusMessage> void register(@NonNull MessageCodec<T> codec) {
        byType.put(codec.type(), codec);
        byChannel.put(codec.channel(), codec);
    }

    /**
     * Retrieves a codec for a specific message type.
     * <br>
     * This method looks up a codec in the registry based on the provided message type.
     * If a codec is found for the specified type, it is returned; otherwise, null
     * is returned. The codec can then be used to encode and decode messages of that type
     * when they are sent or received over the bus.
     * <br>
     * The method is thread-safe and can be called concurrently from multiple threads.
     * <br>
     * Example usage:
     * <pre>
     *     CodecRegistry registry = new CodecRegistry();
     *     MessageCodec<MyMessage> codec = registry.forType(MyMessage.class);
     * </pre>
     *
     * @param type the class of the message type for which to retrieve the codec.
     *             It must not be null and must extend {@link BusMessage}. The method
     *             will return the codec associated with this type, if it exists in the registry.
     *             If no codec is found for the specified type, null is returned.
     * @param <T>  the type of message that the codec handles, which must extend {@link BusMessage}.
     * @return the codec for the specified message type, or null if no codec is registered for that type.
     * @throws NullPointerException if the type is null.
     * @throws ClassCastException   if the type is not a subclass of {@link BusMessage}.
     */
    @SuppressWarnings("unchecked")
    public <T extends BusMessage> MessageCodec<T> forType(Class<T> type) {
        return (MessageCodec<T>) byType.get(type);
    }

    /**
     * Retrieves a codec for a specific channel.
     * <br>
     * This method looks up a codec in the registry based on the provided channel name.
     * If a codec is found for the specified channel, it is returned; otherwise, null
     * is returned. The codec can then be used to encode and decode messages sent over that channel.
     * <br>
     * The method is thread-safe and can be called concurrently from multiple threads.
     * <br>
     * Example usage:
     * <pre>
     *     CodecRegistry registry = new CodecRegistry();
     *     MessageCodec<MyMessage> codec = registry.forChannel("my_channel");
     * </pre>
     *
     * @param ch the name of the channel for which to retrieve the codec.
     *           It must not be null. The method will return the codec associated with this channel,
     *           if it exists in the registry. If no codec is found for the specified channel, null is returned.
     * @return the codec for the specified channel, or null if no codec is registered for that channel.
     */
    public MessageCodec<?> forChannel(String ch) {
        return byChannel.get(ch);
    }
}