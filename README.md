# end-fight
A Minecraft Spigot/Paper plugin that creates a competitive server-wide event when the Ender Dragon is killed: the Dragon Egg spawns at a random overworld location, broadcasts its coordinates, allows temporary teleportation to the carrier, and declares a winner after holding the egg for a set duration.

# 🐉 End Fight  
A competitive Minecraft event plugin for Spigot/Paper servers.  
When the Ender Dragon is killed, the plugin creates a server-wide race to find, steal, and hold the Dragon Egg!

---

## ✨ Features

### 📍 Random Overworld Egg Spawn  
- When the Ender Dragon dies, the Dragon Egg **spawns at a random location in the Overworld**.  
- The plugin automatically **broadcasts the coordinates** to the entire server.

### 🏃 Egg Hunt Race  
- The **first player** to pick up the egg becomes the **Egg Carrier**.  
- All players can run to fight, steal, or defend the egg carrier.

### 🚀 `/eggtp` Temporary Teleportation  
- Once the egg is picked up, every player can use:
- `/eggtp` teleports them **directly to the Egg Carrier**, but **only for 60 seconds** after egg pickup.

### ⏱ Hold-to-Win System  
- If a player holds the Dragon Egg for **120 seconds**, they are declared the **Winner**.  
- Timer resets if the egg drops.

### 🛠 Known Issue (Work in Progress)
Currently, the Dragon Egg **also spawns in the End** after the dragon dies.  
This is a Minecraft mechanic, and the plugin does not yet automatically remove or relocate that egg.

**Temporary workaround:**  
Admins must manually break/remove the End Egg until automatic handling is added in a future update.

---

## 🧪 Tested On  
- Paper 1.20+  
- Spigot 1.20+  
(Should work on most modern versions — open an issue if you find a problem.)

---

## 📥 Installation
1. Download the plugin `.jar`.  
2. Drop it into your server’s `/plugins` folder.  
3. Restart the server.  
4. Enjoy the Dragon Egg race events!

---

## 🔧 Commands

| Command | Description |
|--------|-------------|
| `/eggtp` | Teleports the player to the Egg Carrier (only usable during the 60-second TP window). |

---

## 📅 Future Updates  
- Automatic suppression/removal of the **End-spawned Dragon Egg**  
- Configuration file for customizing:  
- Egg hold duration  
- TP window duration
- Broadcast messages
- Optional rewards for winners

---

## 🤝 Contributing
Pull requests are welcome!  
If you find any bugs or have suggestions, please create an issue in the repository.

---

## 📜 License
MIT License
