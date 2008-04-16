<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Trigger Summary</title>


<link rel="stylesheet" type="text/css" href="../js/yui/reset-fonts-grids/reset-fonts-grids.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/base/base-min.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/container/assets/skins/sam/container.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/datatable/assets/skins/sam/datatable.css">
<script type="text/javascript" src="../js/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../js/yui/dragdrop/dragdrop-min.js"></script>
<script type="text/javascript" src="../js/yui/element/element-beta-min.js"></script>
<script type="text/javascript" src="../js/yui/datasource/datasource-beta-min.js"></script>
<script type="text/javascript" src="../js/yui/datatable/datatable-beta-min.js"></script>
<script type="text/javascript" src="../js/yui/json/json-min.js"></script>
<script type="text/javascript" src="../js/yui/connection/connection-min.js"></script>
<script type="text/javascript" src="../js/yui/container/container.js"></script>
<script type="text/javascript" src="../js/dwr2JSON.js"></script>
<script type="text/javascript" src="../js/AjaxInfo.js"></script>

<style type="text/css">

html, body { background:#edf5ff }

.yui-module { padding:0px; margin-left:5px; margin-right:5px; margin-bottom:0px; display:none; }
.yui-gd { margin-bottom:0px; }
#doc3 { margin-bottom:0px; margin-top:5px; }
#mainLeft { background:#edf5ff; border: 0px solid #B6CDE1; margin:0px; padding:0px }
.headerDiv { margin:0px; padding:0.4em; background:white; border: 1px solid #B6CDE1; border-bottom:0px; }
#info { background:white; border: 1px solid #B6CDE1; border-top:0px; }

</style>

<script type="text/javascript">
// Patch for width and/or minWidth Column values bug in non-scrolling DataTables
(function(){var B=YAHOO.widget.DataTable,A=YAHOO.util.Dom;B.prototype._setColumnWidth=function(I,D,J){I=this.getColumn(I);if(I){J=J||"hidden";if(!B._bStylesheetFallback){var N;if(!B._elStylesheet){N=document.createElement("style");N.type="text/css";B._elStylesheet=document.getElementsByTagName("head").item(0).appendChild(N)}if(B._elStylesheet){N=B._elStylesheet;var M=".yui-dt-col-"+I.getId();var K=B._oStylesheetRules[M];if(!K){if(N.styleSheet&&N.styleSheet.addRule){N.styleSheet.addRule(M,"overflow:"+J);N.styleSheet.addRule(M,"width:"+D);K=N.styleSheet.rules[N.styleSheet.rules.length-1]}else{if(N.sheet&&N.sheet.insertRule){N.sheet.insertRule(M+" {overflow:"+J+";width:"+D+";}",N.sheet.cssRules.length);K=N.sheet.cssRules[N.sheet.cssRules.length-1]}else{B._bStylesheetFallback=true}}B._oStylesheetRules[M]=K}else{K.style.overflow=J;K.style.width=D}return }B._bStylesheetFallback=true}if(B._bStylesheetFallback){if(D=="auto"){D=""}var C=this._elTbody?this._elTbody.rows.length:0;if(!this._aFallbackColResizer[C]){var H,G,F;var L=["var colIdx=oColumn.getKeyIndex();","oColumn.getThEl().firstChild.style.width="];for(H=C-1,G=2;H>=0;--H){L[G++]="this._elTbody.rows[";L[G++]=H;L[G++]="].cells[colIdx].firstChild.style.width=";L[G++]="this._elTbody.rows[";L[G++]=H;L[G++]="].cells[colIdx].style.width="}L[G]="sWidth;";L[G+1]="oColumn.getThEl().firstChild.style.overflow=";for(H=C-1,F=G+2;H>=0;--H){L[F++]="this._elTbody.rows[";L[F++]=H;L[F++]="].cells[colIdx].firstChild.style.overflow=";L[F++]="this._elTbody.rows[";L[F++]=H;L[F++]="].cells[colIdx].style.overflow="}L[F]="sOverflow;";this._aFallbackColResizer[C]=new Function("oColumn","sWidth","sOverflow",L.join(""))}var E=this._aFallbackColResizer[C];if(E){E.call(this,I,D,J);return }}}else{}};B.prototype._syncColWidths=function(){var J=this.get("scrollable");if(this._elTbody.rows.length>0){var M=this._oColumnSet.keys,C=this.getFirstTrEl();if(M&&C&&(C.cells.length===M.length)){var O=false;if(J&&(YAHOO.env.ua.gecko||YAHOO.env.ua.opera)){O=true;if(this.get("width")){this._elTheadContainer.style.width="";this._elTbodyContainer.style.width=""}else{this._elContainer.style.width=""}}var I,L,F=C.cells.length;for(I=0;I<F;I++){L=M[I];if(!L.width){this._setColumnWidth(L,"auto","visible")}}for(I=0;I<F;I++){L=M[I];var H=0;var E="hidden";if(!L.width){var G=L.getThEl();var K=C.cells[I];if(J){var N=(G.offsetWidth>K.offsetWidth)?G.firstChild:K.firstChild;if(G.offsetWidth!==K.offsetWidth||N.offsetWidth<L.minWidth){H=Math.max(0,L.minWidth,N.offsetWidth-(parseInt(A.getStyle(N,"paddingLeft"),10)|0)-(parseInt(A.getStyle(N,"paddingRight"),10)|0))}}else{if(K.offsetWidth<L.minWidth){E=K.offsetWidth?"visible":"hidden";H=Math.max(0,L.minWidth,K.offsetWidth-(parseInt(A.getStyle(K,"paddingLeft"),10)|0)-(parseInt(A.getStyle(K,"paddingRight"),10)|0))}}}else{H=L.width}if(L.hidden){L._nLastWidth=H;this._setColumnWidth(L,"1px","hidden")}else{if(H){this._setColumnWidth(L,H+"px",E)}}}if(O){var D=this.get("width");this._elTheadContainer.style.width=D;this._elTbodyContainer.style.width=D}}}this._syncScrollPadding()}})();
// Patch for initial hidden Columns bug
(function(){var A=YAHOO.util,B=YAHOO.env.ua,E=A.Event,C=A.Dom,D=YAHOO.widget.DataTable;D.prototype._initTheadEls=function(){var X,V,T,Z,I,M;if(!this._elThead){Z=this._elThead=document.createElement("thead");I=this._elA11yThead=document.createElement("thead");M=[Z,I];E.addListener(Z,"focus",this._onTheadFocus,this);E.addListener(Z,"keydown",this._onTheadKeydown,this);E.addListener(Z,"mouseover",this._onTableMouseover,this);E.addListener(Z,"mouseout",this._onTableMouseout,this);E.addListener(Z,"mousedown",this._onTableMousedown,this);E.addListener(Z,"mouseup",this._onTableMouseup,this);E.addListener(Z,"click",this._onTheadClick,this);E.addListener(Z.parentNode,"dblclick",this._onTableDblclick,this);this._elTheadContainer.firstChild.appendChild(I);this._elTbodyContainer.firstChild.appendChild(Z)}else{Z=this._elThead;I=this._elA11yThead;M=[Z,I];for(X=0;X<M.length;X++){for(V=M[X].rows.length-1;V>-1;V--){E.purgeElement(M[X].rows[V],true);M[X].removeChild(M[X].rows[V])}}}var N,d=this._oColumnSet;var H=d.tree;var L,P;for(T=0;T<M.length;T++){for(X=0;X<H.length;X++){var U=M[T].appendChild(document.createElement("tr"));P=(T===1)?this._sId+"-hdrow"+X+"-a11y":this._sId+"-hdrow"+X;U.id=P;for(V=0;V<H[X].length;V++){N=H[X][V];L=U.appendChild(document.createElement("th"));if(T===0){N._elTh=L}P=(T===1)?this._sId+"-th"+N.getId()+"-a11y":this._sId+"-th"+N.getId();L.id=P;L.yuiCellIndex=V;this._initThEl(L,N,X,V,(T===1))}if(T===0){if(X===0){C.addClass(U,D.CLASS_FIRST)}if(X===(H.length-1)){C.addClass(U,D.CLASS_LAST)}}}if(T===0){var R=d.headers[0];var J=d.headers[d.headers.length-1];for(X=0;X<R.length;X++){C.addClass(C.get(this._sId+"-th"+R[X]),D.CLASS_FIRST)}for(X=0;X<J.length;X++){C.addClass(C.get(this._sId+"-th"+J[X]),D.CLASS_LAST)}var Q=(A.DD)?true:false;var c=false;if(this._oConfigs.draggableColumns){for(X=0;X<this._oColumnSet.tree[0].length;X++){N=this._oColumnSet.tree[0][X];if(Q){L=N.getThEl();C.addClass(L,D.CLASS_DRAGGABLE);var O=D._initColumnDragTargetEl();N._dd=new YAHOO.widget.ColumnDD(this,N,L,O)}else{c=true}}}for(X=0;X<this._oColumnSet.keys.length;X++){N=this._oColumnSet.keys[X];if(N.resizeable){if(Q){L=N.getThEl();C.addClass(L,D.CLASS_RESIZEABLE);var G=L.firstChild;var F=G.appendChild(document.createElement("div"));F.id=this._sId+"-colresizer"+N.getId();N._elResizer=F;C.addClass(F,D.CLASS_RESIZER);var e=D._initColumnResizerProxyEl();N._ddResizer=new YAHOO.util.ColumnResizer(this,N,L,F.id,e);var W=function(f){E.stopPropagation(f)};E.addListener(F,"click",W)}else{c=true}}}if(c){}}else{}}for(var a=0,Y=this._oColumnSet.keys.length;a<Y;a++){if(this._oColumnSet.keys[a].hidden){var b=this._oColumnSet.keys[a];var S=b.getThEl();b._nLastWidth=S.offsetWidth-(parseInt(C.getStyle(S,"paddingLeft"),10)|0)-(parseInt(C.getStyle(S,"paddingRight"),10)|0);this._setColumnWidth(b.getKeyIndex(),"1px")}}if(B.webkit&&B.webkit<420){var K=this;setTimeout(function(){K._elThead.style.display=""},0);this._elThead.style.display="none"}}})();

var configFrameUrl;
var configKey;
var dbIndex;
var myDataSource;
var myDataTable;
var myCallback;	
	
function init() 
{
  	//document.getElementById( "mainRight" ).style.visibility = 'collapse';


	//handler for expanding all nodes
	YAHOO.util.Event.on("expand", "click", function(e) {
			treeFrame.tree.expandAll();
			YAHOO.util.Event.preventDefault(e);
		});
		
	//handler for collapsing all nodes
	YAHOO.util.Event.on("collapse", "click", function(e) {
			treeFrame.tree.collapseAll();
			YAHOO.util.Event.preventDefault(e);
		});
		
    var height = 700;
    if ( parent.tabHeight )
    	height = parent.tabHeight - 50;
    document.getElementById( "treeFrame" ).height = height;
    
    prepareTable( height );
}
	
	
function labelClicked( node )
{
  if ( !node.data.key )
  	return;

  document.getElementById( "mainRight" ).style.visibility = 'visible';

  configKey = node.data.key;
  dbIndex = node.data.dbIndex;
  configFrameUrl = "convert2Html.jsp?configKey=" + node.data.key + "&dbIndex=" + node.data.dbIndex + "&bgcolor=FFF5DF"; 
  var height = YAHOO.util.Dom.getViewportHeight() - 75;
  
  var header = "<b>" + node.data.fullName + "</b>";
  document.getElementById( "header" ).innerHTML = header;

  var fileName = node.data.name.replace( '//s/g', '_' ) + "_V" + node.data.version;
  header += '<span style="position:absolute; right:20px;">download <a href="' + fileName + '.cfg?configId='+ node.data.key + '&dbIndex=' + node.data.dbIndex + '">cfg</a>';
  header += '  <a href="' + fileName + '.py?format=python&configId='+ node.data.key + '&dbIndex=' + node.data.dbIndex + '">py</a></span>';
    
  myDataSource.sendRequest( "configName=" + node.data.fullName, myCallback );
    
  treeReady();
}
  
function iframeReady()
{
  //loadingModule.hide();
}
	
function treeReady()
{
  document.getElementById( "info" ).innerHTML = "";
  document.getElementById( "info" ).style.border = '0px';
}
	
function showSummary( summary )
{
	var str = '';
	for ( row in summary.rows )
	{
		for ( prop in row )
		{
			str += prop + ' value :' + row[prop] + '\n';
		} 
	} 
	alert( str );
}
	
function prepareTable( height ) 
{
        var myColumnDefs = [
            {key:"trigger", sortable:true },
            {key:"l1Seed", sortable:true },
            {key:"prescale", formatter:YAHOO.widget.DataTable.formatNumber },
            {label:"Filter1",
              children: [
                {key: "['filters'][0]['name']", label:"Name",sortable:true, resizeable:true},
                {key:"['filters'][0]['PTmin']", label:"pTmin", sortable:true },
                {key:"['filters'][0]['etaMax']", label:"etaMax", sortable:true }
              ]
            },
            {label:"FilterN",
              children: [
                {key: "['filters'][1]['name']", label:"Name",sortable:true, resizeable:true},
                {key:"['filters'][1]['PTmin']", label:"pTmin", sortable:true },
                {key:"['filters'][1]['etaMax']", label:"etaMax", sortable:true }
              ]
            }
        ];

        myDataSource = new YAHOO.util.DataSource( "http://zenulf2:8080/browser/summary/getSummary.jsp?" );
        myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
        myDataSource.responseSchema = {
	        resultsList: "rows",
            fields: [ "trigger", "l1Seed", "prescale", 
            	{ key: "['filters'][0]['name']" }, 
            	{ key: "['filters'][0]['PTmin']" },
            	{ key: "['filters'][0]['etaMax']" }, 
            	{ key: "['filters'][1]['name']" }, 
            	{ key: "['filters'][1]['PTmin']" },
            	{ key: "['filters'][1]['etaMax']" } 
            ]
        };

        /*
		myDataSource.doBeforeParseData = function  (oRequest, oResponse) {
				alert( "doBefore...:  " + oRequest +  " response = " + oResponse );
				return oResponse; 
			};
		*/

        myDataTable = new YAHOO.widget.DataTable("summary", myColumnDefs, this.myDataSource, {initialLoad:false, scrollable:true, height:"" + height + "px" } );
        
        myCallback = { 
		    success: myDataTable.onDataReturnInitializeTable,
		    /*
		    success: function( oRequest, oResponse, oPayload ) { 
		    	alert( oResponse.results.length + " rows\n" 
		    			+ writeProps( oResponse.results[0] ) ); 
		    	myDataTable.onDataReturnInitializeTable( oRequest, oResponse, oPayload );
			},
			*/
			failure: function( oRequest, oResponse, oPayload ) { 
				alert( "failure:  " + oRequest +  " response = " + writeProps( oResponse ) ); 
			}, 
			scope: myDataTable, 
			//argument: 'not used'   // goes to oPayload 
		} 
}

function writeProps( object )
{
	var str = '';
	for ( prop in object )
	{
		str += prop + ' value :' + object[prop] + '\n';
	}		 
	return str;
}
	
//When the DOM is done loading, we can initialize our TreeView
//instance:
YAHOO.util.Event.onContentReady( "doc3", init );
	
</script>

</head>
<body class="yui-skin-sam" style="background:#edf5ff;">

<%
  String treeUrl = "treeFrame.jsp?db=" + request.getParameter( "db" );
%>

<div id="doc3" class="yui-gd">
  <div class="yui-u first">
    <div id="mainLeft"> 
      <div class="headerDiv">
            <a id="expand" href="#">Expand all</a>
            <a id="collapse" href="#">Collapse all</a>
      </div>
      <div id="info"><img src="../img/loading.gif"></div>
	  <iframe name="treeFrame" id="treeFrame" width="100%" frameborder="0" src="<%= treeUrl%>" ></iframe>
    </div>
  </div> 
  <div class="yui-u">
    <div id="mainRight">
      <div id="header"></div>
	  <div id="summary"></div>
    </div>
  </div> 
</div>
</body>
</html>
