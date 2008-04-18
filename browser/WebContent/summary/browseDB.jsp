<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Trigger Summary</title>


<link rel="stylesheet" type="text/css" href="../js/yui/reset-fonts-grids/reset-fonts-grids.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/base/base-min.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/container/assets/skins/sam/container.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/datatable/assets/skins/sam/datatable.css">
<link rel="stylesheet" type="text/css" href="../js/yui/resize/assets/skins/sam/resize.css"">
<script type="text/javascript" src="../js/yui/utilities/utilities.js"></script>
<script type="text/javascript" src="../js/yui/datasource/datasource-beta-min.js"></script>
<script type="text/javascript" src="../js/yui/datatable/datatable-beta-min.js"></script>
<script type="text/javascript" src="../js/yui/resize/resize-beta-min.js"></script>
<!--
<script type="text/javascript" src="../js/yui/json/json-min.js"></script>
<script type="text/javascript" src="../js/yui/container/container.js"></script>
-->
<script type="text/javascript" src="../js/dwr2JSON.js"></script>
<script type="text/javascript" src="../js/AjaxInfo.js"></script>

<style>
html { 
	background:#edf5ff;
}

body { 
	margin: 0;
	padding: 0;
	background:#edf5ff;
    overflow: hidden;
}

.topDiv {
	height: 1.2em;
}

#doc3 { 
	margin-bottom:0px; 
	margin-top:5px; 
	margin-left:5px;
	margin-right:5px;
    overflow: hidden;
}

#pg {
    overflow: hidden;
    border: 1px solid lightgray;
}

#pg .yui-g {
    overflow: hidden;
}

#pg .yui-u {
    overflow: hidden;
}

#mainLeft { 
	background:#edf5ff; 
	border: 0px solid #B6CDE1; 
	margin:0px; 
	padding-right:5px;
	padding-left:1px; 
	padding-top:1px; 
}

#summary { 
	padding:1px; 
    overflow: auto;
}


#info { 
	background:white; 
	border: 1px solid #B6CDE1; 
	border-top:0px; 
}



#headerDiv { 
	margin:0px; 
	padding:0.4em; 
	background:white; 
	border: 1px solid #B6CDE1; 
	border-bottom:0px; 
}
    

</style>


<script type="text/javascript">
// Patch for width and/or minWidth Column values bug in non-scrolling DataTables
(function(){var B=YAHOO.widget.DataTable,A=YAHOO.util.Dom;B.prototype._setColumnWidth=function(I,D,J){I=this.getColumn(I);if(I){J=J||"hidden";if(!B._bStylesheetFallback){var N;if(!B._elStylesheet){N=document.createElement("style");N.type="text/css";B._elStylesheet=document.getElementsByTagName("head").item(0).appendChild(N)}if(B._elStylesheet){N=B._elStylesheet;var M=".yui-dt-col-"+I.getId();var K=B._oStylesheetRules[M];if(!K){if(N.styleSheet&&N.styleSheet.addRule){N.styleSheet.addRule(M,"overflow:"+J);N.styleSheet.addRule(M,"width:"+D);K=N.styleSheet.rules[N.styleSheet.rules.length-1]}else{if(N.sheet&&N.sheet.insertRule){N.sheet.insertRule(M+" {overflow:"+J+";width:"+D+";}",N.sheet.cssRules.length);K=N.sheet.cssRules[N.sheet.cssRules.length-1]}else{B._bStylesheetFallback=true}}B._oStylesheetRules[M]=K}else{K.style.overflow=J;K.style.width=D}return }B._bStylesheetFallback=true}if(B._bStylesheetFallback){if(D=="auto"){D=""}var C=this._elTbody?this._elTbody.rows.length:0;if(!this._aFallbackColResizer[C]){var H,G,F;var L=["var colIdx=oColumn.getKeyIndex();","oColumn.getThEl().firstChild.style.width="];for(H=C-1,G=2;H>=0;--H){L[G++]="this._elTbody.rows[";L[G++]=H;L[G++]="].cells[colIdx].firstChild.style.width=";L[G++]="this._elTbody.rows[";L[G++]=H;L[G++]="].cells[colIdx].style.width="}L[G]="sWidth;";L[G+1]="oColumn.getThEl().firstChild.style.overflow=";for(H=C-1,F=G+2;H>=0;--H){L[F++]="this._elTbody.rows[";L[F++]=H;L[F++]="].cells[colIdx].firstChild.style.overflow=";L[F++]="this._elTbody.rows[";L[F++]=H;L[F++]="].cells[colIdx].style.overflow="}L[F]="sOverflow;";this._aFallbackColResizer[C]=new Function("oColumn","sWidth","sOverflow",L.join(""))}var E=this._aFallbackColResizer[C];if(E){E.call(this,I,D,J);return }}}else{}};B.prototype._syncColWidths=function(){var J=this.get("scrollable");if(this._elTbody.rows.length>0){var M=this._oColumnSet.keys,C=this.getFirstTrEl();if(M&&C&&(C.cells.length===M.length)){var O=false;if(J&&(YAHOO.env.ua.gecko||YAHOO.env.ua.opera)){O=true;if(this.get("width")){this._elTheadContainer.style.width="";this._elTbodyContainer.style.width=""}else{this._elContainer.style.width=""}}var I,L,F=C.cells.length;for(I=0;I<F;I++){L=M[I];if(!L.width){this._setColumnWidth(L,"auto","visible")}}for(I=0;I<F;I++){L=M[I];var H=0;var E="hidden";if(!L.width){var G=L.getThEl();var K=C.cells[I];if(J){var N=(G.offsetWidth>K.offsetWidth)?G.firstChild:K.firstChild;if(G.offsetWidth!==K.offsetWidth||N.offsetWidth<L.minWidth){H=Math.max(0,L.minWidth,N.offsetWidth-(parseInt(A.getStyle(N,"paddingLeft"),10)|0)-(parseInt(A.getStyle(N,"paddingRight"),10)|0))}}else{if(K.offsetWidth<L.minWidth){E=K.offsetWidth?"visible":"hidden";H=Math.max(0,L.minWidth,K.offsetWidth-(parseInt(A.getStyle(K,"paddingLeft"),10)|0)-(parseInt(A.getStyle(K,"paddingRight"),10)|0))}}}else{H=L.width}if(L.hidden){L._nLastWidth=H;this._setColumnWidth(L,"1px","hidden")}else{if(H){this._setColumnWidth(L,H+"px",E)}}}if(O){var D=this.get("width");this._elTheadContainer.style.width=D;this._elTbodyContainer.style.width=D}}}this._syncScrollPadding()}})();
// Patch for initial hidden Columns bug
(function(){var A=YAHOO.util,B=YAHOO.env.ua,E=A.Event,C=A.Dom,D=YAHOO.widget.DataTable;D.prototype._initTheadEls=function(){var X,V,T,Z,I,M;if(!this._elThead){Z=this._elThead=document.createElement("thead");I=this._elA11yThead=document.createElement("thead");M=[Z,I];E.addListener(Z,"focus",this._onTheadFocus,this);E.addListener(Z,"keydown",this._onTheadKeydown,this);E.addListener(Z,"mouseover",this._onTableMouseover,this);E.addListener(Z,"mouseout",this._onTableMouseout,this);E.addListener(Z,"mousedown",this._onTableMousedown,this);E.addListener(Z,"mouseup",this._onTableMouseup,this);E.addListener(Z,"click",this._onTheadClick,this);E.addListener(Z.parentNode,"dblclick",this._onTableDblclick,this);this._elTheadContainer.firstChild.appendChild(I);this._elTbodyContainer.firstChild.appendChild(Z)}else{Z=this._elThead;I=this._elA11yThead;M=[Z,I];for(X=0;X<M.length;X++){for(V=M[X].rows.length-1;V>-1;V--){E.purgeElement(M[X].rows[V],true);M[X].removeChild(M[X].rows[V])}}}var N,d=this._oColumnSet;var H=d.tree;var L,P;for(T=0;T<M.length;T++){for(X=0;X<H.length;X++){var U=M[T].appendChild(document.createElement("tr"));P=(T===1)?this._sId+"-hdrow"+X+"-a11y":this._sId+"-hdrow"+X;U.id=P;for(V=0;V<H[X].length;V++){N=H[X][V];L=U.appendChild(document.createElement("th"));if(T===0){N._elTh=L}P=(T===1)?this._sId+"-th"+N.getId()+"-a11y":this._sId+"-th"+N.getId();L.id=P;L.yuiCellIndex=V;this._initThEl(L,N,X,V,(T===1))}if(T===0){if(X===0){C.addClass(U,D.CLASS_FIRST)}if(X===(H.length-1)){C.addClass(U,D.CLASS_LAST)}}}if(T===0){var R=d.headers[0];var J=d.headers[d.headers.length-1];for(X=0;X<R.length;X++){C.addClass(C.get(this._sId+"-th"+R[X]),D.CLASS_FIRST)}for(X=0;X<J.length;X++){C.addClass(C.get(this._sId+"-th"+J[X]),D.CLASS_LAST)}var Q=(A.DD)?true:false;var c=false;if(this._oConfigs.draggableColumns){for(X=0;X<this._oColumnSet.tree[0].length;X++){N=this._oColumnSet.tree[0][X];if(Q){L=N.getThEl();C.addClass(L,D.CLASS_DRAGGABLE);var O=D._initColumnDragTargetEl();N._dd=new YAHOO.widget.ColumnDD(this,N,L,O)}else{c=true}}}for(X=0;X<this._oColumnSet.keys.length;X++){N=this._oColumnSet.keys[X];if(N.resizeable){if(Q){L=N.getThEl();C.addClass(L,D.CLASS_RESIZEABLE);var G=L.firstChild;var F=G.appendChild(document.createElement("div"));F.id=this._sId+"-colresizer"+N.getId();N._elResizer=F;C.addClass(F,D.CLASS_RESIZER);var e=D._initColumnResizerProxyEl();N._ddResizer=new YAHOO.util.ColumnResizer(this,N,L,F.id,e);var W=function(f){E.stopPropagation(f)};E.addListener(F,"click",W)}else{c=true}}}if(c){}}else{}}for(var a=0,Y=this._oColumnSet.keys.length;a<Y;a++){if(this._oColumnSet.keys[a].hidden){var b=this._oColumnSet.keys[a];var S=b.getThEl();b._nLastWidth=S.offsetWidth-(parseInt(C.getStyle(S,"paddingLeft"),10)|0)-(parseInt(C.getStyle(S,"paddingRight"),10)|0);this._setColumnWidth(b.getKeyIndex(),"1px")}}if(B.webkit&&B.webkit<420){var K=this;setTimeout(function(){K._elThead.style.display=""},0);this._elThead.style.display="none"}}})();

var configFrameUrl,
	configKey,
	dbIndex,
	myDataSource,
	myDataTable,
	myCallback,	
	Dom = YAHOO.util.Dom,
    Event = YAHOO.util.Event,
    col1 = null,
    col2 = null,
    displayHeight,
    displayWidth,
    resize,
    oldWidth = "200px";
	
function init() 
{
    Dom.setStyle( 'expandDiv', 'visibility', 'collapse' );
    Dom.setStyle( 'mainRight', 'visibility', 'collapse' );

	displayWidth  = Dom.getViewportWidth() - 12;
	displayHeight = Dom.getViewportHeight() - 10;


    Dom.setStyle(  'pg', 'height',  displayHeight + 'px' );
    Dom.setStyle(  'pg', 'width',  displayWidth + 'px' );
    Dom.setStyle( "treeFrame", 'height',  (displayHeight - 30) + 'px' );
    Dom.setStyle( "summary", 'height',  (displayHeight - 30) + 'px' );

    col1 = Dom.get('mainLeft');
    col2 = Dom.get('mainRight');
    resize = new YAHOO.util.Resize('mainLeft', {
            proxy: true,
            handles: ['r'],
            maxWidth: displayWidth
        });
    resize.on('resize', function(ev) {
            var w = ev.width;
            Dom.setStyle(col2, 'height', displayHeight + 'px' );
            Dom.setStyle(col2, 'width', (displayWidth - w - 20) + 'px');
            Dom.setStyle( 'summary', 'width', (displayWidth - w - 30) + 'px');
        });

    resize.resize(null, displayHeight, 200, 0, 0, true);
    prepareTable();

	//handler for expanding all nodes
	Event.on("expand", "click", function(e) {
			treeFrame.tree.expandAll();
			YAHOO.util.Event.preventDefault(e);
		});
		
	//handler for collapsing all nodes
	Event.on("collapse", "click", function(e) {
			treeFrame.tree.collapseAll();
			YAHOO.util.Event.preventDefault(e);
		});

	//handler for collapseDiv
	Event.on("collapseDiv", "click", function(e) {
			oldWidth = Dom.getStyle( col1, 'width' );
	        resize.resize( null, displayHeight, 1, 0, 0, true);
            Dom.setStyle( 'expandDiv', 'visibility', 'visible' );
		});

	//handler for expandDiv
	Event.on( "expandDiv", "click", function(e) {
            Dom.setStyle( 'expandDiv', 'visibility', 'collapse' );
	        resize.resize( null, displayHeight, oldWidth, 0, 0, true);
		});

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
  Dom.setStyle( 'loadingDiv', 'visibility', 'visible' );
    
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
	
function prepareTable() 
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

        myDataSource = new YAHOO.util.DataSource( "getSummary.jsp?" );
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

        myDataTable = new YAHOO.widget.DataTable("summary", myColumnDefs, this.myDataSource, 
        	{ renderLoopSize:50, initialLoad:false, scrollable:false, height:"" + (displayHeight - 80) + "px" } );
        
        myCallback = { 
		    //success: myDataTable.onDataReturnInitializeTable,
		    success: function( oRequest, oResponse, oPayload ) {
		    	Dom.setStyle( 'loadingDiv', 'visibility', 'collapse' );
		    	myDataTable.onDataReturnInitializeTable( oRequest, oResponse, oPayload );
			},

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

<div id="doc3">
  <div id="pg">
    <div class="yui-g" id="pg-yui-g">
	  <div class="yui-u first" id="mainLeft">
    	 <div id="headerDiv" class="topDiv">
            <a id="expand" href="#">Expand all</a>
            <a id="collapse" href="#">Collapse all</a>
         </div>
         <div style="position:absolute; right:10px; top:2px; z-index:1; cursor:pointer" id="collapseDiv" > &lt; </div>
         <div style="position:absolute; left2px; top:2px; z-index:2; cursor:pointer" id="expandDiv" > &gt; </div>
    	 <div id="info"><img src="../img/loading.gif"></div>
		 <iframe name="treeFrame" id="treeFrame" width="100%" frameborder="0" src="<%= treeUrl%>" ></iframe>
   	  </div>

      <div class="yui-u" id="mainRight">
        <div style="position:absolute; right:30px; top:6px; background:white" id="loadingDiv" ><img src="../img/loading.gif"></div>
        <div id="header" class="topDiv"></div>
		<div id="summary"></div>
      </div>
    </div>
  </div>
</div>
</body>
</html>
