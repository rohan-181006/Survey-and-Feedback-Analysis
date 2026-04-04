package com.survey.util;

import com.survey.model.Survey;

import java.io.*;
import java.util.logging.*;

/**
 * SerializationUtil - Demonstrates: I/O Serialization (Unit 1)
 * Saves/loads Survey objects to/from disk using Java Object Serialization.
 */
public class SerializationUtil {
    private static final Logger LOGGER = Logger.getLogger(SerializationUtil.class.getName());
    private static final String BACKUP_DIR = "survey_backups/";

    static {
        new File(BACKUP_DIR).mkdirs();
    }

    /**
     * Serialize a Survey object to file (Unit 1 - Object Serialization)
     */
    public static void serializeSurvey(Survey survey, String filename) {
        String path = BACKUP_DIR + filename + ".ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(path)))) {
            oos.writeObject(survey);
            LOGGER.info("Survey serialized to: " + path);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Serialization failed", e);
        }
    }

    /**
     * Deserialize a Survey object from file (Unit 1 - Object Serialization)
     */
    public static Survey deserializeSurvey(String filename) {
        String path = BACKUP_DIR + filename + ".ser";
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(path)))) {
            Survey s = (Survey) ois.readObject();
            LOGGER.info("Survey deserialized from: " + path);
            return s;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Deserialization failed: " + path, e);
            return null;
        }
    }

    /**
     * Export survey responses to a text report (Unit 1 - I/O)
     */
    public static void exportToTextFile(String surveyTitle, String content) {
        String safe = surveyTitle.replaceAll("[^a-zA-Z0-9]", "_");
        String path = BACKUP_DIR + safe + "_report.txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write("=== Survey Report: " + surveyTitle + " ===\n");
            bw.write("Generated: " + new java.util.Date() + "\n\n");
            bw.write(content);
            LOGGER.info("Report exported to: " + path);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Export failed", e);
        }
    }

    public static String getBackupDir() { return BACKUP_DIR; }
}
