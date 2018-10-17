package com.example.cau_coin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends Activity {
    private Handler handler;
    private boolean hasID;
    private String idFromServer;
    private String pwdFromServer;
    private String nameFromServer;
    private String majorFromServer;
    private EditText input_id;
    private EditText input_pwd;
    private CheckBox checkBox;
    private Database_AutoLogin database;

    private String[] datas;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), "자동 로그인 되었습니다", Toast.LENGTH_SHORT).show();
            Intent a = new Intent(LoginActivity.this,MainActivity.class);
            a.putExtra("name",datas[2]);
            a.putExtra("major",datas[3]);
            a.putExtra("id",datas[0]);
            a.putExtra("from","login");
            startActivity(a);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        input_id = (EditText)findViewById(R.id.login_id);
        input_pwd = (EditText)findViewById(R.id.login_pwd);
        Button signin = (Button)findViewById(R.id.login_signin);
        Button signup = (Button)findViewById(R.id.login_signup);
        checkBox = (CheckBox)findViewById(R.id.login_checkbox);

        database = new Database_AutoLogin(getApplicationContext(),"mydb.db",null,1);

        if(database.check_AutoLogin()){
            init();
            String data = database.getData();
            datas = data.split("/");

            input_id.setText(datas[0]);
            input_pwd.setText(datas[1]);
            checkBox.setChecked(true);

            handler.postDelayed(runnable,700);
        }

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetAccount temp = new GetAccount();
                temp.execute(input_id.getText().toString());
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(a);
                finish();
            }
        });
    }

    public void init(){
        handler = new Handler();
    }

    public class GetAccount extends AsyncTask<String,Void,String> {

        public String doInBackground(String ...params)
        {
            try{
                String input_id = params[0];
                String url = "http://115.68.207.101/read_account.php?id="+ input_id;

                URL obj = new URL(url);

                HttpURLConnection conn = (HttpURLConnection) obj.openConnection(); // open connection

                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));

                String line;
                StringBuilder sb = new StringBuilder();

                while((line = reader.readLine())!=null)
                {
                    sb.append(line);
                }

                reader.close();
                return sb.toString();

            }catch(Exception e){

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(s!= null)
            {
                try {
                    JSONObject jsonObject = new JSONObject(s);

                    JSONArray jsonArray = jsonObject.getJSONArray("data");

                    if(jsonArray.length() ==0) {
                        hasID = false; // Login fail.
                        Toast.makeText(getApplicationContext(), "존재하지 않는 아이디입니다", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject item = jsonArray.getJSONObject(i);

                            idFromServer = item.getString("id");
                            pwdFromServer = item.getString("pwd");
                            nameFromServer = item.getString("name");
                            majorFromServer = item.getString("major");

                            hasID = true;

                            if(hasID == true)
                            {
                                String s_pwd = input_pwd.getText().toString();
                                if(pwdFromServer.equals(s_pwd))
                                {
                                    if(checkBox.isChecked()){
                                        database.insertData(idFromServer,pwdFromServer,nameFromServer,majorFromServer);
                                    }

                                    Toast.makeText(getApplicationContext(), "로그인에 성공하였습니다", Toast.LENGTH_SHORT).show();
                                    Intent a = new Intent(LoginActivity.this,MainActivity.class);
                                    a.putExtra("name",nameFromServer);
                                    a.putExtra("major",majorFromServer);
                                    a.putExtra("id",idFromServer);
                                    a.putExtra("from","login");
                                    startActivity(a);
                                    finish();
                                }
                                else{
                                    Toast.makeText(LoginActivity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }


                }catch(JSONException e){}
            }

        }
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("※ Cau Coin 종료");
        builder.setMessage("정말로 앱을 종료하시겠어요?");
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}