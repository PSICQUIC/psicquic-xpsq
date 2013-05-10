package org.hupo.psi.mi.psicquic.server.builder;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # BuildIndex: Build index
 #
 #=========================================================================== */

import java.util.*;
import java.net.*;
import java.io.*;

import java.lang.Thread.State;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.cli.*;

//import org.w3c.dom.*;
//import javax.xml.parsers.*;

import org.hupo.psi.mi.psicquic.util.JsonContext;

public class buildindex{
    
    public static final String 
        DEFAULT_CONTEXT = "/etc/psq-context-default.json";
    
    public static final String 
        DEFAULT_RECORD_FORMAT = "mif254";   

    public static String rfrmt = DEFAULT_RECORD_FORMAT;
    
    public static String host = null;
    public static String idir = null;
    public static String ifile = null;
    public static String delfile = null;
    
    public static boolean clr = false;
    public static boolean zip = false;
    public static boolean gzip = false;
    public static boolean logId = false;
    public static boolean desc = false;

    public static Map<String,String> pax = null; // xslt transformer params

    public static int btCount = -1;
    public static int stCount = -1;
    
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public static void main( String [ ] args ){
        
        Options options = new Options();
        
        Option clrOption = OptionBuilder.withLongOpt( "clear" )
            .withDescription( "clear index" )
            .create( "clr" );

        options.addOption( clrOption );

        Option ctxOption = OptionBuilder.withLongOpt( "context" )
            .withArgName( "file.json" ).hasArg()
            .withDescription( "configuration file" )
            .create( "ctx" );

        options.addOption( ctxOption );
        
        Option dirOption = OptionBuilder.withLongOpt( "directory" )
            .withArgName( "directory" ).hasArg()
            .withDescription( "input file directory" )
            .create( "dir" );
        
        options.addOption( dirOption );

        Option fileOption = OptionBuilder.withLongOpt( "file" )
            .withArgName( "file" ).hasArg()
            .withDescription( "input file" )
            .create( "f" );

        options.addOption( fileOption );
        
        Option fmtOption = OptionBuilder.withLongOpt( "format" )
            .withArgName( "format" ).hasArg()
            .withDescription( "input record format" )
            .create( "fmt" );
        
        options.addOption( fmtOption );
        
        Option hlpOption = OptionBuilder.withLongOpt( "help" )
            .withDescription( "help" )
            .create( "help" );
        
        options.addOption( hlpOption );

        Option hostOption = OptionBuilder.withLongOpt( "host" )
            .withArgName( "host" ).hasArg()
            .withDescription( "host[:80]" )
            .create( "h" );

        options.addOption( hostOption );
               
        Option recOption = OptionBuilder
            .withDescription( "recursively process directory" )
            .create( "r" );
        
        options.addOption( recOption );

        Option tbOption = OptionBuilder.withLongOpt( "threads-builder" )
            .withArgName( "count" ).hasArg()
            .withDescription( "number of builder threads" )
            .create( "tb" );
        
        options.addOption( tbOption );
        
        Option tsOption = OptionBuilder.withLongOpt( "threads-solr" )
            .withArgName( "count" ).hasArg()
            .withDescription( "number of solr connection threads" )
            .create( "ts" );
        
        options.addOption( tsOption );


        Option xsltOption = OptionBuilder.withLongOpt( "xslt-param" )
            .hasArgs(2).withValueSeparator()
            .withArgName( "name=value" )
            .withDescription( "XSLT transformers: parameter name=value pairs" )
            .create( "xp" );
        
        options.addOption( xsltOption );
        
        Option zipOption = OptionBuilder.withLongOpt( "zip" )
            .withDescription( "zipped files" )
            .create( "z" );

        options.addOption( zipOption );

        Option gzipOption = OptionBuilder.withLongOpt( "gzip" )
            .withDescription( "gzipped files" )
            .create( "gz" );

        options.addOption( gzipOption );
        
        Option logOption = OptionBuilder.withLongOpt( "log-id" )
            .withDescription( "generate record identifier log files" )
            .create( "l" );

        options.addOption( logOption );
        
        Option delOption = OptionBuilder.withLongOpt( "delete" )
            .withArgName( "xpsq-log-file" ).hasArg()
            .withDescription( "delete records listed in the log file" )
            .create( "del" );

        options.addOption( delOption );
        
        String context = null;     
        String ifrmt = DEFAULT_RECORD_FORMAT;
        
        try{
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse( options, args);

            if( cmd.hasOption("help") ){
                HelpFormatter formatter = new HelpFormatter();
                formatter.setWidth( 127 );
                formatter.printHelp( "xpsq-index-builder [options]", options );
                System.exit(0);
            }
            
            if( cmd.hasOption( "clr" ) ){
                clr = true;
            } 
            
            if( cmd.hasOption( "ctx" ) ){
                context = cmd.getOptionValue( "ctx" );
            } 
        
            if( cmd.hasOption( "fmt" ) ){
                ifrmt = cmd.getOptionValue( "fmt" );
            }

            if( cmd.hasOption( "dir" ) ){
                idir = cmd.getOptionValue( "dir" );
            }
            
            if( cmd.hasOption( "f" ) ){
                ifile = cmd.getOptionValue( "f" );
            }

            if( cmd.hasOption( "del" ) ){
                delfile = cmd.getOptionValue( "del" );
            }
            
            if( cmd.hasOption( "h" ) ){
                host = cmd.getOptionValue( "h" );
                System.out.println( "Host: " + host );
            }
            
            if( cmd.hasOption( "z" ) ){
                zip = true;
            }

            if( cmd.hasOption( "gz" ) ){ // gz has precedence over z
                gzip = true;
                zip = false;
            }

            if( cmd.hasOption( "l" ) ){
                logId = true;
            }
            
            if( cmd.hasOption("r") ){
                desc = true;
            }

            if( cmd.hasOption("tb") ){
                try{
                    String sBtc =  cmd.getOptionValue( "tb" );
                    int btc = Integer.parseInt( sBtc ); 
                    btCount = btc;
                }catch( NumberFormatException nfx ){
                    System.out.println( "TB format Error. Using default" );
                }
            }

            if( cmd.hasOption("ts") ){
                try{
                    String sStc =  cmd.getOptionValue( "ts" );
                    int stc = Integer.parseInt( sStc ); 
                    stCount = stc;
                }catch( NumberFormatException nfx ){
                    System.out.println( "TS format Error. Using default" );
                }
            }
            
            if( cmd.hasOption("xp") ){

                pax = new HashMap<String,String>();
                try{
                    Map paxMap =  cmd.getOptionProperties("xp");
                    
                    for( Iterator i = paxMap.keySet().iterator(); i.hasNext();){
                        
                        String k = (String) i.next();
                        System.out.println(" XP: " + k + "=" + paxMap.get(k) );  
                        pax.put( k, (String) paxMap.get(k) );                        
                    }
                }catch( Exception paxx ){
                    System.out.println( "pxslt fornat error" );
                }
            }
            
        } catch( Exception exp ) {
            System.out.println( "BuildIndex: Options parsing failed. " +
                                "Reason: " + exp.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(127);
            formatter.printHelp( "BuildIndex", options );
            System.exit(1);
        }

        IndexBuilder ibuilder = null;
        
        System.out.println( "Host: " + host );
        System.out.println( "PAX: " + pax );
        
        if( context != null ){
            System.out.println( "Context: " + context );
            ibuilder = new IndexBuilder( context, host, btCount, stCount, 
                                         ifrmt, zip, gzip, logId, pax );
        }else{
            
            System.out.println( "Context(default): " + DEFAULT_CONTEXT );
            try{
                InputStream ctxStream = buildindex.class
                    .getResourceAsStream( DEFAULT_CONTEXT );
                
                ibuilder = new IndexBuilder( ctxStream, host, btCount, stCount, 
                                             ifrmt, zip, gzip, logId, pax );
            } catch( Exception ex){
                ex.printStackTrace();
            }            
        }
        
        if( clr ){
            System.out.println( "Clear index" );
            ibuilder.clear();
        }

        if( ibuilder != null && delfile != null ){
            System.out.println( "Delete records: identifier file=" + delfile );
            
            List<String> idList = readLogFile( delfile );
            
            if( idList != null && idList.size() > 0){
                ibuilder.deleteRecords( idList );
            }
            return;
        }
        
        if( ibuilder != null && ifile != null ){  
            ibuilder.processFile( ifile );
            ibuilder.start();
        } else { // file option has a precedence over directory
            if( ibuilder != null && idir != null ){
                ibuilder.processDirectory( idir, desc );
                ibuilder.start();
            }
        }
    }
    
    private static List<String> readLogFile( String fileName ){

        List<String> idList = new ArrayList<String>();

        try{
            FileReader frd = new FileReader( new File(fileName) );
            BufferedReader bfrd = new BufferedReader( frd ); 
            String ln = null;
            while( (ln = bfrd.readLine()) != null ){
                ln = ln.replaceAll("\\s", "");
                if( !ln.startsWith("#") ){
                idList.add( ln );
                }
            }
            bfrd.close();
        } catch( Exception ex){
            System.out.println( "Error reading record identifier file" );
            ex.printStackTrace();
        }
        return idList;
    }
}

