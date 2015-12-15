package com.jiangzuomeng.travelmap;import android.content.Intent;import android.graphics.Color;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.support.v4.view.PagerAdapter;import android.support.v4.view.ViewPager;import android.support.v7.app.AppCompatActivity;import android.view.MenuItem;import android.view.View;import android.view.ViewGroup;import android.widget.Button;import android.widget.ImageView;import android.widget.LinearLayout;import android.widget.TextView;import android.util.Log;import com.jiangzuomeng.dataManager.DataManager;import com.jiangzuomeng.modals.TravelItem;import com.jiangzuomeng.networkManager.NetworkJsonKeyDefine;import org.json.JSONException;import org.json.JSONObject;import org.json.JSONTokener;import java.net.URI;import java.net.URISyntaxException;import uk.co.senab.photoview.PhotoView;public class AlbumViewerActivity extends AppCompatActivity {    public static final String INTERT_TRAVEL_ITEM_OBJECT = "INTERT_TRAVEL_ITEM_OBJECT";    private ViewPager viewPager;    private PhotoView[] imageViews;    private ImageView[] indicatorViews;    private int travelItemID;//    private final int MSG_TRAVELITEM_UPDATE = 0x1;//    private final int MSG_TRAVEL_IMAGE_UPDATE = 0x2;    private Handler getTravelHandler;    private Handler getImageHandler;    private TravelItem travelItem;    DataManager dataManager;    URI[] imgSrcs;    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_albumviewer);        getSupportActionBar().setDisplayHomeAsUpEnabled(true);        viewPager = (ViewPager) findViewById(R.id.album_pager);        dataManager = DataManager.getInstance(this);        Intent intent = getIntent();        final Bundle bun = intent.getExtras();        if (bun != null) {            travelItemID = bun.getInt(SingleTravelActivity.INTENT_TRAVEL_ITEM_KEY);//        travelItemID = 13;            getTravelHandler = new AlbumHandler();            getImageHandler = new ImageHandler();            dataManager.queryTravelItemByTravelItemId(travelItemID, getTravelHandler);        }        final Button likeBtn = (Button) findViewById(R.id.album_like_btn);        likeBtn.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View v) {                if (travelItem != null) {                    travelItem.like = Integer.valueOf(likeBtn.getText().toString()) + 1;                    likeBtn.setText(String.valueOf(travelItem.like));                    dataManager.updateTravelItem(travelItem, new Handler());                }            }        });        Button commentBtn = (Button) findViewById(R.id.album_comment_btn);        commentBtn.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View v) {                Intent it = new Intent(AlbumViewerActivity.this, AlbumDetailsActivity.class);                Bundle bundle = new Bundle();                bundle.putSerializable(INTERT_TRAVEL_ITEM_OBJECT, travelItem);                it.putExtras(bundle);                startActivity(it);            }        });    }    @Override    public boolean onOptionsItemSelected(MenuItem item) {        switch (item.getItemId()) {            case android.R.id.home:                finish();                return true;        }        return true;    }    public class AlbumViewerAdapter extends PagerAdapter {        @Override        public int getCount() {            return imageViews.length;        }        @Override        public boolean isViewFromObject(View arg0, Object arg1) {            return arg0 == arg1;        }        @Override        public void destroyItem(ViewGroup container, int position, Object object) {            container.removeView(imageViews[position]);        }        @Override        public Object instantiateItem(ViewGroup container, int position) {            container.addView(imageViews[position], 0);            return imageViews[position];        }    }    private class AlbumHandler extends Handler {        private void onGetTravelItem(TravelItem item) {            String[] imgUrlStrs = item.media.split(";");            int image_count = imgUrlStrs.length;            imgSrcs = new URI[image_count];            try {                for (int i = 0; i < imgUrlStrs.length; ++i) {                    imgSrcs[i] = new URI(imgUrlStrs[i]);                }            } catch (URISyntaxException e) {                e.printStackTrace();            }            ViewGroup indicators = (ViewGroup) findViewById(R.id.album_indicators);            // 添加图片的指示器            indicatorViews = new ImageView[image_count];            for (int i = 0; i < image_count; ++i) {                ImageView imageView = new ImageView(AlbumViewerActivity.this);                imageView.setLayoutParams(new ViewGroup.LayoutParams(10, 10));                imageView.setImageResource(R.drawable.page_indicator_unfocused);                indicatorViews[i] = imageView;                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,                                ViewGroup.LayoutParams.WRAP_CONTENT));                layoutParams.leftMargin = 5;                layoutParams.rightMargin = 5;                indicators.addView(imageView, layoutParams);            }            indicatorViews[0].setImageResource(R.drawable.page_indicator_focused);            // 设置ViewPager            imageViews = new PhotoView[image_count];            for (int i = 0; i < image_count; ++i) {                PhotoView imageView = new PhotoView(AlbumViewerActivity.this);                imageView.setBackgroundColor(Color.BLACK);                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);                imageView.setAdjustViewBounds(true);                imageViews[i] = imageView;            }            viewPager.setAdapter(new AlbumViewerAdapter());            // Change the indicator            viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {                @Override                public void onPageSelected(int selected) {                    for (int i = 0; i < indicatorViews.length; ++i) {                        if (i == selected) {                            indicatorViews[i].setImageResource(R.drawable.page_indicator_focused);                        } else {                            indicatorViews[i].setImageResource(R.drawable.page_indicator_unfocused);                        }                    }                }            });            TextView textView = (TextView) findViewById(R.id.album_state);            textView.setText(item.text);            Button likeBtn = ((Button) findViewById(R.id.album_like_btn));            likeBtn.setText(String.valueOf(item.like));        }        @Override        public void handleMessage(Message msg) {            switch (msg.what) {                case NetworkJsonKeyDefine.NETWORK_OPERATION:                    try {                        Bundle bundle = msg.getData();                        JSONTokener jsonTokener = new JSONTokener(bundle.getString((NetworkJsonKeyDefine.NETWORK_RESULT_KEY)));                        JSONObject responeObj = (JSONObject) jsonTokener.nextValue();                        String travelItemJson = responeObj.getString("data");                        travelItem = TravelItem.fromJson(travelItemJson, true);                        onGetTravelItem(travelItem);                        // TODO dataManager 获取图片                    } catch (JSONException e) {                        e.printStackTrace();                    }                    break;            }        }    }    private class ImageHandler extends Handler {        @Override        public void handleMessage(Message msg) {            switch (msg.what) {                case NetworkJsonKeyDefine.NETWORK_OPERATION:                    // TODO 获取图片                    break;            }        }    }}