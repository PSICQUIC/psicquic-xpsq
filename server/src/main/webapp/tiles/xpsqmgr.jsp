<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="t" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="mode == 'services'">
 <iframe id="service-frame" src="service"></iframe>
</s:if>
<s:elseif test="mode == 'index'">
  <s:if test="op.status != null">
    <div id="status"></div>
    <script>
      YAHOO.util.Event.addListener( window, "load",
        YAHOO.xpsq.status.init({"mode":"index","anchor":"status"}) );
    </script> 
  </s:if>
  <s:else>
    <iframe id="index-frame" src="solr/admin"></iframe>
  </s:else>
</s:elseif>
<s:elseif test="mode == 'store'">
 <s:if test="op.status != null">
   <div id="status"></div> 
   <script>
     YAHOO.util.Event.addListener( window, "load", 
       YAHOO.xpsq.status.init({"mode":"store","anchor":"status"}) );
   </script>
  </s:if>
</s:elseif>
<s:else>
 <h1>XPSQ: Extensible PSICQUC Server</h1>
 <pre style="border: 1px solid #444444; background-color: #eeeeee; padding: 2em;">
<t:insertDefinition name="readme"/>
 </pre>
 <br/><br/>
</s:else>
<br/><br/><br/>

