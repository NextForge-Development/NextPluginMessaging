# NextPluginMessaging

A modern, Redis-backed replacement for Minecraft's Plugin Messaging channels.  
Built for **Paper (Bukkit)** and **Velocity**, with support for **typed messages** and custom DTOs.  
Powered by **Redisson** and **Jackson**.

---

## ✨ Features

- 🔌 Drop-in style API inspired by Bukkit’s `Messenger`:
  - `registerIncomingChannel`, `registerOutgoingChannel`, `sendPluginMessage`
- 🎯 **Typed Messages**: Define your own message classes (`BusMessage`) instead of raw byte arrays.
- 📦 **Pluggable Codecs**: Use Jackson, Protobuf, or custom encoders.
- ⚡ **Cross-Server Communication**: No more client tunneling — servers and proxies talk directly.
- 🛡️ **Main Thread Safety**:
  - Paper listeners are executed back on the Bukkit main thread.
  - Velocity listeners are scheduled on the main event thread.
- 🧩 **Multi-Module**: Shared API, with `bus-bukkit` and `bus-velocity` implementations.
- 🗄️ **Namespace Isolation**: Each channel is isolated under a configurable Redis namespace.
- 🔒 **Extendable**: Swap out `RTopic` for `RStream` or `RQueue` for guaranteed delivery.

---

## 📦 Project Structure

```
NextPluginMessaging/
├─ bus-api/            # Shared API (no platform dependencies)
│  └─ gg.nextforge.pluginmessaging.api
├─ bus-bukkit/         # Paper (Bukkit) implementation
│  └─ gg.nextforge.pluginmessaging.bukkit
└─ bus-velocity/       # Velocity implementation
   └─ gg.nextforge.pluginmessaging.velocity
```

**Base package**: `gg.nextforge.pluginmessaging.[module]`

- **API**: `gg.nextforge.pluginmessaging.api`
- **Bukkit**: `gg.nextforge.pluginmessaging.bukkit`
- **Velocity**: `gg.nextforge.pluginmessaging.velocity`

---

## 🛠️ Installation

### Requirements
- Java 17+
- Redis 6+
- Gradle or Maven build system

### Gradle (Kotlin DSL)
Add the dependency to your module:

```kotlin
repositories {
    maven {
        name = "nextforge-repo"
        url = uri("https://repo.nextforge.gg/releases")
    }
}

dependencies {
    implementation("gg.nextforge:pluginmessaging-api:1.0.0")
    implementation("gg.nextforge:pluginmessaging-bukkit:1.0.0") // for Paper
    implementation("gg.nextforge:pluginmessaging-velocity:1.0.0") // for Velocity
}
```

And make sure you have the Paper repository:

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}
```

---

## ⚙️ Configuration

Make sure you have a Redis instance running and accessible.

---

## 🚀 Usage

### 1. Define your own message class
```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PartyInvite implements BusMessage {
    private String partyId;
    private String inviter;
    private String targetPlayer;
    private long expiresAt;
}
```

### 2. Provide a codec
```java
public class PartyInviteCodec implements MessageCodec<PartyInvite> {
    private static final ObjectMapper M = new ObjectMapper();
    @Override public byte[] encode(PartyInvite msg) throws Exception { return M.writeValueAsBytes(msg); }
    @Override public PartyInvite decode(byte[] data) throws Exception { return M.readValue(data, PartyInvite.class); }
    @Override public String channel() { return "party:invite"; }
    @Override public Class<PartyInvite> type() { return PartyInvite.class; }
}
```

### 3. Register codec and messenger
```java
CodecRegistry registry = new CodecRegistry();
registry.register(new PartyInviteCodec());

var messenger = new BukkitMessenger(redisson, plugin, "mc:bus", registry);

messenger.registerHandler(PartyInvite.class, (msg, source, target) -> {
    plugin.getLogger().info("Invite " + msg.getTargetPlayer() + " to " + msg.getPartyId());
});
```

### 4. Send a message
```java
messenger.send(PartyInvite.builder()
    .partyId("abc-123")
    .inviter("SoldatMax")
    .targetPlayer("Steve")
    .expiresAt(System.currentTimeMillis() + 60000)
    .build(), "ALL");
```

---

## 🧩 Velocity Integration

Same API, different entrypoint:

```java
var messenger = new VelocityMessenger(redisson, proxy, "MyPlugin", "mc:bus", registry);

messenger.registerHandler(PartyInvite.class, (msg, source, target) -> {
    proxy.getConsoleCommandSource().sendMessage(Component.text("Invite for " + msg.getTargetPlayer()));
});
```

---

## 📖 License

MIT License © 2025 NextForge | See [LICENSE](LICENSE) for details.

## 📞 Support
For issues, suggestions, or contributions, please open an issue on GitHub: [NextPluginMessaging Issues](https://github.com/NextForge-Development/NextPluginMessaging/issues)
Or join our Discord: [NextForge Discord](https://discord.com/invite/nextforge)
