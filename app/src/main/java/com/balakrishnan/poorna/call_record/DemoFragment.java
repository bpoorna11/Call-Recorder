package com.balakrishnan.poorna.call_record;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
//import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */

public class DemoFragment extends Fragment {
    Button btnOne;
    Button btnTwo;
    Button btnThree;
    Button btnFour;
    Button btnFive;
    Button btnSix;
    Button btnSeven;
    Button btnEight;
    Button btnNine;
    Button btnStar;
    Button btnZero;
    Button btnHash;
    Button btnDelete;
    Button btnDial;

    EditText input;
    TelephonyManager tm;
    public boolean buttonpress=false;
    PhoneStateListener ls;
    public DemoFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_demo, container,false);
        tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        btnOne = view.findViewById(R.id.buttonOne);
        btnTwo = view.findViewById(R.id.buttonTwo);
        btnThree = view.findViewById(R.id.buttonThree);
        btnFour = view.findViewById(R.id.buttonFour);
        btnFive = view.findViewById(R.id.buttonFive);
        btnSix = view.findViewById(R.id.buttonSix);
        btnSeven = view.findViewById(R.id.buttonSeven);
        btnEight = view.findViewById(R.id.buttonEight);
        btnNine = view.findViewById(R.id.buttonNine);
        btnStar = view.findViewById(R.id.buttonStar);
        btnZero = view.findViewById(R.id.buttonZero);
        btnHash = view.findViewById(R.id.buttonHash);
        btnDelete = view.findViewById(R.id.buttonDelete);
        btnDial = view.findViewById(R.id.buttonDial);
        btnOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(btnSeven, input, "1");
            }
        });
        btnTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(btnSeven, input, "2");
            }
        });
        btnThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(btnSeven, input, "3");
            }
        });
        btnFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(btnSeven, input, "4");
            }
        });
        btnFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(btnSeven, input, "5");
            }
        });
        btnSix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(btnSeven, input, "6");
            }
        });
        btnSeven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(btnSeven, input, "7");
            }
        });
        btnEight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(btnSeven, input, "8");
            }
        });
        btnNine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(btnSeven, input, "9");
            }
        });
        btnStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(btnSeven, input, "*");
            }
        });
        btnZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(btnSeven, input, "0");
            }
        });
        btnHash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(btnSeven, input, "#");
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //input.setText("");

                String word = input.getText().toString();
                int input1 = word.length();
                if (input1 > 0){
                    input.setText(word.substring(0, input1-1));
                }
                input.setSelection(input.getText().length());
            }
        });
        btnDial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(input.getText().toString().length()<10)
                    Toast.makeText(getContext(), "Enter a valid number", Toast.LENGTH_SHORT).show();
                else {
                    buttonpress=true;
                    makeCall(input.getText().toString(),getContext());
                    ls = new PhoneStateListener() {
                        @Override
                        public void onCallStateChanged(int state, String incomingNumber) {
                            if (TelephonyManager.CALL_STATE_OFFHOOK ==state && buttonpress==true ) {
                                Toast.makeText(getContext(), "Starting call recording service", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getContext(), RecordingService.class);
                                getContext().startService(intent);
                                Toast.makeText(getContext(), "Call Recording is set ON", Toast.LENGTH_SHORT).show();
                                System.out.println("Main Activity hook off running");
                                buttonpress=false;
                            }

                        }
                    };
                    tm.listen(ls, PhoneStateListener.LISTEN_CALL_STATE);
                    input.setText("");
                }
            }
        });
        input = view.findViewById(R.id.editText);

        input.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int inType = input.getInputType(); // backup the input type
                input.setInputType(InputType.TYPE_NULL); // disable soft input
                input.onTouchEvent(event); // call native handler
                input.setInputType(inType); // restore input type
                return true; // consume touch even
            }
        });

        return view;
    }

    public void onButtonClick(Button button, EditText inputNumber, String number) {
        String cache = input.getText().toString();
        inputNumber.setText(cache + number);
        input.setSelection(input.getText().length());
    }
    public void makeCall(String number,Context context)
    {
        Intent in = new Intent(Intent.ACTION_CALL);
        String phnno = String.format("tel: %s", number);
        in.setData(Uri.parse(phnno));
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);
        if (result == PackageManager.PERMISSION_GRANTED){

            startActivity(in);

        } else {

            ((SecondActivity1)getActivity()).requestForCallPermission();
        }

    }


}
