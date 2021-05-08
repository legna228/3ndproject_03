package ce.yildiz.edu.tr.calendar.database;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import ce.yildiz.edu.tr.calendar.R;
import ce.yildiz.edu.tr.calendar.database.DBTables;

public class insertSql05 extends AppCompatActivity {

    private int maxDrwNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getVal();
        insertFromOracle();
        //System.out.println("\n"+"실행됐음!작업후!!");
    }

    public void getVal() {

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from sqlite_sequence;",null);

        while (cursor.moveToNext()){
            maxDrwNo = Integer.parseInt(cursor.getString(1))+1;
            System.out.println("sqlite_sequence+1 : "+maxDrwNo);
        }
        cursor.close();
    }

    public void insertFromOracle() {

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();


        db.execSQL("INSERT INTO `event`(`title`, `date`, `month`, `year`, `time`, `color`, `parent_id`) VALUES ('test03','29-Apr-2021','April','2021','1:00 PM',-16718337, "+ maxDrwNo +");");
        db.execSQL("INSERT INTO `EVENT_INSTANCE_EXCEPTION`(`title`, `date`, `month`, `year`, `time`, `color`, `event_id`) VALUES ('test03','29-Apr-2021','April','2021','1:00 PM',-16718337,"+ maxDrwNo +");");

        dbHelper.close();
    }
}