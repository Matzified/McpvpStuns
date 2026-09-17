# 🛡️ McPvpStuns

![Minecraft](https://img.shields.io/badge/Minecraft-1.21%2B%20%7C%2026.1-brightgreen?style=for-the-badge)
![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Purpur%20%7C%20Fabric-007acc?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21--25-orange?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**McPvpStuns** brings **mcpvp.club** shield stun and 1-tick stun-slam mechanics to Paper servers and Fabric setups. It automatically toggles Paper's `skip-vanilla-damage-tick-when-shield-blocked` setting on startup and removes the default 10-tick damage immunity window when a shield gets disabled, allowing instant axe-to-mace follow-up hits and clean knockback.

---

> 🎉 **v1.0.0 — Initial Release**  
> Initial release of McPvpStuns with Paper global config auto-patching and 1-tick hit detection support across Paper and Fabric.

---

## Features

- **Paper Config Auto-Setup**: Modifies `config/paper-global.yml` on launch so you do not have to edit it manually.
- **Zero I-Frames on Shield Break**: Clears damage immunity when a shield is disabled so follow-up hits register immediately.
- **1-Tick Stun Slams**: Allows fast weapon swaps (such as axe into mace) to land hits on the exact tick the shield drops.
- **Follow-up Knockback**: Suppresses knockback during the shield break itself and applies knockback on the follow-up hit.
- **5-Second Shield Cooldown**: Keeps the default 100-tick shield cooldown.
- **In-Game Reload**: Update options with `/mcstuns reload`.

---

## Configuration (`config.yml`)

```yaml
# Auto-configure paper-global.yml on boot
auto-configure-paper-yml: true

# Enable stun mechanics
runtime-stun-override: true

# Shield disable cooldown in ticks (100 ticks = 5 seconds)
shield-cooldown-ticks: 100

# Reset invulnerability ticks when shield breaks
reset-invulnerability-on-break: true

# Apply knockback to the follow-up hit instead of the shield break
knockback-on-followup-only: true

# Window in milliseconds to register follow-up hits
followup-window-ms: 600

# Follow-up knockback multipliers
followup-knockback-horizontal-multiplier: 1.0
followup-knockback-vertical-multiplier: 1.0

# Sound effects
play-stun-sound: true
play-slam-sound: true
```

---

## Commands & Permissions

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/mcstuns status` | `mcpvpstuns.admin` | View current status |
| `/mcstuns reload` | `mcpvpstuns.admin` | Reload configuration |
| `/stuns`, `/mcpvpstuns` | `mcpvpstuns.admin` | Aliases |

---

## Installation

### Paper / Purpur
1. Put `McPvpStuns-Paper-1.0.0.jar` into your `plugins/` folder.
2. Start your server. The plugin will configure `paper-global.yml` automatically.
3. Restart once if Paper reports config updates.

### Fabric
1. Put `McPvpStuns-Fabric-1.0.0+26.1.jar` into your `mods/` folder.
2. Requires Fabric Loader and Fabric API.

---

## Building from Source

### Paper
```bash
cd paper
mvn clean package
```

### Fabric
```bash
cd fabric
./gradlew build
```

---

## License
MIT License.
