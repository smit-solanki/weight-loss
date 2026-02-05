# 📋 Weight Loss & Health Suite

A comprehensive Android fitness application designed to help users track health metrics, hydration, and physical activity in one unified dashboard. This project demonstrates advanced Android development concepts including **Hardware Sensor integration**, **Data Persistence**, and **Dynamic UI Animations**.

---

## ✨ Key Features

### 🚶 Smart Step Counter
* **Hardware Integration:** Utilizes the device's `TYPE_STEP_COUNTER` sensor for high-accuracy, battery-efficient activity tracking.
* **State Persistence:** Implemented custom logic using `SharedPreferences` to save step baselines, ensuring data remains persistent even if the app is killed or the device restarts.
* **Activity Recognition:** Handles runtime permissions for physical activity tracking as required by modern Android versions (Android 10+).

### 💧 Interactive Hydration Tracker
* **Circular Progress UI:** Custom XML-based circular progress bar with `ObjectAnimator` for smooth, real-time visual updates.
* **Dynamic Goal Setting:** Flexible goal management allows users to customize daily intake targets (e.g., 2000ml to 4000ml) via an interactive popup menu.
* **Automated Reset:** Integrated **Temporal Logic** to automatically reset daily water intake at midnight based on system time.

### 🥗 Health Analytics (BMI/BMR)
* **Personalized Metrics:** Calculates Body Mass Index (BMI) and Basal Metabolic Rate (BMR) using personalized user data (Age, Weight, Height, and Gender).
* **Calorie Budgeting:** Automatically calculates a daily calorie goal to guide users toward their weight management targets.
* **Persistent History:** Saves the latest health metrics to provide a consistent user experience across sessions.

### 📈 Progress Visualization
* **MPAndroidChart Integration:** Visualizes weight fluctuations and fitness progress over time using interactive, professional-grade line graphs.

---

## 🛠️ Technical Stack
* **Language:** Java
* **UI Framework:** XML / Material Design Components
* **Data Storage:** SharedPreferences (Internal Data Persistence)
* **Hardware Sensors:** Android SensorManager (Step Counter)
* **Animation:** Android Property Animation API (`ObjectAnimator`)
* **External Libraries:** MPAndroidChart

---

👤 Author
Smit Solanki

GitHub: @smit-solanki

Portfolio: smit-solanki.github.io
