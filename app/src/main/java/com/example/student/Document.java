package com.example.student;

public class Document {
    private String name;
    private String url;
    private String subjectName;
    private String semester;

    public Document(String name, String url, String subjectName, String semester) {
        this.name = name;
        this.url = url;
        this.subjectName = subjectName;
        this.semester = semester;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getSemester() {
        return semester;
    }
}