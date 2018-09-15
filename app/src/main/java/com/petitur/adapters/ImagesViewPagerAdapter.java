package com.petitur.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;

import com.makeramen.roundedimageview.RoundedImageView;
import com.petitur.R;
import com.petitur.resources.Utilities;

import java.util.List;

//inspired by: https://stackoverflow.com/questions/2758224/what-does-the-java-assert-keyword-do-and-when-should-it-be-used
public class ImagesViewPagerAdapter extends PagerAdapter {

    private List<Uri> mUris;
    private final Context mContext;
    private final ImageClickHandler mOnClickHandler;
    private LayoutInflater mInflater;

    public ImagesViewPagerAdapter(Context context, ImageClickHandler listener, List<Uri> uris) {
        this.mContext = context;
        this.mUris = uris;
        this.mOnClickHandler = listener;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override public int getCount() {
        return (mUris == null) ? 0 : mUris.size();
    }

    public void setContents(List<Uri> uris) {
        mUris = uris;
        if (uris != null) {
            this.notifyDataSetChanged();
        }
    }

    @NonNull @Override public Object instantiateItem(@NonNull ViewGroup view, final int position) {
        View imageLayout = mInflater.inflate(R.layout.list_item_viewpager_image, view, false);

        assert imageLayout != null;
        final RoundedImageView imageView = imageLayout.findViewById(R.id.list_item_viewpager_image_element);

        if (URLUtil.isNetworkUrl(mUris.get(position).toString())) {
            imageView.setImageResource(R.mipmap.ic_play_image);
        }
        else {
            Uri uri = mUris.get(position);
            Utilities.displayUriInImageView(mContext, uri, imageView);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnClickHandler.onImageClick(position);
            }
        });

        view.addView(imageLayout, 0);

        return imageLayout;
    }

//    @Override public int getItemPosition(@NonNull Object object) {
//        //see link for more efficient way: https://stackoverflow.com/questions/7263291/viewpager-pageradapter-not-updating-the-view
//        return POSITION_NONE;
//    }

    @Override public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public interface ImageClickHandler {
        void onImageClick(int clickedItemIndex);
    }
}
