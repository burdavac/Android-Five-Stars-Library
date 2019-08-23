package angtrim.com.sampleapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import angtrim.com.fivestarslibrary.FiveStarsDialog;
import angtrim.com.fivestarslibrary.NegativeReviewListener;
import angtrim.com.fivestarslibrary.ReviewListener;


public class MainActivity extends AppCompatActivity implements NegativeReviewListener, ReviewListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences shared = getApplicationContext().getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean("disabled", false);
        editor.apply();

        FiveStarsDialog fiveStarsDialog = new FiveStarsDialog(this,"vasek.burda@gmail.com");
        fiveStarsDialog
                //.setRateText("Your custom text")
                .setTitle("Your custom title")
                .setHideMainTitle(true)
                .setForceMode(false)
                .setUpperBound(4)
                .setShowAskEmailDialog(false)
                .setShowAskMarketDialog(true)
                .setIconVisible(true)
                .setIconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_rate_review))
                //.setNegativeReviewListener(this)
                .setReviewListener(this)
                //.showAfter(500)
                .showImmediatelly();

        ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNegativeReview(int stars) {
        Log.d(TAG, "Negative review " + stars);
    }

    @Override
    public void onReview(int stars) {
        Log.d(TAG, "Review " + stars);
    }
}
