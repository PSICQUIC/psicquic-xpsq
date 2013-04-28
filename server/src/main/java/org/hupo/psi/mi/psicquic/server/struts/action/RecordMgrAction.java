package org.hupo.psi.mi.psicquic.server.struts.action;

import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;

import com.opensymphony.xwork2.ActionSupport;

import org.apache.struts2.util.ServletContextAware;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hupo.psi.mi.psicquic.util.JsonContext;

import org.hupo.psi.mi.psicquic.server.*;
import org.hupo.psi.mi.psicquic.server.store.*;
import org.hupo.psi.mi.psicquic.server.store.derby.*;

public class RecordMgrAction extends ActionSupport
    implements ServletContextAware{
    
    PsqContext psqContext;
    
    //--------------------------------------------------------------------------
    
    public void setPsqContext( PsqContext context ){
        psqContext = context;
    }
    
    //--------------------------------------------------------------------------

    private void initialize() {
        initialize( false );
    }

    //--------------------------------------------------------------------------

    private void initialize( boolean force) {
        
        if ( psqContext.getJsonConfig() == null || force ) {

            Log log = LogFactory.getLog( this.getClass() );
            log.info( " initilizing psq context" );

            /*
            String jsonPath =
                (String) getPsqContext().getConfig().get( "json-config" );
            log.info( "JsonPsqDef=" + jsonPath );

            if ( jsonPath != null && jsonPath.length() > 0 ) {

                String cpath = jsonPath.replaceAll("^\\s+","" );
                cpath = jsonPath.replaceAll("\\s+$","" );
                
                try {
                    InputStream is =
                        servletContext.getResourceAsStream( cpath );
                    getPsqContext().readJsonConfigDef( is );

                } catch ( Exception e ){
                    log.info( "JsonConfig reading error" );
                }
            }
            */
        }
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    String op="";
    String pid="";

    String vt="";
    String vv="";

    String mitab=""; // ???
    
    public void setOp( String op ){
        this.op = op;
    }

    List<String> idLst = null;
    
    public void setId( List<String> lst ){
        this.idLst = lst;
    }

    public String getPid(){
        return pid;
    }
    
    public void setPid( String pid){
        this.pid=pid;
    }

    public void setMitab( String mitab){
        this.mitab=mitab;
    }
    
    public String getMitab(){
        return mitab;
    }

    public void setVt( String viewType ){
        vt = viewType;
    }
    
    public String getVt(){
        return vt;
    }
    
    public void setVv( String viewValue ){
        vv = viewValue;
    }
    
    public String getVv(){
        return vv;
    }
    
    //--------------------------------------------------------------------------
    /*
    RecordDao  rdao = null;

    public void setRecordDao( RecordDao dao ){
        this.rdao= dao;
    }
    */
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public String execute() throws Exception {

        Log log = LogFactory.getLog( this.getClass() );
        log.info( "OP=" + op + " ID=" + pid + " VT=" + vt );
        log.debug( "STORE=" + psqContext.getActiveStore() );
        
        initialize();
        
        if( op != null && op.equals( "add" ) ){
            if( pid != null && vt != null && vv != null ){
                psqContext.getActiveStore().addRecord( pid, vv, vt );
                vv="";
            }
            return ActionSupport.SUCCESS;
        }
        
        if( op != null && op.equals( "get" ) ){
            if( pid != null && vt!= null ){
                vv = psqContext.getActiveStore().getRecord( pid, vt );
            }
            return ActionSupport.SUCCESS;
        }

        if( op != null && op.equals( "clear" ) ){
            psqContext.getActiveStore().clearLocal();
        }

        if( op != null && op.equals( "delete" ) ){
            
            log.debug( "STORE=" + psqContext.getActiveStore() );
            log.debug( "IDL=" + idLst );

            if( idLst != null && idLst.size() >  0 ){
                psqContext.getActiveStore().deleteRecords( idLst );
            }
        }
        
        return ActionSupport.SUCCESS;
    }

    //--------------------------------------------------------------------------
    // ServletContextAware interface implementation
    //---------------------------------------------

    private ServletContext servletContext;

    public void setServletContext( ServletContext context){
        this.servletContext = context;
    }

    public ServletContext getServletContext(){
        return servletContext;
    }

}
