package com.miseskeygenius;

import android.app.Activity;
import android.content.ClipData;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qrcode.ErrorCorrectionLevel;
import com.qrcode.QRCode;

import static com.miseskeygenius.MisesBip32.BITCOIN_LEGACY;
import static com.miseskeygenius.MisesBip32.BITCOIN_SEGWIT;
import static com.miseskeygenius.MisesBip32.ETHEREUM;
import static com.miseskeygenius.MisesBip32.PRIVATE_KEY;
import static com.miseskeygenius.MisesBip32.PUBLIC_ADDRESS;
import static com.miseskeygenius.MisesBip32.generateSeed;
import static com.miseskeygenius.MisesBip32.getMasterPrivateKey;
import static com.miseskeygenius.MisesBip32.mnemonicsOk;
import static com.miseskeygenius.MisesBip32.seedOk;

public class MainActivity extends Activity
{
    private MisesBip32 misesBip32;

    private static Thread seedThread = null;
    private static Thread derivationThread = null;

    private int mode;
    public static final int MODE_MNEMONICS = 0;
    public static final int MODE_PASSPHRASE = 1;
    //public static final int MODE_MNEMONICS_PLUS_PASSPHRASE = 2;
    public static final int MODE_SEED = 3;
    //public static final int MODE_MPK = 4;

    Toast toast;

    // QR atributes
    int qrMaskPattern = 3;
    int maxQRsize;

    private EditTextLabel mnemonicsBox;
    private EditTextLabel passphraseBox;
    private EditTextLabel seedBox;
    private EditTextLabel mpkBox;

    private LinearLayout grayPanel;
    private SekizbitSwitch graySwitch;
    private LinearLayout grayHiddenLayout;

    private EditTextLabel graySeedBox;
    private EditTextLabel grayMpkBox;

    private SpinnerLabel coinSpinner;
    private EditableSpinnerLabel pathBox;
    private EditTextLabel addressNumberBox;
    private SpinnerLabel ppKeySpinner;

    private TextView keyDescriptionBox;
    private ImageView qrImageView;
    private TextView  keyBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // hide the title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(null); // savedInstanceState?
        setContentView(R.layout.activity_main);

        // find views in the XML file
        SpinnerLabel modeSpinner = findViewById(R.id.modeSpinner);
        modeSpinner.requestFocus();
        RelativeLayout graySwitchPanel = findViewById(R.id.graySwitchPanel);

        mnemonicsBox = findViewById(R.id.mnemonicsBox);
        passphraseBox = findViewById(R.id.passphraseBox);
        seedBox = findViewById(R.id.seedBox);
        mpkBox = findViewById(R.id.mpkBox);

        grayPanel = findViewById(R.id.grayPanel);
        LinearLayout graySwitchLayout = findViewById(R.id.graySwitchLayout);
        graySwitch = new SekizbitSwitch(graySwitchLayout);
        grayHiddenLayout = findViewById(R.id.grayHiddenLayout);

        graySeedBox = findViewById(R.id.graySeedBox);
        grayMpkBox = findViewById(R.id.grayMpkBox);

        coinSpinner = findViewById(R.id.coinSpinner);
        pathBox = findViewById(R.id.pathBox);
        pathBox.hideText();
        addressNumberBox = findViewById(R.id.addressNumberBox);
        ppKeySpinner = findViewById(R.id.ppKeySpinner);

        keyDescriptionBox = findViewById(R.id.keyDescriptionBox);
        qrImageView = findViewById(R.id.qrImageView);
        keyBox = findViewById(R.id.keyBox);

        // initialization
        String mnemonics = "word word word word word word";
        String seed = "F3E88F40D6D94FAFCB184A9994970A0F48B1C0D00292E3471D36A0F83F7ABF0812E69EA0E0BB122339341F019E5DEAB36CA07CF655B28EAB11A7B1347D26F85A";
        String mpk = "xprv9s21ZrQH143K4H9nJinLQoUZf2vwfnKNcrgegdSpDp4tSmpQFHFjfo4QuLCu24ysYoXgQasgkQrqJMgn3E8rap5KbyNU5yvEawn1gFiJGjV";
        String path = "m/0'/0/i'";
        String addressNumbers = "0-0";

        mode = 0;
        mnemonicsBox.setText(mnemonics);
        passphraseBox.setText("Enter your passphrase");
        seedBox.setText(seed);
        mpkBox.setText(mpk);
        graySeedBox.setText(seed);
        grayMpkBox.setText(mpk);
        pathBox.setText(path);
        addressNumberBox.setText(addressNumbers);
        misesBip32 = new MisesBip32(mpk, path, addressNumbers);

        // set listeners for view objects
        modeSpinner.setOnItemSelectedListener(modeSpinnerListener);
        mnemonicsBox.addTextChangedListener(mnemonicsBoxWatcher);
        passphraseBox.addTextChangedListener(passphraseBoxWatcher);
        seedBox.addTextChangedListener(seedBoxWatcher);
        mpkBox.addTextChangedListener(mpkBoxWatcher);
        graySwitchPanel.setOnClickListener(graySwitchPanelListener);
        graySeedBox.addTextChangedListener(graySeedBoxWatcher);
        grayMpkBox.addTextChangedListener(grayMpkBoxWatcher);
        coinSpinner.setOnItemSelectedListener(coinSpinnerListener);
        pathBox.addTextChangedListener(pathBoxWatcher);
        addressNumberBox.addTextChangedListener(addressNumberBoxWatcher);
        ppKeySpinner.setOnItemSelectedListener(ppKeySpinnerListener);
        qrImageView.setOnClickListener(qrImageListener);
        keyBox.setOnClickListener(keyBoxListener);
        keyBox.setFocusableInTouchMode(false);

        // calculate QR max image size
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        maxQRsize = width;
        if (height < width) maxQRsize = height;
        maxQRsize -= grayPanel.getPaddingLeft() * 4;
    }

    // Listener for modeSpinner
    AdapterView.OnItemSelectedListener modeSpinnerListener= new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (mode!=position) {
                modeSpinnerChanged(position);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    };

    // Listener for mnemonicsBox
    private final TextWatcher mnemonicsBoxWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        public void onTextChanged(CharSequence mnemonics, int start, int before, int count) {
            mnemonicsBoxChanged(mnemonics.toString());
        }
    };

    // Listener for passphraseBox
    private final TextWatcher passphraseBoxWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        public void onTextChanged(CharSequence passphrase, int start, int before, int count) {
            passphraseBoxChanged(passphrase.toString());
        }
    };

    // Listener for seedBox
    private final TextWatcher seedBoxWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        public void onTextChanged(CharSequence seed, int start, int before, int count) {
            seedBoxChanged(seed.toString());
        }
    };

    // Listener for mpkBox
    private final TextWatcher mpkBoxWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        public void onTextChanged(CharSequence mpk, int start, int before, int count) {
            mpkBoxChanged(mpk.toString());
        }
    };

    // Listener for graySwitchPanel
    View.OnClickListener graySwitchPanelListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            graySwitch.toggle();
            if (graySwitch.isActivated()) grayHiddenLayout.setVisibility(View.VISIBLE);
            else grayHiddenLayout.setVisibility(View.GONE);
        }
    };

    // Listener for graySeedBox
    private final TextWatcher graySeedBoxWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        public void onTextChanged(CharSequence seed, int start, int before, int count) {
            graySeedBoxChanged(seed.toString());
        }
    };

    // Listener for grayMpkBox
    private final TextWatcher grayMpkBoxWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        public void onTextChanged(CharSequence mpk, int start, int before, int count) {
            grayMpkBoxChanged();
        }
    };

    // Listener for coinSpinner
    AdapterView.OnItemSelectedListener coinSpinnerListener= new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (misesBip32.coin!=position) {
                coinSpinnerChanged(position);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    };

    // Listener for pathBox
    private final TextWatcher pathBoxWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        public void onTextChanged(CharSequence path, int start, int before, int count) {
            pathBoxChanged(path.toString());
        }
    };

    // Listener for address number Box
    private final TextWatcher addressNumberBoxWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) { }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        public void onTextChanged(CharSequence addressNumber, int start, int before, int count) {
            addressNumberBoxChanged(addressNumber.toString());
        }
    };

    // Listener for ppKeySpinner
    AdapterView.OnItemSelectedListener ppKeySpinnerListener= new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // check if spinner item has been changed
            if ((position==0)!=misesBip32.isPrivateKey()) {
                ppKeySpinnerChanged(position);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    };

    View.OnClickListener qrImageListener = new View.OnClickListener() {
        public void onClick(View v) {
            qrImageClicked();
        }
    };

    View.OnClickListener keyBoxListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            keyBoxClicked();
        }
    };

    // when user changes modeSpinner...
    public void modeSpinnerChanged (int mode) {

        this.mode = mode;

        int mnemonicsVisible = View.GONE;
        int passphraseVisible = View.GONE;
        int seedVisible = View.GONE;
        int mpkVisible = View.GONE;
        int grayPanelVisible = View.VISIBLE;
        int graySeedVisible = View.VISIBLE;
        int grayMpkVisible = View.VISIBLE;

        switch(mode){
            case 0:
                // MODE_MNEMONICS
                mnemonicsVisible = View.VISIBLE;
                updategraySeed();
                break;

            case 1:
                // MODE_PASSPHRASE
                passphraseBox.setLabel("BIP32 Passphrase");
                passphraseVisible = View.VISIBLE;
                firePassphraseBoxListener();
                break;

            case 2:
                // MODE_MNEMONICS_PLUS_PASSPHRASE
                passphraseBox.setLabel("BIP39 Passphrase");
                mnemonicsVisible = View.VISIBLE;
                passphraseVisible = View.VISIBLE;
                firePassphraseBoxListener();
                break;

            case 3:
                // MODE_SEED
                seedVisible = View.VISIBLE;
                graySeedVisible = View.GONE;
                updategraySeed();
                break;

            case 4:
                // MODE_MPK
                mpkVisible = View.VISIBLE;
                grayPanelVisible = View.GONE;
                fireMpkBoxListener();
        }

        mnemonicsBox.setVisibility(mnemonicsVisible);
        passphraseBox.setVisibility(passphraseVisible);
        seedBox.setVisibility(seedVisible);
        mpkBox.setVisibility(mpkVisible);
        grayPanel.setVisibility(grayPanelVisible);
        graySeedBox.setVisibility(graySeedVisible);
        grayMpkBox.setVisibility(grayMpkVisible);
    }

    private boolean inputSeedOk()
    {
        boolean ok = true;

        switch(mode)
        {
            case 0:
                // MODE_MNEMONICS
                if (mnemonicsBox.isWrong()) ok = false;
                break;

            case 1:
                // MODE_PASSPHRASE
                if (passphraseBox.isWrong()) ok = false;
                break;

            case 2:
                // MODE_MNEMONICS_PLUS_PASSPHRASE
                if (mnemonicsBox.isWrong()) ok = false;
                else if (passphraseBox.isWrong()) ok = false;
                break;

            case 3:
                // MODE_SEED
                if (seedBox.isWrong()) ok = false;
                break;

            case 4:
                // MODE_MPK
                if (mpkBox.isWrong()) ok = false;
        }
        return ok;
    }

    private boolean allInputsOk(){
        boolean ok = true;

        if (!inputSeedOk()) ok = false;
        else if (pathBox.isWrong()) ok = false;
        else if (misesBip32.lookForAddresses & addressNumberBox.isWrong()) ok = false;

        return ok;
    }

    private void updategraySeed() {

        if (!inputSeedOk()) setStatus("ERROR", null);
        else
        {
            setStatus("PROCESSING", null);

            String mnemonics = mnemonicsBox.getText();
            String passphrase = passphraseBox.getText();
            String seed = seedBox.getText();

            if (mode==MODE_SEED) graySeedBox.setText(seed);
            else if (mode==MODE_PASSPHRASE)
                graySeedBox.setText(generateSeed(mnemonics, passphrase, mode));

            else { // MODE_MNEMONICS or MODE_MNEMONICS_PLUS_PASSPHRASE
                // kill previous thread
                if (seedThread != null) seedThread.interrupt();

                seedThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // execute the hard work
                        String resultSeed = generateSeed(mnemonics, passphrase, mode);
                        boolean wasInterrupted = !carryOn();

                        runOnUiThread(new Runnable() {
                            public void run() {
                                // do post execute stuff
                                if (!wasInterrupted) graySeedBox.setText(resultSeed);
                            }
                        });
                    }
                });
                seedThread.start();
            }
        }
    }

    // update seed when mnemonics changes
    private void mnemonicsBoxChanged(String mnemonics) {

        if (mnemonicsOk(mnemonics)) {
            mnemonicsBox.setCorrect(true);
            updategraySeed();
        }
        else {
            if (derivationThread != null) derivationThread.interrupt();
            mnemonicsBox.setCorrect(false);
            setStatus("ERROR", null);
        }
    }

    private void firePassphraseBoxListener(){
        passphraseBoxChanged(passphraseBox.getText());
    }

    // update seed when passphrase changes
    private void passphraseBoxChanged(String passphrase) {

         if (passphraseOk(passphrase)) {
             passphraseBox.setCorrect(true);
             updategraySeed();
        }
        else {
             if (derivationThread != null) derivationThread.interrupt();
             passphraseBox.setCorrect(false);
             setStatus("ERROR", null);
         }
    }

    private boolean passphraseOk(String passphrase) {
        return mode!=MODE_PASSPHRASE | passphrase.length()!=0;
    }

    // update master private key when seed changes
    private void seedBoxChanged(String seed)
    {
        if (seedOk(seed)) {
            seedBox.setCorrect(true);
            graySeedBox.setText(seed);
            updategraySeed();
        }

        else {
            if (derivationThread != null) derivationThread.interrupt();
            seedBox.setCorrect(false);
            setStatus("ERROR", null);
        }
    }

    private void fireMpkBoxListener(){
        mpkBoxChanged(mpkBox.getText());
    }

    // when mpk changes ...
    private void mpkBoxChanged(String mpk){

        boolean ok = misesBip32.setMasterPrivateKey(mpk);

        if (ok) {
            mpkBox.setCorrect(true);
            setStatus("PROCESSING", null);
            grayMpkBox.setText(mpk);
        }
        else {
            if (derivationThread != null) derivationThread.interrupt();
            mpkBox.setCorrect(false);
            setStatus("ERROR", null);
        }
    }

    private void graySeedBoxChanged(String seed)
    {
        // seed has already been checked
        String mpk = getMasterPrivateKey(seed);
        misesBip32.setMasterPrivateKey(mpk);
        grayMpkBox.setText(mpk);
    }

    private void grayMpkBoxChanged(){
        // mpk has already been checked and set in misesBip32
        processDerivation();
    }

    public void coinSpinnerChanged (int coin) {

        misesBip32.coin=coin;

        updateAddressNumberLayout();
        processDerivation();
    }
    
    // when path changes ...
    private void pathBoxChanged(String path) {

        //boolean oldLook = misesBip32.lookForAddresses;
        boolean ok = misesBip32.setPath(path);

        if (ok) {
            pathBox.setCorrect(true);
            updateAddressNumberLayout();
            // if changed between addresses and extended key...
            //if (misesBip32.lookForAddresses==!oldLook) updateppKeySpinner();
            processDerivation();
        }
        else {
            if (derivationThread != null) derivationThread.interrupt();
            pathBox.setCorrect(false);
            setStatus("ERROR", null);
        }
    }

    private void updateppKeySpinner() {
        if (misesBip32.lookForAddresses) ppKeySpinner.setItems("Private WIF,Private Ethereum,Public Legacy,Public SegWit,Public Ethereum", this);
        else ppKeySpinner.setItems("Private Legacy xPrv,Public Legacy xPub,Private Segwit zPrv,Public Segwit zPub", this);
    }

    public void ppKeySpinnerChanged (int position) {

        misesBip32.setPpKey(position);

        updateAddressNumberLayout();
        processDerivation();
    }

    // when address numbers changes ...
    private void addressNumberBoxChanged(String string) {

        boolean ok = misesBip32.setAddressNumbers(string);

        if (ok) {
            addressNumberBox.setCorrect(true);
            processDerivation();
        }
        else {
            if (derivationThread != null) derivationThread.interrupt();
            addressNumberBox.setCorrect(false);
            setStatus("ERROR", null);
        }
    }
    
    private void updateAddressNumberLayout()
    {
        if (misesBip32.lookForAddresses) {
            // update address letter
            String path = pathBox.getText();
            char addressLetter = path.charAt(path.length()-1);
            if (addressLetter=='\'' | addressLetter=='H') addressLetter = path.charAt(path.length()-2);

            // update max number of addresses text
            int maxNAdresses = misesBip32.getMaxNAdresses();

            addressNumberBox.setVisibility(View.VISIBLE);
            addressNumberBox.setLabel("Address number '" + addressLetter + "' from-to: (max " + maxNAdresses + ")");
        }
        else addressNumberBox.setVisibility(View.GONE);
    }

    private void qrImageClicked(){
        // change to next mask pattern, from 0 to 7
        qrMaskPattern = (qrMaskPattern +1) % 8;
        showToast("QR Mask Pattern " + qrMaskPattern);
        qrImageView.setImageBitmap(buildQRCode());
    }

    public void keyBoxClicked()
    {
        // show toast
        int nKeys = misesBip32.getNAdresses();
        String toastText;

        if (nKeys>1) toastText = nKeys + " keys copied to clipboard";
        else toastText = "Copied to clipboard";

        showToast(toastText);
        copyToClipboard(misesBip32.keyList);
    }

    private void copyToClipboard(String text)
    {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", text);
            clipboard.setPrimaryClip(clip);
        }

        else {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setText(text);
        }
    }

    private void showToast(String text){
        if (toast!=null) toast.cancel();
        toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    void setStatus(String status, Bitmap bitmap) {
        if (status.equals("PROCESSING")) {
            keyDescriptionBox.setText("Processing...");
            qrImageView.setVisibility(View.GONE);
            keyBox.setVisibility(View.GONE);
        }

        else if (status.equals("ERROR")) {
            keyDescriptionBox.setText("Check the required fields");
            qrImageView.setVisibility(View.GONE);
            keyBox.setVisibility(View.GONE);
        }

        else // status=="SUCCESS"
        {
            // set key description
            String pp;
            if (misesBip32.isPrivateKey()) pp = "Private";
            else pp = "Public";

            String coin;
            if (misesBip32.coin==BITCOIN_LEGACY) coin = "Bitcoin Legacy";
            else if (misesBip32.coin==BITCOIN_SEGWIT) coin = "Bitcoin Segwit";
            else coin = "Ethereum";

            int nKeys = misesBip32.getNAdresses();

            String description;

            if (misesBip32.lookForAddresses)
            {
                if (nKeys>1) description = nKeys + " " + coin + " " + pp + " Addresses";
                else description = coin + " " + pp + " Address";
            }
            else description = "Extended " + pp + " Key";

            keyDescriptionBox.setText(description);
            qrImageView.setImageBitmap(bitmap);

            if (misesBip32.lookForAddresses) keyBox.setText(misesBip32.keyListNumbers);
            else keyBox.setText(misesBip32.keyList);

            qrImageView.setVisibility(View.VISIBLE);
            keyBox.setVisibility(View.VISIBLE);
        }
    }

    private void processDerivation()
    {
        if (!allInputsOk() | !misesBip32.inputOk()) setStatus("ERROR", null);

        else
        {
            setStatus("PROCESSING", null);

            // kill previous thread
            if (derivationThread != null) derivationThread.interrupt();

            derivationThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // execute the hard work
                    misesBip32.processDerivation();

                    // get the QR image
                    Bitmap bitmap = null;
                    if (carryOn()) {
                        qrMaskPattern = 3;
                        bitmap = buildQRCode();
                    }
                    final Bitmap finalBitmap = bitmap;

                    boolean wasInterrupted = !carryOn();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            // do post execute stuff
                            if (!wasInterrupted) setStatus("SUCCESS", finalBitmap);
                        }
                    });
                }
            });
            derivationThread.start();
        }
    }

    private static boolean carryOn(){
        return !Thread.currentThread().isInterrupted();
    }

    public Bitmap buildQRCode()
    {
        Bitmap bitmap = null;

        int errorCorrectionLevel = ErrorCorrectionLevel.H;
        if (misesBip32.getNAdresses()>1) errorCorrectionLevel = ErrorCorrectionLevel.L;

        try {
            QRCode qrcode = new QRCode(misesBip32.keyList, errorCorrectionLevel, qrMaskPattern);

            int cellSize = maxQRsize/qrcode.getModuleCount();
            if (cellSize>10) cellSize=10;

            bitmap = qrcode.createImage(cellSize);
        } catch (Exception e) {
            System.out.println("QR error: " + e.getMessage());
        }
        return bitmap;
    }
}