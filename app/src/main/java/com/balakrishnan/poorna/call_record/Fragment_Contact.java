package com.balakrishnan.poorna.call_record;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

//import android.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_Contact extends Fragment {
    ListView lv;
    TelephonyManager tm;
    PhoneStateListener ls;
    boolean buttonpress=false;
    ArrayList<Name> cname;
    MyAdapter adapter;
    Cursor phones;
    String no="";

    public Fragment_Contact() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_contact, container,false);
        tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        lv=view.findViewById(R.id.listview);
        createlist();
       /* ls = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (TelephonyManager.CALL_STATE_IDLE == state) {
                    //Toast.makeText(getContext(), "Idle state", Toast.LENGTH_SHORT).show();
                } else if (TelephonyManager.CALL_STATE_RINGING == state) {
                    Toast.makeText(getContext(), "Phone ringing", Toast.LENGTH_SHORT).show();
                    System.out.println("Phone ringing");
                    if(buttonpress==false) {
                        Toast.makeText(getContext(), "Starting call recording service", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), RecordingService.class);
                        getContext().startService(intent);
                        Toast.makeText(getContext(), "Call Recording is set ON", Toast.LENGTH_SHORT).show();
                        System.out.println("Incoming call ringing running from Contact side");
                        buttonpress=true;
                    }
                } else if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                    //Toast.makeText(getApplicationContext(), "Call received", Toast.LENGTH_LONG).show();
                }

            }
        };
        tm.listen(ls, PhoneStateListener.LISTEN_CALL_STATE);*/
        return view;
    }

    private void createlist() {
        Name nam;
        cname=new ArrayList<>();
        phones=getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,"upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");
        String lastnam="";
        while (phones.moveToNext()){
            String disname=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phnno=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            nam=new Name(disname,phnno);
            cname.add(nam);
            if(disname.equals(lastnam)){
                cname.remove(nam);
            }
            else{
                lastnam=disname;
            }
        }

        adapter=new MyAdapter(getContext(),cname);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Name n=   cname.get(position);
                String nm=n.name;
                no=n.num;
                makeCall(no);
                ls = new PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(int state, String incomingNumber) {
                        if (TelephonyManager.CALL_STATE_OFFHOOK ==state ) {
                            Toast.makeText(getContext(), "Starting call recording service", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getContext(), RecordingService.class);
                            getContext().startService(intent);
                            Toast.makeText(getContext(), "Call Recording is set ON", Toast.LENGTH_SHORT).show();
                            System.out.println("Main Activity hook off running");
                        }

                    }
                };
                tm.listen(ls, PhoneStateListener.LISTEN_CALL_STATE);
                   /* if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                        Toast.makeText(getApplicationContext(), "Error in permission", Toast.LENGTH_LONG).show();
                        // ActivityCompat.requestPermissions((Activity) getApplicationContext(),new String[]{Manifest.permission.CALL_PHONE},MY_PERMISSION_REQUEST_CALL_PHONE);
                        return;
                    }

                    startActivity(in);*/

            }
        });
    }
    public void makeCall(String no)
    {
        Intent in = new Intent(Intent.ACTION_CALL);
        String phnno = String.format("tel: %s", no);
        in.setData(Uri.parse(phnno));
        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE);
        if (result == PackageManager.PERMISSION_GRANTED){

            startActivity(in);

        } else {
            ((SecondActivity1)getActivity()).requestForCallPermission();

        }
    }


}
