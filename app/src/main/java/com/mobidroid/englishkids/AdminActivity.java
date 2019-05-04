package com.mobidroid.englishkids;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.mobidroid.englishkids.fragment.CourseFragment;
import com.mobidroid.englishkids.fragment.ImageFragment;
import com.mobidroid.englishkids.fragment.VideoFragment;
import com.mobidroid.englishkids.item.FragmentTag;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView navigationViewEx;
    //private FragmentTransaction transaction;// = getSupportFragmentManager().beginTransaction();
    private ArrayList<String> mFragmentTAG = new ArrayList<>();
    private ArrayList<FragmentTag> mFragment = new ArrayList<>();
    private int mExitCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        //transaction = getSupportFragmentManager().beginTransaction();
        setUpFragment(new CourseFragment(), CourseFragment.class.getSimpleName());
        setUpNavigationView();
    }

    private void setUpFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (fragment != null) {
            if (tag.contentEquals(fragment.getClass().getSimpleName())) {
                transaction.replace(R.id.frame_container_admin, fragment, tag);
                transaction.commit();
            } else {
                transaction.add(R.id.frame_container_admin, fragment, tag);
                transaction.addToBackStack(tag);
                transaction.commit();
            }

            mFragment.add(new FragmentTag(fragment, tag));
        } else {

        }

//        if (tag.contains(fragment.getClass().getSimpleName())) {
//            transaction.add(R.id.frame_container_admin, fragment, tag);
//            transaction.commit();
//        } else {
//          transaction.remove(fragment);
//          transaction.commit();
//        }


//        if (!tag.contains(fragment.getTag())) {
//            transaction.add(R.id.frame_container_admin, fragment, tag);
//            //transaction.addToBackStack(CourseFragment.class.getSimpleName());
//            mFragmentTAG.add(tag);
//            //mFragment.add(new FragmentTag(fragment, tag));
//            transaction.commit();
//        } else {
//            mFragmentTAG.remove(tag);
//            //mFragmentTAG.add(tag);
//            transaction.remove(fragment);
//            transaction.commit();
//        }

    }

    private void setUpNavigationView() {
        navigationViewEx = findViewById(R.id.navigation_view_admin);
        //navigationViewEx.enableAnimation(false);
        navigationViewEx.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.action_image: {
                setUpFragment(new ImageFragment(), ImageFragment.class.getSimpleName());
            }
            break;

            case R.id.action_course: {
                setUpFragment(new CourseFragment(), CourseFragment.class.getSimpleName());
            }
            break;

            case R.id.action_video: {
                setUpFragment(new VideoFragment(), VideoFragment.class.getSimpleName());
            }
            break;
        }
        return true;
    }

//    @Override
//    public void onBackPressed() {
//        int backStack = mFragmentTAG.size();
//        if (backStack > 1) {
//            transaction.remove(mFragment.get(backStack).getFragment());
//        }
//        super.onBackPressed();
//    }

}
