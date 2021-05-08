package ce.yildiz.edu.tr.calendar;

import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

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

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


public class NotificationListener extends NotificationListenerService {

    // 실제 서버에 요청하는 객체
    private RequestQueue queue;

    //요청 시 데이터 및 문자열(정보) 저장 객체
    private StringRequest stringRequest;

    //요청 구분 문자열
    private final String TAG = "main";
    JSONObject object;
    String res;

    String from="start";
    String text="text";
    //이명주: 여기서부터는 카톡에서 알림 받아오기
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Notification Listener created!");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void addNotification(StatusBarNotification sbn, boolean updateDash)  {
        if (sbn==null) return;

        Notification mNotification=sbn.getNotification();
        Bundle extras = mNotification.extras;

        if(sbn!=null && sbn.getPackageName().equalsIgnoreCase("com.facebook.orca")){
            StringTokenizer stringTokenizer=new StringTokenizer(sbn.getNotification().tickerText.toString(),":");
            Log.d(TAG,"Ticker: "+sbn.getNotification().tickerText.toString());
            from=stringTokenizer.nextToken();
            text=stringTokenizer.nextToken();
            Log.d(TAG,"facebook>>"+"from: "+from+", Text: "+text);

        }else if(sbn!=null && sbn.getPackageName().equalsIgnoreCase("com.kakao.talk")) {
            if (extras.getString(Notification.EXTRA_TITLE)!=null && extras.getString(Notification.EXTRA_TEXT)!=null){
                from=extras.getString(Notification.EXTRA_TITLE); // 이름
                text=extras.getString(Notification.EXTRA_TEXT); //텍스트
                try {
                    Thread.sleep(3000);
                    res = sendRequest();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "kakao>> " + "Title: " + from + ", Text: " + text);

            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "Notification Posted:\n");
        addNotification(sbn,true);



        }
        //이명주:카톡 알림 받아오기 끝

    public String sendRequest() {
        // Volley Lib 새로운 요청객체 생성
        queue = Volley.newRequestQueue(this);
        // 서버에 요청할 주소
        String url = "http://59.0.234.238:8091/AndroidMember/KakaoCheck"; //이명주: url 호스트를 너껄로 변경

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
                params.put("from", from);
                params.put("text", text);
                return params;
            }
        };
        stringRequest.setTag(TAG);
        queue.add(stringRequest);
        return res;
    }
    }









//
//    @Override
//    public void onNotificationRemoved(StatusBarNotification sbn) {
//        Log.d(TAG, "Notification Removed:\n");
//        if (sbn!=null && sbn.getPackageName().equalsIgnoreCase("com.whatsapp")){
//
//        }else if(sbn!=null && sbn.getPackageName().equalsIgnoreCase("com.facebook.orca")){
//
//        }
//    }





