package com.seaman.utils;

import com.amazonaws.util.Base64;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class FileUtils {

    public static String getMimeType(String base64ImageString){

        String delims="[,]";
        String[] parts = base64ImageString.split(delims);
        String imageString = parts[1];
        byte[] imageByteArray = Base64.decode(imageString );

        InputStream is = new ByteArrayInputStream(imageByteArray);

        //Find out image type
        String mimeType = null;
        String fileExtension = null;
        try {
            mimeType = URLConnection.guessContentTypeFromStream(is); //mimeType is something like "image/jpeg"
            String delimiter="[/]";
            String[] tokens = mimeType.split(delimiter);
            fileExtension = tokens[1];
        } catch (IOException ioException){
            throw new RuntimeException(ioException);
        }

        System.out.println("mimeType : " + mimeType);
        System.out.println("fileExtension : " + fileExtension);

        return mimeType;
    }
}
