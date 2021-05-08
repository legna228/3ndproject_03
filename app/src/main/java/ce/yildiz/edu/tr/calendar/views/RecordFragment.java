package ce.yildiz.edu.tr.calendar.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ce.yildiz.edu.tr.calendar.R;
import ce.yildiz.edu.tr.calendar.database.DBHelper;

public class RecordFragment extends Fragment {

    DBHelper dbHelper;
    RecordFragment recordFragment;

    Context cThis;  //context 설정
    String LogTT = "[STT]";   //LOG타이틀
    //음성 인식용
    Intent SttIntent;
    SpeechRecognizer mRecognizer;

    //화면 처리용
    Button btnSttStart, btnSttStop, btn_refresh_id;
    //음성 -> txt
    EditText txtInMsg;
    // 여러 경우의 말
    EditText txtSystem;

    // 실제 서버에 요청하는 객체
    private RequestQueue queue;

    //요청 시 데이터 및 문자열(정보) 저장 객체
    private StringRequest stringRequest;
    //요청 구분 문자열
    private final String TAG = "main";
    String txt;
    String res;

    JSONObject object;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_record, container, false);
        dbHelper = new DBHelper(getActivity());

        defineViews(view);

        return view;


    }

    // 정의
    private void defineViews(final View view) {
        btnSttStart = view.findViewById(R.id.btn_stt_start);
        btnSttStop = view.findViewById(R.id.btn_stt_stop);
        btn_refresh_id = view.findViewById(R.id.btn_refresh_id);
        txtInMsg=view.findViewById(R.id.txtInMsg);
        txtSystem=view.findViewById(R.id.txtSystem);

        btnSttStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("음성인식 시작!");
                txtInMsg.setText(null);
                txtSystem.setText(null);
                //requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},2);
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    //권한 허용X인경우
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},2);
                } else{
                    //권한 허용한 경우
                    try {
                        // 음성인식
                        SttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getContext().getPackageName());
                        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");  //한국어 사용
                        mRecognizer=SpeechRecognizer.createSpeechRecognizer(getActivity());
                        mRecognizer.setRecognitionListener(listner);

                        mRecognizer.startListening(SttIntent);

                    } catch (SecurityException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        btnSttStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("음성인식 멈춰!");
                mRecognizer.stopListening();
                txt =txtInMsg.getText().toString();
                try {
                    Thread.sleep(1000);
                    res = sendRequest();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "text>> "+txt);


            }
        });

        //이명주: 버튼
        btn_refresh_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest_refresh();

//            SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
//            int s = dbHelper.createEvent(sqLiteDatabase, event);

            }
        });
    }
    private RecognitionListener listner = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            txtSystem.setText(null);
            txtSystem.setText("일정을 말해주세요"+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onBeginningOfSpeech() {
            txtSystem.setText(null);
            txtSystem.setText("녹음 중....."+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            txtSystem.setText(null);
            txtSystem.setText("로딩중....."+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onEndOfSpeech() {
            txtSystem.setText(null);
            txtSystem.setText("인식완료 / '녹음 중지'를 눌러주세요 / 일정을 등록하고 싶다면 '일정 동기화'를 눌러주세요");
        }

        @Override
        public void onError(int error) {
            txtSystem.setText(null);
            txtSystem.setText("천천히 말해주세요." +"\r\n"+txtSystem.getText());
            mRecognizer.startListening(SttIntent);
        }

        @Override
        public void onResults(Bundle results) {
            String key="";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            txtInMsg.setText(rs[0]+"\r\n"+txtInMsg.getText());
            FuncVoiceOrderCheck(rs[0]);

        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            //txtSystem.setText("onPartialResults....."+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            //txtSystem.setText("onEvent....."+"\r\n"+txtSystem.getText());
        }
    };

    //입력된 음성 메세지 확인 후 동작 처리
    private void FuncVoiceOrderCheck(String VoiceMsg) {
        if (VoiceMsg.length()<1) return;
        // 공백제거거
        VoiceMsg =VoiceMsg.replace(" ","");

        if (VoiceMsg.indexOf("멈춰")>-1|| VoiceMsg.indexOf("그만")>-1){
            mRecognizer.destroy();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRecognizer!=null){
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer=null;
        }
    }

    public String sendRequest() {
        // Volley Lib 새로운 요청객체 생성
        if (queue == null){
            queue = Volley.newRequestQueue(getContext());
        }
        // 서버에 요청할 주소
        String url = "http://59.0.234.238:8091/AndroidMember/RecordCheck"; //이명주: url 호스트를 너껄로 변경


        // 요청 문자열 저장
        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            // 응답데이터를 받아오는 곳
            @Override
            public void onResponse(String response) {
                Log.v("resultValue",response);

            }
        }, new Response.ErrorListener() {
            // 서버와의 연동 에러시 출력
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override //response를 UTF8로 변경해주는 소스코드
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    // log error
                    return Response.error(new ParseError(e));
                } catch (Exception e) {
                    // log error
                    return Response.error(new ParseError(e));
                }
            }

            // 보낼 데이터를 저장하는 곳
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //데이터를 넣어주는 곳 params.put("key", "value");
                params.put("txt", txt);
                return params;
            }
        };
        stringRequest.setTag(TAG);
        queue.add(stringRequest);
        return res;
    }

    public void sendRequest_refresh(){
        // Volley Lib 새로운 요청객체 생성
        JSONArray arr;
        queue = Volley.newRequestQueue(getContext());
        // 서버에 요청할 주소
        String url = "http://59.0.234.238:8091/AndroidMember/ScheduleSelect";//수정

        // 요청 문자열 저장
        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    StaticData.array = new JSONArray(response);
                    Log.v("sss", StaticData.array.length()+"");
                    for (int i = 0; i< StaticData.array.length(); i++){
                        object = (JSONObject) StaticData.array.get(i);
                        String purpose = object.getString("shlocation");
                        Log.v("resultValue2", purpose);
                    }

                    try {
                        Thread.sleep(5000);
                        insertSql02 insert = new insertSql02(getContext());
                        insert.addBook();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // 응답데이터를 받아오는 곳

        }, new Response.ErrorListener() {
            // 서버와의 연동 에러시 출력
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override //response를 UTF8로 변경해주는 소스코드
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    // log error
                    return Response.error(new ParseError(e));
                } catch (Exception e) {
                    // log error
                    return Response.error(new ParseError(e));
                }
            }

            // 보낼 데이터를 저장하는 곳
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //데이터를 넣어주는 곳 params.put("key", "value");
                params.put("id", "smart");
                return params;
            }
        };
        stringRequest.setTag(TAG);
        queue.add(stringRequest);
    }
}
