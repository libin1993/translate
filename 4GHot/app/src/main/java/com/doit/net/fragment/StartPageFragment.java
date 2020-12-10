package com.doit.net.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.doit.net.base.BaseFragment;
import com.doit.net.event.EventAdapter;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.model.CacheManager;
import com.doit.net.protocol.Send2GManager;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.ucsi.R;
import com.doit.net.utils.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Zxc on 2019/12/17.
 */

public class StartPageFragment extends BaseFragment {


    private ImageButton ivPowerStart;
    public StartPageFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.start_page_fragment, null);
        ivPowerStart = rootView.findViewById(R.id.ivPowerStart);
        initWidget();
//        turnToDetectPage();
        return rootView;
    }

    private void initWidget() {

        ivPowerStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    ivPowerStart.setImageResource(R.drawable.start_button_press);
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    //改为抬起时的图片
                    ivPowerStart.setImageResource(R.drawable.start_button);

                    if (CacheManager.checkDevice(getActivity())){
                        CacheManager.hasPressStartButton = true;
                        turnToDetectPage();
                        LTESendManager.openAllRf();
                        Send2GManager.setRFState("1");

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                CacheManager.redirect2G("",null,"redirect");
                            }
                        }, 1000);
                    }
                }


                return false;
            }
        });
    }

    private void turnToDetectPage() {
        EventAdapter.call(EventAdapter.POWER_START);
    }
}
