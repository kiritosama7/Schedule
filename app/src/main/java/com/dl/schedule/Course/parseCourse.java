package com.dl.schedule.Course;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.dl.schedule.DB.MySubject;
import com.dl.schedule.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class parseCourse extends AppCompatActivity {

    private WebView webView;

    private Pattern nodePattern = Pattern.compile("\\(\\d{1,2}[-]*\\d*节");

    String js = "javascript:var ifrs=document.getElementsByTagName(\"iframe\");" +
            "var iframeContent=\"\";" +
            "for(var i=0;i<ifrs.length;i++){" +
            "iframeContent=iframeContent+ifrs[i].contentDocument.body.parentElement.outerHTML;" +
            "}\n" +
            "var frs=document.getElementsByTagName(\"frame\");" +
            "var frameContent=\"\";" +
            "for(var i=0;i<frs.length;i++){" +
            "frameContent=frameContent+frs[i].contentDocument.body.parentElement.outerHTML;" +
            "}\n" +
            "window.local_obj.showSource(document.getElementsByTagName('html')[0].innerHTML + iframeContent + frameContent);";


    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parse_course);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        webView = (WebView) findViewById(R.id.webview);
        initData();



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.importCourse);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                webView.loadUrl(js);

            }
        });


    }

    private void initData() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new InJavaScriptLocalObj(), "local_obj");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.loadUrl("javascript:window.local_obj.showSource('<head>'+"
                        + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

        });
        webView.loadUrl("http://www.gdjw.zjut.edu.cn/");
    }

    final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void showSource(String html) {
            try {
                parseHtml(html);
            }catch (Exception e){
                Log.d("parseCourse", "showSource: "+e);
            }

        }
    }

    private void parseHtml(String html) {
        Document document = Jsoup.parse(html);

        Element table1 = document.getElementById("table1");
        Elements trs = table1.getElementsByTag("tr");

        int node = 0;
        int day = 0;
        String teacher = "";
        String room = "";
        int step = 0;
        int startWeek = 0;
        int endWeek = 0;
        String timeStr = "";
        List<Integer> WeekintegerList=new ArrayList<>();
        LitePal.deleteAll(MySubject.class);

        for (Element tr : trs) {
            String nodeStr = tr.getElementsByClass("festival").text();
            if (nodeStr.isEmpty()) {
                continue;
            }
            node = Integer.parseInt(nodeStr);
            Elements tds = tr.getElementsByTag("td");
            for (Element td : tds) {
                Elements divs = td.getElementsByTag("div");
                for (Element div : divs) {
                    String courseValue = div.text().trim();
                    if (courseValue.length() <= 1) {
                        continue;
                    }

                    String courseName = div.getElementsByClass("title").text();

                    if (courseName.isEmpty()) {
                        continue;
                    }

                    day = Integer.parseInt(td.attr("id").substring(0,1));

                    Elements pList = div.getElementsByTag("p");
                    List <String>weekList = new ArrayList<>();
                    for (Element it : pList) {
                        switch (it.getElementsByAttribute("title").attr("title")) {
                            case "教师":
                                teacher = it.getElementsByTag("font").text().trim();
                                break;
                            case "上课地点":
                                room = it.getElementsByTag("font").text().trim();
                                room=room.substring(room.indexOf("校区")+3,room.length());
                                break;
                            case "节/周": {
                                timeStr = it.getElementsByTag("font").last().text().trim();
                                Matcher matcher = nodePattern.matcher(timeStr);
                                if (matcher.find()) {
                                    String nodeInfo = matcher.group(0);
                                    String nodes[] = nodeInfo.substring(1, nodeInfo.length() - 1).split("-");

                                    if (nodes.length == 0) {
                                        node = Integer.parseInt(nodes[0]);
                                    }
                                    if (nodes.length > 1) {
                                        int endNode = Integer.parseInt(nodes[1]);
                                        step = endNode - node + 1;
                                    }
                                }
                                weekList.clear();
                                weekList.addAll(Arrays.asList(timeStr.substring(timeStr.indexOf(')') + 1).split(",")));
                                break;
                            }
                        }
                    }


                    for( String it : weekList) {
                        if(it.length()<3){
                            startWeek=endWeek=Integer.parseInt(it.substring(0, it.indexOf('周')));
                        }else{
                            startWeek = Integer.parseInt(it.substring(0, it.indexOf('-')));
                            endWeek = Integer.parseInt(it.substring(it.indexOf('-')+1, it.indexOf('周')));
                        }

                    }

                    WeekintegerList.clear();
                    for(int i=startWeek;i<=endWeek;i++){
                        WeekintegerList.add(i);
                    }

                    MySubject mySubject=new MySubject();
                    mySubject.setName(courseName);
                    mySubject.setRoom(room);
                    mySubject.setTeacher(teacher);
                    mySubject.setWeekList(WeekintegerList);
                    mySubject.setStart(node);
                    mySubject.setStep(step);
                    mySubject.setDay(day);
                    mySubject.save();
                }
            }

        }
        if(LitePal.findAll(MySubject.class).size()!=0){
            Toast.makeText(parseCourse.this, "导入成功", Toast.LENGTH_SHORT).show();
            finish();
        }else {
            Toast.makeText(parseCourse.this, "查询出要导入的课程后点击确认导入!!!", Toast.LENGTH_SHORT).show();
        }

    }

}
