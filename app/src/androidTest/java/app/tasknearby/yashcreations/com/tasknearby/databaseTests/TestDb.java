
package app.tasknearby.yashcreations.com.tasknearby.databaseTests;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

import app.tasknearby.yashcreations.com.tasknearby.database.TaskDbHelper;
import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(TaskDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.
        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(TasksContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(TasksContract.TaskEntry.TABLE_NAME);

        mContext.deleteDatabase(TaskDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new TaskDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and tasks entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + TasksContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(TasksContract.LocationEntry._ID);
        locationColumnHashSet.add(TasksContract.LocationEntry.COLUMN_PLACE_NAME);
        locationColumnHashSet.add(TasksContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(TasksContract.LocationEntry.COLUMN_COORD_LONG);


        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public void testLocationTable() {
      //  insertLocation();
    }

    /*
        Students: This is a helper method for the testWeatherTable quiz. You can move your
        code from testLocationTable to here so that you can call this code from both
        testWeatherTable and testLocationTable.
     */
//    public long insertLocation() {
//        TaskDbHelper dbHelper = new TaskDbHelper(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
//        long location_rowID = db.insert(TasksContract.LocationEntry.TABLE_NAME, null, testValues);
//        assertTrue("is -1", location_rowID != -1);
//
//        Cursor c = db.query(TasksContract.LocationEntry.TABLE_NAME, null, null, null, null, null, null);
//        assertTrue("Error:No record Returned", c.moveToFirst());
//
//        TestUtilities.validateCurrentRecord("Record not correct ", c, testValues);
//        assertFalse("Error:More than one record Returned", c.moveToNext());
//
//        // Finally, close the cursor and database
//        c.close();
//        db.close();
//        return location_rowID;
//    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
//    public void testWeatherTable() {
//        // First insert the location, and then use the locationRowId to insert
//        // the weather. Make sure to cover as many failure cases as you can.
//
//        // Instead of rewriting all of the code we've already written in testLocationTable
//        // we can move this code to insertLocation and then call insertLocation from both
//        // tests. Why move it? We need the code to return the ID of the inserted location
//        // and our testLocationTable can only return void because it's a test.
//
//        // First step: Get reference to writable database
//        TaskDbHelper dbHelper = new TaskDbHelper(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        // Create ContentValues of what you want to insert
//        // (you can use the createWeatherValues TestUtilities function if you wish)
//        ContentValues testValues = TestUtilities.createWeatherValues(insertLocation());
//
//
//        // Insert ContentValues into database and get a row ID back
//        db.insert(TasksContract.TaskEntry.TABLE_NAME, null, testValues);
//
//        // Query the database and receive a Cursor back
//        Cursor c = db.query(TasksContract.TaskEntry.TABLE_NAME, null, null, null, null, null, null);
//        // Move the cursor to a valid database row
//        assertTrue("Failed To move Cursor to first Row", c.moveToFirst());
//
//
//        // Validate data in resulting Cursor with the original ContentValues
//        TestUtilities.validateCurrentRecord("Failed To validate Current Record", c, testValues);
//        // (you can use the validateCurrentRecord function in TestUtilities to validate the
//        // query if you like)
//
//        // Finally, close the cursor and database
//        c.close();
//        db.close();
//    }


}