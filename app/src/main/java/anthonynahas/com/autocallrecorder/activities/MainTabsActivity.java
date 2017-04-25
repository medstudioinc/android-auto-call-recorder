package anthonynahas.com.autocallrecorder.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;

import anthonynahas.com.autocallrecorder.R;
import anthonynahas.com.autocallrecorder.classes.Resources;
import anthonynahas.com.autocallrecorder.fragments.RecordsRecyclerListFragment;
import anthonynahas.com.autocallrecorder.utilities.decoraters.DemoRecordSupport;
import anthonynahas.com.autocallrecorder.utilities.helpers.DialogHelper;
import anthonynahas.com.autocallrecorder.utilities.helpers.MemoryCacheHelper;
import anthonynahas.com.autocallrecorder.utilities.helpers.PermissionsHelper;

public class MainTabsActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainTabsActivity.class.getSimpleName();

    private DrawerLayout mDrawer;
    private FloatingSearchView mSearchView;
    private SwitchCompat mSwitch_auto_rec;
    private SharedPreferences mSharedPreferences;
    private int mCurrentFragmentPosition;
    private AppCompatActivity mActivity;
    private FloatingSearchView.OnQueryChangeListener mOnQueryChangeListener;

    private BroadcastReceiver mActionModeBroadcastReceiver;


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer_tabs);
        mActivity = this;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        MemoryCacheHelper.init();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        mSearchView.attachNavigationDrawerToMenuButton(mDrawer);
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            /**
             * Perform an action when a menu item is selected
             *
             * @param item the selected item
             */
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                int id = item.getItemId();

                //noinspection SimplifiableIfStatement
                switch (id) {
                    case R.id.action_add_demo_record:
                        DemoRecordSupport.getInstance().createDemoRecords(getApplicationContext());
                        break;
                    case R.id.action_sort:
                        Log.d(TAG, "MenuItem = sort");
                        DialogHelper.openSortDialog(mActivity, mSectionsPagerAdapter.getItem(0));
                        break;
                    case R.id.action_settings:
                        startActivity(new Intent(mActivity, SettingsActivity.class));
                        break;
                    default:
                        break;
                }
            }
        });


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        MenuItem menuItem_switch_auto_rec = menu.findItem(R.id.nav_switch_auto_rec);
        View actionView = MenuItemCompat.getActionView(menuItem_switch_auto_rec);
        mSwitch_auto_rec = (SwitchCompat) actionView;

        if (mSwitch_auto_rec != null) {
            mSwitch_auto_rec.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mSharedPreferences.edit().putBoolean(SettingsActivityOld.KEY_PREF_AUTO_RECORD, isChecked).apply();
                    Log.d(TAG, "autorecord = "
                            + mSwitch_auto_rec.isChecked());
                }
            });
        }


        mActionModeBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isInActionMode = intent.getBooleanExtra(Resources.ACTION_MODE_SATE, false);
                if (isInActionMode) {
                    mSearchView.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                } else {
                    mSearchView.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.GONE);
                }
            }
        };

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mActionModeBroadcastReceiver,
                        new IntentFilter(Resources.BROADCAST_ACTION_ON_ACTION_MODE));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // requesting required permission on run time
        PermissionsHelper permissionsHelper = new PermissionsHelper(this);
        permissionsHelper.requestAllPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSwitch_auto_rec != null) {
            mSwitch_auto_rec.setChecked(mSharedPreferences.getBoolean(SettingsActivityOld.KEY_PREF_AUTO_RECORD, true));
            Log.d(TAG, "switch state | auto record = " + mSwitch_auto_rec.isChecked());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mActionModeBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSectionsPagerAdapter.getItem(mCurrentFragmentPosition)
                .onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_tabs, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private RecordsRecyclerListFragment mRecordsRecyclerListFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mRecordsRecyclerListFragment = RecordsRecyclerListFragment.getInstance();
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            mCurrentFragmentPosition = position;
            switch (position) {
                case 0:
                    mSearchView.setOnQueryChangeListener(mRecordsRecyclerListFragment.getOnQueryChangeListener());
                    return mRecordsRecyclerListFragment;
                case 1:
                    return PlaceholderFragment.newInstance(position);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages (TABS).
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Aufzeichnungen";
                case 1:
                    return "Favoriten";
            }
            return null;
        }
    }
}
