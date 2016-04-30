package camera.fizz5.com.cameramodule;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private ArrayList<MyImage> images;
    public ArrayList<String> title;
    private ImageAdapter adapter;
    private AlarmReceiver Areceiver;
    private ListView listView;
    private ListView list;
    private Uri mCapturedImageURI;
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private DAOdb daOdb;
    private String[] FilePathStrings;
    private String[] FileNameStrings;
    private File[] listFile;
    // for storing
    private static final int SELECT_PICTURE = 1;
    private Uri fileUri;
    private String file_image = "myimage", id;
    private String string,selectedImagePath;
    File imageFile;
    private Context mContext;
    public String timeStamp = new SimpleDateFormat("dd-MM-yyyy hh:mm").format(new Date());
    public String fname= "IMG_"+ timeStamp + ".jpg";
    static final int DATE_DIALOG_ID = 0;
    private int currentYear, currentMonth, currentDay;
    public String dayG,monthG,yearG;
    TextView describe;
    AlertDialog alert;
    AlertDialog.Builder build;
    Button newDate;
    String dateString="4-4-2016";
    private SQLiteDatabase database;
    private DBhelper dbHelper;
    public File mediaFile;
    NotificationManager manager;
    private NotificationManager myGoalNotifyMgr;
    NotificationCompat.Builder gBuilder;
    ArrayList<String> TitleRem=new ArrayList<String>();
    ArrayList<String> dateRem=new ArrayList<String>();
    ArrayList<Integer> countRem=new ArrayList<Integer>();
    ArrayList<Integer> IdRem=new ArrayList<Integer>();
    ArrayList<Integer> mon = new ArrayList<Integer>(Arrays.asList(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31));
    ArrayList<String> monStr = new ArrayList<String>(Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"));
    private ArrayList<String> daysLeftGoal = new ArrayList<String>();
    PendingIntent resultPendingIntent,deletePendingIntent;
    private DatePicker picker;
    public int NotiId=0;
    BroadcastReceiver receiver;
    public int delGoalId,delGoalId1;
    Button EditButton;
    TextView textEdit;
    EditText DescEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Construct the data source
        TitleRem.clear();
        countRem.clear();
        IdRem.clear();
        dateRem.clear();
        //picker = (DatePicker) findViewById(R.id.scheduleTimePicker);
        //describe = (TextView) findViewById(R.id.text_view_description);
        images = new ArrayList();
        dbHelper = new DBhelper(this);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //display image
        // Check for SD Card
        // Locate the image folder in your SD Card
        imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "FiZZ");
        if (!imageFile.exists()) {
            if (!imageFile.mkdirs()) {
                Log.d("FiZZ", "failed to create directory");
                Toast.makeText(getBaseContext(), "File directory creation failed", Toast.LENGTH_SHORT).show();
                //return null;
            } else {
                // Create a new folder if no folder named SDImageTutorial exist
                imageFile.mkdirs();
            }
        }

        final SwipeMenuListView swipelist = (SwipeMenuListView) findViewById(R.id.main_list_view);

        // Pass String arrays to ListAdapter Class
        adapter = new ImageAdapter(this, images);
        // Set the ListAdapter to the ListView
        //list.setAdapter(adapter);

        swipelist.setAdapter(adapter);
        addItemClickListener(swipelist);
        initDB();
        //NotifyFunc(images);

        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // Create different menus depending on the view type
                SwipeMenuItem item1 = new SwipeMenuItem(
                        getApplicationContext());
                item1.setBackground(new ColorDrawable(Color.rgb(0x30, 0xB1,
                        0xF5)));
                item1.setWidth(dp2px(90));
                item1.setIcon(R.drawable.reminder);
                menu.addMenuItem(item1);
                SwipeMenuItem item2 = new SwipeMenuItem(
                        getApplicationContext());
                item2.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                item2.setWidth(dp2px(90));
                item2.setIcon(R.drawable.ic_action_discard);
                menu.addMenuItem(item2);
                SwipeMenuItem item3 = new SwipeMenuItem(
                        getApplicationContext());
                item3.setBackground(new ColorDrawable(Color.rgb(0xE5, 0xE0,
                        0x3F)));
                item3.setWidth(dp2px(90));
                item3.setIcon(R.drawable.ic_action_important);
                menu.addMenuItem(item3);

            }

        };

        // set creator
        swipelist.setMenuCreator(creator);

        // step 2. listener item click event
        swipelist.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                // ApplicationInfo item = mAppList.get(position);
                final MyImage image = (MyImage) swipelist.getItemAtPosition((int) position);
                switch (index) {
                    case 0:
                        // open

                        LayoutInflater li = LayoutInflater.from(MainActivity.this);
                        View DateView = li.inflate(R.layout.calendar_cam, null);
                        build = new AlertDialog.Builder(MainActivity.this);
                        build.setTitle("Reminder");
                        build.setMessage("Pick a date");
                        build.setView(DateView);
                        final Calendar c = Calendar.getInstance();
                        currentYear = c.get(Calendar.YEAR);
                        currentMonth = c.get(Calendar.MONTH);
                        currentDay = c.get(Calendar.DAY_OF_MONTH);
                        //picker = (DatePicker) findViewById(R.id.scheduleTimePicker);
                        newDate = (Button) DateView.findViewById(R.id.buttonCalCam); //Button which opens the calender
                        newDate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Get the date from our datepicker
                                showDialog(DATE_DIALOG_ID);
                            }
                        });


                        build.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                database = dbHelper.getWritableDatabase();
                                ContentValues cv = new ContentValues();
                                cv.put(DBhelper.COLUMN_DESCRIPTION, dateString);
                                Log.d("Updating Date: ", ".....");
                                String whereClause =
                                       /* DBhelper.COLUMN_TITLE + "=? AND " +*/ DBhelper.COLUMN_DATETIME + "=?";
                                String[] whereArgs = new String[]{/*image.getTitle(),*/ String.valueOf(image.getDatetimeLong())};
                                database.update(DBhelper.TABLE_NAME, cv, whereClause, whereArgs);
                                Log.d("Updating Date: ", ".....");
                                image.setDescription(dateString);

                                swipelist.invalidateViews();

                            }
                        });
                        build.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        alert = build.create();
                        alert.show();
                        break;
                    case 1:
                        // delete
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        Toast.makeText(getApplicationContext(), image + " " + " is deleted.", Toast.LENGTH_LONG).show();
                                        Log.d("Delete Image: ", "Deleting.....");
                                        adapter.remove(adapter.getItem((int) position));
                                        swipelist.invalidateViews();
                                        File fdelete = new File(image.getTitle());
                                        if (fdelete.exists())

                                        {
                                            if (fdelete.delete()) {
                                                daOdb.deleteImage(image);
                                                daOdb.getImages();
                                                System.out.println("File Deleted :" + image.getPath());
                                            } else {
                                                daOdb.deleteImage(image);
                                                daOdb.getImages();
                                                System.out.println("File Not Deleted :" + image.getPath());
                                            }
                                        }
                                        swipelist.invalidateViews();
                                        dialog.cancel();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        dialog.cancel();
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Do You Wish To Delete?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();

                        break;
                    case 2:
                        if(image.getPriority()=="OFF"){
                            database = dbHelper.getWritableDatabase();
                            ContentValues cv = new ContentValues();
                            cv.put(DBhelper.COLUMN_PRIORITY, "ON");
                            Log.d("Updating Priority: ", ".....");
                            String whereClause =
                                       /* DBhelper.COLUMN_TITLE + "=? AND " +*/ DBhelper.COLUMN_DATETIME + "=?";
                            String[] whereArgs = new String[]{/*image.getTitle(),*/ String.valueOf(image.getDatetimeLong())};
                            database.update(DBhelper.TABLE_NAME, cv, whereClause, whereArgs);
                            Log.d("Updating Priority: ", ".....");
                        image.setPriority("ON");
                        Toast.makeText(getApplicationContext(), image + " " + " is marked Important.", Toast.LENGTH_SHORT).show();
                            swipelist.invalidateViews();
                        }
                        else{
                            database = dbHelper.getWritableDatabase();
                            ContentValues cv = new ContentValues();
                            cv.put(DBhelper.COLUMN_PRIORITY, "OFF");
                            Log.d("Updating Priority: ", ".....");
                            String whereClause =
                                       /* DBhelper.COLUMN_TITLE + "=? AND " +*/ DBhelper.COLUMN_DATETIME + "=?";
                            String[] whereArgs = new String[]{/*image.getTitle(),*/ String.valueOf(image.getDatetimeLong())};
                            database.update(DBhelper.TABLE_NAME, cv, whereClause, whereArgs);
                            Log.d("Updating Priority: ", ".....");
                            image.setPriority("OFF");
                            Toast.makeText(getApplicationContext(), image + " " + " is marked UnImportant.", Toast.LENGTH_SHORT).show();
                            swipelist.invalidateViews();
                        }
                        break;
                }

                return false;
            }
        });


        swipelist.invalidateViews();

        swipelist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int arg2, final long position) {
                final MyImage image = (MyImage) swipelist.getItemAtPosition((int) position);

                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                //alert.setTitle("Edit"); //Set Alert dialog title here
                alert.setMessage("Description"); //Message here

                // Set an EditText view to get user input
                final EditText input = new EditText(MainActivity.this);
                alert.setView(input);

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //You will get as string input data in this variable.
                        // here we convert the input to a string and show in a toast.
                        String srt = input.getEditableText().toString();
                        database = dbHelper.getWritableDatabase();
                        ContentValues cv = new ContentValues();
                        cv.put(DBhelper.COLUMN_NAME, srt);
                        Log.d("Updating Date: ", ".....");
                        String whereClause =  /*DBhelper.COLUMN_TITLE + "=? AND " +*/ DBhelper.COLUMN_DATETIME + "=?";
                        String[] whereArgs = new String[]{/*image.getTitle(),*/ String.valueOf(image.getDatetimeLong())};
                        database.update(DBhelper.TABLE_NAME, cv, whereClause, whereArgs);
                        Log.d("Updating Date: ", ".....");
                        image.setName(srt);
                        swipelist.invalidateViews();
                        Toast.makeText(MainActivity.this,srt,Toast.LENGTH_LONG).show();
                    } // End of onClick(DialogInterface dialog, int whichButton)
                }); //End of alert.setPositiveButton
                alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                        dialog.cancel();
                    }
                }); //End of alert.setNegativeButton
                AlertDialog alertDialog = alert.create();
                alertDialog.show();
               /* alert = build.create();
                alert.show();*/
                return true;
            }
        });
        Log.d("Intent: ", " " + getIntent());
        //Intent i;
        //i.putExtra("Swipe",String.valueOf(findViewById(R.id.main_list_view)));

        Intent in=getIntent();

        // delGoalId = getIntent().getIntExtra("ID", 0);

        if(in != null){
            delGoalId = in.getIntExtra("ID",0);
            //catNotify = String.valueOf(getIntent().getExtras().getInt("Category"));
            if(delGoalId >0) {

                onReceive(delGoalId-1);
            }
        }else {

            onReceive(delGoalId-1);
        }
        Intent in1=getIntent();
        // delGoalId = getIntent().getIntExtra("ID", 0);
        if(in1 != null){
            delGoalId1 = in1.getIntExtra("DEL",0);
            //catNotify = String.valueOf(getIntent().getExtras().getInt("Category"));
            if(delGoalId1 >0) {
                onRec(delGoalId1-1);
            }
        }else {
            onRec(delGoalId1-1);
        }

    }
    public void onRec(final int position) {
        final SwipeMenuListView swipelist = (SwipeMenuListView) findViewById(R.id.main_list_view);
        Toast.makeText(getApplicationContext(), "onReceive", Toast.LENGTH_LONG).show();
        final MyImage image = (MyImage) swipelist.getItemAtPosition((int) position);
        String action = getIntent().getAction();
        if ("Del".equals(action)) {// to execute delete option
            // delete
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            Toast.makeText(getApplicationContext(), image + " " + " is deleted.", Toast.LENGTH_LONG).show();
                            Log.d("Delete Image: ", "Deleting.....");
                            adapter.remove(adapter.getItem((int) position));
                            swipelist.invalidateViews();
                            File fdelete = new File(image.getTitle());
                            if (fdelete.exists())
                            {
                                if (fdelete.delete()) {
                                    daOdb.deleteImage(image);
                                    daOdb.getImages();
                                    System.out.println("File Deleted :" + image.getPath());
                                } else {
                                    daOdb.deleteImage(image);
                                    daOdb.getImages();
                                    System.out.println("File Not Deleted :" + image.getPath());
                                }
                            }
                            swipelist.invalidateViews();
                            dialog.cancel();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            dialog.cancel();
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Do You Wish To Delete?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }


    public void onReceive(final int position) {
        final SwipeMenuListView swipelist = (SwipeMenuListView) findViewById(R.id.main_list_view);
        Toast.makeText(getApplicationContext(), "onReceive", Toast.LENGTH_LONG).show();
        final MyImage image = (MyImage) swipelist.getItemAtPosition((int) position);
        String action = getIntent().getAction();

        if ("Delete".equals(action)) {// to execute delete option
            Toast.makeText(getApplicationContext(), "Delete", Toast.LENGTH_LONG).show();
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            Toast.makeText(getApplicationContext(), image + " " + " is deleted.", Toast.LENGTH_LONG).show();
                            Log.d("Delete Image: ", "Deleting.....");
                            adapter.remove(adapter.getItem((int) position));
                            swipelist.invalidateViews();
                            File fdelete = new File(image.getTitle());
                            if (fdelete.exists())

                            {
                                if (fdelete.delete()) {
                                    daOdb.deleteImage(image);
                                    daOdb.getImages();
                                    myGoalNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    // Builds the notification and issues it.
                                    myGoalNotifyMgr.cancel(position);
                                    // Gets an instance of the NotificationManager service

                                    System.out.println("File Deleted :" + image.getPath());
                                } else {
                                    daOdb.deleteImage(image);
                                    daOdb.getImages();
                                    System.out.println("File Not Deleted :" + image.getPath());
                                }
                            }

                            swipelist.invalidateViews();
                            dialog.cancel();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            dialog.cancel();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Do You Wish To Delete?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
            /*alert = build.create();
            alert.show();*/
        }
    }



    // calculates days from months
    int calMonthDay(int m,int y){//calMonthDay(month,year)
        int x=0,c;
        for(c = 1; c < m; c++) {// Jan to less than the month 'm' as 'm' we are not taking the the whole days of that month
            if(c == 2) {//if Feb
                if(y%4 == 0)//checks if year is leap or not
                    x += 29;
                else
                    x += 28;
            }
            else
                x += mon.get(c-1);
        }
        return(x);
    }

    //calculates no. of months from current month & year to goal month & year
    int calDateMonth(int mC,int yC,int mG,int yG){//(current-month, current-year, goal-month, goal-year)
        int x = 0,i,countM=0;
        if(yC<=yG){
            for(i = yC; i < yG; i++)
                countM += 12;
        }

        countM -= mC;
        countM += mG;
        return (countM);
    }

    //calculates no. of weeks from current month & year to goal month & year
    int calDateWeek(int mC,int yC,int mG,int yG){
        int x = 0,i,countW=0;
        if(yC<=yG){
            for(i = yC; i < yG; i++)
                countW+=52;
        }

        countW -= mC;
        countW += mG;
        countW *= 4;
        return (countW);
    }


    /**
     * initialize database
     */
    public void initDB() {
        daOdb = new DAOdb(this);
        //        add images from database to images ArrayList
        for (MyImage mi : daOdb.getImages()) {
            images.add(mi);

        }
    }

    @Override
    protected void onResume() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        AlarmManager am = (AlarmManager) MainActivity.this.getSystemService(ALARM_SERVICE);
        Intent intent1 = new Intent(MainActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60*1000, pendingIntent);

        super.onResume();
    }

    protected Dialog onCreateDialog(int id) {
        switch(id){
            case DATE_DIALOG_ID:
                //return new DatePickerDialog(this, reservationDate, currentYear, currentMonth, currentDay);
                DatePickerDialog da=new DatePickerDialog(this, reservationDate, currentYear, currentMonth, currentDay);
                da.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
                return da;

        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener reservationDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day){
            final Calendar c = Calendar.getInstance();
            Date convertedDate = new Date();
            int curYear = c.get(Calendar.YEAR), curMonth = c.get(Calendar.MONTH)+1, curDay = c.get(Calendar.DAY_OF_MONTH);
            //Picks the selected date, month & year & displays on button
            if((year>=curYear)||(year==curYear && month+1>=curMonth)||(year==curYear && month+1==curMonth && day>=curDay)) {
                dayG = Integer.toString(day);
                monthG = Integer.toString(month + 1);
                yearG = Integer.toString(year);
                Log.d("Setting Date: ", ".....");
                dateString=String.valueOf(dayG)+"-"+String.valueOf(monthG)+"-"+String.valueOf(yearG);
                // Toast.makeText(getBaseContext(), "Your reminder is set to "  + day + "-" + (month + 1) + "-" + year + ".", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getBaseContext(), "Please choose date after " + curDay + "-" + curMonth + "-" + curYear, Toast.LENGTH_SHORT).show();
            }
        }
    };


    public void btnAddOnClick(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_box);
        dialog.setTitle("Choose an option");
        dialog.findViewById(R.id.btnChoosePath)
                .setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        activeGallery();
                        dialog.dismiss();
                    }
                });
        dialog.findViewById(R.id.btnTakePhoto)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activeTakePhoto();
                        dialog.dismiss();
                    }
                });
        // show dialog on screen
        dialog.show();
    }
    /**
     * take a photo
     */
    private void activeTakePhoto() {
        final Dialog dialog = new Dialog(MainActivity.this);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            int MEDIA_TYPE_IMAGE = 1;
            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

            // start the image capture Intent
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            try {
                FileOutputStream outputStream_image = openFileOutput(file_image, MODE_WORLD_READABLE);
                outputStream_image.write(string.getBytes());
                outputStream_image.close();
                Toast.makeText(getBaseContext(), "location of image saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private Uri getOutputMediaFileUri(int MEDIA_TYPE_IMAGE) {
        // TODO Auto-generated method stub

        if(isExternalStorageWritable()) {
            //Toast.makeText(getBaseContext(), "value: "+ Uri.fromFile(getOutputMediaFile(MEDIA_TYPE_IMAGE)), Toast.LENGTH_LONG).show();
            return Uri.fromFile(getOutputMediaFile(MEDIA_TYPE_IMAGE));
        }
        else

            return null;
    }
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "FiZZ");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("FiZZ", "failed to create directory");
                Toast.makeText(getBaseContext(),"File directory creation failed",Toast.LENGTH_LONG).show();
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        int MEDIA_TYPE_IMAGE = 1;
        if (type == MEDIA_TYPE_IMAGE){
            //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fname= "IMG_"+ timeStamp + ".jpg";
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }


    /**
     * to gallery
     */
    private void activeGallery() {
        final Dialog dialog = new Dialog(this);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
        dialog.dismiss();

    }
    /**
     * helper to retrieve the path of an image URI
     */
    //for gallery -> retrieving gallery image path
    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    public void copyFile(File src, File dst) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "IMG_"+ timeStamp + ".jpg";
        File file = new File (dst, fname);
        if (file.exists ())
            file.delete ();
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(file);
            IOUtils.copy(in, out);
            if (src.exists()) {
                FileChannel srce = new FileInputStream(src).getChannel();
                FileChannel dste = new FileOutputStream(dst).getChannel();
                dste.transferFrom(srce, 0, srce.size());}
        } catch (IOException ioe) {
            String LOGTAG = "Error";
            Log.e(LOGTAG, "IOException occurred.", ioe);
        } finally {
            out.flush();
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }
    private void moveFile(File file, File dir) throws IOException {
        File newFile = new File(dir, file.getName());
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            file.delete();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

    }

    @Override protected void onActivityResult(int requestCode, int resultCode,
                                              Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if (requestCode == SELECT_PICTURE &&
                        resultCode == RESULT_OK && null != data) {
                    final Dialog dialog = new Dialog(this);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String fname = "IMG_"+ timeStamp + ".jpg";
                    File mediaFile1;


                    //Selected Image Uri
                    Uri selectedImageUri = data.getData();
                    selectedImagePath = getPath(selectedImageUri);
                    Toast.makeText(getApplication(),selectedImagePath,Toast.LENGTH_SHORT).show();
                    File mediaStored = new File(getPath(selectedImageUri));//Source file
                    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "FiZZ");
                    mediaFile1 = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
                    String imageName=String.valueOf(mediaFile1);
                    // This location works best if you want the created images to be shared
                    // between applications and persist after your app has been uninstalled.
                    // Create the storage directory if it does not exist
                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                            Log.d("FiZZ", "failed to create directory");
                            Toast.makeText(getBaseContext(),"File directory creation failed",Toast.LENGTH_SHORT).show();
                            //return null;
                        }else{
                            try {
                                copyFile(mediaStored, mediaStorageDir);
                                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                                Cursor cursor = getContentResolver()
                                        .query(selectedImageUri, filePathColumn, null, null,
                                                null);
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                cursor.close();
                                MyImage image = new MyImage();
                                image.setTitle(imageName);
                                image.setDescription(picturePath);
                                image.setDatetime(System.currentTimeMillis());
                                image.setPath(picturePath);
                                image.setName(null);
                                image.setPriority("OFF");
                                images.add(image);
                                daOdb.addImage(image);
                                //swipelist.invalidateViews();
                                adapter.notifyDataSetChanged();
                            }catch (IOException e){
                                e.printStackTrace();
                            }finally {
                            }
                        }
                    }else{
                        try {
                            //moveFile(mediaStored,mediaStorageDir);
                            Toast.makeText(getApplication(),"Copying",Toast.LENGTH_SHORT).show();
                            copyFile(mediaStored, mediaStorageDir);
                            String[] filePathColumn = {MediaStore.Images.Media.DATA};
                            String picturePath = null;
                            Cursor cursor = getContentResolver()
                                    .query(selectedImageUri, filePathColumn, null, null,
                                            null);
                            if(cursor.moveToFirst()) {
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                picturePath = cursor.getString(columnIndex);
                            }
                            cursor.close();
                            MyImage image = new MyImage();
                            image.setTitle(imageName);
                            image.setDescription(" ");
                            image.setDatetime(System.currentTimeMillis());
                            image.setPath(picturePath);
                            image.setName(null);
                            image.setPriority("OFF");
                            images.add(image);
                            daOdb.addImage(image);
                            adapter.notifyDataSetChanged();
                            //swipelist.invalidateViews();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            case REQUEST_IMAGE_CAPTURE:
                if (requestCode == REQUEST_IMAGE_CAPTURE &&
                        resultCode == RESULT_OK) {
                    String filePath=imageFile.getAbsolutePath();
                    String imageName=String.valueOf(mediaFile);
                    Cursor cursor =
                            getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    new String[]{MediaStore.Images.Media._ID},
                                    MediaStore.Images.Media.DATA + "=? ",
                                    new String[]{filePath}, null);


                    if( cursor != null && cursor.moveToFirst() ){
                        int column_index_data = cursor.getColumnIndexOrThrow(
                                MediaStore.MediaColumns._ID);
                        String picturePath = cursor.getString(column_index_data);
                        MyImage image = new MyImage();
                        image.setTitle(imageName);
                        image.setDescription(" ");
                        image.setDatetime(System.currentTimeMillis());
                        image.setPath(picturePath);
                        image.setName(null);
                        image.setPriority("OFF");
                        images.add(image);
                        daOdb.addImage(image);
                        adapter.notifyDataSetChanged();
                        cursor.close();
                    }
                    else{
                        MyImage image = new MyImage();
                        image.setTitle(imageName);
                        image.setDescription(" ");
                        image.setDatetime(System.currentTimeMillis());
                        image.setPath(fileUri.getPath());
                        image.setName(null);
                        image.setPriority("OFF");
                        images.add(image);
                        daOdb.addImage(image);
                        adapter.notifyDataSetChanged();
                        //swipelist.invalidateViews();
                    }
                }
        }
    }

    /**
     * item clicked listener used to implement the react action when an item is
     * clicked.
     *
     * @param list
     */
    private void addItemClickListener(final ListView list) {
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                MyImage image = (MyImage) list.getItemAtPosition(position);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra("IMAGE", (new Gson()).toJson(image));
                intent.setDataAndType(Uri.parse("file://"  + image.getTitle()), "image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        // Save the user's current game state
        if (mCapturedImageURI != null) {
            outState.putString("mCapturedImageURI",
                    mCapturedImageURI.toString());
        }
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }
    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        // Restore state members from saved instance
        if (savedInstanceState.containsKey("mCapturedImageURI")) {
            mCapturedImageURI = Uri.parse(
                    savedInstanceState.getString("mCapturedImageURI"));
        }
    }
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
