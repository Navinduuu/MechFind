package org.example.identificationservice.model;

public class User {

    private String id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String street;
    private String gender = "Not Specified";
    private String speciality;
    private Double latitude;
    private Double longitude;
    private UserType userType;

    public User() {}

    public User(String name, String speciality, String email, String password, UserType userType, String phone, String street, String gender, Double latitude, Double longitude) {
        this.name = name;
        this.speciality = speciality;
        this.email = email;
        this.password = password;
        this.userType = userType;
        this.phone = phone;
        this.street = street;
        this.gender = gender != null ? gender : "Not Specified";
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }
}