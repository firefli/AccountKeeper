//package org.firefli.accountkeeper;
//
//import android.app.Activity;
//import android.app.DialogFragment;
//import android.app.ProgressDialog;
//import android.view.inputmethod.EditorInfo;
//import android.widget.TextView;
//
//import com.jayway.awaitility.Awaitility;
//
//import junit.framework.TestCase;
//
//import org.firefli.accountkeeper.security.EncryptionManager;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.junit.runner.RunWith;
//import org.robolectric.Robolectric;
//import org.robolectric.annotation.Config;
//import org.robolectric.shadows.ShadowAlertDialog;
//
//import java.security.GeneralSecurityException;
//import java.util.concurrent.Callable;
//
//import static org.fest.assertions.api.Assertions.assertThat;
//import static org.hamcrest.Matchers.is;
//
///**
// * Created by firefli on 9/1/2015.
// */
//@RunWith(CustomRobolectricTestRunner.class)
//@Config(constants = BuildConfig.class)
//public class MainActivityTest extends TestCase {
//
//    private static final String KEY = "TEST";
//
//    @Rule
//    public final ExpectedException exception = ExpectedException.none();
//
//    @Before
//    public void setUp() throws GeneralSecurityException {
//        Activity activity = Robolectric.buildActivity(MainActivity.class).create().start().resume().get();
//        DialogFragment pwdFrag = (DialogFragment) activity.getFragmentManager().findFragmentByTag("fragment_enter_pwd");
//        //FragmentTestUtil.startVisibleFragment(pwdFrag);
//        TextView pwdText = (TextView)pwdFrag.getView().findViewById(R.id.txt_pwd);
//        pwdText.setText(KEY);
//        pwdText.onEditorAction(EditorInfo.IME_ACTION_DONE);
//
//        Awaitility.await().until(new Callable<Boolean>() {
//            public Boolean call() throws Exception {
//                return ShadowAlertDialog.getLatestAlertDialog() instanceof ProgressDialog;
//            }
//        }, is(false));
//
//        assertThat(ShadowAlertDialog.getLatestAlertDialog()).isNull();
//    }
//
//    @Test
//    public void testEncrypt() throws GeneralSecurityException, EncryptionManager.EncryptionManagerNeedsKeyException {
//
//    }
//}
