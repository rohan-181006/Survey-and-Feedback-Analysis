package com.survey.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Survey Model - Demonstrates: OOP (Unit 1), Serialization (Unit 1)
 */
public class Survey implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String description;
    private String category;
    private Date createdAt;
    private List<Question> questions;
    private boolean active;

    // Constructors
    public Survey() {
        this.questions = new ArrayList<>();
        this.createdAt = new Date();
        this.active = true;
    }

    public Survey(int id, String title, String description, String category) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
    }

    // Encapsulation - Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public void addQuestion(Question q) { this.questions.add(q); }

    @Override
    public String toString() { return title; }

    // Demonstrating Cloneable interface (Unit 1 - Interfaces)
    @Override
    public Survey clone() throws CloneNotSupportedException {
        Survey cloned = (Survey) super.clone();
        cloned.questions = new ArrayList<>(this.questions);
        return cloned;
    }
}
