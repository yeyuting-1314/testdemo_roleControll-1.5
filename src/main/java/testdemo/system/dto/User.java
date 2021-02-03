package testdemo.system.dto;

import com.baomidou.mybatisplus.annotation.TableName;


@TableName("sys_user")
public class User{
    int id ;
    String userName ;
    String password ;

    //加一个属性  用于记录token
    String token ;

    private String role;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
