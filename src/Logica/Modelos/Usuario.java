package Logica.Modelos;

import java.io.Serializable;

/** Registro maestro de users.ins; conserva el formato binario existente. */
public final class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    private String realName;
    private char gender;
    private final String username;
    private final String password;
    private final long entryDate;
    private int age;
    private final boolean active;
    private String profilePicture;

    public Usuario(String name, char gender, String username, String password, long date,
            int age, boolean active, String picture) {
        this.realName = name; this.gender = gender; this.username = username;
        this.password = password; this.entryDate = date; this.age = age;
        this.active = active; this.profilePicture = picture;
    }

    public String getRealName() { return realName; }
    public void setRealName(String value) { realName = value; }
    public char getGender() { return gender; }
    public void setGender(char value) { gender = value; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public long getEntryDate() { return entryDate; }
    public int getAge() { return age; }
    public void setAge(int value) { age = value; }
    public boolean getActive() { return active; }
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String value) { profilePicture = value; }
}
