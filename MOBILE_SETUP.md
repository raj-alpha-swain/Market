# 📱 Mobile Phone Setup Instructions

## The Problem

When testing your React Native app on a **physical mobile phone** via Expo Go, using `localhost` won't work because:
- `localhost` on your phone refers to the phone itself
- Your Spring Boot backend is running on your **computer**, not your phone
- Your phone needs to connect to your computer's IP address on the local network

## ✅ Solution Applied

I've updated `App.js` to use your computer's IP address: **`192.168.1.1`**

## 📋 Steps to Test on Your Phone

### 1. Ensure Same WiFi Network
- Make sure your **phone** and **computer** are connected to the **same WiFi network**
- This is CRITICAL - they must be on the same network!

### 2. Check Your Computer's IP Address
Your detected IP addresses are:
- `192.168.10.1`
- `192.168.149.1`
- `192.168.1.1` ← **Currently used in the app**

If the app still doesn't connect, try updating `App.js` line 7 to use one of the other IP addresses.

### 3. Allow Firewall Access (Important!)
Windows Firewall might be blocking port 8080. When you first run the backend, Windows should show a firewall prompt - **click "Allow access"**.

If you didn't see the prompt or accidentally blocked it:

**Option A: Quick Command (Run in PowerShell as Administrator)**
```powershell
netsh advfirewall firewall add rule name="Spring Boot Backend" dir=in action=allow protocol=TCP localport=8080
```

**Option B: Manual Firewall Setup**
1. Open Windows Defender Firewall
2. Click "Advanced settings"
3. Click "Inbound Rules" → "New Rule"
4. Select "Port" → Next
5. Select "TCP" and enter port `8080` → Next
6. Select "Allow the connection" → Next
7. Check all profiles (Domain, Private, Public) → Next
8. Name it "Spring Boot Backend" → Finish

### 4. Verify Backend is Running
In terminal, check if backend is accessible:
```powershell
curl http://192.168.1.1:8080/api/status
```

Should return:
```json
{"status":"Server is running","active":true}
```

### 5. Test on Your Phone
1. Open Expo Go app on your phone
2. Scan the QR code from the `npm start` terminal
3. App should load on your phone
4. Tap the **"Check Server"** button
5. Should see ✅ "Server is running" with green background

## 🔧 Troubleshooting

### If you see "Network request failed" or error message:

#### Check 1: WiFi Network
- Verify both devices are on the SAME WiFi
- Some public/guest WiFi networks block device-to-device communication

#### Check 2: IP Address
The app is currently using `192.168.1.1`. To change it:
1. Open `mobile-app/App.js`
2. Find line 7: `const API_BASE_URL = 'http://192.168.1.1:8080/api';`
3. Replace `192.168.1.1` with the correct IP from your network
4. App will reload automatically

#### Check 3: Firewall
- Make sure Windows Firewall allows port 8080
- Run the PowerShell command above (as Administrator)

#### Check 4: Backend Running
- Ensure `.\start-backend.ps1` is running in a terminal
- Check terminal for error messages
- Should see "Started BackendApplication"

### Still not working?

**Test from your computer first:**
1. In terminal: `npm start`
2. Press `w` to open in web browser
3. Click "Check Server" button
4. If it works in browser but not on phone, it's a network/firewall issue

## 📝 Quick Reference

| Component | Address | Status Check |
|-----------|---------|--------------|
| Backend API | `http://192.168.1.1:8080` | Browser: http://192.168.1.1:8080/api/status |
| Expo Dev Server | `http://localhost:19006` | Shows in npm start output |
| Mobile App | Via Expo Go | Scan QR code |

## 💡 Pro Tips

1. **IP Address Changes**: If your computer's IP changes (e.g., after restarting router), update line 7 in `App.js`
2. **Easier Testing**: Use web version (press `w`) for quick testing without phone
3. **Error Messages**: The app now shows error messages - read them for hints!
4. **API URL Display**: The app shows the API URL it's trying to connect to

---

**Need the IP address again?**
Run: `ipconfig | findstr "IPv4"`

**Current Status in App:**
The app now displays:
- 📡 The API URL it's using
- ⚠️ Any error messages if connection fails
- ✅/❌ Server status when connection succeeds
- 💬 Reminder about WiFi network
