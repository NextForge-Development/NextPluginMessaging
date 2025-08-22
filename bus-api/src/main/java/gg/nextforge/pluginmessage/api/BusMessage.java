package gg.nextforge.pluginmessage.api;

/**
 * Interface representing a message that can be sent over the bus.
 * <br>
 * This interface serves as a marker for messages that are intended to be
 * communicated through the bus system. Implementing classes should
 * provide the necessary data and functionality for the message.
 * <br>
 * Messages can be used to convey various types of information, such as
 * events, commands, or data updates, and can be processed by
 * subscribers listening for specific message types.
 * <br>
 * Implementing classes should ensure that they are serializable if they
 * need to be transmitted over a network or stored for later use.
 *
 * @author FrogTheDev
 * @since 1.0
 */
public interface BusMessage {
}
