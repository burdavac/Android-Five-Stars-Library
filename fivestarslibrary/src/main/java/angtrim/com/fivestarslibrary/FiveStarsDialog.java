package angtrim.com.fivestarslibrary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

//import androidx.appcompat.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.iarcuschin.simpleratingbar.SimpleRatingBar;


/**
 * Created by angtrim on 12/09/15.
 */
public class FiveStarsDialog implements DialogInterface.OnClickListener {

    private static final String SP_NUM_OF_ACCESS = "numOfAccess";
    private static final String SP_DISABLED = "disabled";
    private static final String TAG = FiveStarsDialog.class.getSimpleName();
    private final Context context;
    private boolean isForceMode = false;
    private final SharedPreferences sharedPrefs;
    private String supportEmail;
    private String supportEmailTitle;
    private String supportEmailText;
    private TextView contentTextView;
    private TextView aboveContentTextView;
    private TextView belowContentTextView;
    private SimpleRatingBar ratingBar;
    private ImageView icon;
    private boolean isIconVisible = false;
    private Drawable iconDrawable = null;
    private String title = null;
    private boolean hideMainTitle = false;
    private boolean hideNegativeButton = false;
    private boolean hideNeutralButton = false;
    private boolean hidepositiveButton = false;
    private String aboveRateText = null;
    private String rateText = null;
    private String belowRateText = null;
    private AlertDialog alertDialog;
    private boolean showAskEmailDialog = true;
    private boolean showAskMarketDialog = true;
    private View dialogView;
    private int upperBound = 4;
    private NegativeReviewListener negativeReviewListener;
    private ReviewListener reviewListener;
    private StarListener starListener;
    private int starColor;
    private String positiveButtonText;
    private String negativeButtonText;
    private String neverButtonText;
    private String appName;

    public FiveStarsDialog(Context context, String supportEmail) {
        this.context = context;
        sharedPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        this.supportEmail = supportEmail;
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    private void build() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogView = inflater.inflate(R.layout.stars, null);
        String titleToAdd = (title == null) ? context.getString(R.string.default_title) : title;
        String textToAdd = (rateText == null) ? context.getString(R.string.default_text) : rateText;

        contentTextView = dialogView.findViewById(R.id.text_content);
        contentTextView.setText(textToAdd);

        aboveContentTextView = dialogView.findViewById(R.id.text_abovecontent);
        if (aboveRateText != null) {
            aboveContentTextView.setText(aboveRateText);
        } else {
            aboveContentTextView.setVisibility(View.GONE);
        }
        belowContentTextView = dialogView.findViewById(R.id.text_belowcontent);
        if (belowRateText != null) {
            belowContentTextView.setText(belowRateText);
        } else {
            belowContentTextView.setVisibility(View.GONE);
        }

        ratingBar = dialogView.findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new SimpleRatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(SimpleRatingBar ratingBar, float v, boolean b) {
                Log.d(TAG, "Rating changed : " + v);
                if (starListener != null)
                    starListener.onRatingChanged((int) ratingBar.getRating());

                if (isForceMode && v >= upperBound) {
                    disable();
                    openMarket();
                    if (reviewListener != null)
                        reviewListener.onReview((int) ratingBar.getRating());
                }
            }
        });
        icon = dialogView.findViewById(R.id.icon);
        if (isIconVisible) {
            icon.setVisibility(View.VISIBLE);
        }
        if (iconDrawable != null) {
            icon.setImageDrawable(iconDrawable);
        }


        if (starColor != 0) {
            ratingBar.setFillColor(starColor);
            ratingBar.setPressedFillColor(starColor);
            ratingBar.setBorderColor(starColor);
            ratingBar.setPressedBorderColor(starColor);
        }

        String mainTitle = titleToAdd;
        if (hideMainTitle == true) {
            mainTitle = "";
        }

        builder.setTitle(mainTitle);
        builder.setView(dialogView);
        if (!hideNegativeButton) {
            builder.setNegativeButton((negativeButtonText == null) ? context.getString(R.string.default_negative) : negativeButtonText, this);
        }
        if (!hidepositiveButton) {
            builder.setPositiveButton((positiveButtonText == null) ? context.getString(R.string.default_positive) : positiveButtonText, this);
        }
        if (!hideNeutralButton) {
            builder.setNeutralButton((neverButtonText == null) ? context.getString(R.string.default_never) : neverButtonText, this);
        }

        alertDialog = builder.create();
    }

    private void disable() {
        SharedPreferences shared = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(SP_DISABLED, true);
        editor.apply();
    }

    public void enable() {
        SharedPreferences shared = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(SP_DISABLED, false);
        editor.apply();
    }

    public void openMarket() {
        final String appPackageName = context.getPackageName();
        String titleToAdd = (title == null) ? context.getString(R.string.default_title) : title;

        if (showAskMarketDialog) {
            // Ask the user if they would leave a rating on the app store
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(titleToAdd)
                    .setMessage(context.getString(R.string.store_text))
                    .setPositiveButton(context.getString(R.string.store_positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openMarketFunction(appPackageName);
                        }
                    })
                    .setNegativeButton(context.getString(R.string.store_negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    }).create().show();
        } else {
            openMarketFunction(appPackageName);
        }
    }

    private void openMarketFunction(String appPackageName) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

//    private void sendEmail_old() {
//        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
//        emailIntent.setType("text/email");
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{supportEmail});
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, (supportEmailTitle == null) ? context.getString(R.string.default_mail_title, getApplicationName(context)) : supportEmailTitle);
//        emailIntent.putExtra(Intent.EXTRA_TEXT, (supportEmailText == null) ? "" : supportEmailText);
//        context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.send_mail)));
//    }
//
//    private void sendEmail_intent() {
//        String nameToUse = appName == null ? getApplicationName(context) : appName;
//        String titleToUse = (supportEmailTitle == null) ? context.getString(R.string.default_mail_title, getApplicationName(context)) : supportEmailTitle;
//        String bodyToUse = (supportEmailText == null) ? "" : supportEmailText;
//
//        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
//        emailIntent.setType("text/email");
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { supportEmail });
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, titleToUse);
//        emailIntent.putExtra(Intent.EXTRA_TEXT, bodyToUse);
//
//        String titleToAdd = (title == null) ? context.getString(R.string.default_title) : title;
//
//        // Ask the user if they would leave a rating on the app store
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle(titleToAdd)
//                .setMessage(context.getString(R.string.mail_text))
//                .setPositiveButton(context.getString(R.string.mail_positive), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.mail_info)));
//                    }
//                })
//                .setNegativeButton(context.getString(R.string.mail_negative), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // do nothing
//                    }
//                }).create().show();
//    }

    public void sendEmail() {
        String titleToAdd = (title == null) ? context.getString(R.string.default_title) : title;

        if (showAskEmailDialog) {
            // Ask the user if they would leave a rating on the app store
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(titleToAdd)
                    .setMessage(context.getString(R.string.mail_text))
                    .setPositiveButton(context.getString(R.string.mail_positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendEmailFunction();
                        }
                    })
                    .setNegativeButton(context.getString(R.string.mail_negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    }).create().show();
        } else {
            sendEmailFunction();
        }
    }

    private void sendEmailFunction() {
        String nameToUse = appName == null ? getApplicationName(context) : appName;
        String titleToUse = (supportEmailTitle == null) ? context.getString(R.string.default_mail_title, getApplicationName(context)) : supportEmailTitle;
        String bodyToUse = (supportEmailText == null) ? "" : supportEmailText;

        try {
            String mailto = "mailto:" + supportEmail + "?subject=" + titleToUse + "&body=" + bodyToUse.replace("\n","%0D%0A");

            Log.d(TAG, mailto);

            final Intent emailIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mailto));

            context.startActivity(emailIntent);
        } catch (Exception e) {
            final Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{supportEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, titleToUse);
            emailIntent.putExtra(Intent.EXTRA_TEXT, bodyToUse);

            context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.mail_info)));
        }
    }

    private void show() {
        boolean disabled = sharedPrefs.getBoolean(SP_DISABLED, false);
        if (!disabled && alertDialog == null) {
            build();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    public void showAfter(int numberOfAccess) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        int numOfAccess = sharedPrefs.getInt(SP_NUM_OF_ACCESS, 0);
        editor.putInt(SP_NUM_OF_ACCESS, numOfAccess + 1);
        editor.apply();
        if (numOfAccess + 1 >= numberOfAccess) {
            show();
        }
    }

    public void showImmediatelly() {
        show();
    }

    public void showImmediatellyAnyway() {
        if (alertDialog == null) {
            build();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }


    public void hide() {
        if (alertDialog == null) {
            alertDialog.hide();
        }
    }

    public void dismiss() {
        if (alertDialog == null) {
            alertDialog.dismiss();
        }
    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == DialogInterface.BUTTON_POSITIVE) {
            if (ratingBar.getRating() < upperBound) {
                if (negativeReviewListener == null) {
                    sendEmail();
                } else {
                    negativeReviewListener.onNegativeReview((int) ratingBar.getRating());
                }

            } else if (!isForceMode) {
                openMarket();
            }
            disable();
            if (reviewListener != null)
                reviewListener.onReview((int) ratingBar.getRating());
        }
        if (i == DialogInterface.BUTTON_NEUTRAL) {
            disable();
        }
        if (i == DialogInterface.BUTTON_NEGATIVE) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt(SP_NUM_OF_ACCESS, 0);
            editor.apply();
        }
        alertDialog.hide();
    }

    public FiveStarsDialog setTitle(String title) {
        this.title = title;
        return this;

    }

    public FiveStarsDialog setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
        return this;
    }

    public FiveStarsDialog setSupportEmailTitle(String title) {
        this.supportEmailTitle = title;
        return this;
    }

    public FiveStarsDialog setSupportEmailText(String text) {
        this.supportEmailText = text;
        return this;
    }

    public FiveStarsDialog setRateText(String rateText) {
        this.rateText = rateText;
        return this;
    }

    public FiveStarsDialog setAboveRateText(String aboveRateText) {
        this.aboveRateText = aboveRateText;
        return this;
    }

    public FiveStarsDialog setBelowRateText(String belowRateText) {
        this.belowRateText = belowRateText;
        return this;
    }

    public FiveStarsDialog setStarColor(int color) {
        starColor = color;
        return this;
    }

    public FiveStarsDialog setPositiveButtonText(String positiveButtonText) {
        this.positiveButtonText = positiveButtonText;
        return this;
    }

    public FiveStarsDialog setNegativeButtonText(String negativeButtonText) {
        this.negativeButtonText = negativeButtonText;
        return this;
    }

    public FiveStarsDialog setNeverButtonText(String neverButtonText) {
        this.neverButtonText = neverButtonText;
        return this;
    }

    public FiveStarsDialog setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public FiveStarsDialog setIconVisible(boolean iconVisible) {
        isIconVisible = iconVisible;
        return this;
    }

    public FiveStarsDialog setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
        return this;
    }

    public FiveStarsDialog setHideMainTitle(boolean hideMainTitle) {
        this.hideMainTitle = hideMainTitle;
        return this;
    }

    public FiveStarsDialog setHideNegativeButton(boolean hideNegativeButton) {
        this.hideNegativeButton = hideNegativeButton;
        return this;
    }

    public FiveStarsDialog setHideNeutralButton(boolean hideNeutralButton) {
        this.hideNeutralButton = hideNeutralButton;
        return this;
    }

    public FiveStarsDialog setHidePositiveButton(boolean hidepositiveButton) {
        this.hidepositiveButton = hidepositiveButton;
        return this;
    }

    public FiveStarsDialog setShowAskEmailDialog(boolean showAskEmailDialog) {
        this.showAskEmailDialog = showAskEmailDialog;
        return this;
    }

    public FiveStarsDialog setShowAskMarketDialog(boolean showAskMarketDialog) {
        this.showAskMarketDialog = showAskMarketDialog;
        return this;
    }

    /**
     * Set to true if you want to send the user directly to the market
     *
     * @param isForceMode
     * @return
     */
    public FiveStarsDialog setForceMode(boolean isForceMode) {
        this.isForceMode = isForceMode;
        return this;
    }

    /**
     * Set the upper bound for the rating.
     * If the rating is >= of the bound, the market is opened.
     *
     * @param bound the upper bound
     * @return the dialog
     */
    public FiveStarsDialog setUpperBound(int bound) {
        this.upperBound = bound;
        return this;
    }

    /**
     * Set a custom listener if you want to OVERRIDE the default "send email" action when the user gives a negative review
     *
     * @param listener
     * @return
     */
    public FiveStarsDialog setNegativeReviewListener(NegativeReviewListener listener) {
        this.negativeReviewListener = listener;
        return this;
    }

    /**
     * Set a listener to get notified when a review (positive or negative) is issued, for example for tracking purposes
     *
     * @param listener
     * @return
     */
    public FiveStarsDialog setReviewListener(ReviewListener listener) {
        this.reviewListener = listener;
        return this;
    }

}
