package com.a10835.easywol.fragment;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.a10835.easywol.Database.StudentListDatabaseHelper;
import com.a10835.easywol.MyApplication;
import com.a10835.easywol.R;
import com.a10835.easywol.ScrollablePanel.ColumnInfo;
import com.a10835.easywol.ScrollablePanel.ContentInfo;
import com.a10835.easywol.ScrollablePanel.RowInfo;
import com.a10835.easywol.ScrollablePanel.ScrollablePanelAdapter;
import com.kelin.scrollablepanel.library.ScrollablePanel;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by 10835 on 2018/4/15.
 */

public class HomeFragment extends Fragment {
    public static final int MAX_READ_SIZE = 1024;
    private Context mContext;
    private ProgressBar mProgressView;
    private TextView mSelectDate;
    private String mClassroomID;
    private String mPassword;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    private InputStreamReader inputStreamReader = null;
    private final String SYNC = "4";
    private final int LOGIN_OK = 32;
    private StudentListDatabaseHelper studentListDatabaseHelper;
    private byte[] read_byte_data = new byte[MAX_READ_SIZE];

    private SQLiteDatabase sqLiteDatabase;
    private Cursor cursor;
    private String dbName;

    private String[] piese;
    private String courseNumber;
    private String teacherID;
    private String[] Row_data = {"学号", "姓名", "签到时间", "签到状态", ""};
    //private String Host_IP = "192.168.43.176";

    private SyncTask syncTask = null;
    private ScrollablePanel scrollablePanel;
    private ScrollablePanelAdapter scrollablePanelAdapter;
    private String classNumber;
    private int numOfStudent;
    private int newVersion;
    private static final String TAG = "HomeFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        Intent intent = getActivity().getIntent();
        Bundle bundle = intent.getExtras();
        String UserMessage = bundle.getString("UserMessage");
        if (UserMessage != null) {
            String[] pieces = UserMessage.split(":");
            mClassroomID = pieces[0];
            mPassword = pieces[1];
        }

        Log.d("MyLog", mClassroomID + "  " + mPassword);
    }

    private int Get_Send_Data() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        int WEEKDAY = calendar.get(Calendar.DAY_OF_WEEK);
        int HOUR_OF_DAY = calendar.get(Calendar.HOUR_OF_DAY);
        int Send_data = 0;
        if (HOUR_OF_DAY < 10) {
            Send_data = 10 + WEEKDAY;
        } else if (HOUR_OF_DAY < 12) {
            Send_data = 20 + WEEKDAY;
        } else if (HOUR_OF_DAY < 16) {
            Send_data = 30 + WEEKDAY;
        } else if (HOUR_OF_DAY < 18) {
            Send_data = 40 + WEEKDAY;
        } else if (HOUR_OF_DAY < 22) {
            Send_data = 50 + WEEKDAY;
        }
        return Send_data;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_homepage, container, false);
        scrollablePanel = (ScrollablePanel) view.findViewById(R.id.scrollable_panel);
        mProgressView = view.findViewById(R.id.sync_progress);
        mSelectDate = view.findViewById(R.id.tv_pick_date);
        final Calendar calendar = Calendar.getInstance();
        mSelectDate.setText(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));

        mSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(mContext,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                                String info = "您选择的日期是：";
//                                info += year + "年" + monthOfYear + "月" + dayOfMonth + "日";
                                Calendar calendar1 = Calendar.getInstance();
                                calendar1.set(year, monthOfYear, dayOfMonth);
                                Date date = calendar1.getTime();
                                String info = new SimpleDateFormat("yyyy-MM-dd").format(date);
                                String strings = year + "" + (monthOfYear + 1) + "" + monthOfYear;
                                mSelectDate.setText(info);
                                syncTask.execute(strings);

                            }
                        }, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        syncTask = new SyncTask();
        syncTask.execute();
    }

    public static byte[] intToByteArray(int a) {
        byte[] ret = new byte[4];
        ret[0] = (byte) (a & 0xFF);
        ret[1] = (byte) ((a >> 8) & 0xFF);
        ret[2] = (byte) ((a >> 16) & 0xFF);
        ret[3] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }

    private int byteArrayToInt(byte[] b) {
        return b[0] & 0xFF |
                (b[1] & 0xFF) << 8 |
                (b[2] & 0xFF) << 16 |
                (b[3] & 0xFF) << 24;
    }

    public class SyncTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExevute");
            mProgressView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            Log.d(TAG, "doInBackground");
            try {
                int Data = Get_Send_Data();
                Socket socket = new Socket(MyApplication.Host_IP, 50000);
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);

                byte[] send_data = (mClassroomID + ":" + mPassword + ":" + SYNC + ":").getBytes();
                Log.d(TAG, new String(send_data, "UTF-8"));
                outputStream.write(send_data);
                inputStream.read(read_byte_data);
                Log.d(TAG, "read_byte_data");
                if (byteArrayToInt(read_byte_data) == LOGIN_OK) {
                    outputStream.write(intToByteArray(Data));
                    inputStream.read(read_byte_data);
                    if (read_byte_data.toString() == "DOWN") {
                        Log.d(TAG, "strings:" + strings);
                        outputStream.write(strings[0].getBytes());
                    }
                    Log.d("MyLog", "Login ok!!!!");
                } else {
                    Log.d("MyLog", "Login wrong!!!!");
                    Toast.makeText(mContext, "身份校验失败！请重新登录！", Toast.LENGTH_SHORT).show();
                    return false;
                }
                Log.d("MyLog", "create socket connection");

                studentListDatabaseHelper = new StudentListDatabaseHelper(mContext, "StudentList.db", null, 1);
                sqLiteDatabase = studentListDatabaseHelper.getWritableDatabase();
                inputStream.read(read_byte_data);
                piese = new String(read_byte_data, "UTF-8").split(":");
                courseNumber = piese[0];
                teacherID = piese[1];
                classNumber = piese[2];
                numOfStudent = Integer.parseInt(piese[3]);
                dbName = courseNumber + "_" + teacherID + "_" + classNumber + "_StudentList_table";
                Log.d("MyLog", dbName);

                newVersion = 2;
//                if (sqLiteDatabase.needUpgrade(newVersion)) {
                if (true) {
                    sqLiteDatabase.execSQL("drop table " + dbName);
                    Log.d("MyLog", "upgrade database " + dbName);
                }
                if (!studentListDatabaseHelper.tabIsExist(dbName)) {
                    String CREATE_STUDENT_LIST = "create table " + dbName + "(" +
                            "StudentID text primary key," +
                            "StudentName text)";
                    sqLiteDatabase.execSQL(CREATE_STUDENT_LIST);
                    Log.d("MyLog", "Create a new database");
                    int count = 0;
                    inputStream.read(read_byte_data);
                    piese = new String(read_byte_data, "UTF-8").split(":");
                    ContentValues values = new ContentValues();
                    Log.d("MyLog", "" + piese.length);
                    int i = 0;
                    while (i <= piese.length - 1 - 2) {
                        values.put("StudentID", piese[i]);
                        values.put("StudentName", piese[i + 1]);
                        sqLiteDatabase.insert(dbName, null, values);
                        Log.d("MyLog", "insert database :" + piese[i] + "  " + piese[i + 1]);
                        values.clear();
                        count++;
                        publishProgress((count / numOfStudent) * 100);
                        i += 2;
                    }
                    Log.d("MyLog", "while1 down");
                }
                Log.d("MyLog", "upgrade down");

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("MyLog", "error!!!!!!!!!!!!!!!!!!!!!!!");
                return false;
            }

            String SELECT_ALL_DATA = "select * from " + dbName;
            cursor = sqLiteDatabase.rawQuery(SELECT_ALL_DATA, null);
            Log.d("MyLog", "do in backgroude  down");
            return true;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Log.d("MyLog", "onPostExecute");
            if (aBoolean) {
                mProgressView.setVisibility(View.INVISIBLE);
                scrollablePanelAdapter = new ScrollablePanelAdapter();
                paddingData(scrollablePanelAdapter);
                Log.d("MyLog", "padding data ok");
                scrollablePanel.setPanelAdapter(scrollablePanelAdapter);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int value = values[0];
            mProgressView.setProgress(value);
        }

    }

    private void paddingData(ScrollablePanelAdapter scrollablePanelAdapter) {
        Log.d("MyLog", "padding data");
        List<ColumnInfo> columnInfoList = new ArrayList<>();
        Log.d("MyLog", "" + cursor.getCount());
        Log.d("MyLog", "" + cursor.getColumnCount());
        for (int i = 0; i < cursor.getCount(); i++) {
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setColumnNumber(i + 1 + "");
            columnInfoList.add(columnInfo);
        }
        scrollablePanelAdapter.setColumnInfoList(columnInfoList);

        List<RowInfo> rowInfoList = new ArrayList<>();
        for (int i = 0; i < Row_data.length; i++) {
            RowInfo rowInfo = new RowInfo();
            rowInfo.setString(Row_data[i]);
            rowInfoList.add(rowInfo);
        }
        scrollablePanelAdapter.setRowInfoList(rowInfoList);

        cursor.moveToFirst();
        List<List<ContentInfo>> contentList = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            List<ContentInfo> contentInfoList = new ArrayList<>();
            for (int j = 0; j < Row_data.length; j++) {
                ContentInfo contentInfo = new ContentInfo();
                if (j == 0)
                    contentInfo.setContent((cursor.getString(cursor.getColumnIndex("StudentID"))));
                else if (j == 1)
                    contentInfo.setContent((cursor.getString(cursor.getColumnIndex("StudentName"))));
                else
                    contentInfo.setContent("");
                if (j == 0)
                    contentInfo.setBegin(true);
                else
                    contentInfo.setBegin(false);
                contentInfo.setStatus(ContentInfo.Status.SIGN_OUT);
                contentInfoList.add(contentInfo);
            }
            cursor.moveToNext();
            contentList.add(contentInfoList);
        }
        scrollablePanelAdapter.setOrdersList(contentList);

    }

}
