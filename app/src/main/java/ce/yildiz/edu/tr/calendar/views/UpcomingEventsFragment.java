package ce.yildiz.edu.tr.calendar.views;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ce.yildiz.edu.tr.calendar.R;
import ce.yildiz.edu.tr.calendar.Utils;
import ce.yildiz.edu.tr.calendar.adapters.UpcomingEventAdapter;
import ce.yildiz.edu.tr.calendar.database.DBHelper;
import ce.yildiz.edu.tr.calendar.database.DBTables;
import ce.yildiz.edu.tr.calendar.models.Event;
import ce.yildiz.edu.tr.calendar.models.RecurringPattern;

import static android.app.Activity.RESULT_OK;

public class UpcomingEventsFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    private static final int EDIT_EVENT_ACTIVITY_REQUEST_CODE = 1;

    private ImageButton changePeriodImageButton;
    public TextView periodTextView;
    private RecyclerView eventsRecyclerView;

    private DBHelper dbHelper;

    //public String period;
    private String todayDate;

    public EditText editText_title;
    private List<Event> events;
    UpcomingEventAdapter disp_adapter;
    ArrayAdapter<String> adapter;
    //스피너 생성
    Spinner spinner;
    //스피너에 들어갈 item들
    String[] items = {"-검색 항목-","일정 제목","일정 장소","일정 목적"};

    Button btn_send;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming_events, container, false);

        dbHelper = new DBHelper(getActivity());
//        period = Utils.CURRENT_FILTER;

        defineViews(view);
        initViews();
        defineListeners();

        eventsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false);


        return view;
    }


    private void defineViews(View view) {
        changePeriodImageButton = (ImageButton) view.findViewById(R.id.UpcomingEventsFragment_ImageButton_Period);
        periodTextView = (TextView) view.findViewById(R.id.UpcomingEventsFragment_TextView_Period);
        eventsRecyclerView = (RecyclerView) view.findViewById(R.id.UpcomingEventsFragment_RecyclerView_Events);
        editText_title = (EditText) view.findViewById(R.id.editText_title);
        spinner = (Spinner) view.findViewById(R.id.spinner);

        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.invalidate();

        /*btn_send=view.findViewById(R.id.btn_send);

        // 버튼 검색부분
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 여기서 부터 검색창에 입력한 단어가 들어간 목록들만 나와야 한다
                String search = editText_title.getText().toString();

                List<Event> temp = new ArrayList();
                temp.clear();
                SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
                List<Event> allEvents = dbHelper.readAllEvents(sqLiteDatabase);

                for (Event d : allEvents){
                    if(d.getTitle().contains(search)){
                        temp.add(d);
                        Log.v("clickTest",d.getTitle());
                        Log.v("clickTest",d.getTitle() + temp.size());
                    }
                }

                if (getActivity()!=null){
                    disp_adapter = new UpcomingEventAdapter(getActivity(),temp,null);
                    // disp_adapter.updateList(temp);
                    // disp_adapter.notifyDataSetChanged();
                    eventsRecyclerView.setAdapter(disp_adapter);
                    eventsRecyclerView.invalidate();
                    eventsRecyclerView.getAdapter().notifyDataSetChanged();
                }


            }
        });*/

        editText_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //input값으로부터 리스트를 filter
                filter(editable.toString());
            }
        });
    }

    private void filter(String text) {
        List<Event> temp = new ArrayList();
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        List<Event> allEvents = dbHelper.readAllEvents(sqLiteDatabase);
        for (Event d : allEvents){
            //스피너 선택값 불러오기
            if (spinner.getSelectedItem().toString().equals("일정 제목")){
                if(d.getTitle().contains(text)){
                    temp.add(d);
                }
            } else if (spinner.getSelectedItem().toString().equals("일정 장소")){
                if (d.getLocation().contains(text)){
                    temp.add(d);
                    System.out.println(temp);
                }
            } else if (spinner.getSelectedItem().toString().equals("일정 목적")){
                if(d.getNote().contains(text)){
                    temp.add(d);
                }
            } else {
                if(d.getTitle().contains(text)||d.getLocation().contains(text)||d.getNote().contains(text)){
                    temp.add(d);
                }
            }
        }
        if (getActivity()!=null){
            disp_adapter = new UpcomingEventAdapter(getActivity(),temp,null);
            // disp_adapter.updateList(temp);
            // disp_adapter.notifyDataSetChanged();

            eventsRecyclerView.setAdapter(disp_adapter);
            eventsRecyclerView.invalidate();
            eventsRecyclerView.getAdapter().notifyDataSetChanged();
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void initViews() {
        periodTextView.setText(Utils.CURRENT_FILTER);
        setUpRecyclerView();
    }

    private void defineListeners() {
        changePeriodImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inflate menu
                PopupMenu popup = new PopupMenu(getActivity(), view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.popup_period, popup.getMenu());
                popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
                popup.show();

                setUpRecyclerView();
            }

            class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.PopupPeriod_Item_All:
                            Utils.CURRENT_FILTER = Utils.ALL;
                            periodTextView.setText(Utils.CURRENT_FILTER);
                            break;
                        case R.id.PopupPeriod_Item_Today:
                            Utils.CURRENT_FILTER = Utils.TODAY;
                            periodTextView.setText(Utils.CURRENT_FILTER);
                            break;
                        case R.id.PopupPeriod_Item_Next7Days:
                            Utils.CURRENT_FILTER = Utils.NEXT_7_DAYS;
                            periodTextView.setText(Utils.CURRENT_FILTER);
                            break;
                        case R.id.PopupPeriod_Item_Next30Days:
                            Utils.CURRENT_FILTER = Utils.NEXT_30_DAYS;
                            periodTextView.setText(Utils.CURRENT_FILTER);
                            break;
                    }
                    setUpRecyclerView();
                    return true;
                }
            }


        });
    }


    public void setUpRecyclerView() {
        eventsRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setMeasurementCacheEnabled(false);
        eventsRecyclerView.setLayoutManager(layoutManager);
        UpcomingEventAdapter upcomingEventAdapter = new UpcomingEventAdapter(getActivity(), collectEvents(Calendar.getInstance().getTime()), this);
        eventsRecyclerView.setAdapter(upcomingEventAdapter);
    }


    private List<Event> collectEvents(Date today) {
        List<Event> events = null;
        try {
            switch (Utils.CURRENT_FILTER) {
                case Utils.ALL:
                    events = collectAllEvents(today);
                    System.out.print(events);
                    break;
                case Utils.TODAY:
                    events = collectTodayEvents(today);
                    break;
                case Utils.NEXT_7_DAYS:
                    events = collectNext7DaysEvents(today);
                    break;
                case Utils.NEXT_30_DAYS:
                    events = collectNext30DaysEvents(today);
                    break;
            }
        } catch (ParseException e) {
            Log.e(TAG, "An error has occurred while parsing the date string");
        }

        return events;
    }

    private List<Event> collectTodayEvents(Date today) {
        List<Event> eventList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Add recurring events
        List<RecurringPattern> recurringPatterns = readRecurringPatterns();
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Event event = new Event();
        for (RecurringPattern recurringPattern : recurringPatterns) {
            switch (recurringPattern.getPattern()) {
                case Utils.DAILY:
                    event = dbHelper.readEvent(sqLiteDatabase, recurringPattern.getEventId());
                    event.setDate(Utils.eventDateFormat.format(today));
                    eventList.add(event);
                    break;
                case Utils.WEEKLY:
                    if (dayOfWeek == recurringPattern.getDayOfWeek()) {
                        event = dbHelper.readEvent(sqLiteDatabase, recurringPattern.getEventId());
                        event.setDate(Utils.eventDateFormat.format(today));
                        eventList.add(event);
                    }
                    break;
                case Utils.MONTHLY:
                    if (dayOfMonth == recurringPattern.getDayOfMonth()) {
                        event = dbHelper.readEvent(sqLiteDatabase, recurringPattern.getEventId());
                        event.setDate(Utils.eventDateFormat.format(today));
                        eventList.add(event);
                    }
                    break;
                case Utils.YEARLY:
                    if (month == recurringPattern.getMonthOfYear() && dayOfMonth == recurringPattern.getDayOfMonth()) {
                        event = dbHelper.readEvent(sqLiteDatabase, recurringPattern.getEventId());
                        event.setDate(Utils.eventDateFormat.format(today));
                        eventList.add(event);
                    }
                    break;
            }
        }


        // Add non-recurring events
        Cursor cursor = dbHelper.readEventsByDate(sqLiteDatabase, Utils.eventDateFormat.format(today));
        while (cursor.moveToNext()) {
            int eventID = cursor.getInt(cursor.getColumnIndex(DBTables.EVENT_ID));
            if (!isContains(eventList, eventID)) {
                eventList.add(dbHelper.readEvent(sqLiteDatabase, eventID));
            }
        }
        cursor.close();
        sqLiteDatabase.close();
        return eventList;
    }

    private List<Event> collectNext7DaysEvents(Date today) throws ParseException {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(today);

        Calendar toCalendar = (Calendar) fromCalendar.clone();
        toCalendar.add(Calendar.DAY_OF_MONTH, 8);

        Date fromDate = fromCalendar.getTime();
        Date toDate = toCalendar.getTime();

        List<Event> eventList = new ArrayList<>();
        // Add recurring events
        List<RecurringPattern> recurringPatterns = readRecurringPatterns();
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Event event = new Event();
        for (RecurringPattern recurringPattern : recurringPatterns) {
            switch (recurringPattern.getPattern()) {
                case Utils.DAILY:
                    Calendar mCalendar = (Calendar) fromCalendar.clone();
                    event = dbHelper.readEvent(sqLiteDatabase, recurringPattern.getEventId());
                    for (int i = 0; i < 7; i++) {
                        mCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        event.setDate(Utils.eventDateFormat.format(mCalendar.getTime()));
                        eventList.add(event);
                        event = dbHelper.readEvent(sqLiteDatabase, recurringPattern.getEventId()); // TODO: clone the object
                    }
                    break;
                case Utils.WEEKLY:
                    mCalendar = (Calendar) fromCalendar.clone();
                    mCalendar.add(Calendar.DAY_OF_MONTH, 7);
                    mCalendar.set(Calendar.DAY_OF_WEEK, recurringPattern.getDayOfWeek());
                    event = dbHelper.readEvent(sqLiteDatabase, recurringPattern.getEventId());
                    event.setDate(Utils.eventDateFormat.format(mCalendar.getTime()));
                    eventList.add(event);
                    break;
                case Utils.MONTHLY:
                    mCalendar = (Calendar) fromCalendar.clone();
                    mCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    if (recurringPattern.getDayOfMonth() >= mCalendar.get(Calendar.DAY_OF_MONTH)) {
                        mCalendar.set(Calendar.DAY_OF_MONTH, recurringPattern.getDayOfMonth());
                        event = dbHelper.readEvent(sqLiteDatabase, recurringPattern.getEventId());
                        event.setDate(Utils.eventDateFormat.format(mCalendar.getTime()));
                        eventList.add(event);
                    }
                    break;
            }
        }

        List<Event> allEvents = dbHelper.readAllEvents(sqLiteDatabase);
        for (Event mEvent : allEvents) {
            Date currentDate = Utils.eventDateFormat.parse(mEvent.getDate());
            if (currentDate.after(fromDate) && currentDate.before(toDate) && !isContains(eventList, mEvent.getId())) {
                eventList.add(mEvent);
            }
        }
        sqLiteDatabase.close();
        return eventList;
    }

    private List<Event> collectNext30DaysEvents(Date today) throws ParseException {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(today);

        Calendar toCalendar = (Calendar) fromCalendar.clone();
        toCalendar.add(Calendar.DAY_OF_MONTH, 31);

        Date fromDate = fromCalendar.getTime();
        Date toDate = toCalendar.getTime();

        List<Event> eventList = new ArrayList<>();
        // Add recurring events
        List<RecurringPattern> recurringPatterns = readRecurringPatterns();
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Event event = new Event();
        for (RecurringPattern recurringPattern : recurringPatterns) {
            switch (recurringPattern.getPattern()) {
                case Utils.DAILY:
                    Calendar mCalendar = (Calendar) fromCalendar.clone();
                    for (int i = 0; i < 30; i++) {
                        event = dbHelper.readEvent(sqLiteDatabase, recurringPattern.getEventId());// TODO: clone the object
                        mCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        event.setDate(Utils.eventDateFormat.format(mCalendar.getTime()));
                        eventList.add(event);
                    }
                    break;
                case Utils.WEEKLY:
                    mCalendar = (Calendar) fromCalendar.clone();
                    for (int i = 0; i < 4; i++) {
                        event = dbHelper.readEvent(sqLiteDatabase, recurringPattern.getEventId()); // TODO: clone the object
                        mCalendar.add(Calendar.DAY_OF_MONTH, 7);
                        mCalendar.set(Calendar.DAY_OF_WEEK, recurringPattern.getDayOfWeek());
                        if (mCalendar.getTime().before(toDate)) {
                            event.setDate(Utils.eventDateFormat.format(mCalendar.getTime()));
                            eventList.add(event);
                        }
                    }
                    break;
                case Utils.MONTHLY:
                    mCalendar = (Calendar) fromCalendar.clone();
                    mCalendar.set(Calendar.DAY_OF_MONTH, recurringPattern.getDayOfMonth());
                    mCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    if (mCalendar.getTime().before(toDate) && mCalendar.getTime().after(fromDate)) {
                        mCalendar.set(Calendar.DAY_OF_MONTH, recurringPattern.getDayOfMonth());
                        event = dbHelper.readEvent(sqLiteDatabase, recurringPattern.getEventId());
                        event.setDate(Utils.eventDateFormat.format(mCalendar.getTime()));
                        eventList.add(event);
                    }
                    break;
            }
        }

        List<Event> allEvents = dbHelper.readAllEvents(sqLiteDatabase);
        for (Event mEvent : allEvents) {
            Date currentDate = Utils.eventDateFormat.parse(mEvent.getDate());
            if (currentDate.after(fromDate) && currentDate.before(toDate) && !isContains(eventList, mEvent.getId())) {
                eventList.add(mEvent);
            }
        }
        sqLiteDatabase.close();
        return eventList;
    }

    private List<Event> collectAllEvents(Date today) {
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        return dbHelper.readAllEvents(sqLiteDatabase);
    }

    private List<RecurringPattern> readRecurringPatterns() {
        List<RecurringPattern> recurringPatterns = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.readAllRecurringPatterns(sqLiteDatabase);
        while (cursor.moveToNext()) {
            RecurringPattern recurringPattern = new RecurringPattern();
            recurringPattern.setEventId(cursor.getInt(cursor.getColumnIndex(DBTables.RECURRING_PATTERN_EVENT_ID)));
            recurringPattern.setPattern(cursor.getString(cursor.getColumnIndex(DBTables.RECURRING_PATTERN_TYPE)));
            recurringPattern.setMonthOfYear(cursor.getInt(cursor.getColumnIndex(DBTables.RECURRING_PATTERN_MONTH_OF_YEAR)));
            recurringPattern.setDayOfMonth(cursor.getInt(cursor.getColumnIndex(DBTables.RECURRING_PATTERN_DAY_OF_MONTH)));
            recurringPattern.setDayOfWeek(cursor.getInt(cursor.getColumnIndex(DBTables.RECURRING_PATTERN_DAY_OF_WEEK)));
            recurringPatterns.add(recurringPattern);
        }
        return recurringPatterns;
    }

    private boolean isContains(List<Event> events, int eventId) {
        for (Event event : events) {
            if (event.getId() == eventId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            setUpRecyclerView();
            Toast.makeText(getActivity(), "일정 수정됨!", Toast.LENGTH_SHORT).show();
        }
    }
}
