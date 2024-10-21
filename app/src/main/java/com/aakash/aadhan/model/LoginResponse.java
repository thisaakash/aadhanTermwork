package com.aakash.aadhan.model;

public class LoginResponse {
    private String token;
    private String companyname;
    private String studentImage;
    private String studentemail;
    private String studentid;
    private String studentname;
    private String studentphone;

    // Constructor
    public LoginResponse(String token, String companyname, String studentImage, String studentemail, String studentid, String studentname) {
        this.token = token;
        this.companyname = companyname;
        this.studentImage = studentImage;
        this.studentemail = studentemail;
        this.studentid = studentid;
        this.studentname = studentname;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getCompanyname() {
        return companyname;
    }

    public String getStudentImage() {
        return studentImage;
    }

    public String getStudentemail() {
        return studentemail;
    }

    public String getStudentid() {
        return studentid;
    }

    public String getStudentname() {
        return studentname;
    }

    public String getStudentphone() {
        return studentphone;
    }

    // Setters
    public void setToken(String token) {
        this.token = token;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public void setStudentImage(String studentImage) {
        this.studentImage = studentImage;
    }

    public void setStudentemail(String studentemail) {
        this.studentemail = studentemail;
    }

    public void setStudentid(String studentid) {
        this.studentid = studentid;
    }

    public void setStudentname(String studentname) {
        this.studentname = studentname;
    }

    public void setStudentphone(String studentphone) {
        this.studentphone = studentphone;
    }
}
