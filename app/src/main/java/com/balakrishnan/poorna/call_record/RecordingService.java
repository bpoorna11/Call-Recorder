package com.balakrishnan.poorna.call_record;

import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.BassBoost;
import android.media.audiofx.NoiseSuppressor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Thread.sleep;


public class RecordingService extends Service {
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private AudioManager audioManager;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private Handler handler = null;
    private boolean isRecording = false;
    public boolean recoderstarted = false;
    public boolean secondif = false;
    private boolean firstif = false;
    public boolean compidle = false;
    File audiofile = null;
    Cursor cur;
    String phNumber="",callType="",callDate="",dir="";
    int callDuration=0;
    Date callDayTime;
    static final String TAG = "MediaRecording";
    public String FILE_NAME = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //  bufferSize = AudioRecord.getMinBufferSize(8000,
        //   AudioFormat.CHANNEL_CONFIGURATION_MONO,
        //   AudioFormat.ENCODING_PCM_16BIT);
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 3;
        TelephonyManager manager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        manager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);

                if (TelephonyManager.CALL_STATE_IDLE == state && secondif == true) {
                    try {
                        if (null != recorder) {
                            isRecording = false;
                            firstif = true;
                            int i = recorder.getState();
                            if (i == 1)
                                recorder.stop();
                            recorder.release();

                            recorder = null;
                            recordingThread = null;
                            audioManager.setSpeakerphoneOn(false);
                            Toast.makeText(getApplicationContext(), "Audio file saved in AudioRecorder Folder", Toast.LENGTH_LONG).show();
                            System.out.println("Copy wave file method called");
                            FILE_NAME = getFilename();
                            copyWaveFile(getTempFilename(), FILE_NAME);
                            System.out.println("File name sent to cw " + FILE_NAME);
                            SecondActivity1.fun(FILE_NAME);
                            deleteTempFile();
                            stopSelf();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handler=new Handler();
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // get start of cursor
                            Log.i("CallLogDetailsActivity", "Getting Log activity...");

                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                Log.i("Permission","Permission not granted");
                                return;
                            }
                            cur = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " desc");

                            int number = cur.getColumnIndex(CallLog.Calls.NUMBER);
                            int type = cur.getColumnIndex(CallLog.Calls.TYPE);
                            int date = cur.getColumnIndex(CallLog.Calls.DATE);
                            int duration = cur.getColumnIndex(CallLog.Calls.DURATION);
                            //Check if call was made from sim 1 or sim 2 , if it returns 0 its from sim 1 else if 1 its from sim 2.
                            int idSimId = getSimIdColumn(cur);
                            String callid = "0";

                            if (cur.moveToFirst() == true) {
                                phNumber = cur.getString(number);
                                callType = cur.getString(type);
                                callDate = cur.getString(date);
                                callDayTime = new Date(Long.valueOf(callDate));
                                callDuration = Integer.valueOf(cur.getString(duration));
                                dir = null;
                                int dircode = Integer.parseInt(callType);

                                switch (dircode) {
                                    case CallLog.Calls.OUTGOING_TYPE:
                                        dir = "OUTGOING";
                                        break;

                                    case CallLog.Calls.INCOMING_TYPE:
                                        dir = "INCOMING";
                                        break;

                                    case CallLog.Calls.MISSED_TYPE:
                                        dir = "MISSED";
                                        break;

                                }


                                if(idSimId >= 0){
                                    callid = cur.getString(idSimId);
                                }


                                cur.close();
                                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                StringBuffer sb = new StringBuffer();
                                sb.append("Outgoing Call Log"
                                        + "\nPhone Number:--- " + phNumber
                                        + " \nCall Type:--- " + dir
                                        + " \nCall Date:--- " + sdfDate.format(Long.valueOf(callDate))
                                        + " \nCalling Sim:--- " + callid
                                        + " \nCall duration in sec :--- " + callDuration);
                                sb.append("\n----------------------------------");
                                Log.i("sb", sb.toString());

                               // Toast.makeText(getApplicationContext(), sb.toString(),Toast.LENGTH_LONG).show();

                            }
                        }
                    }, 1500);
                    //System.out.println("File name "+getFilename());
                    compidle=true;
                    Intent intent = new Intent(RecordingService.this, UploadService.class);
                    startService(intent);

                }

                if((TelephonyManager.CALL_STATE_OFFHOOK==state && firstif==false && secondif==false)) {
                    secondif=true;
                    System.out.println(" CALL_STATE_OFFHOOK");
                    recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                            RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
                    System.out.println("2------ CALL_STATE_OFFHOOK");
                    int i = recorder.getState();
                    if(i==1)
                        recorder.startRecording();
                    isRecording = true;
                    System.out.println("3------ CALL_STATE_OFFHOOK");
                    recordingThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                           /* try {
                                while(true) {
                                    sleep(1000);
                                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                                    if (!audioManager.isSpeakerphoneOn())
                                        audioManager.setSpeakerphoneOn(true);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                           // audioManager.setMode(AudioManager.MODE_IN_CALL);
                            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
                            audioManager.setParameters("noise_suppression=on");
                            //audioManager.setSpeakerphoneOn(true);
                           // audioManager.setMode(AudioManager.MODE_NORMAL);
                            NoiseSuppressor noiseSuppressor = null;
                            AcousticEchoCanceler echoCanceler=null;
                            AutomaticGainControl gain=null;
                            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                            {
                                noiseSuppressor = NoiseSuppressor.create(recorder.getAudioSessionId());
                                noiseSuppressor.setEnabled(true);
                                Log.d(TAG, "NoiseSuppressor.isAvailable() " + NoiseSuppressor.isAvailable());
                            }
                            if (AcousticEchoCanceler.isAvailable()) {
                                echoCanceler = AcousticEchoCanceler.create(recorder.getAudioSessionId());
                                echoCanceler.setEnabled(true);
                            }
                            if(AutomaticGainControl.isAvailable()){
                                gain=AutomaticGainControl.create(recorder.getAudioSessionId());
                                gain.setEnabled(true);
                            }

                            try {
                                writeAudioDataToFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if(noiseSuppressor != null)
                            {
                                noiseSuppressor.release();
                            }
                            if (echoCanceler != null) {
                                echoCanceler.release();
                                //echoCanceler = null;
                            }
                            if(gain!=null){
                                gain.release();
                            }
                        }
                    },"AudioRecorder Thread");
                    recordingThread.start();


                    System.out.println("4------ CALL_STATE_OFFHOOK");

                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);

        return START_STICKY;
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());

        file.delete();
    }

    private void copyWaveFile(String inFilename,String outFilename){
        System.out.println(isRecording);
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 1;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            SrcApplog.logString("File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
               // System.out.println("Printing data: "+data.length);
              //  for(int i=0;i<data.length;i++){
               //     System.out.print(data[i]+" ");
              //  }
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("copywave file name "+outFilename);
    }
    public static int getSimIdColumn(final Cursor c) {

        for (String s : new String[] { "sim_id", "simid", "sub_id" }) {
            int id = c.getColumnIndex(s);
            if (id >= 0) {
                Log.d(TAG, "sim_id column found: " + s);
                return id;
            }
        }
        Log.d(TAG, "no sim_id column found");
        return -1;
    }
    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
    private String getFilename(){
        String mail=SecondActivity1.pEmail;
        System.out.println("Returned from an activity "+mail);
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }
        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(yourmilliseconds);
        System.out.println("getfilename method called "+sdf.format(resultdate));
        mail+="_"+sdf.format(resultdate);
        mail=mail.replaceAll("\\.", "_");
        mail=mail.replaceAll("\\s+","_");
        mail=mail.replaceAll(",","-");
        mail=mail.replaceAll(":", "-");
        mail=mail.replaceAll("@","_");
        return (file.getAbsolutePath() + "/" + mail + AUDIO_RECORDER_FILE_EXT_WAV);
    }

    private String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }
    private void writeAudioDataToFile() throws IOException {
        byte data[] = new byte[bufferSize];
        int recBufferByteSize = bufferSize;
        //  byte[] data = new byte[recBufferByteSize];
        int frameByteSize = bufferSize;
        int sampleBytes = frameByteSize;
        int recBufferBytePtr = 0;
        int copyread=0;
        String filename = getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);

        } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;

        if(null != os ){
            while(isRecording){
                read = recorder.read(data, recBufferBytePtr, sampleBytes);
                float gain = 2.0f; // taken from the UI control, perhaps in range from 0.0 to 2.0
                if (read > 0) {
                    for (int i = 0; i < read; ++i) {
                        data[i] = (byte) Math.min((int)(data[i] * gain), (int)Integer.MAX_VALUE);
                    }
                }
               /* int i = 0;
                while ( i < read ) {
                    float sample = (float)( data[recBufferBytePtr+i  ] & 0xFF
                            | data[recBufferBytePtr+i+1] << 8 );

                    // THIS is the point were the work is done:
                    // Increase level by about 6dB:
                    // sample *= 2;
                    // Or increase level by 20dB:
                    sample *= 2;
                    // Or if you prefer any dB value, then calculate the gain factor outside the loop
                    // float gainFactor = (float)Math.pow( 10., dB / 20. );    // dB to gain factor
                    // sample *= gainFactor;

                    // Avoid 16-bit-integer overflow when writing back the manipulated data:
                    if ( sample >= 32767f ) {
                        data[recBufferBytePtr+i  ] = (byte)0xFF;
                        data[recBufferBytePtr+i+1] =       0x7F;
                    } else if ( sample <= -32768f ) {
                        data[recBufferBytePtr+i  ] =       0x00;
                        data[recBufferBytePtr+i+1] = (byte)0x80;
                    } else {
                        int s = (int)( 0.5f + sample );  // Here, dithering would be more appropriate
                        data[recBufferBytePtr+i  ] = (byte)(s & 0xFF);
                        data[recBufferBytePtr+i+1] = (byte)(s >> 8 & 0xFF);
                    }
                    i += 2;
                }
                recBufferBytePtr += read;

                // Wrap around at the end of the recording buffer, e.g. like so:
                if ( recBufferBytePtr >= recBufferByteSize ) {
                    recBufferBytePtr = 0;
                    sampleBytes = frameByteSize;
                } else {
                    sampleBytes = recBufferByteSize - recBufferBytePtr;
                    if ( sampleBytes > frameByteSize )
                        sampleBytes = frameByteSize;
                }*/
                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            //save(left); save1(right);
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void save(byte[] left) {

        FileOutputStream fos = null;

        try {
            fos = openFileOutput("leftChannel.txt", MODE_PRIVATE);
            fos.write(left);
            //System.out.println( "Saved to " + getFilesDir() + "/" + "leftChannel.txt");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void save1(byte[] right) {

        FileOutputStream fos = null;

        try {
            fos = openFileOutput("rightChannel.txt", MODE_PRIVATE);
            fos.write(right);


           // System.out.println( "Saved to " + getFilesDir() + "/" + "rightChannel.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }




}