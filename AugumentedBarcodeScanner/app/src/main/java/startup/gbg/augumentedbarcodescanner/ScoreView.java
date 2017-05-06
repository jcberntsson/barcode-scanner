package startup.gbg.augumentedbarcodescanner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by simonarneson on 2017-05-06.
 */

public class ScoreView extends LinearLayout {
    private ImageView icon;
    private TextView scoreText;

    public ScoreView(Context context) {
        super(context);
        initViews(context);
    }

    public ScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public ScoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }


    public ScoreView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews(context);
    }


    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.score_view, this);
        icon = (ImageView) findViewById(R.id.icon);
        scoreText = (TextView) findViewById(R.id.scoreText);
    }

    public void setScore(String score) {
        scoreText.setText(score + " / 10");
    }

    public void setScoreType(ScoreType type) {
        switch (type) {
            case HEALTH:
                icon.setImageResource(R.drawable.carrot_icon);
                break;
            case ENVIRONMENT:
                icon.setImageResource(R.drawable.environment_icon);
                break;
            case SOCIAL:
                icon.setImageResource(R.drawable.social_icon);
                break;
            case ECONOMY:
                icon.setImageResource(R.drawable.economy_icon);
                break;
            default:
                icon.setImageResource(R.drawable.carrot_icon);
                break;
        }
    }
}
