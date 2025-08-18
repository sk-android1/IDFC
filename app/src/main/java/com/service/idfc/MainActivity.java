package com.service.idfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.service.idfc.databinding.ActivityMainBinding;
import com.service.idfcmodule.IdfcMainActivity;
import com.service.idfcmodule.utils.MyConstantKey;
import com.service.idfcmodule.utils.UserListSingelton;

import org.json.JSONArray;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    ArrayList<UserModel> retailerList;

    Activity activity;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        activity = MainActivity.this;
        context = MainActivity.this;

        getRetailerList();

        clickEvents();

    }

    private void clickEvents() {

        binding.tvMain.setOnClickListener(v -> {

    //          JSONArray jsonArray =  getRetList();
    //          String s = jsonArray.toString();

                Gson gson = new GsonBuilder().create();
                JsonArray jsonArray = gson.toJsonTree(retailerList).getAsJsonArray();
                String s = jsonArray.toString();

                Intent in = new Intent(MainActivity.this, IdfcMainActivity.class);
                in.putExtra(MyConstantKey.ASM_ID, "A003028");
                in.putExtra(MyConstantKey.BANK_ID, "6");
                //   in.putExtra(MyConstantKey.USER_LIST, s);
                UserListSingelton.userListStr = s;         // using this approach for large data parsing
                in.putExtra(MyConstantKey.APP_TYPE, "Partner");        // Partner or Retailer
                in.putExtra(MyConstantKey.COM_TYPE, "Relipay");             // pass Vidcom or Relipay
                resultLauncher.launch(in);

        });

        binding.tvRetailer.setOnClickListener(v -> {

            Intent in = new Intent(MainActivity.this, IdfcMainActivity.class);

            in.putExtra(MyConstantKey.RETAILER_ID, "R001042891");
            in.putExtra(MyConstantKey.PAN_NO, "AVLPL3397H");
            in.putExtra(MyConstantKey.LATITUDE, "28.9876");
            in.putExtra(MyConstantKey.LONGITUDE, "26.3596");
            in.putExtra(MyConstantKey.APP_TYPE, "Retailer");        // Partner or Retailer
            in.putExtra(MyConstantKey.COM_TYPE, "Relipay");      // pass Vidcom or Relipay
            in.putExtra(MyConstantKey.LOGIN_TYPE, "APP");      // pass APP, RelipaySDK,RelipayPartnerSDK, VidcomSDK  for provider

            resultLauncher.launch(in);

        });

        binding.tvRetailer2.setOnClickListener(v -> {

            Intent in = new Intent(MainActivity.this, IdfcMainActivity.class);

            in.putExtra(MyConstantKey.RETAILER_ID, "R001046045");
            in.putExtra(MyConstantKey.PAN_NO, "DFHPS9533B");
            in.putExtra(MyConstantKey.LATITUDE, "28.6552555");
            in.putExtra(MyConstantKey.LONGITUDE, "77.1433433");
            in.putExtra(MyConstantKey.APP_TYPE, "Retailer");        // Partner or Retailer
            in.putExtra(MyConstantKey.COM_TYPE, "Relipay");             // pass Vidcom or Relipay
            in.putExtra(MyConstantKey.LOGIN_TYPE, "APP");      // pass APP, RelipaySDK,RelipayPartnerSDK, VidcomSDK  for provider

            resultLauncher.launch(in);

        });

        binding.tvRetailer3.setOnClickListener(v -> {

            Intent in = new Intent(MainActivity.this, IdfcMainActivity.class);

            in.putExtra(MyConstantKey.RETAILER_ID, "R0032459");
            in.putExtra(MyConstantKey.PAN_NO, "AAHCR4860R");
            in.putExtra(MyConstantKey.LATITUDE, "28.9876");
            in.putExtra(MyConstantKey.LONGITUDE, "26.3596");
            in.putExtra(MyConstantKey.APP_TYPE, "Retailer");        // Partner or Retailer
            in.putExtra(MyConstantKey.COM_TYPE, "Relipay");             // pass Vidcom or Relipay
            in.putExtra(MyConstantKey.LOGIN_TYPE, "APP");      // pass APP, RelipaySDK,RelipayPartnerSDK, VidcomSDK  for provider

            resultLauncher.launch(in);

        });

        binding.tvRetailer4.setOnClickListener(v -> {

            Intent in = new Intent(MainActivity.this, IdfcMainActivity.class);

            in.putExtra(MyConstantKey.RETAILER_ID, "R004002");
            in.putExtra(MyConstantKey.PAN_NO, "BBQPT3337K");
            in.putExtra(MyConstantKey.LATITUDE, "28.9876");
            in.putExtra(MyConstantKey.LONGITUDE, "26.3596");
            in.putExtra(MyConstantKey.APP_TYPE, "Retailer");        // Partner or Retailer
            in.putExtra(MyConstantKey.COM_TYPE, "Relipay");             // pass Vidcom or Relipay
            in.putExtra(MyConstantKey.LOGIN_TYPE, "APP");      // pass APP, RelipaySDK,RelipayPartnerSDK, VidcomSDK  for provider

            resultLauncher.launch(in);

        });

    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                String message = data.getStringExtra("message");
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

        }
    });

    private JSONArray getRetList() {
        JSONArray dataArray = null;

        try {
            dataArray = new JSONArray("[{\"name\":\"Sushma\",\"pannumber\":\"ABRPL9228M\",\"username\":\"R007120\",\"email\":\"test@test.com\",\"phone\":\"9582916949\",\"firmname\":\"DEMONETLINK\",\"pv_status\":\"0\",\"userdir\":\"retailer\",\"pincode\":null},{\"name\":\"GOVINDARASU RAJAN\",\"pannumber\":\"BXVPR8457F\",\"username\":\"R00219699\",\"email\":\"RAJANBALL19@GMAIL.COM\",\"phone\":\"8015194391\",\"firmname\":\"KASHIK BANKING SERVICES\",\"pv_status\":\"0\",\"userdir\":\"retailer\",\"pincode\":\"505652\"},{\"name\":\"LALITHA VEMULA\",\"pannumber\":\"AVPPV2928N\",\"username\":\"R00228848\",\"email\":\"LALITHA.VIJAYA8@GMAIL.COM\",\"phone\":\"9686514748\",\"firmname\":\"LALITHA BANKING SERVICES\",\"pv_status\":\"0\",\"userdir\":\"retailer\",\"pincode\":\"692022\"},{\"name\":\"BHUSHAN KUMAR LAMBA\",\"pannumber\":\"AVLPL3397H\",\"username\":\"R001042891\",\"email\":\"INFO@RNFISERVICES.COM\",\"phone\":\"8527365890\",\"firmname\":\"NIKHIL RNFI\",\"pv_status\":\"1\",\"userdir\":\"retailer\",\"pincode\":\"110015\"}]");
            String s = dataArray.toString();
            //   Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
            return dataArray;
        } catch (Exception ignored) {
        }
        return dataArray;
    }

    public void getRetailerList() {

        retailerList = new ArrayList<>();

        retailerList.add(new UserModel("Farhan", "R001335228", "8130099728", "MJFPK7005K", "farhankhandar05@gmail.com"));
        retailerList.add(new UserModel("Hafiz", "R001337790", "8130098945", "GBZPP5651N", "hafizqadrii2002@gmail.com"));
        retailerList.add(new UserModel("Chirag Valecha", "R001365154", "8766245689", "CCPPV8353L", "valechachirag08@gmail.com"));
        retailerList.add(new UserModel("Abhay Kumar", "R001343102", "8595391344", "FOZPK4689R", "ABHYKR9935@GMAIL.COM"));
        retailerList.add(new UserModel("Inderjeet", "R001213624", "9910445862", "AEBPI4078R", "inderjeet@rnfiservices.co.in"));
        retailerList.add(new UserModel("Rohit Pravin Gajjala", "R00601869", "9769812535", "AHPPR4444K", "rohitgajjala21@gmail.com"));
        retailerList.add(new UserModel("DHIRAJ SHARMA", "R001379131", "8448998617", "ICNPS0045G", "DHIRAJ2128@GMAIL.COM"));

        retailerList.add(new UserModel("Bhushan Lamba", "R001042891", "8527365890", "AVLPL3397H", "bhushanlamba@gmail.com"));
        retailerList.add(new UserModel("Nilesh More", "R0032459", "9967193547", "AAHCR4860R", "mailto:nilesh@gmail.com"));
        retailerList.add(new UserModel("Soumil Patel", "R004002", "7498471351", "BBQPT3337K", "soumil@gmail.com"));
        retailerList.add(new UserModel("Nikhil Nayyar", "R0032459", "9599902025", "AAHCR4860R", "vivekkush05@gmail.com"));
        retailerList.add(new UserModel("Rajdeep Sir", "R001046045", "9811947512", "DFHPS9533B", "attrsg@gmail.com"));
        retailerList.add(new UserModel("NITESH KUMAR SHARMA", "R003032", "9900705292", "EGJPS2520E", "ab@gmail.com"));
        retailerList.add(new UserModel("ABDULBARI A K SHAIKH", "P003068", "9999855359", "BGNPS5864C", "abcd@gmail.com"));
        retailerList.add(new UserModel("RAJESH YADAV", "D003114", "9871068029", "ABNPY4867B", "abef@gmail.com"));
        retailerList.add(new UserModel("RAHUL R RAJPUT", "P003265", "9601112271", "BSOPR0448A", "abf@gmail.com"));
        retailerList.add(new UserModel("SAJID SAFIQUE SAYED", "P003405", "7718805786", "AMJPS3002P", "abg@gmail.com"));
        retailerList.add(new UserModel("HARISH HARISH", "D003536", "8447428915", "AFCPH5873H", "abh@gmail.com"));
        retailerList.add(new UserModel("Tulsi", "R004002", "9811790920", "BBQPT3337K", "abi@gmail.com"));
        retailerList.add(new UserModel("MOHD. ASLAM", "D004137", "7599610153", "BDGPA4423D", "abj@gmail.com"));
        retailerList.add(new UserModel("RNFI API HO DI", "D005154", "9953555756", "AAHCR4860R", "abk@gmail.com"));
        retailerList.add(new UserModel("RUSHAB LOVELY DEDHIA", "P005296", "9270999970", "ANWPD5097K", "abl@gmail.com"));
        retailerList.add(new UserModel("AMIT SHARMA", "S005436", "9871068029", "RAJRN1234J", "abm@gmail.com"));
        retailerList.add(new UserModel("KUSUM GUPTA", "D005446", "8851032154", "BUAPG8380C", "abn@gmail.com"));
        retailerList.add(new UserModel("PARVEEN SHARMA", "P005667", "9911351939", "AZIPS5255J", "abo@gmail.com"));
        retailerList.add(new UserModel("PRIYANKA KUMARI", "D005781", "9910590890", "ECCPK1455H", "abp@gmail.com"));
        retailerList.add(new UserModel("BANAJA DILIP NAYAK", "D006001", "9168232218", "AQIPN9308C", "abq@gmail.com"));
        retailerList.add(new UserModel("INDERDEV SAHANI", "D006041", "9699996382", "BKYPS4616K", "abr@gmail.com"));
        retailerList.add(new UserModel("AZHAR SAYYED", "D006057", "9325827765", "FUCPS7870F", "abs@gmail.com"));
        retailerList.add(new UserModel("IMRAN SHAIKH", "D006060", "8007278612", "CACPS9354P", "abt@gmail.com"));
        retailerList.add(new UserModel("MD ZAMIR ANSARI", "P006097", "9831638582", "ANBPA3902F", "abu@gmail.com"));
        retailerList.add(new UserModel("SUMAIYYA BANO MOHAMMED ASHFAQUE SHAIKH", "D006165", "9987653640", "CFIPS9432R", "abv@gmail.com"));
        retailerList.add(new UserModel("RAVINDRA SHATU MAURYA", "P006484", "9029872946", "AQGPM1950K", "abw@gmail.com"));
        retailerList.add(new UserModel("VIMALKUMAR DHARAMVEERBHAI MEHTA", "P006774", "7046175817", "ABOPM3822B", "abx@gmail.com"));
        retailerList.add(new UserModel("SACHIN RAMCHANDRA NADEKAR", "D006834", "9930344394", "AGQPN7455C", "aby@gmail.com"));
        retailerList.add(new UserModel("DILIP KUMAR SHARMA", "D006874", "9910915307", "CETPS5184N", "aab@gmail.com"));
        retailerList.add(new UserModel("Sushma", "R007120", "9582916949", "ABRPL9228M", "abb@gmail.com"));
        retailerList.add(new UserModel("JYOTI OJHA", "D007170", "7000692914", "ABKPO1149F", "acb@gmail.com"));
        retailerList.add(new UserModel("SHYAM BABU", "S007271", "9814093800", "AZCPB0840J", "adb@gmail.com"));
        retailerList.add(new UserModel("MUKESH KUMAR", "D007307", "7889067811", "DZNPK8177B", "aeb@gmail.com"));
        retailerList.add(new UserModel("VIKASH RUHELA", "D007409", "8009678408", "COVPR1318P", "afb@gmail.com"));
        retailerList.add(new UserModel("RAJAN SINGH", "P007784", "9106344744", "GCYPS3777L", "agb@gmail.com"));
        retailerList.add(new UserModel("BABY KUMARI", "D008116", "7837885723", "CFGPK0841C", "ahb@gmail.com"));
        retailerList.add(new UserModel("RAHUL KUMAR", "D008117", "7307337978", "DRFPK3335H", "aib@gmail.com"));
        retailerList.add(new UserModel("HARIPARKASH SHUKLA", "D008203", "6393151156", "FNCPS1688N", "ajb@gmail.com"));
        retailerList.add(new UserModel("SURESH KUMAR GOYAL", "D008226", "9999483897", "AIPPG6922J", "akb@gmail.com"));
        retailerList.add(new UserModel("MITESH SHAH", "D008785", "8879644283", "AAMHM7488J", "alb@gmail.com"));
        retailerList.add(new UserModel("MUNNA KUMAR KUSHWAHA", "P008883", "9525773470", "COOPK2232L", "amb@gmail.com"));
        retailerList.add(new UserModel("MUKESH KUMAR RAI", "R007120", "9716833045", "BVVPR0042N", "anb@gmail.com"));
        retailerList.add(new UserModel("SEKHMOHAMMAD", "D009181", "9725780988", "CDXPS9098A", "aob@gmail.com"));
        retailerList.add(new UserModel("VIVEK KUMAR", "D009223", "9507388003", "DBVPK5572E", "apb@gmail.com"));
        retailerList.add(new UserModel("MANI KANT KARMAKAR", "D009660", "7254879988", "BPDPK0442K", "aqb@gmail.com"));
        retailerList.add(new UserModel("DIPESH KABIR", "D009756", "9860214097", "AYSPC1710H", "arb@gmail.com"));
        retailerList.add(new UserModel("DIPESH KABIR CHAUDHARI", "D009756", "9860214097", "AYSPC1710H", "asb@gmail.com"));
        retailerList.add(new UserModel("MOHAMMAD MUZAFFAR", "D0010589", "9504595045", "AHAPH7860E", "atb@gmail.com"));
        retailerList.add(new UserModel("CHETAN GUPTA", "P0011050", "7340706530", "ANMPG2467L", "aub@gmail.com"));
        retailerList.add(new UserModel("Sushma", "R007120", "9582916949", "ABRPL9228M", "avb@gmail.com"));

    }

}
