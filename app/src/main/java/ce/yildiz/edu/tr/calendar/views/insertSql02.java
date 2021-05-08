package ce.yildiz.edu.tr.calendar.views;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import ce.yildiz.edu.tr.calendar.database.DBHelper;
import ce.yildiz.edu.tr.calendar.database.DBStructure;
import ce.yildiz.edu.tr.calendar.database.DBTables;

public class insertSql02 extends SQLiteOpenHelper {

    //public static final String DATABASE_NAME = "EVENTS_DB";
    //public static final int DATABASE_VERSION = 4;

    //private static final String TABLE_NAME = "EVENT";
    //public static final String EVENT_ID = "id";
    //public static final String EVENT_TITLE = "title";
    //public static final String EVENT_DATE = "date";
    //public static final String EVENT_MONTH = "month";
    //public static final String EVENT_YEAR = "year";
    //public static final String EVENT_TIME = "time";


    private final Context context;
    DBHelper dbHelper;

    private int maxDrwNo;

    public insertSql02(@Nullable Context context)
    {
        super(context, DBStructure.DATABASE_NAME, null, DBStructure.DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
//        String query = "CREATE TABLE " + TABLE_NAME
//                + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
//                + COLUMN_TITLE + " TEXT, "
//                + COLUMN_AUTHOR + " TEXT, "
//                + COLUMN_PAGES + " INTEGER); ";


//        db.execSQL(query);
        getVal();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + DBTables.EVENT_TABLE_NAME);
        onCreate(db);
    }

    long result;
    int cnt = 0;
    void addBook()    {
        dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        for (int i=0; i<StaticData.array.length();i++){
            try {
                JSONObject object = (JSONObject)StaticData.array.get(i);

                String purpose = object.getString("shpurpose");
                String day = object.getString("shday");
                String month = object.getString("shmonth");
                String year = object.getString("shyear");
                String time = object.getString("shtime");
                String location = object.getString("shlocation");

                String month2 =null;
                String month3 = null;

                switch (month){
                    case "1":
                        month2="Jan";
                        month3="January";
                        break;
                    case "2":
                        month2="Feb";
                        month3="February";
                        break;
                    case "3":
                        month2="Mar";
                        month3="March";
                        break;
                    case "4":
                        month2="Apr";
                        month3="April";
                        break;
                    case "5":
                        month2="May";
                        month3="May";
                        break;
                    case "6":
                        month2="Jun";
                        month3="June";
                        break;
                    case "7":
                        month2="Jul";
                        month3="July";
                        break;
                    case "8":
                        month2="Aug";
                        month3 ="August";
                        break;
                    case "9":
                        month2="Sep";
                        month3="September";
                        break;
                    case "10":
                        month2="Oct";
                        month3="October";
                        break;
                    case "11":
                        month2="Nov";
                        month3="November";
                        break;
                    case "12":
                        month2="Dec";
                        month3="December";
                        break;

                }

                String date = day+"-"+month2+"-"+year;

                //db.execSQL("INSERT INTO `event`(`title`, `date`, `month`, `year`, `time`,`location`, `is_allDay`, `is_notify`, `is_recurring`,`color`, `parent_id`) VALUES(purpose,shday,shmonth,shyear,shtime,shlocation,0,0,0,-16718337,'3');");

                String sql = "insert into " + DBTables.EVENT_TABLE_NAME+"('"+ DBTables.EVENT_TITLE+"','"+ DBTables.EVENT_IS_ALL_DAY+"','"+ DBTables.EVENT_DATE+"','"+ DBTables.EVENT_MONTH+"','"+ DBTables.EVENT_YEAR+"','"+ DBTables.EVENT_TIME+"','"+ DBTables.EVENT_NOTE+"','"+ DBTables.EVENT_COLOR+"','"+ DBTables.EVENT_LOCATION+"','"+ DBTables.EVENT_PARENT_ID+"')"+
                        " values('"+purpose+"',0,'"+date+"','"+month3+"','"+year+"','"+time+"','"+purpose+"',-16777216,'"+location+"','20'+"+ maxDrwNo +");";
                db.execSQL(sql);
                String sql2 = "insert into " + DBTables.EVENT_INSTANCE_EXCEPTION_TABLE_NAME+"('"+ DBTables.EVENT_INSTANCE_EXCEPTION_EVENT_TITLE+"','"+ DBTables.EVENT_INSTANCE_EXCEPTION_EVENT_IS_ALL_DAY+"','"
                        + DBTables.EVENT_INSTANCE_EXCEPTION_EVENT_DATE+"','"+ DBTables.EVENT_INSTANCE_EXCEPTION_EVENT_MONTH+"','"+ DBTables.EVENT_INSTANCE_EXCEPTION_EVENT_YEAR+"','"+ DBTables.EVENT_INSTANCE_EXCEPTION_EVENT_TIME+"','"+ DBTables.EVENT_INSTANCE_EXCEPTION_EVENT_EVENT_NOTE+"','"
                        + DBTables.EVENT_INSTANCE_EXCEPTION_EVENT_EVENT_COLOR+"','"+ DBTables.EVENT_INSTANCE_EXCEPTION_EVENT_EVENT_LOCATION+"','"+ DBTables.EVENT_INSTANCE_EXCEPTION_EVENT_ID+"')"+
                        " values('"+purpose+"',0,'"+date+"','"+month2+"','"+year+"','"+time+"','"+purpose+"',-16777216,'"+location+"','20'+"+ maxDrwNo +");";
                db.execSQL(sql2);
                cnt++;
//                cv.put(EVENT_ID, i+"");
//                cv.put(EVENT_TITLE, purpose);
//                cv.put(EVENT_DATE, day);
//                cv.put(EVENT_MONTH, month);
//                cv.put(EVENT_YEAR, year);
//                cv.put(EVENT_TIME, time);
//
//                result = db.insert(TABLE_NAME, null, cv);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (result == -1)
            {
                Toast.makeText(context, "일정 추가 실패", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(context, "일정이 "+cnt +"개 추가 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        dbHelper.close();
    }

    public void getVal() {

        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from sqlite_sequence;",null);

        while (cursor.moveToNext()){
            maxDrwNo = Integer.parseInt(cursor.getString(1))+1;
            System.out.println("sqlite_sequence+1 : "+maxDrwNo);
        }
        cursor.close();
    }


}