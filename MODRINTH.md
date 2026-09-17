# McPvpStuns

```
Short Description (Modrinth / Hangar / Spigot):
Replicates mcpvp.club shield stuns and 1-tick stun slams with zero i-frames on shield break and automatic paper-global.yml configuration.
```

---

### Release Notes (v1.0.0)

Initial release for 1.21+ / 26.1 across Paper and Fabric.

- Removes the vanilla 10-tick invulnerability window on shield break.
- Enables 1-tick axe-to-mace swaps, double-clicks, and follow-up hits.
- Suppresses horizontal launch velocity on shield break so targets stay in reach; applies knockback on the follow-up hit.
- Automatically sets `skip-vanilla-damage-tick-when-shield-blocked: true` in `config/paper-global.yml` on launch.
- Supports runtime reload via `/mcstuns reload`.

---

### Description

<p align="center">
  <img src="https://raw.githubusercontent.com/Matzified/McpvpStuns/main/logo.png" alt="McPvpStuns Logo" width="550" />
</p>

<p align="center">
  <a href="https://github.com/Matzified/McpvpStuns"><img src="https://img.shields.io/badge/Minecraft-1.21%2B%20%7C%2026.1-brightgreen?style=for-the-badge" alt="Minecraft Version"></a>
  <a href="https://github.com/Matzified/McpvpStuns"><img src="https://img.shields.io/badge/Platform-Paper%20%7C%20Purpur%20%7C%20Fabric-007acc?style=for-the-badge" alt="Platforms"></a>
  <a href="https://github.com/Matzified/McpvpStuns"><img src="https://img.shields.io/badge/Java-21--25-orange?style=for-the-badge" alt="Java Version"></a>
  <a href="https://github.com/Matzified/McpvpStuns/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="License"></a>
</p>

McPvpStuns replicates mcpvp.club shield stun and 1-tick stun-slam mechanics on Paper servers and Fabric setups (1.21+ / 26.1).

In vanilla combat, disabling a shield triggers an unintended 10-tick (0.5s) damage immunity window that swallows follow-up strikes and breaks combo timing. McPvpStuns clears this delay, configures Paper's `skip-vanilla-damage-tick-when-shield-blocked` option on startup, and routes knockback to the follow-up strike instead of the shield break itself.

---

## Features

- **Zero Invulnerability Ticks**: Clears damage immunity when a shield is disabled by an axe so follow-up hits register immediately.
- **1-Tick Stun Slams**: Allows fast weapon swaps (such as axe into mace) to land hits on the exact tick the shield drops.
- **Follow-up Knockback**: Dampens knockback during the shield break itself and applies full knockback on the follow-up hit.
- **Automatic Paper Setup**: Automatically checks and configures `config/paper-global.yml` on startup with safety backups (`.bak`).
- **Standard 5-Second Cooldown**: Keeps the default 100-tick shield cooldown.
- **In-Game Reload**: Update configuration values on the fly with `/mcstuns reload`.
- **Dual-Platform**: Works out of the box on Paper and Fabric.

---

## Mechanic Comparison

| Mechanic | Vanilla Minecraft (1.21+) | McPvpStuns (mcpvp.club style) |
| :--- | :--- | :--- |
| Shield Break Damage Immunity | 10 Ticks (swallows follow-up hits) | 0 Ticks (follow-up hits register immediately) |
| Axe-to-Mace Stun Slam | Swallowed or delayed by i-frames | Consistent hit registration and damage |
| Shield Break Velocity | Pushes defender back mid-break | Suppressed to keep target in combo range |
| Follow-Up Hit Knockback | Frequently dropped due to i-frames | Delivered directly on follow-up contact |
| Paper Config Setup | Manual YAML editing | Automated on launch |

---

## Configuration (`config.yml`)

```yaml
# Automatically configure paper-global.yml on boot
auto-configure-paper-yml: true

# Enable runtime shield stun and 1-tick follow-up handling
runtime-stun-override: true

# Shield disable cooldown in ticks (100 ticks = 5.0 seconds)
shield-cooldown-ticks: 100

# Reset target invulnerability frames on shield break
reset-invulnerability-on-break: true

# Apply knockback to the follow-up strike instead of the shield break
knockback-on-followup-only: true

# Time window (in milliseconds) to register the follow-up combo
followup-window-ms: 600

# Optional horizontal and vertical multipliers for follow-up knockback
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
| `/mcstuns status` | `mcpvpstuns.admin` | View current status and active configuration |
| `/mcstuns reload` | `mcpvpstuns.admin` | Reload config.yml and re-verify Paper configs |
| `/stuns`, `/mcpvpstuns` | `mcpvpstuns.admin` | Command aliases |

---

## Installation

### Paper / Purpur
1. Put `McPvpStuns-Paper-1.0.0.jar` into your `plugins/` directory.
2. Start the server (Java 21+). The plugin will configure `paper-global.yml` automatically.
3. Restart once if Paper reports config updates.

### Fabric
1. Put `McPvpStuns-Fabric-1.0.0+26.1.jar` into your `mods/` directory.
2. Requires Fabric Loader and Fabric API.

---

## License

MIT License.
