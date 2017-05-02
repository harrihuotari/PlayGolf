package com.example.android.playgolf;

import java.util.Comparator;

/**
 * Created by Harri on 31.10.2016.
 */

public class Course implements Cloneable {
    private int mCourseId;
    private String mCourseName;
    private String mAbbreviation;
    private String mLocation;
    private int mCoursePar;
    private double mCourseSlope;
    private double mCourseRating;

    public Course (int courseId, String courseName, String abbreviation, String location, int coursePar, double courseSlope, double courseRating) {
        mCourseId = courseId;
        mCourseName = courseName;
        mAbbreviation = abbreviation;
        mLocation = location;
        mCoursePar = coursePar;
        mCourseSlope = courseSlope;
        mCourseRating = courseRating;
    }

    /*
    public class CourseNameComparator implements Comparator<Course> {
        @Override
        public int compare(Course c1, Course c2) {
            return c1.getCourseName().compareTo(c2.getCourseName());
        }
    } */

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getCourseId() {
        return  mCourseId;
    }
    public void setCourseId(int courseId) {
        mCourseId = courseId;
    }

    public String getCourseName() {
        return  mCourseName;
    }
    public void setCourseName(String courseName) {
        mCourseName = courseName;
    }

    public String getAbbreviation() {
        return mAbbreviation;
    }
    public void setAbbreviation(String abbreviation) {
        mAbbreviation = abbreviation;
    }

    public String getLocation() {
        return mLocation;
    }
    public void setLocation(String location) {
        mLocation = location;
    }


    public int getCoursePar() {
        return mCoursePar;
    }
    public void setCoursePar(int coursePar) {
        mCoursePar = coursePar;
    }

    public double getCourseSlope() {
        return mCourseSlope;
    }
    public void setCourseSlope(double courseSlope) {
        mCourseSlope = courseSlope;
    }


    public double getCourseRating() {
        return mCourseRating;
    }
    public void setCourseRating(double courseRating) {
        mCourseRating = courseRating;
    }

    @Override
    public boolean equals (Object other) {
        if(!(other instanceof Course)) {
            return false;
        }
        Course that = (Course) other;
        return this.getCourseName().equals(that.getCourseName());
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 37 + this.hashCode();
        hashCode = hashCode * 37 + this.hashCode();

        return hashCode;

    }
}
