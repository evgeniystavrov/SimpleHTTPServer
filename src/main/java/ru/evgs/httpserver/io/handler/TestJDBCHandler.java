package ru.evgs.httpserver.io.handler;

import ru.evgs.httpserver.io.HttpHandler;
import ru.evgs.httpserver.io.HttpRequest;
import ru.evgs.httpserver.io.HttpResponse;
import ru.evgs.httpserver.io.HttpServerContext;
import ru.evgs.httpserver.io.exception.HttpServerException;
import ru.evgs.httpserver.io.utils.DataUtils;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TestJDBCHandler implements HttpHandler {
    @Override
    public void handle(HttpServerContext context, HttpRequest request, HttpResponse response) throws IOException {
        try (Connection c = context.getDataSource().getConnection()) {
            List<Contact> list = JDBCUtils.select(c, "select * from contact", CONTACTS_RESULT_SET_HANDLER);
            Map<String, Object> args = DataUtils.buildMap(new Object[][] {
                    { "TABLE-BODY", getTableBody(context, list) }
            });
            // setting body for template
            response.setBody(context.getHtmlTemplateManager().processTemplate("contacts.html", args));
        } catch (SQLException e) {
            throw new HttpServerException("Error with database: " + e.getMessage(), e);
        }
    }

    protected String getTableBody(HttpServerContext context, List<Contact> list) {
        StringBuilder body = new StringBuilder();
        // forming a table row
        for (Contact c : list) {
            Map<String, Object> args = DataUtils.buildMap(new Object[][] {
                    { "ID", c.getId() },
                    { "ID_CATEGORY", c.getId_category() },
                    { "FIRST-NAME", c.getFirstName() },
                    { "MIDDLE-NAME", c.getMiddleName() },
                    { "LAST-NAME", c.getLastName() },
                    { "AGE", c.getAge() }
            });
            String fragment = context.getHtmlTemplateManager().processTemplate("contact-row.html", args);
            body.append(fragment);
        }
        return body.toString();
    }
    // handler for data received from the request
    private static ResultSetHandler<List<Contact>> CONTACTS_RESULT_SET_HANDLER = new ResultSetHandler<List<Contact>>() {
        @Override
        public List<Contact> handle(ResultSet rs) throws SQLException {
            List<Contact> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Contact(rs.getLong("id"), rs.getLong("id_category"), rs.getString("first_name"), rs.getString("middle_name"), rs.getString("last_name"), rs.getInt("age")));
            }
            return list;
        }
    };

    public interface ResultSetHandler<T> {
        T handle(ResultSet rs) throws SQLException;
    }
    // inner utils class for working with database
    public static class JDBCUtils {
        // method for handle select query
        public static <T> T select(Connection c, String sql, ResultSetHandler<T> resultSetHandler, Object... parameters)
                throws SQLException {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                populatePreparedStatement(ps, parameters);
                try (ResultSet rs = ps.executeQuery()) {
                    return resultSetHandler.handle(rs);
                }
            }
        }
        // data conversion for query
        private static void populatePreparedStatement(PreparedStatement ps, Object... parameters) throws SQLException {
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    ps.setObject(i + 1, parameters[i]);
                }
            }
        }
    }
    // inner class for storing information from database
    public static class Contact implements Comparable<Contact> {
        private Long id;
        private Long id_category;
        private String firstName;
        private String middleName;
        private String lastName;
        private int age;

        public Contact(long id, long id_category, String firstName, String middleName, String lastName, int age) {
            super();
            this.id = id;
            this.id_category = id_category;
            this.firstName = firstName;
            this.middleName = middleName;
            this.lastName = lastName;
            this.age = age;
        }

        public Contact(long id_category, String firstName, String middleName, String lastName, int age) {
            setId_category(id_category);
            setFirstName(firstName);
            setMiddleName(middleName);
            setLastName(lastName);
            setAge(age);
        }

        public void setId_category(Long id_category) {
            this.id_category = id_category;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            if (firstName.length() > 1) {
                this.firstName = Character.toUpperCase(firstName.charAt(0)) + firstName.substring(1).toLowerCase();
            } else {
                this.firstName = firstName;
            }
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            if (middleName.length() > 1) {
                this.middleName = Character.toUpperCase(middleName.charAt(0)) + middleName.substring(1).toLowerCase();
            } else if (middleName.length() <= 1) {
                this.middleName = middleName;
            } else if (middleName.length() == 0) {
                this.middleName = null;
            }
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            if (lastName.length() > 1) {
                this.lastName = Character.toUpperCase(lastName.charAt(0)) + lastName.substring(1).toLowerCase();
            } else {
                this.lastName = lastName;
            }
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Long getId() {
            return id;
        }

        public String getId_category() {
            return id_category == 1 ? "Friends" : "Collegs";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Contact)) return false;
            Contact contact = (Contact) o;
            return getAge() == contact.getAge() &&
                    getId().equals(contact.getId()) &&
                    getId_category().equals(contact.getId_category()) &&
                    getFirstName().equals(contact.getFirstName()) &&
                    Objects.equals(getMiddleName(), contact.getMiddleName()) &&
                    getLastName().equals(contact.getLastName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId(), getId_category(), getFirstName(), getMiddleName(), getLastName(), getAge());
        }

        @Override
        public String toString() {
            return String.format("Contact [id=%s, id_category=%s, firstName=%s, middleName=%s, lastName=%s, age=%s]", id, id_category, firstName, middleName, lastName, age);
        }

        @Override
        public int compareTo(Contact o) {
            if (getId() != null && o != null && o.getId() != null) {
                return getId().compareTo(o.getId());
            }
            throw new IllegalArgumentException("Can't compare students: id=" + getId() + ", o=" + o);
        }
    }
}
