package controller;

import bo.BOFactory;
import bo.custom.UserManageBO;
import com.google.gson.Gson;
import dto.UserDTO;
import util.StandardResponse;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name = "UserManageServlet", urlPatterns = "/userManageServlet")
public class UserManageServlet extends HttpServlet {

    private final UserManageBO userManageBO = BOFactory.getInstance().getBO(BOFactory.BoType.UserManageBOImpl);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (JsonReader reader = Json.createReader(request.getReader()); PrintWriter writer = response.getWriter()) {
            response.setContentType("application/json");
            JsonObject user = reader.readObject();
            UserDTO userDTO = new UserDTO(user.getString("userName"), user.getString("userAddress"), user.getString("phoneNumber"),
                    user.getString("emailAddress"), user.getString("password"));
            String isValidUser = validateUserData(userDTO);
            if (isValidUser.equals("true")) {
                if (userManageBO.saveUser(userDTO))
                    writer.print(new Gson().toJson(new StandardResponse("201", "user save successful", userDTO)));
                else {
                    writer.print(new Gson().toJson(new StandardResponse("500", "something went wrong", userDTO)));
                }
            } else {
                writer.print(new Gson().toJson(new StandardResponse("400", isValidUser, userDTO)));
            }
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    private String validateUserData(UserDTO userDTO) {
        if (userDTO == null) {
            return "No User Data Found";
        } else if (userDTO.getName().trim().isEmpty()) {
            return "No User Name Found";
        } else if (userDTO.getAddress().trim().isEmpty()) {
            return "No User Address Found";
        } else if (userDTO.getContact().trim().isEmpty()) {
            return "No User Contact Found";
        } else if (userDTO.getEmailAddress().trim().isEmpty()) {
            return "No User Email Address Found";
        } else if (userDTO.getPassword().trim().isEmpty()) {
            return "No User Password Found";
        } else {
            return "true";
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (PrintWriter writer = response.getWriter()) {
            ArrayList<UserDTO> all = userManageBO.getAll();
            JsonArrayBuilder allUsers = Json.createArrayBuilder();
            for (UserDTO userDTO : all) {
                allUsers.add(new Gson().toJson(userDTO));
            }
            writer.print(new Gson().toJson(new StandardResponse("200", "All Users", all)));
        } catch (SQLException | ClassNotFoundException | IOException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (JsonReader reader = Json.createReader(request.getReader()); PrintWriter writer = response.getWriter()) {
            response.setContentType("application/json");
            JsonObject user = reader.readObject();
            UserDTO userDTO = new UserDTO(user.getInt("id"), user.getString("userName"), user.getString("userAddress"), user.getString("phoneNumber"),
                    user.getString("emailAddress"), user.getString("password"));
            String isValidUser = validateUserData(userDTO);
            if (isValidUser.equals("true")) {
                if (userManageBO.updateUser(userDTO))
                    writer.print(new Gson().toJson(new StandardResponse("204", "user update successful", userDTO)));
                else {
                    writer.print(new Gson().toJson(new StandardResponse("500", "something went wrong", userDTO)));
                }
            } else {
                writer.print(new Gson().toJson(new StandardResponse("400", isValidUser, userDTO)));
            }
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        try (PrintWriter writer = response.getWriter()) {
            if (null != id) {
                if (!id.trim().isEmpty()) {
                    if (userManageBO.deleteUser(id)) {
                        writer.print(new Gson().toJson(new StandardResponse("204", "user delete successful", "")));
                    } else {
                        writer.print(new Gson().toJson(new StandardResponse("500", "something went wrong", "")));
                    }
                } else {
                    writer.print(new Gson().toJson(new StandardResponse("400", "No Valid ID found", "")));
                }
            } else {
                writer.print(new Gson().toJson(new StandardResponse("400", "No Valid ID found", "")));
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
