# Modrinth Project Details: McPvpStuns

---

### 📝 Short Summary (Project Description - Max 140 chars)
> Replicates 1:1 mcpvp.club shield stuns & 1-tick stun slams with auto Paper config & zero i-frames on shield break.

---

### 📦 Version / Release Text (v1.0.0 — Initial Release)
```markdown
### 🛡️ McPvpStuns v1.0.0 — Initial Release

This initial release introduces authentic **1:1 mcpvp.club shield stun mechanics** across both **Paper servers** and **Fabric client/server setups** for Minecraft 1.21+ / 26.1!

#### 🚀 What's New:
- **Zero I-Frames on Shield Break**: Disables the vanilla 10-tick invulnerability window so follow-up hits register immediately.
- **1-Tick Stun Slams**: Allows instant follow-up strikes (axe-to-mace swaps, double-clicks, and sword combos) without dropped hits.
- **Target Knockback Routing**: Cancels launch velocity on the shield break itself to keep targets in combo range, then applies knockback on the follow-up hit.
- **Auto Paper Global Config**: Automatically detects and enables `skip-vanilla-damage-tick-when-shield-blocked: true` in `config/paper-global.yml`.
- **Dual-Platform Builds**: Separate production-ready JARs for Paper (1.21+) and Fabric (1.21+ / 26.1).
- **Admin Hot-Reload**: Live reload settings via `/mcstuns reload`.
```

---

### 📄 Main Body Description (Formatted for Modrinth)

```markdown
<p align="center">
  <img src="https://raw.githubusercontent.com/Matzified/McpvpStuns/main/logo.png" alt="McPvpStuns Logo" width="550" />
</p>

<p align="center">
  <a href="https://github.com/Matzified/McpvpStuns"><img src="https://img.shields.io/badge/Minecraft-1.21%2B%20%7C%2026.1-brightgreen?style=for-the-badge" alt="Minecraft Version"></a>
  <a href="https://github.com/Matzified/McpvpStuns"><img src="https://img.shields.io/badge/Platform-Paper%20%7C%20Purpur%20%7C%20Fabric-007acc?style=for-the-badge" alt="Platforms"></a>
  <a href="https://github.com/Matzified/McpvpStuns"><img src="https://img.shields.io/badge/Java-21--25-orange?style=for-the-badge" alt="Java Version"></a>
  <a href="https://github.com/Matzified/McpvpStuns/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="License"></a>
</p>

**McPvpStuns** brings genuine **mcpvp.club** shield stun and 1-tick stun-slam mechanics to modern Paper servers and Fabric setups (1.21+ / 26.1). 

In modern vanilla combat, disabling a shield triggers an unintended 10-tick (0.5s) damage immunity window that swallows follow-up strikes and breaks combo momentum. **McPvpStuns** eliminates this delay, automatically manages Paper's internal `skip-vanilla-damage-tick-when-shield-blocked` engine flag on startup, and ensures knockback is delivered cleanly on the follow-up hit rather than the shield break itself.

---

## ✨ Features

- **⚡ Zero Invulnerability Delay**: Clears damage immunity when a shield is disabled by an axe so follow-up hits register immediately.
- **💥 Reliable 1-Tick Stun Slams**: Enables fluid axe-to-mace swaps, double-click hits, and sword combos without dropped hits.
- **🎯 Follow-Up Knockback Routing**: Dampens velocity on the shield break strike itself to prevent opponents from flying out of reach, then applies knockback on the immediate follow-up strike.
- **⚙️ Automated Paper Setup**: Automatically checks and configures `config/paper-global.yml` on launch with safety backups (`.bak`). No manual YAML editing needed.
- **⏱️ Standard 5.0s Cooldown**: Maintains the authentic 100-tick competitive shield recovery window.
- **🔄 Live Hot-Reload**: Update config options on the fly with `/mcstuns reload` without interrupting duels or server operations.
- **🔌 Dual-Platform Support**: Works out of the box as a Paper plugin or as a Fabric server/client mod.

---

## 🎮 Mechanic Comparison

| Feature | Vanilla Minecraft (1.21+) | McPvpStuns (mcpvp.club style) |
| :--- | :--- | :--- |
| **Shield Break Damage Immunity** | ⏳ 10 Ticks (Swallows follow-up hits) | ⚡ **0 Ticks (Instant follow-up allowed)** |
| **Axe-to-Mace Stun Slam** | ❌ Swallowed / Blocked by i-frames | ✅ **100% Hit Registration & Damage** |
| **Shield Break Velocity** | 💨 Pushes defender back mid-break | 🎯 **Velocity suppressed to keep target in range** |
| **Follow-Up Hit Knockback** | ⚠️ Inconsistent / Ignored by i-frames | 💥 **Delivered directly on follow-up contact** |
| **Paper Config Setup** | 📝 Manual YAML editing | 🤖 **Fully automated on launch** |

---

## ⚙️ Configuration (`config.yml`)

The default configuration works out of the box with competitive PvP defaults:

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

## 📜 Commands & Permissions

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/mcstuns status` | `mcpvpstuns.admin` | Displays current engine status and configuration |
| `/mcstuns reload` | `mcpvpstuns.admin` | Reloads `config.yml` and re-verifies Paper configs |
| `/stuns`, `/mcpvpstuns` | `mcpvpstuns.admin` | Aliases for `/mcstuns` |

> *Permissions default to server operators (`op: true`).*

---

## 📦 Installation

### Paper / Purpur Server
1. Download `McPvpStuns-Paper-1.0.0.jar`.
2. Drop it into your server's `plugins/` directory.
3. Start the server (Java 21+). The plugin will automatically configure `paper-global.yml`.
4. Restart once if Paper reports config updates to apply native kernel options.

### Fabric (Client or Server)
1. Download `McPvpStuns-Fabric-1.0.0+26.1.jar`.
2. Drop it into your `.minecraft/mods` or server `mods/` directory.
3. Requires Fabric Loader `0.16.0+` and Fabric API.

---

## 📄 License
This project is licensed under the [MIT License](https://opensource.org/licenses/MIT). Free for public, private, and commercial server use.
```
