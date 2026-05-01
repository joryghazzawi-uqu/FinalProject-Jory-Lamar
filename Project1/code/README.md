# Smart Task Manager

A Java-based Smart Task Manager that integrates weather data to provide smart, weather-aware task scheduling recommendations. Built using Project Reactor for asynchronous operations, Swing for the GUI, and Jackson for JSON persistence.

---

## Features

- Task Management: Add, edit, delete, and view tasks with due dates and weather sensitivity.
- Weather Integration: Fetches weather data and updates task status based on conditions.
- Smart Recommendations: Provides clear scheduling advice based on weather conditions.
- Reactive Programming: Uses Project Reactor (Mono/Flux) for non-blocking operations.
- Persistence: Tasks are saved to a JSON file asynchronously.
- Swing GUI: Simple and user-friendly desktop interface.

---

## Requirements

- Java 17 or higher  
- Maven 3.6+  

---

## Installation

1. Clone or download the project.  
2. Navigate to the project directory:
   ```
   cd code
   ```
3. Compile the project:
   ```
   mvn compile
   ```

---

## Running the Application

Run the application using:

```
mvn exec:java
```

The Swing GUI will open.

---

## Weather API Setup

The API key is currently set directly in the code inside `MainApp.java`:

```java
String apiKey = "bcd27f96c4d0c0e34e6876798a6db7fa";
```

You can replace it with your own OpenWeatherMap API key if needed.

If no valid API key is provided, the application will automatically use fallback weather data.

---

## Usage

1. Add a Task  
   - Click "Add Task"  
   - Enter task title and description  
   - Set due date and time (format: yyyy-MM-dd HH:mm)  
   - Check "Weather Sensitive" if needed  
   - Click OK  

2. View Tasks  
   - Tasks appear in the table with:
     - ID  
     - Title  
     - Due Time  
     - Weather Sensitivity  
     - Status  

3. Check Weather  
   - Select a task  
   - Click "Check Weather"  
   - The status column will display messages such as:
     - Rain expected - consider rescheduling  
     - High temperature - consider a cooler time  
     - Suitable weather conditions  

4. Get Suggestions  
   - Click "Get Suggestions"  
   - A popup will show scheduling advice for all tasks  

5. Edit or Delete Tasks  
   - Select a task  
   - Use "Edit Task" or "Delete Task"  

6. Refresh  
   - Click "Refresh" to reload tasks  

---

## Architecture

- API Layer  
  TaskManager, TaskService, SchedulePlanner  

- Implementation Layer  
  Reactive services using Project Reactor  

- UI Layer  
  Swing interface (taskmanager.ui.swing)  

- Persistence  
  JSON storage using Jackson  

- Weather  
  Weather service with fallback support  

---

## Notes

- Background operations run on separate threads using Reactor.  
- UI updates use SwingUtilities.invokeLater.  
- The system provides user-friendly recommendations based on weather.  

---

## License

This project is for educational purposes.