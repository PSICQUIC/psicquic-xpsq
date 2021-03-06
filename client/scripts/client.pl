#!/usr/bin/perl
use SOAP::Lite;
use XML::XPath;
use XML::XPath::XMLParser;

my $URL = "http://127.0.0.1:8080/xpsq/service/soap/current";
my $PURL= "http://%%%:8080/xpsq/service/soap/current";

my $ip="";
my $op="getByQuery";
my $rtp="psi-mi/tab27";
my $query="*:*";

my $firstRec = 0;
my $maxRec = 10;

for( my $i=0; $i < @ARGV; $i++ ) {

    if( $ARGV[$i]=~/IP=(.+)/ ) {
        $ip=$1;
        $URL=$PURL;
        $URL=~s/%%%/$ip/;
    }

    if( $ARGV[$i]=~/Q=(.+)/ ) {
        $query=$1;
    }

    if( $ARGV[$i]=~/FREC=(.+)/ ) {
        $firstRec=$1;
    }

    if( $ARGV[$i]=~/MREC=(.+)/ ) {
        $maxRec=$1;
    }
    if( $ARGV[$i]=~/RTP=(.+)/ ) {
        $rtp=$1;
    }
}

print "URL: $URL\n";
print "OP: $op\n";
print "Q: $query\n";

my $som="";
my $rns ="";    

if($op ne "" ) {

    $rns ="http://psi.hupo.org/mi/psicquic";

    if( $op eq "getByQuery" ) {            
    
        my $q= "<query>$query</query>";
       
	my $rqinfo = 
            "<infoRequest>".
	    " <resultType>$rtp</resultType>".
	    " <firstResult>$firstRec</firstResult>".
	    " <blockSize>$maxRec</blockSize>".
	    "</infoRequest>";
	
        print "::\n".$rqinfo."\n::\n";

        $som=SOAP::Lite->uri($URL)
            ->proxy($URL)
            ->default_ns($rns)
            ->outputxml('true')
            -> on_action(sub { sprintf 'getByQuery', shift })
            ->getByQuery( SOAP::Data->type( 'xml' => $q), 
                          SOAP::Data->type( 'xml' => $rqinfo) );
    }
    
}

print "SOM: ",$som,"\n";

my $xp_som = XML::XPath->new(xml=>$som);
$xp_som->set_namespace("rns",$rns);

my $nodeset;

#$xp_som->set_namespace("rns","http://dip.doe-mbi.ucla.edu/services/dxf14");

#$nodeset = $xp_som->find('//rns:dataset'); # find all paragraphs
#foreach my $node ($nodeset->get_nodelist) {
#    print "FOUND DXF\n\n",
#    XML::XPath::XMLParser::as_string($node),"\n";
#}


