package router.tairan.com.androidcommonplatform;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import com.trc.android.common.widget.TrTextView;

import java.util.HashMap;

public class TrTextViewTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //配置Map的数据可以是来自服务器
        HashMap map = new HashMap();
        map.put("hello", "hello budy!");
        TrTextView.updateTextMap(map);

        setContentView(R.layout.activity_tr_text_view_test);
        TextView textView = findViewById(R.id.trTextView);
        textView.setText("hello");
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, TrTextViewTestActivity.class);
        context.startActivity(starter);
    }
}
