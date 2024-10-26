package org.scijava.ui.swing;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URITest
{
    public static void main( String[] args ) throws URISyntaxException, MalformedURLException
    {
        File f = new File("tischer");
        System.out.println(f.toURI());
        System.out.println( new URI( "file", "/tischer/test.txt" , null).toString() );
        URI uri = new URI( "file:/path/to/local/file.txt" );
        System.out.println( uri.getScheme() );
        URL url = uri.toURL();
        System.out.println(url.getFile());
        if ( uri.getScheme().equals( "file" ) )
        {
            File file = new File( uri.toURL().getPath() );
            System.out.println( file.getAbsolutePath() );
        }
    }
}
