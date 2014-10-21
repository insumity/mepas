package ch.ethz.inf.asl.testutils;

public class TestConstants {
    public static final String SMALL = "small";
    public static final String DATABASE = "database";
    public static final String END_TO_END = "endtoend";

    // constants used for the "endtoend" and "database" tests
    // those constants need to be changed depending on whether the tests are executed
    public static final String USERNAME = "postgres";
    public static final String PASSWORD = "";
    public static final String HOST = "localhost";
    public static final Integer PORT_NUMBER = 5432;

    // this database doesn't have to exist for the tests to run, it's going to
    // be created. If one exists already it's going to be replace by a new one
    public static final String DATABASE_NAME = "databasetest";
}
