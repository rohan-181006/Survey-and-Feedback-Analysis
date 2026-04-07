# Survey & Feedback Analysis System

A modern Java Swing desktop application designed for creating, filling, and analyzing surveys with local SQLite persistence. This system provides a professional interface for data collection and real-time visual analytics.

## 📋 Features

* **Survey Management**: Create custom surveys with titles, descriptions, and categories.
* **Dynamic Question Types**: Supports Multiple Choice, Rating (1-5), Yes/No, and Open-ended Text responses.
* **Interactive Dashboard**: At-a-glance overview of total surveys, responses, and average satisfaction scores.
* **Real-time Analysis**: Visualizes data using custom-painted bar charts and rating progress bars.
* **Multithreaded Processing**: Uses background workers for heavy analytics computation to keep the UI responsive.
* **Data Export**: Generates and saves detailed text-based reports to local storage.
* **Dual Persistence**: Combines SQLite for structured data and Java Serialization for survey backups.

## 🛠️ Technologies Used

* **Language**: Java (Supports versions 17, 21, and 24).
* **GUI Framework**: Java Swing (MVC Architecture).
* **Database**: SQLite via JDBC.
* **Build Tool**: Apache Maven.
* **Design**: Modern Dark/Monochrome Theme with custom Cell Renderers and Painting.

## 🚀 Getting Started

### Prerequisites
* **JDK**: Java 17 or higher (configured for Java 24 by default).
* **Maven**: Apache Maven installed and configured in your system PATH.

### Installation
1.  Clone or download the repository.
2.  Navigate to the root directory:
    ```bash
    cd Survey-and-Feedback-Analysis-main
    ```

### Running the Application
You can run the application directly using Maven:
```bash
mvn clean compile exec:java
```

*Note: If you encounter an `invalid target release: 24` error, update the `<properties>` in `pom.xml` to match your installed JDK version (e.g., change `24` to `17`).*

## 📁 Project Structure

* `com.survey.ui`: Contains the main application window (`SurveyApp`), analysis logic (`AnalysisPanel`), and custom dialogs.
* `com.survey.dao`: Data Access Objects for Surveys and Responses, including `DatabaseManager` for SQLite connection handling.
* `com.survey.model`: Core data entities: `Survey`, `Question`, and `Response`.
* `com.survey.beans`: Implements the JavaBeans event model for decoupled UI communication.
* `com.survey.util`: Utility classes for background threading (`AnalyticsWorker`) and File I/O (`SerializationUtil`).

## 🎓 Concepts Demonstrated

This project serves as a comprehensive demonstration of core and advanced Java concepts:
* **Unit 1**: OOP Principles, Exception Handling, Multithreading, and I/O Serialization.
* **Unit 2**: JavaBeans Property Change Support and Custom Event Models.
* **Unit 3**: JDBC API, PreparedStatements, Transactions, and SQL Aggregation.
* **Unit 4**: Advanced Swing components, Layout Management, Custom Painting, and MVC design.
## Screenshot
<img width="1806" height="1015" alt="image" src="https://github.com/user-attachments/assets/99b500ff-eb96-49cb-b9a9-a1552b432ca5" />


## 📄 License
This project is for educational purposes. All rights reserved.
