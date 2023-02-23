package com.example.buzzer_thomas;

public class User {
    String mac;
    String name;
    int pts;

    public User(String mac, String name, int pts) {
        this.mac = mac;
        this.name = name;
        this.pts = pts;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPts() {
        return pts;
    }

    public void setPts(int pts) {
        this.pts = pts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return mac.equals(user.mac);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
