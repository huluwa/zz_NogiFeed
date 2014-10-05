package android.shts.jp.nogifeed.views;

import android.content.Context;
import android.shts.jp.nogifeed.R;
import android.shts.jp.nogifeed.utils.PicassoHelper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

public class Showcase extends FrameLayout {

    private ViewPager mViewPager;
    private ViewPageIndicator mViewPageIndicator;
    private final List<String> mImageUrls;

    public Showcase(Context context, List<String> imageUrls) {
        this(context, null, imageUrls);
    }

    public Showcase(Context context, AttributeSet attrs, List<String> imageUrls) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.showcase, this);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPageIndicator = (ViewPageIndicator) findViewById(R.id.indicator);
        mImageUrls = imageUrls;

        setupAdapter();
    }

    private void setupAdapter() {
        CustomPageAdapter adapter = new CustomPageAdapter(getContext(), mImageUrls);
        mViewPager.setAdapter(adapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mViewPageIndicator.setCurrentPosition(position);
            }
        });
        mViewPageIndicator.setCount(adapter.getCount());
    }

    public class CustomPageAdapter extends PagerAdapter {

        Context mContext;
        private final List<String> mImageUrls;

        public CustomPageAdapter(Context context, List<String> imageUrls) {
            mContext = context;
            mImageUrls = imageUrls;
        }

        @Override
        public int getCount() {
            return mImageUrls.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return ((View)o).equals(view);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView iv = new ImageView(mContext);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            PicassoHelper.load(mContext, iv, mImageUrls.get(position));
            container.addView(iv);
            return iv;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View v = (View) object;
            container.removeView(v);
        }
    }

}