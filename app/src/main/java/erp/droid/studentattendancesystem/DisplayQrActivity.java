package erp.droid.studentattendancesystem;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static android.R.attr.bitmap;

public class DisplayQrActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_qr);

        displayQrCode();
    }

    private void displayQrCode() {
        try {
            String qrKey = getIntent().getStringExtra("qrKey");
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;

            QRGEncoder qrgEncoder = new QRGEncoder(
                    qrKey, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);

            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            ((ImageView)findViewById(R.id.ivQrCode)).setImageBitmap(bitmap);
        }
        catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }
}
