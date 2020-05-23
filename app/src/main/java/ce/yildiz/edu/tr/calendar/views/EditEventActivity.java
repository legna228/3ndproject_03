package ce.yildiz.edu.tr.calendar.views;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ce.yildiz.edu.tr.calendar.R;
import ce.yildiz.edu.tr.calendar.Utils;
import ce.yildiz.edu.tr.calendar.adapters.NotificationAdapter;
import ce.yildiz.edu.tr.calendar.database.DBHelper;
import ce.yildiz.edu.tr.calendar.database.DBTables;
import ce.yildiz.edu.tr.calendar.models.Event;
import ce.yildiz.edu.tr.calendar.models.Notification;
import ce.yildiz.edu.tr.calendar.other.ServiceAutoLauncher;
import petrov.kristiyan.colorpicker.ColorPicker;

public class EditEventActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private final int MAPS_ACTIVITY_REQUEST = 1;

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextInputLayout eventTitleTextInputLayout;
    private Switch allDayEventSwitch;
    private LinearLayout setDateLinearLayout;
    private TextView setDateTextView;
    private LinearLayout setTimeLinearLayout;
    private TextView setTimeTextView;
    private Button setDurationButton;
    private RecyclerView notificationsRecyclerView;
    private TextView addNotificationTextView;
    private TextView repeatTextView;
    private TextInputLayout eventNoteTextInputLayout;
    private TextView pickNoteColorTextView;
    private TextInputLayout eventLocationTextInputLayout;
    private ImageButton locationImageButton;
    private TextInputLayout phoneNumberTextInputLayout;
    private TextInputLayout mailTextInputLayout;
    private TextInputEditText mailTextInputEditText;
    private Switch mailSwitch;

    private AlertDialog notificationAlertDialog;
    private AlertDialog repetitionAlertDialog;
    private int alarmYear, alarmMonth, alarmDay, alarmHour, alarmMinute;

    private int notColor;
    private DBHelper dbHelper;
    private List<Notification> currentNotifications;
    private List<Notification> eventNotifications;
    private int oldEventId;
    private NotificationAdapter notificationAdapter;
    private Event mEvent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(getAppTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        dbHelper = new DBHelper(this);

        defineViews();
        initViews();
        initVariables();
        createAlertDialogs();
        defineListeners();

        setSupportActionBar(toolbar);
    }

    private void defineViews() {
        eventTitleTextInputLayout = (TextInputLayout) findViewById(R.id.AddNewEventActivity_TextInputLayout_EventTitle);
        allDayEventSwitch = (Switch) findViewById(R.id.AddNewEventActivity_Switch_AllDayEvent);
        setDateLinearLayout = (LinearLayout) findViewById(R.id.AddNewEventActivity_LinearLayout_SetDate);
        setDateTextView = (TextView) findViewById(R.id.AddNewEventActivity_TexView_SetDate);
        setTimeLinearLayout = (LinearLayout) findViewById(R.id.AddNewEventActivity_LinearLayout_SetTime);
        setTimeTextView = (TextView) findViewById(R.id.AddNewEventActivity_TexView_SetTime);
        setDurationButton = (Button) findViewById(R.id.AddNewEventActivity_Button_Duration);
        notificationsRecyclerView = (RecyclerView) findViewById(R.id.AddNewEventActivity_RecyclerView_Notifications);
        repeatTextView = (TextView) findViewById(R.id.AddNewEventActivity_TextView_Repeat);
        addNotificationTextView = (TextView) findViewById(R.id.AddNewEventActivity_TextView_Add_Notification);
        eventNoteTextInputLayout = (TextInputLayout) findViewById(R.id.AddNewEventActivity_TextInputLayout_Note);
        pickNoteColorTextView = (TextView) findViewById(R.id.AddNewEventActivity_TextView_PickNoteColor);
        eventLocationTextInputLayout = (TextInputLayout) findViewById(R.id.AddNewEventActivity_TextInputLayout_Location);
        locationImageButton = (ImageButton) findViewById(R.id.AddNewEventActivity_ImageButton_Location);
        phoneNumberTextInputLayout = (TextInputLayout) findViewById(R.id.AddNewEventActivity_TextInputLayout_PhoneNumber);
        mailTextInputLayout = (TextInputLayout) findViewById(R.id.AddNewEventActivity_TextInputLayout_Mail);
        mailTextInputEditText = (TextInputEditText) findViewById(R.id.AddNewEventActivity_TextInputEditText_Mail);
        mailSwitch = (Switch) findViewById(R.id.AddNewEventActivity_Switch_Mail);

        progressBar = (ProgressBar) findViewById(R.id.AddNewEventActivity_ProgressBar);
        toolbar = (Toolbar) findViewById(R.id.AddNewEventActivity_Toolbar);

    }

    @SuppressLint("ResourceType")
    private void initViews() {

        Intent intent = getIntent();
        String eventTitle = intent.getStringExtra("eventTitle");
        String eventDate = intent.getStringExtra("eventDate");
        String eventTime = intent.getStringExtra("eventTime");
        mEvent = readEvent(eventTitle, eventDate, eventTime);
        oldEventId = mEvent.getId();

        eventTitleTextInputLayout.getEditText().setText(mEvent.getTitle());

        setDateTextView.setText(mEvent.getDate());

        if (mEvent.isAllDay()) {
            allDayEventSwitch.setChecked(true);
            setTimeLinearLayout.setVisibility(View.GONE);

        } else {
            allDayEventSwitch.setChecked(false);
            setTimeTextView.setText(mEvent.getTime());
        }

        setDurationButton.setText(mEvent.getDuration());

        eventNotifications = readNotifications(mEvent.getId());
        cancelAlarms(eventNotifications);
        currentNotifications = new ArrayList<>(eventNotifications);
        setUpRecyclerView();

        repeatTextView.setText(mEvent.getRepetition());

        eventNoteTextInputLayout.getEditText().setText(mEvent.getNote());

        GradientDrawable bgShape = (GradientDrawable) pickNoteColorTextView.getBackground();
        bgShape.setColor(mEvent.getColor());

        eventLocationTextInputLayout.getEditText().setText(mEvent.getLocation());

        phoneNumberTextInputLayout.getEditText().setText(mEvent.getPhoneNumber());

        if (mEvent.getMail() == null | "".equals(mEvent.getMail())) {
            mailSwitch.setChecked(false);
            mailTextInputEditText.setText("");
            mailTextInputEditText.setEnabled(false);
            mailTextInputLayout.setEnabled(false);
        } else {
            mailSwitch.setChecked(true);
            mailTextInputEditText.setEnabled(true);
            mailTextInputLayout.setEnabled(true);
            mailTextInputLayout.getEditText().setText(mEvent.getMail());
        }
    }

    private void initVariables() {
        Calendar mCal = Calendar.getInstance();
        mCal.setTimeZone(TimeZone.getDefault());
        try {
            mCal.setTime(Utils.eventDateFormat.parse(setDateTextView.getText().toString()));
            alarmYear = mCal.get(Calendar.YEAR);
            alarmMonth = mCal.get(Calendar.MONTH);
            alarmDay = mCal.get(Calendar.DAY_OF_MONTH);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void createAlertDialogs() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        // Notification AlertDialog
        final View notificationDialogView = LayoutInflater.from(this).inflate(R.layout.layout_alert_dialog_notification, null, false);
        RadioGroup notificationRadioGroup = (RadioGroup) notificationDialogView.findViewById(R.id.AlertDialogLayout_RadioGroup);
        notificationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                currentNotifications.add(new Notification(((RadioButton) notificationDialogView.findViewById(checkedId)).getText().toString()));
                notificationAlertDialog.dismiss();
                setUpRecyclerView();
            }
        });
        builder.setView(notificationDialogView);
        notificationAlertDialog = builder.create();
        ((Button) notificationDialogView.findViewById(R.id.AlertDialogLayout_Button_Back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationAlertDialog.dismiss();
            }
        });

        // Event repetition AlertDialog
        final View eventRepetitionDialogView = LayoutInflater.from(this).inflate(R.layout.layout_alert_dialog_repeat, null, false);
        RadioGroup eventRepetitionRadioGroup = (RadioGroup) eventRepetitionDialogView.findViewById(R.id.AlertDialogLayout_RadioGroup);
        eventRepetitionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                repeatTextView.setText("Repeat " + ((RadioButton) eventRepetitionDialogView.findViewById(checkedId)).getText().toString());
                repetitionAlertDialog.dismiss();
            }
        });
        builder.setView(eventRepetitionDialogView);
        repetitionAlertDialog = builder.create();
        ((Button) eventRepetitionDialogView.findViewById(R.id.AlertDialogLayout_Button_Back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repetitionAlertDialog.dismiss();
            }
        });

    }

    private void defineListeners() {
        allDayEventSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    setTimeLinearLayout.setVisibility(View.GONE);
                } else {
                    setTimeLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        setDateLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDate(view);
            }
        });

        setTimeLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime(view);
            }
        });

        setDurationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDuration(view);
            }
        });

        addNotificationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationAlertDialog.show();
            }
        });

        repeatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repetitionAlertDialog.show();
            }
        });

        pickNoteColorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickNoteColor(view);
            }
        });

        locationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), MapsActivity.class), MAPS_ACTIVITY_REQUEST);
            }
        });

        mailSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    mailTextInputEditText.setEnabled(true);
                    mailTextInputLayout.setEnabled(true);
                } else {
                    mailTextInputEditText.setText("");
                    mailTextInputEditText.setEnabled(false);
                    mailTextInputLayout.setEnabled(false);
                }

            }
        });
    }

    private Event readEvent(String eventTitle, String eventDate, String eventTime) {
        Event event = new Event();
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.readEvent(sqLiteDatabase, eventTitle, eventDate, eventTime);
        while (cursor.moveToNext()) {
            event.setId(cursor.getInt(cursor.getColumnIndex(DBTables.EVENT_ID)));
            event.setTitle(cursor.getString(cursor.getColumnIndex(DBTables.EVENT_TITLE)));
            event.setAllDay(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(DBTables.EVENT_ALL_DAY))));
            event.setDate(cursor.getString(cursor.getColumnIndex(DBTables.EVENT_DATE)));
            event.setMonth(cursor.getString(cursor.getColumnIndex(DBTables.EVENT_MONTH)));
            event.setYear(cursor.getString(cursor.getColumnIndex(DBTables.EVENT_YEAR)));
            event.setTime(cursor.getString(cursor.getColumnIndex(DBTables.EVENT_TIME)));
            event.setDuration(cursor.getString(cursor.getColumnIndex(DBTables.EVENT_DURATION)));
            event.setNotify(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(DBTables.EVENT_NOTIFY))));
            event.setRepetition(cursor.getString(cursor.getColumnIndex(DBTables.EVENT_REPETITION)));
            event.setNote(cursor.getString(cursor.getColumnIndex(DBTables.EVENT_NOTE)));
            event.setColor(cursor.getInt(cursor.getColumnIndex(DBTables.EVENT_COLOR)));
            event.setLocation(cursor.getString(cursor.getColumnIndex(DBTables.EVENT_LOCATION)));
            event.setPhoneNumber(cursor.getString(cursor.getColumnIndex(DBTables.EVENT_PHONE_NUMBER)));
            event.setPhoneNumber(cursor.getString(cursor.getColumnIndex(DBTables.EVENT_MAIL)));
        }

        cursor.close();
        sqLiteDatabase.close();

        return event;
    }

    private ArrayList<Notification> readNotifications(int eventId) {
        ArrayList<Notification> notifications = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.readEventNotifications(sqLiteDatabase, eventId);
        while (cursor.moveToNext()) {
            Notification notification = new Notification();
            notification.setId(cursor.getInt(cursor.getColumnIndex(DBTables.NOTIFICATION_ID)));
            notification.setEventId(cursor.getInt(cursor.getColumnIndex(DBTables.NOTIFICATION_EVENT_ID)));
            notification.setTime(cursor.getString(cursor.getColumnIndex(DBTables.NOTIFICATION_TIME)));
            notification.setChannelId(cursor.getInt(cursor.getColumnIndex(DBTables.NOTIFICATION_CHANNEL_ID)));
            notifications.add(notification);
        }

        return notifications;
    }


    private void setDuration(View view) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.DurationPickerTheme, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                setDurationButton.setText("DURATION: " + Integer.toString(hourOfDay) + " HOURS " + Integer.toString(minute) + " MINUTES");
            }
        }, 0, 0, true);
        timePickerDialog.setTitle("Duration");

        timePickerDialog.show();
    }

    public void setTime(View view) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar aCal = Calendar.getInstance();
                aCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                aCal.set(Calendar.MINUTE, minute);
                aCal.setTimeZone(TimeZone.getDefault());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("K:mm a", Locale.ENGLISH);
                String eventTime = simpleDateFormat.format(aCal.getTime());

                alarmHour = hourOfDay;
                alarmMinute = minute;

                setTimeTextView.setText(eventTime);

            }
        }, hour, minute, false);
        timePickerDialog.show();

    }

    public void setDate(View view) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar aCal = Calendar.getInstance();
                aCal.set(Calendar.YEAR, year);
                aCal.set(Calendar.MONTH, month);
                aCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                aCal.setTimeZone(TimeZone.getDefault());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
                String eventTime = simpleDateFormat.format(aCal.getTime());

                alarmYear = year;
                alarmMonth = month;
                alarmDay = dayOfMonth;

                setDateTextView.setText(eventTime);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    public void pickNoteColor(View view) {
        final ArrayList<String> colors = Utils.getColors(this);
        ColorPicker colorPicker = new ColorPicker(this);
        colorPicker
                .setColors(colors)
                .setColumns(5)
                .setDefaultColorButton(R.color.blue)
                .setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position, int color) {
                        notColor = color;
                        GradientDrawable bgShape = (GradientDrawable) pickNoteColorTextView.getBackground();
                        bgShape.setColor(color);
                    }

                    @Override
                    public void onCancel() {

                    }
                }).show();
    }

    private void setUpRecyclerView() {
        notificationsRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setMeasurementCacheEnabled(false);
        notificationsRecyclerView.setLayoutManager(layoutManager);
        notificationAdapter = new NotificationAdapter(this, currentNotifications);
        notificationsRecyclerView.setAdapter(notificationAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.ToolBar_Item_Save:
                if (confirmInputs()) {
                    getViewValues();
                    new UpdateAsyncTask().execute();
                    if (mEvent.isNotify()) {
                        setAlarms();
                    }
                }
                break;
        }

        return true;
    }

    private void cancelAlarms(List<Notification> notifications) {
        for (Notification notification : notifications) {
            cancelAlarm(notification.getId());
        }
    }

    private void cancelAlarm(int requestCode) {
        Intent intent = new Intent(this, ServiceAutoLauncher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void setAlarms() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(alarmYear, alarmMonth, alarmDay);

        calendar.set(Calendar.HOUR_OF_DAY, alarmHour);
        calendar.set(Calendar.MINUTE, alarmMinute);
        calendar.set(Calendar.SECOND, 0);

        for (Notification notification : notificationAdapter.getNotifications()) {
            Calendar aCal = (Calendar) calendar.clone();
            String notificationPreference = notification.getTime();

            if (notificationPreference.equals(getString(R.string._10_minutes_before))) {
                aCal.add(Calendar.MINUTE, -10);
            } else if (notificationPreference.equals(getString(R.string._1_hour_before))) {
                aCal.add(Calendar.HOUR_OF_DAY, -1);
            } else if (notificationPreference.equals(getString(R.string._1_day_before))) {
                aCal.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                Log.i(TAG, "setAlarms: ");
            }

            setAlarm(notification, aCal.getTimeInMillis());
        }
    }

    private void setAlarm(Notification notification, long triggerAtMillis) {
        Intent intent = new Intent(this, ServiceAutoLauncher.class);
        intent.putExtra("eventTitle", mEvent.getTitle());
        intent.putExtra("eventNote", mEvent.getNote());
        intent.putExtra("eventColor", mEvent.getColor());
        intent.putExtra("eventTimeStamp", mEvent.getDate() + ", " + mEvent.getTime());
        intent.putExtra("interval", getInterval());
        intent.putExtra("soundName", getString("ringtone"));
        String asd = getInterval();
        Log.d(TAG, "setAlarm: " + asd);
        intent.putExtra("notificationId", notification.getChannelId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notification.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    private String getInterval() {
        String interval = getString(R.string.one_time);
        String repeatingPeriod = repeatTextView.getText().toString();
        if (repeatingPeriod.equals(getString(R.string.daily))) {
            interval = getString(R.string.daily);
        } else if (repeatingPeriod.equals(getString(R.string.weekly))) {
            interval = getString(R.string.weekly);
        } else if (repeatingPeriod.equals(getString(R.string.monthly))) {
            interval = getString(R.string.monthly);
        } else if (repeatingPeriod.equals(getString(R.string.yearly))) {
            interval = getString(R.string.yearly);
        }
        return interval;
    }

    @SuppressLint("ResourceType")
    private void getViewValues() {
        Date aDate = null;
        try {
            aDate = Utils.eventDateFormat.parse((String) setDateTextView.getText());
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "An error has occurred while parsing the date string");
        }
        mEvent.setTitle(eventTitleTextInputLayout.getEditText().getText().toString().trim());
        mEvent.setAllDay(allDayEventSwitch.isChecked());
        mEvent.setDate(Utils.eventDateFormat.format(aDate));
        mEvent.setMonth(Utils.monthFormat.format(aDate));
        mEvent.setYear(Utils.yearFormat.format(aDate));
        mEvent.setTime(setTimeTextView.getText().toString());
        mEvent.setDuration(setDurationButton.getText().toString());
        mEvent.setNotify(!notificationAdapter.getNotifications().isEmpty());
        mEvent.setRepetition(repeatTextView.getText().toString());
        mEvent.setNote(eventNoteTextInputLayout.getEditText().getText().toString().trim());
        if (notColor == 0) {
            notColor = getResources().getInteger(R.color.red);
        } else {
            mEvent.setColor(notColor);
        }
        mEvent.setLocation(eventLocationTextInputLayout.getEditText().getText().toString().trim());
        mEvent.setPhoneNumber(phoneNumberTextInputLayout.getEditText().getText().toString().trim());
        mEvent.setMail(mailTextInputLayout.getEditText().getText().toString().trim());

    }

    private boolean confirmInputs() {
        if (!validateEventTitle()) {
            return false;
        }

        if (!validateNotifications()) {
            Snackbar.make(addNotificationTextView, "You cannot set a notification to the past.", BaseTransientBottomBar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateEventTitle() {
        String eventTitleString = eventTitleTextInputLayout.getEditText().getText().toString().trim();
        if (eventTitleString.isEmpty()) {
            eventTitleTextInputLayout.setError("Field can't be empty!");
            return false;
        } else {
            eventTitleTextInputLayout.setError(null);
            return true;
        }
    }

    private boolean validateNotifications() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(alarmYear, alarmMonth, alarmDay);

        calendar.set(Calendar.HOUR_OF_DAY, alarmHour);
        calendar.set(Calendar.MINUTE, alarmMinute);
        calendar.set(Calendar.SECOND, 0);

        for (Notification notification : notificationAdapter.getNotifications()) {
            Calendar aCal = (Calendar) calendar.clone();
            String notificationPreference = notification.getTime();

            if (notificationPreference.equals(getString(R.string._10_minutes_before))) {
                aCal.add(Calendar.MINUTE, -10);
            } else if (notificationPreference.equals(getString(R.string._1_hour_before))) {
                aCal.add(Calendar.HOUR_OF_DAY, -1);
            } else if (notificationPreference.equals(getString(R.string._1_day_before))) {
                aCal.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                Log.i(TAG, "setAlarms: ");
            }

            if (aCal.before(Calendar.getInstance())) {
                return false;
            }
        }
        return true;
    }

    private class UpdateAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dbHelper.updateEvent(dbHelper.getWritableDatabase(), oldEventId, mEvent);
            for (Notification notification : eventNotifications) {
                dbHelper.deleteNotificationById(dbHelper.getWritableDatabase(), notification.getId());
            }

            for (Notification notification : notificationAdapter.getNotifications()) {
                notification.setEventId(mEvent.getId());
                dbHelper.saveNotification(dbHelper.getWritableDatabase(), notification);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dbHelper.close();
            setResult(RESULT_OK);
            finish();
        }
    }

    private int getEventId(String eventTitle, String eventDate, String eventTime) {
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.readEvent(sqLiteDatabase, eventTitle, eventDate, eventTime);

        int eventId = -1;
        while (cursor.moveToNext()) {
            eventId = cursor.getInt(cursor.getColumnIndex(DBTables.EVENT_ID));
        }

        cursor.close();
        sqLiteDatabase.close();
        return eventId;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == MAPS_ACTIVITY_REQUEST) {
            if (resultCode == RESULT_OK) {
                eventLocationTextInputLayout.getEditText().setText(data.getStringExtra("address"));

            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }

    }

    private boolean getFlag(String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(key, false);
    }

    private int getAppTheme() {
        switch (getString("theme")) {
            case "Dark":
                return R.style.DarkTheme;
            case "Indigo":
                return R.style.DarkIndigoTheme;
        }

        return R.style.DarkIndigoTheme;
    }

    private String getString(String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(key, "Indigo");
    }
}