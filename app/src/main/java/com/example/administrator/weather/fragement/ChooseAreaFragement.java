package com.example.administrator.weather.fragement;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.weather.R;
import com.example.administrator.weather.db.City;
import com.example.administrator.weather.db.County;
import com.example.administrator.weather.db.Province;
import com.example.administrator.weather.util.HttpUtil;
import com.example.administrator.weather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/1/8.
 */

public class ChooseAreaFragement extends Fragment {


    public  static  final int LEVEL_PROVINCE=0;
    public  static  final int LEVEL_CITY=1;
    public  static  final int LEVEL_COUNTY=2;





    private ProgressDialog progressDialog;
    TextView titleText;
    Button backButton;
    ListView listView;
    ArrayAdapter<String>  adapter;
    private List<String> dataList=new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectProvince;
    private City selectCity;

    private  int currentLevel;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText= (TextView) view.findViewById(R.id.title_text);
        backButton= (Button) view.findViewById(R.id.back_button);
        listView=(ListView) view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (currentLevel){
                    case LEVEL_PROVINCE:
                        selectProvince=provinceList.get(position);
                        queryCities();//查城市
                    break;
                    case LEVEL_CITY:
                        selectCity=cityList.get(position);
                        queryCounties();//查县
                        break;
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (currentLevel==LEVEL_COUNTY){
                   queryCities();
               }else if (currentLevel==LEVEL_CITY){
                   queryProvince();//查省
               }
            }
        });

         queryProvince();


    }

    private void queryProvince() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province p:provinceList){
                dataList.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else{
                String address="http://guolin.tech/api/china";
                queryFromServer(address,"province");
        }

    }




    private void queryCities() {

        titleText.setText(selectProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList= DataSupport.where("provinceid=?",String.valueOf(selectProvince.getProvinceCode())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City c:cityList){
                dataList.add(c.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            String provinceCode=selectProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }

    }

    private void queryCounties() {

        titleText.setText(selectCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityid=?",String.valueOf(selectCity.getCityCode())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for (County c:countyList){
                dataList.add(c.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else{
            String provinceCode=selectProvince.getProvinceCode();
            String cityCode=selectCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }

    }


    /**
     * 根据类型不同查询数据
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {
        showProgessDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgessDialog();
                        Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                Log.i("lx",responseText+"-----");
                boolean result=false;
                if ("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result= Utility.handleCityResponse(responseText,Integer.valueOf(selectProvince.getProvinceCode()));
                }else if ("county".equals(type)){
                    result= Utility.handleCountyResponse(responseText,Integer.valueOf(selectCity.getCityCode()));
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgessDialog();
                            if ("province".equals(type)){
                                queryProvince();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });


                }
            }
        });
    }


    /**
     * 显示进度条
     */

        public void showProgessDialog(){
            if (progressDialog==null){
                progressDialog=new ProgressDialog(getActivity());
                progressDialog.setMessage("加载中");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(true);
            }
            progressDialog.show();
        }


    /**
     * 关闭进度条
     *
     */

    public void closeProgessDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }



}
