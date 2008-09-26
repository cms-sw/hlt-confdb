<html>
<head>

<script type="text/javascript" src="../js/graph/diagram.js"></script>
<script type="text/javascript" src="../js/graph/Histogram.js"></script>
<script type='text/javascript'>

if (document.layers) 
  document.writeln("<script type=\"text/javascript\" src=\"../js/graph/diagram_nav.js\"><\/script>");
else 
  document.writeln("<script type=\"text/javascript\" src=\"../js/graph/diagram_dom.js\"><\/script>");

<%
String text = request.getParameter( "text" );
if ( text == null )
	text = "";
String height = request.getParameter( "height" );
if ( height == null )
	height = "200";
String width = request.getParameter( "width" );
if ( width == null )
	width = "400";
String max = request.getParameter( "max" );
if ( max == null )
	max = "400";
String dt = request.getParameter( "dt" );
if ( dt == null )
	dt = "20 * 60";

String values = request.getParameter( "values" );
String[] numbers = null;
if ( values != null )
	numbers = values.split( "_" );

String colorString = request.getParameter( "colors" );
String[] colors = null;
if ( colorString != null )
	colors = colorString.split( "_" );

out.println( "var text = \"" + text + "\";"); 
out.println( "var max = " + max + ";"); 
out.println( "var width = " + width + ";"); 
out.println( "var height = " + height + ";"); 
out.println( "var DT = " + dt + " * 1000;" ); 
if ( numbers == null )
	out.println( "var dataDim = 1;");
else
{
	out.println( "var dataDim = " + numbers.length + ";");
	out.print( "var firstData = [ " );
	for ( String number : numbers )
		out.print( number + ", " );
	out.println( "0 ];" );
}

if ( colorString == null )
	out.println( "var histColor = new Array( \"blue\", \"orange\" );" );
else
{
	out.print( "var histColor = [ " );
	for ( String color : colors )
		out.print( "\""+ color + "\"," );
	out.println( "\"\" ];" );		
}

%>

//Histograms
var hists = 0;


function init()
{
  var now = new Date();
  var then = new Date();
  then.setMilliseconds(now.getMilliseconds() - hists.dt * (1.0-hists.scrollRatio))
  // to be filled with the following data
  // String triggerName, Date startTime, Date endTime, int numOfPoints
  //AjaxInfo.getHistoryBean( page, "rateIn", then, now, 270, prefillHltIn);
  //AjaxInfo.getHistoryBean( page, "rateOut", then, now, 270, prefillHltOut);
  //window.setTimeout("update()", 200);
  if ( parent )
    parent.appendData = appendData;
}

function prefillHltIn(rateHistory)
{
  hists.PreFill(rateHistory.historyArray, 0);
}

function prefillHltOut(rateHistory)
{
  hists.PreFill(rateHistory.historyArray, 1);
}


  
function appendData( allRates )
{
  hists.UpdateRate( allRates );
}


</script>

<title>graphIFrame</title>

</head>

<body onLoad="init()">
<div style="position:relative; top:0px;">
<SCRIPT Language="JavaScript">
	document.open();
	hists = new Histogram( dataDim );
	hists.startTime = (new Date()).getTime();
	hists.SetFrame(60, 30, width, height );
	hists.SetBorder(DT, max);
	hists.SetXScale(2);
	hists.SetText( "", "", text );
	hists.SetGridColor("#808080", "#CCCCCC");
	hists.Draw("#DDDDDD", "#000000", false);
	document.close();
</SCRIPT>
</div>
</body>
</html>

