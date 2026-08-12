package com.remoteagent.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import org.json.*;

public class MainActivity extends Activity {
  LinearLayout log; EditText input; TextView state; SharedPreferences p;
  public void onCreate(Bundle b){super.onCreate(b);p=getSharedPreferences("agent",0);home();}
  TextView text(String s){TextView v=new TextView(this);v.setText(s);v.setTextSize(16);v.setPadding(24,18,24,18);return v;}
  public void home(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);
    LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);h.setBackgroundColor(Color.rgb(20,108,148));
    TextView title=text("Remote Agent");title.setTextColor(Color.WHITE);h.addView(title,new LinearLayout.LayoutParams(0,-2,1));
    Button set=new Button(this);set.setText("\u8bbe\u7f6e");set.setOnClickListener(v->settings());h.addView(set);r.addView(h);
    state=text(p.getString("key","").isEmpty()?"\u8bf7\u5148\u914d\u7f6e\u8fde\u63a5":"\u5df2\u5c31\u7eea");r.addView(state);
    ScrollView s=new ScrollView(this);log=new LinearLayout(this);log.setOrientation(LinearLayout.VERTICAL);s.addView(log);r.addView(s,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout bar=new LinearLayout(this);input=new EditText(this);input.setHint("\u7ed9 Agent \u4e0b\u8fbe\u4efb\u52a1\u6216\u53d1\u9001\u6d88\u606f");bar.addView(input,new LinearLayout.LayoutParams(0,-2,1));Button go=new Button(this);go.setText("\u53d1\u9001");go.setOnClickListener(v->send());bar.addView(go);r.addView(bar);setContentView(r);}
  void add(String who,String msg){TextView v=text(who+"\n"+msg);log.addView(v);}
  void send(){String q=input.getText().toString().trim();if(q.isEmpty())return;if(p.getString("key","").isEmpty()){settings();return;}add("\u4f60",q);input.setText("");state.setText("Agent \u6b63\u5728\u5904\u7406\u2026");new Thread(()->{try{String a=ask(q);new Handler(Looper.getMainLooper()).post(()->{add("Agent",a);state.setText("\u5df2\u5b8c\u6210");});}catch(Exception e){new Handler(Looper.getMainLooper()).post(()->{add("Agent","\u8bf7\u6c42\u5931\u8d25: "+e.getMessage());state.setText("\u5931\u8d25");});}}).start();}
  String ask(String q)throws Exception{URL u=new URL(p.getString("base","https://api.openai.com/v1").replaceAll("/+$","")+"/responses");HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Authorization","Bearer "+p.getString("key",""));c.setRequestProperty("Content-Type","application/json");JSONObject b=new JSONObject().put("model",p.getString("model","gpt-5-mini")).put("input",q).put("store",false);c.getOutputStream().write(b.toString().getBytes(StandardCharsets.UTF_8));InputStream in=c.getResponseCode()<300?c.getInputStream():c.getErrorStream();String raw=new BufferedReader(new InputStreamReader(in)).readLine();if(c.getResponseCode()>=300)throw new Exception(raw);return new JSONObject(raw).optString("output_text","\u5df2\u6536\u5230\u54cd\u5e94");}
  void settings(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);EditText base=new EditText(this);base.setHint("\u670d\u52a1\u5730\u5740");base.setText(p.getString("base","https://api.openai.com/v1"));EditText key=new EditText(this);key.setHint("API Key");key.setText(p.getString("key",""));EditText model=new EditText(this);model.setHint("\u6a21\u578b");model.setText(p.getString("model","gpt-5-mini"));r.addView(base);r.addView(key);r.addView(model);Button save=new Button(this);save.setText("\u4fdd\u5b58");save.setOnClickListener(v->{p.edit().putString("base",base.getText().toString()).putString("key",key.getText().toString()).putString("model",model.getText().toString()).apply();home();});r.addView(save);setContentView(r);}
}