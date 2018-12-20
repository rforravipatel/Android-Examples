package com.example.android.bitmapimagedemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView im = (ImageView) findViewById(R.id.imageView2); //original image
        ImageView shareImage1 = (ImageView) findViewById(R.id.imageView3); //image of share1 created
        ImageView shareImage2 = (ImageView) findViewById(R.id.imageView4); //image of share2 created
        ImageView decryptedImageView = (ImageView) findViewById(R.id.imageView5); //image we get after reconstructed from share1 and share2

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ax); //image we want to pass to create shares.
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        im.setImageBitmap(bitmap);

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Log.d("Image original width:", String.valueOf(w));

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);// will get pixels of image.

        int[] int_share1 = new int[w * h];
        int[] int_share2 = new int[w * h];

        Bitmap decrytedImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); //bitmap object for image we get after decrytion from both shares.

      //Encryption -- Creating shares of image.
        for (int i = 0; i < pix.length; i++) {

            int redColorInPixel = Color.red(pix[i]);// getting red color from each pixel.
            int blueColorInPixel = Color.blue(pix[i]);// getting blue color from each pixel.
            int greenColorInPixel = Color.green(pix[i]); // getting green color from each pixel.
            int alphaColorInPixel = Color.alpha(pix[i]); // getting alpha from each pixel.

            if(i == 0){
                Log.d("Original: " , i + " : " + redColorInPixel);

            }
            //setting value to 250 if greater than 250.
            if (redColorInPixel > 250) {
                redColorInPixel = 250;
            }
            if (blueColorInPixel > 250) {
                blueColorInPixel = 250;
            }
            if (greenColorInPixel > 250) {
                greenColorInPixel = 250;
            }
            if (alphaColorInPixel > 250) {
                alphaColorInPixel = 250;
            }
//SSS algo implementation.
            int x[] = {1, 2};
            int redy, bluey, greeny, alphay;
            int q = 251;
            Random r = new Random();
            int a = r.nextInt(250);
            for (int j = 0; j < 2; j++) {

                    int tempRedColorInPixel = redColorInPixel;
                    int tempBlueColorInPixel = blueColorInPixel;
                    int tempGreenColorInPixel = greenColorInPixel;
                    int tempAlphaColorInPixel = alphaColorInPixel;


                    redy = tempRedColorInPixel + (a * x[j]); //(byte) ((byte) (triangle[i]) + (byte)(a[0] * powerof(x[j], 1)) + (byte)(a[1] * powerof(x[j], 2)));

                    bluey = tempBlueColorInPixel + (a * x[j]);

                    greeny = tempGreenColorInPixel + (a * x[j]);

                    alphay = tempAlphaColorInPixel + (a * x[j]);

                    if (redy > q) {
                        redy = (redy % q);
                    }
                    tempRedColorInPixel = redy;

                    if (bluey > q) {
                        bluey = (bluey % q);
                    }
                    tempBlueColorInPixel = bluey;

                    if (greeny > q) {
                        greeny = (greeny % q);
                    }
                    tempGreenColorInPixel = greeny;

                    if (alphay > q) {
                        alphay = (alphay % q);
                    }
                    tempAlphaColorInPixel = alphay;


                    if(i == 0 && j == 0){
                        Log.d("First  Encrpted: " , String.valueOf(tempRedColorInPixel));
                    }
                    if(i == 0 && j == 1){
                        Log.d("Second Encrpted: " , String.valueOf(tempRedColorInPixel));
                    }

                    if(j == 0){
                        int_share1[i] = Color.argb(tempAlphaColorInPixel, tempRedColorInPixel, tempGreenColorInPixel, tempBlueColorInPixel); //setting new value of each color to same pixel position.

                        if(i == 0){
                            Log.d("First int_share value: " , String.valueOf(int_share1[i]));
                        }

                    }
                    if(j == 1){
                        int_share2[i] = Color.argb(tempAlphaColorInPixel, tempRedColorInPixel, tempGreenColorInPixel, tempBlueColorInPixel); //setting new value of each color to same pixel position.
//                        da1[i] = (byte) int_share2[i];
                    }

            }


        }

//        Log.d("Byte arra:" , String.valueOf(Arrays.toString(da)));


        Bitmap share1 = Bitmap.createBitmap(int_share1, w, h, Bitmap.Config.ARGB_8888); //bitmap object for share1.
        Bitmap share2 = Bitmap.createBitmap(int_share2, w, h, Bitmap.Config.ARGB_8888); //bitmap object for share2.

        //Code to write bitmap in file form
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("SecureGallery", Context.MODE_PRIVATE);
        file = new File(file, "UniqueFileName"+".txt");
        FileChannel fc = null;
        try  {
            Log.d("Int_share1 size:" , String.valueOf(int_share1.length));
            fc = new FileOutputStream(file).getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(4 * (int_share1.length + 2));
            for (int i : int_share1) {
//                Log.i("Buffer va:", String.valueOf(i));
                buffer.putInt(i);
            }
            buffer.putInt(w);
            buffer.putInt(h);
            buffer.flip();

            fc.write(buffer);

            Log.d("File size:", String.valueOf(fc.size()));

        }catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }finally {
            try {
                fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        shareImage1.setImageBitmap(share1);

        ContextWrapper wrapper2 = new ContextWrapper(getApplicationContext());
        File file2 = wrapper2.getDir("SecureGallery", Context.MODE_PRIVATE);
        file2 = new File(file2, "UniqueFileName2"+".txt");
        FileChannel fc2 = null;
        try  {
            fc2 = new FileOutputStream(file2).getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(4 * int_share2.length);
            for (int i : int_share2) {
//                Log.i("Buffer va:", String.valueOf(i));
                buffer.putInt(i);
            }
            buffer.flip();

            fc2.write(buffer);

            Log.d("File size:", String.valueOf(fc2.size()));

        }catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }finally {
            try {
                fc2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        shareImage2.setImageBitmap(share2);

        //Decrypt


        int[] imageDecrypt = new int[int_share1.length];
        int[] imageDecryptShare1 = new int[int_share1.length + 2];
        int[] imageDecryptShare2 = new int[int_share1.length];



//        for(int i = 0; i<int_share1.length; i++) {
////            imageDecryptShare1[i] = int_share1[i];
//            imageDecryptShare2[i] = int_share2[i];
//
//        }

        int x1 = 1, x2 = 2;

        int inverse_x1 = (-x2  * mulInverse(x1-x2)) % 251;
        int inverse_x2 = (-x1 * mulInverse(x2-x1)) % 251;

        int counter = 0;
        File f1 = new File(file.getAbsolutePath());
        FileChannel fr;
        try {

            fr = new FileInputStream(f1).getChannel();
            ByteBuffer bufferread = ByteBuffer.allocate(10247777);
            fr.read(bufferread);
            bufferread.flip();


            while (bufferread.hasRemaining()) {

                imageDecryptShare1[counter] = bufferread.getInt();
//                Log.d("While: " , String.valueOf(imageDecryptShare1[0]));
                counter++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File fd2 = new File(file2.getAbsolutePath());
        FileChannel frd2;
        try {

            frd2 = new FileInputStream(fd2).getChannel();
            ByteBuffer bufferread = ByteBuffer.allocate(10247777);
            frd2.read(bufferread);
            bufferread.flip();

            int i = 0;
            while (bufferread.hasRemaining()) {

                imageDecryptShare2[i] = bufferread.getInt();
//                Log.d("While: " , String.valueOf(imageDecryptShare1[0]));
                i++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.d("Size of get array: ", String.valueOf(imageDecryptShare1.length));

        for(int i = 0; i<imageDecryptShare1.length - 2; i++){


            int redColorInPixel1 = Color.red(imageDecryptShare1[i]);
            int blueColorInPixel1 = Color.blue(imageDecryptShare1[i]);
            int greenColorInPixel1 = Color.green(imageDecryptShare1[i]);
            int alphaColorInPixel1 = Color.alpha(imageDecryptShare1[i]);

            int redColorInPixel2 = Color.red(imageDecryptShare2[i]);
            int blueColorInPixel2 = Color.blue(imageDecryptShare2[i]);
            int greenColorInPixel2 = Color.green(imageDecryptShare2[i]);
            int alphaColorInPixel2 = Color.alpha(imageDecryptShare2[i]);

            if(i == 0){
                Log.d("First : " , String.valueOf(redColorInPixel1));
                Log.d("Second : " , String.valueOf(redColorInPixel2));
            }

            redColorInPixel1 =  (redColorInPixel1 * inverse_x1)
                    + (redColorInPixel2 * inverse_x2);
            redColorInPixel1 =  (redColorInPixel1 % 251);

            blueColorInPixel1 =  (blueColorInPixel1 * inverse_x1)
                    + (blueColorInPixel2 * inverse_x2);
            blueColorInPixel1 =  (blueColorInPixel1 % 251);

            greenColorInPixel1 =  (greenColorInPixel1 * inverse_x1)
                    + (greenColorInPixel2 * inverse_x2);
            greenColorInPixel1 =  (greenColorInPixel1 % 251);

            alphaColorInPixel1 =  (alphaColorInPixel1 * inverse_x1)
                    + (alphaColorInPixel2 * inverse_x2);
            alphaColorInPixel1 =  (alphaColorInPixel1 % 251);

            if(redColorInPixel1 < 0){
                redColorInPixel1 = 251 + redColorInPixel1;
            }
            if(blueColorInPixel1 < 0){
                blueColorInPixel1 = 251 + blueColorInPixel1;
            }
            if(greenColorInPixel1 < 0){
                greenColorInPixel1 = 251 + greenColorInPixel1;
            }
            if(alphaColorInPixel1 < 0){
                alphaColorInPixel1 = 251 + alphaColorInPixel1;
            }

            if(i == 0){
                Log.d("Decrypted re: " , i + " : " + redColorInPixel1);

            }
            imageDecrypt[i] = Color.argb(alphaColorInPixel1, redColorInPixel1, greenColorInPixel1, blueColorInPixel1);

        }

        Log.d("Image width array:", String.valueOf(imageDecryptShare1[counter - 2]));

        decrytedImage.setPixels(imageDecrypt,0, imageDecryptShare1[counter - 2], 0, 0, imageDecryptShare1[counter - 2], imageDecryptShare1[counter - 1]);
        decryptedImageView.setImageBitmap(decrytedImage);

    }

    private static int powerof(int x, int i) {
        for (int j = 1; j < i; j++) {
            x = (x * x);
            //System.out.println(x);
        }

        return x;
    }

    private static int mulInverse(int x)
    {
        x = x % 251;
        if(x < 0)
        {
            x = x + 251;
            for(int i = 1; i < 251; i++)
            {
                if((x*i) % 251 == 1)
                {
                    i = 251 - i;

                    return -i;
                }
            }
        }
        else
        {
            for(int i = 1; i < 251; i++)
            {
                if((x*i) % 251 == 1)
                {
                    //System.out.println(i);
                    return i;
                }
            }
        }
        return 0;

    }

}