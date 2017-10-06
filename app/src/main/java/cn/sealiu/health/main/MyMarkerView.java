package cn.sealiu.health.main;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import cn.sealiu.health.R;
import cn.sealiu.health.statistic.StatisticFragment;


/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
public class MyMarkerView extends MarkerView {

    private TextView tvContent;
    private int type;

    public MyMarkerView(Context context, int layoutResource, int type) {
        super(context, layoutResource);
        this.type = type;
        tvContent = findViewById(R.id.tvContent);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        float y = 0f;

        if (e instanceof CandleEntry) {
            CandleEntry ce = (CandleEntry) e;
            y = ce.getHigh();
        } else {
            y = e.getY();
            if (type == StatisticFragment.TYPE_DAY)
                tvContent.setText(String.format("%s分钟", Utils.formatNumber(e.getY(), 0, true)));
        }

        switch (type) {
            case StatisticFragment.TYPE_DAY:
                tvContent.setText(String.format("%s分钟",
                        Utils.formatNumber(y, 0, true)));
                break;
            case StatisticFragment.TYPE_WEEK:
            case StatisticFragment.TYPE_MONTH:
                tvContent.setText(String.format("%s小时",
                        Utils.formatNumber(y, 2, true)));
                break;
            case StatisticFragment.TYPE_YEAR:
                tvContent.setText(String.format("%s天",
                        Utils.formatNumber(y, 2, true)));
                break;
        }


        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
