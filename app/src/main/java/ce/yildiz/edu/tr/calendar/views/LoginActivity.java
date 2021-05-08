package ce.yildiz.edu.tr.calendar.views;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import ce.yildiz.edu.tr.calendar.R;


public class LoginActivity extends AppCompatActivity {
    private EditText et_id, et_pass;
    private Button btn_login, btn_register,inserBtn;
    private SQLiteDatabase sqlDB;
    //myDBHelper myHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_id = findViewById(R.id.et_id);
        et_pass = findViewById(R.id.et_pass);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        //myHelper = new myDBHelper(this);

        System.out.println("실행됐음!버튼누르기전!!");

        //DB에 테스트입력하는 버튼
//        inserBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SQLiteDatabase
//                try {
//                    sqlDB = myHelper.getWritableDatabase();
//                    sqlDB.execSQL("INSERT INTO EVENT VALUES (2,'테스트02',0,'29-Apr-2021','April','2021','10:00 AM','일정지속: 4 시간 0 분',1,0,'개발일정진행',-16777216,'4층강의실','666-4444','','2');");
//                    sqlDB.execSQL("INSERT INTO EVENT_INSTANCE_EXCEPTION VALUES (2,'2',0,0,'테스트02',0,'29-Apr-2021','April','2021','10:00 AM','일정지속: 4 시간 0 분',1,NULL,'개발일정진행','-16777216','4층강의실','666-4444','');");
//
//                    sqlDB.close();
//                    Toast.makeText(getApplicationContext(), "저장 완료", Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Toast.makeText(getApplicationContext(), "유효한 값을 입력해주세요.", Toast.LENGTH_SHORT).show();
//                }

//                Intent intent = new Intent(LoginActivity.this, insertSql03.class);
//                startActivity(intent);

//            }
//        });

        // 회원가입 버튼을 클릭 시 수행
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditText에 현재 입력되어있는 값을 get(가져온다)해온다.
                String userID = et_id.getText().toString();
                String userPass = et_pass.getText().toString();

                //System.out.println("입력한 아이디: " + userID);
                //System.out.println("입력한 패스워드: " + userPass);

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // TODO : 인코딩 문제때문에 한글 DB인 경우 로그인 불가
                            System.out.println("hongchul" + response);
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) { // 로그인에 성공한 경우
                                String userID = jsonObject.getString("userID");
                                String userPass = jsonObject.getString("userPassword");

                                Toast.makeText(getApplicationContext(),"로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
//                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("userID", userID);
                                intent.putExtra("userPass", userPass);
                                startActivity(intent);
                            } else { // 로그인에 실패한 경우
                                Toast.makeText(getApplicationContext(),"로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                LoginRequest loginRequest = new LoginRequest(userID, userPass, responseListener);
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                queue.add(loginRequest);
            }
        });


    }
}