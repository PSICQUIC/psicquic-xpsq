package org.hupo.psi.mi.psicquic.server.store.hibernate;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 #  HibernateRecordStore: Hibernate-backed RecordStore implementation
 #
 #
 #=========================================================================== */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.*;

import org.hupo.psi.mi.psicquic.util.JsonContext;
import org.hupo.psi.mi.psicquic.server.*;
import org.hupo.psi.mi.psicquic.server.store.*;

//------------------------------------------------------------------------------

public class HibernateRecordStore extends RdbRecordStore {

    Map<String,Map<String,PsqTransformer>> inTransformerMap = null;

    String rmgrURL = null;
    //String host = null;

    SessionFactory sessionFactory = null;
    
    public HibernateRecordStore(){
        Log log = LogFactory.getLog( this.getClass() );
        log.debug("HibernateRecordStore() called");
    }

    public HibernateRecordStore( PsqContext context ){

        Log log = LogFactory.getLog( this.getClass() );
        log.debug("HibernateRecordStore(PsqContext) called");
        setPsqContext( context );
       
    }

    public HibernateRecordStore( PsqContext context, String host ){
        
        Log log = LogFactory.getLog( this.getClass() );
        log.debug("HibernateRecordStore(PsqContext,String) called");
        setPsqContext( context );
        
        if( host != null ){
            this.host = host;
        }           
    }
    
    public void setSessionFactory( SessionFactory factory ){
        this.sessionFactory = factory;
    }
    
    public void initialize(){
        Log log = LogFactory.getLog( this.getClass() );
        log.debug("initialize() called");

    }
    
    private void connect(){
        
        Log log = LogFactory.getLog( this.getClass() );
        
        try{
            Session session = sessionFactory.getCurrentSession();
            Transaction tx = session.beginTransaction();

            Long count = null;

            try {
                Query query 
                    = session.createQuery( "select count(pr)"
                                           + " from PsicquicRecord pr" );
                count  = (Long) query.uniqueResult();
            } catch ( HibernateException e ) {
                // log error ?
                e.printStackTrace();
            } finally {
                tx.commit();
            }
            
            if( count != null){
                log.info( "   record count= " + count );
            }
        } catch( Exception ex ){
            log.info( "   connection failed" ); 
        }
        
    }

    //--------------------------------------------------------------------------

    private void create(){
        // no-op
    }
    
    public void shutdown(){
        // no-op
    }
    
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public void addRecord( String rid, String record, String format ){

        Log log = LogFactory.getLog( this.getClass() );
        log.debug("rid=" + rid + " format=" + format 
                  + " record(length)=" + record.length() );
        PsicquicRecord pr = new PsicquicRecord( rid, format, record);
     
        Session session = null;
        Transaction tx = null;

        try{
            session = sessionFactory.getCurrentSession();
            tx = session.beginTransaction();
            
            session.saveOrUpdate( pr );
            
            tx.commit();
            
        } catch( Exception ex ){
            if (tx != null) tx.rollback();
            log.info( "   addRecord failed" );
            ex.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------

    public void deleteRecord( String rid, String format ){
        Log log = LogFactory.getLog( this.getClass() );

        if( rid == null || format == null ) return;
        
        Session session = null;
        Transaction tx = null;

        try{
            session = sessionFactory.getCurrentSession();
            tx = session.beginTransaction();

            String hql = "delete from PsicquicRecord where"
                + " rid = :rid and format = :fmt";

            session.createQuery( hql ).setParameter("rid", rid )
                .setParameter("fmt", format).executeUpdate();
            
            tx.commit();

        } catch( Exception ex ){
            if (tx != null) tx.rollback();
            log.info( "   deleteRecord failed" );
            ex.printStackTrace();
        }     
    }

    //--------------------------------------------------------------------------
    
    public void deleteRecords( List<String> idList, String format ){

        Log log = LogFactory.getLog( this.getClass() );
        if( idList == null || idList.size() == 0 || format == null ) return;
        
        Session session = null;
        Transaction tx = null;
        
        try{
            session = sessionFactory.getCurrentSession();
            tx = session.beginTransaction();
            
            String hql = "delete from PsicquicRecord where" 
                + " rid in( :rid) and format = :fmt";
            
            session.createQuery( hql ).setParameter("rid", idList)
                .setParameter("fmt", format).executeUpdate();
            
            tx.commit();
            
        } catch( Exception ex ){
            if (tx != null) tx.rollback();
            log.info( "   deleteRecord failed" );
            ex.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------

    public void deleteRecords( List<String> idList ){
        
        Log log = LogFactory.getLog( this.getClass() );
        if( idList == null || idList.size() == 0 ) return;
        
        Session session = null;
        Transaction tx = null;
        
        try{
            session = sessionFactory.getCurrentSession();
            tx = session.beginTransaction();
            
            String hql = "delete from PsicquicRecord  where rid in( :rid)";
            
            session.createQuery( hql ).setParameter("rid", idList)
                .executeUpdate();
            
            tx.commit();
            
        } catch( Exception ex ){
            if (tx != null) tx.rollback();
            log.info( "   deleteRecord failed" );
            ex.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------

    public void updateRecord( String rid, String record, String fmt ){
        Log log = LogFactory.getLog( this.getClass() );
        log.warn( "updateRecord: not implemented" );
    }

    //--------------------------------------------------------------------------

    public String getRecord( String rid, String format ){
      
        String record = "";
        
	Log log = LogFactory.getLog( this.getClass() );
        
        Session session = null;
        
        try{
            session = sessionFactory.getCurrentSession();
            
            Query query
                = session.createQuery( "select pr.value" +
                                       " from PsicquicRecord pr" +
                                       "  where pr.rid = :rid" +
                                       "   and pr.format = :format" );
         
            query.setParameter( "rid", rid );
            query.setParameter( "format", format );
            
            record = (String) query.uniqueResult();
            
        } catch ( Exception e ) {
            // log error ?
            e.printStackTrace();
        } finally {
            // ???
        }
        return record;
    }

    //--------------------------------------------------------------------------
    
    public List<String> getRecordList( List<String> ridl, String format ){
        
        List<String> recordList = new ArrayList<String>();
        
        Session session = null;
        
        try{
            session = sessionFactory.getCurrentSession();
            Query query
                = session.createQuery( "select pr.value" + 
                                       " from PsicquicRecord pr" +
                                       "  where pr.rid in (:ridl)" +
                                       "   and pr.format = :format" );
            
            query.setParameterList("ridl", ridl );
            query.setParameter("format", format );

            recordList = (List<String>) query.list();
            
        } catch ( Exception e ) {
            // log error ?
            e.printStackTrace();
        } finally {
            // ???
        }
        
        return recordList;
    }
    
    //--------------------------------------------------------------------------
    
    public void clearLocal(){
        
        Log log = LogFactory.getLog( this.getClass() );
      
        Session session = null;
        Transaction tx = null;
  
        try{
            session = sessionFactory.getCurrentSession();
            tx = session.beginTransaction();

            Query query = session.createQuery("delete from PsicquicRecord");
            query.executeUpdate();
            tx.commit();
            log.info( "record table truncated" );
            
        } catch( Exception ex ){
            if( tx != null ) tx.rollback();
            log.info( "   clearLocal failed" );
            ex.printStackTrace();
        }   
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public Map getMeta(){

        Map meta = new HashMap();
        meta.put("resource-class","store");
        meta.put("resource-type","hibernate");

        Log log = LogFactory.getLog( this.getClass() );

        Session session = null;
        Transaction tx = null;
  
        try{
            session = sessionFactory.getCurrentSession();
            tx = session.beginTransaction();
            
            Map viewCount = new HashMap();

            Query query
                = session.createQuery( "select pr.format, count(pr)" +
                                       " from PsicquicRecord pr" +
                                       " group by pr.format" );
            
            long all = 0;
            for( Iterator<Object[]> qi = (Iterator<Object[]>) query.iterate();
                 qi.hasNext(); ){
                
                Object[] cq = qi.next();
                
                String vt = (String) cq[0];
                long vc = (Long) cq[1];
                all += vc;
                viewCount.put( vt, vc );
            }
            viewCount.put( "all", all);
            meta.put( "counts", viewCount );
        } catch( Exception ex ){
            ex.printStackTrace();
        }   
        return meta;
    }

}
