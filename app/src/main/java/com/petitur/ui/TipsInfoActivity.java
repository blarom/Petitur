package com.petitur.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.petitur.R;
import com.petitur.resources.Utilities;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class TipsInfoActivity extends AppCompatActivity {

    private Unbinder mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips_info);

        initializeParameters();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        mBinding.unbind();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tips_info_menu, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        switch (itemThatWasClickedId) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //Functionality methods
    private void initializeParameters() {

        mBinding =  ButterKnife.bind(this);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.find_a_pet);
        }

    }


    //View click listeners
    @OnClick(R.id.tips_info_container1) public void onContainer1Click() {
        Utilities.goToWebLink(this, "");
    }
    @OnClick(R.id.tips_info_container2) public void onContainer2Click() {
        Utilities.goToWebLink(this, "");
    }
}
