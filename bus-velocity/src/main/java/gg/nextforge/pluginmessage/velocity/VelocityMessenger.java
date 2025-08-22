package gg.nextforge.pluginmessage.velocity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velocitypowered.api.proxy.ProxyServer;
import gg.nextforge.pluginmessage.api.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the Messenger interface for Velocity.
 * Handles incoming and outgoing plugin messages using Redisson and Jackson for serialization.
 * This class provides methods to register and unregister raw and typed message listeners,
 * send raw messages, and manage message channels.
 * <br>
 * It uses a Redisson client to communicate with Redis topics, allowing for efficient message
 * distribution across different instances of the plugin. The ObjectMapper from Jackson is used
 * for serializing and deserializing messages into JSON format.
 * <br>
 * The class is thread-safe and designed to handle concurrent access, ensuring that message
 * listeners can be registered and unregistered dynamically at runtime without
 * issues. It also provides a mechanism to handle typed messages through a codec registry,
 * allowing for type-safe message handling.
 * <br>
 * The messenger can be closed to clean up resources, removing all listeners and topics.
 * <br>
 * @author FrogTheDev
 * @since 1.0
 */
@RequiredArgsConstructor
public class VelocityMessenger implements Messenger {

    private final @NonNull RedissonClient redisson; // Redisson client for Redis communication
    private final @NonNull Object plugin; // Velocity plugin instance
    private final @NonNull ProxyServer proxy; // ProxyServer instance
    private final @NonNull String pluginName; // For source identification
    private final @NonNull String namespace; // Namespace for message topics
    private final @NonNull CodecRegistry codecs; // Registry for message codecs

    private final ObjectMapper mapper = new ObjectMapper(); // JSON serializer/deserializer

    private final Map<String, Set<RawListener>> rawListeners = new ConcurrentHashMap<>(); // Raw message listeners
    private final Map<String, RTopic> topics = new ConcurrentHashMap<>(); // Redis topics
    private final Set<String> outgoing = ConcurrentHashMap.newKeySet(); // Outgoing channels
    private final Map<Class<?>, Set<TypedListener<?>>> typedHandlers = new ConcurrentHashMap<>(); // Typed message handlers

    /**
     * Constructs the topic name for a given channel.
     *
     * @param ch The channel name.
     * @return The constructed topic name.
     */
    private String topicName(String ch){ return namespace + ":ch:" + ch; }

    /**
     * Registers a listener for incoming messages on a specific channel.
     *
     * @param ch The channel name.
     * @param l  The listener to register.
     */
    @Override
    public void registerIncomingChannel(@NonNull String ch, @NonNull RawListener l) {
        rawListeners.computeIfAbsent(ch, k -> {
            var t = redisson.getTopic(topicName(ch));
            t.addListener(byte[].class, (ignored, bytes) -> handleIncoming(ch, bytes));
            topics.put(ch, t);
            return ConcurrentHashMap.newKeySet();
        }).add(l);
    }

    /**
     * Unregisters a listener for incoming messages on a specific channel.
     *
     * @param ch The channel name.
     * @param l  The listener to unregister.
     */
    @Override
    public void unregisterIncomingChannel(@NonNull String ch, @NonNull RawListener l) {
        var set = rawListeners.get(ch);
        if (set == null) return;
        set.remove(l);
        if (set.isEmpty()) {
            var t = topics.remove(ch);
            if (t != null) t.removeAllListeners();
            rawListeners.remove(ch);
        }
    }

    /**
     * Registers an outgoing channel.
     *
     * @param ch The channel name.
     */
    @Override
    public void registerOutgoingChannel(@NonNull String ch) { outgoing.add(ch); }

    /**
     * Unregisters an outgoing channel.
     *
     * @param ch The channel name.
     */
    @Override
    public void unregisterOutgoingChannel(@NonNull String ch) { outgoing.remove(ch); }

    /**
     * Sends a raw message to a specific channel and target.
     *
     * @param ch     The channel name.
     * @param target The target recipient (nullable).
     * @param data   The message data.
     */
    @Override
    public void sendRaw(@NonNull String ch, @Nullable String target, byte @NonNull [] data) {
        if (!outgoing.contains(ch)) throw new IllegalStateException("Outgoing not registered: " + ch);
        var env = Envelope.builder()
                .v("1").channel(ch)
                .source(pluginName + "@proxy")
                .target(target)
                .timestamp(Instant.now().toEpochMilli())
                .id(UUID.randomUUID())
                .data(data).build();
        try { redisson.getTopic(topicName(ch)).publish(mapper.writeValueAsBytes(env)); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Handles incoming messages for a specific channel.
     *
     * @param channel The channel name.
     * @param bytes   The raw message data.
     */
    private void handleIncoming(String channel, byte[] bytes) {
        Envelope env;
        try { env = mapper.readValue(bytes, Envelope.class); }
        catch (Exception e) { return; }
        proxy.getScheduler().buildTask(this.plugin, () -> {
            // RAW
            rawListeners.getOrDefault(channel, Set.of())
                    .forEach(l -> safe(() -> l.onMessage(env.getChannel(), env.getSource(), env.getTarget(), env.getData())));
            // TYPED
            var codec = codecs.forChannel(channel);
            if (codec != null) {
                try {
                    var msg = codec.decode(env.getData());
                    var handlers = typedHandlers.getOrDefault(codec.type(), Set.of());
                    //noinspection rawtypes, unchecked
                    handlers.forEach(h -> safe(() -> ((TypedListener)h).onMessage(msg, env.getSource(), env.getTarget())));
                } catch (Exception ignored) {}
            }
        }).schedule();
    }

    /**
     * Executes a runnable safely, catching and ignoring any exceptions.
     *
     * @param r The runnable to execute.
     */
    private static void safe(Runnable r){ try { r.run(); } catch (Throwable ignored){} }

    /**
     * Registers a typed message handler for a specific message type.
     *
     * @param type    The message type.
     * @param handler The handler to register.
     * @param <T>     The type of the message.
     */
    @Override
    public <T extends BusMessage> void registerHandler(@NonNull Class<T> type, @NonNull TypedListener<T> handler) {
        typedHandlers.computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet()).add(handler);
        var codec = codecs.forType(type);
        if (codec != null) registerIncomingChannel(codec.channel(), (c,s,t,d)->{});
    }

    /**
     * Unregisters a typed message handler for a specific message type.
     *
     * @param type    The message type.
     * @param handler The handler to unregister.
     * @param <T>     The type of the message.
     */
    @Override
    public <T extends BusMessage> void unregisterHandler(@NonNull Class<T> type, @NonNull TypedListener<T> handler) {
        var set = typedHandlers.get(type);
        if (set != null) set.remove(handler);
    }

    /**
     * Sends a typed message to a specific target.
     *
     * @param message The message to send.
     * @param target  The target recipient.
     * @param <T>     The type of the message.
     */
    @Override
    public <T extends BusMessage> void send(@NonNull T message, String target) {
        var codec = codecs.forType(message.getClass());
        if (codec == null) throw new IllegalStateException("No codec for " + message.getClass().getName());
        registerOutgoingChannel(codec.channel());
        byte[] data;
        try {
            //noinspection unchecked
            data = ((MessageCodec<T>) codec).encode(message); }
        catch (Exception e) { throw new RuntimeException(e); }
        sendRaw(codec.channel(), target, data);
    }

    /**
     * Closes the messenger, cleaning up all resources.
     */
    @Override
    public void close() {
        topics.values().forEach(RTopic::removeAllListeners);
        topics.clear();
        rawListeners.clear();
        typedHandlers.clear();
        outgoing.clear();
    }
}