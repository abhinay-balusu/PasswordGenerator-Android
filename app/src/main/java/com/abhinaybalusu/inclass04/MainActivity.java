package com.abhinaybalusu.inclass04;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Message;
import android.os.Handler;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView passwordsCountTextView;
    private TextView passwordsLengthTextView;
    private SeekBar passwordsCountSeekBar;
    private SeekBar passwordsLengthSeekBar;
    private TextView passwordValueTextView;
    private Handler handler;
    private ArrayList passwordsList;
    private int passwordsCount;
    private int passwordsLength;
    private ProgressDialog progressDialog;
    ExecutorService threadPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        passwordsList = new ArrayList<String>();

        threadPool = Executors.newFixedThreadPool(2);

        passwordsCountTextView = (TextView)findViewById(R.id.passwordCountValueTextView);
        passwordsLengthTextView = (TextView)findViewById(R.id.passwordsLengthValueTextView);

        passwordsCountSeekBar = (SeekBar)findViewById(R.id.passwordsCountSeekBar);
        passwordsCountSeekBar.setProgress(1);
        passwordsLengthSeekBar = (SeekBar)findViewById(R.id.passwordsLengthSeekBar);
        passwordsLengthSeekBar.setProgress(8);

        passwordsCount = Integer.parseInt(passwordsCountTextView.getText().toString());
        passwordsLength = Integer.parseInt(passwordsLengthTextView.getText().toString());

        passwordValueTextView = (TextView)findViewById(R.id.passwordValueTextView);

        passwordsCountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


                if(progress<1)
                {
                    progress = 1;
                }
                passwordsCountSeekBar.setProgress(progress);
                passwordsCount = progress;
                passwordsCountTextView.setText(String.valueOf(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        passwordsLengthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(progress<8)
                {
                    progress = 8;
                }
                passwordsLengthSeekBar.setProgress(progress);
                passwordsLength = progress;
                passwordsLengthTextView.setText(String.valueOf(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Generating Passwords ...");
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(progressDialog.STYLE_HORIZONTAL);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                if(msg.what == executeThread.STATUS_START)
                {
                    progressDialog.show();
                }
                else if(msg.what == executeThread.STATUS_STEP)
                {
                    progressDialog.setProgress((int)msg.obj);
                }
                else if(msg.what == executeThread.STATUS_DONE)
                {
                    progressDialog.setProgress(0);
                    progressDialog.dismiss();
                    showAlertDialogue();

                }
                return false;
            }
        });

        findViewById(R.id.threadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                passwordsList.clear();
                threadPool.execute(new executeThread());
                //Thread thread = new Thread(new executeThread());
                //thread.start();

            }
        });

        findViewById(R.id.asyncButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                passwordsList.clear();
                new executeAsync().execute();

            }
        });

    }
    void showAlertDialogue()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Passwords");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.select_dialog_item);

        for(int i=0;i<passwordsList.size();i++)
        {
            arrayAdapter.add(passwordsList.get(i).toString());
        }

        builder.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        passwordValueTextView.setText(passwordsList.get(which).toString());

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public class executeThread extends Util implements Runnable{
        static final int STATUS_START = 100;
        static final int STATUS_STEP = 101;
        static final int STATUS_DONE = 102;

        public void sendMessage(int msg){

            Message message = new Message();
            message.what = msg;
            handler.sendMessage(message);
        }
        public void sendMessage(int msg,int percentageValue){

            Message message = new Message();
            message.what = msg;
            message.obj = percentageValue;
            handler.sendMessage(message);
        }

        @Override
        public void run() {

            sendMessage(STATUS_START);
            for(int i=1;i<=passwordsCount;i++)
            {
                passwordsList.add(Util.getPassword(passwordsLength));
                sendMessage(STATUS_STEP,(i*100)/passwordsCount);
            }
            sendMessage(STATUS_DONE);
        }
    }

    public class executeAsync extends AsyncTask<Integer, Integer, Void> {

        ProgressDialog progressDialog_Async;
        @Override
        protected Void doInBackground(Integer... params) {

            for(int i=1;i<=passwordsCount;i++)
            {
                passwordsList.add(Util.getPassword(passwordsLength));
                publishProgress((i*100)/passwordsCount);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {

            progressDialog_Async = new ProgressDialog(MainActivity.this);
            progressDialog_Async.setMessage("Generating Passwords ...");
            progressDialog_Async.setMax(100);
            progressDialog_Async.setCancelable(false);
            progressDialog_Async.setProgressStyle(progressDialog.STYLE_HORIZONTAL);
            progressDialog_Async.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            progressDialog_Async.setProgress(0);
            progressDialog_Async.dismiss();
            showAlertDialogue();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog_Async.setProgress(values[0]);
        }
    }
}
