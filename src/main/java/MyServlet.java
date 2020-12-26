import ReturnObjects.*;
import SQLConstructor.*;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//This declares this program to be a servlet
@WebServlet(urlPatterns = {"/patients"}, displayName = "Baby Health Servlet", loadOnStartup = 1)
public class MyServlet extends HttpServlet{
    @Override
    //The DoPost Method edits the database and sends an updated information back to client
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        //Setup
        String reqBody=req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        resp.setContentType("application/json");
        resp.getWriter().write(reqBody);
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        try {
            // Registers the driver
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
        }
        Connection conn = null;
        try {
            conn= DriverManager.getConnection(dbUrl);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //Carrying out SQL
        try {
            Statement s=conn.createStatement();
            Gson gson = new Gson();
            //Takes in Initial Query in the superclass InitQuery. This is used to identify
            //which type of command the client is sending to us
            SQLQuery initquery = gson.fromJson(reqBody, SQLQuery.class);
            String sqlStr = "Invalid Command";
            ResultSet rset = null;
            if (initquery.getType() == "EditClinician") {
                //EditClinician returns an object that says "Comment Added!"
                //and prints out the updated patient
                //carrying out Edit
                SQLEditClinician query = gson.fromJson(reqBody,SQLEditClinician.class);
                sqlStr = query.getSQL();
                System.out.println(sqlStr);
                s.executeQuery(sqlStr);
                //Returning updated patient
                SQLViewClinician viewClinician = new SQLViewClinician(query.getPatientID());
                sqlStr = viewClinician.getSQL();
                rset=s.executeQuery(sqlStr);
            }
            else if (initquery.getType() == "EditEngineer") {
                //Edits the filter type for a particular patient
                //then returns the updated patient
                //Updating
                SQLEditEngineer query = gson.fromJson(reqBody, SQLEditEngineer.class);
                sqlStr = query.getSQL();
                System.out.println(sqlStr);
                s.executeQuery(sqlStr);
                //Returning all information for one clinician
                SQLViewAll viewAll = new SQLViewAll(query.getPatientID());
                sqlStr = viewAll.getSQL();
                rset=s.executeQuery(sqlStr);
            }
            else if (initquery.getType() == "EditPhysician") {
                //EditClinician returns an object that says "Prescription Added!"
                //and prints out the updated patient
                //updating
                SQLEditPhysician query = gson.fromJson(reqBody, SQLEditPhysician.class);
                sqlStr = query.getSQL();
                System.out.println(sqlStr);
                s.executeQuery(sqlStr);
                //printing updated patient
                SQLViewClinician viewClinician = new SQLViewClinician(query.getPatientID());
                sqlStr = viewClinician.getSQL();
                rset=s.executeQuery(sqlStr);
            }
            //All returned info for doPost is of the returnObject "Patient" class
            Patient patient = new Patient (rset);
            List results = new ArrayList();
            results = patient.resultSetToList(rset);
            String jsonString = gson.toJson(results); // RETURN THIS
            resp.getWriter().write(jsonString);
            //rset.close();
            s.close();
            conn.close();
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        //Setup
        String reqBody=req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        resp.setContentType("application/json");
        resp.getWriter().write(reqBody);
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        try {
            // Registers the driver
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
        }
        Connection conn = null;
        try {
            conn= DriverManager.getConnection(dbUrl);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //Carrying out SQL
        try {
            Statement s = conn.createStatement();
            Gson gson = new Gson();
            //Takes in Initial Query in the superclass InitQuery. This is used to identify
            //which type of command the client is sending to us
            SQLQuery initquery = gson.fromJson(reqBody, SQLQuery.class);
            String sqlStr = "Invalid Command";
            ResultSet rset = null;

            if (initquery.getType() == "ViewClinician")
            {
                SQLViewClinician query = gson.fromJson(reqBody,SQLViewClinician.class);
                sqlStr = query.getSQL();
                try {
                    rset=s.executeQuery(sqlStr);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }

            else if (initquery.getType() == "ViewEngineer")
            {
                SQLViewEngineer query = gson.fromJson(reqBody,SQLViewEngineer.class);
                sqlStr = query.getSQL();
                try {
                    rset=s.executeQuery(sqlStr);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        catch (Exception e){
            System.out.println(e);
        }

    }

}
