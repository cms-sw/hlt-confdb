<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Trigger Summary</title>


<link rel="stylesheet" type="text/css" href="../js/yui/reset-fonts-grids/reset-fonts-grids.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/base/base-min.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/container/assets/skins/sam/container.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/datatable/assets/skins/sam/datatable.css" />
<link rel="stylesheet" type="text/css" href="../js/yui/resize/assets/skins/sam/resize.css"" />
<link rel="stylesheet" type="text/css" href="../assets/css/confdb.css" />

<script type="text/javascript" src="../js/yui/utilities/utilities.js"></script>
<script type="text/javascript" src="../js/yui/cookie/cookie-beta-min.js"></script>
<script type="text/javascript" src="../js/yui/datasource/datasource-beta-min.js"></script>
<script type="text/javascript" src="../js/yui/resize/resize-beta-min.js"></script>
<script type="text/javascript" src="../js/yui/json/json-min.js"></script>
<!--
<script type="text/javascript" src="../js/yui/container/container.js"></script>
-->
<script type="text/javascript" src="../js/dwr2JSON.js"></script>
<script type="text/javascript" src="../js/AjaxInfo.js"></script>

<style>

body {
	margin:0px; 
	padding:0px; 
    overflow: hidden;
    position: fixed;
}
.blindTable {
	margin:0px; 
	padding:0px; 
}

.blindTable td {
  border:0px;
}


.topDiv {
	height: 1.2em;
}

#doc3 { 
    padding:0px; 
    margin:0px; 
}

#pg {
	margin:0px; 
	padding:0px; 
}

#pg .yui-g {
}

#pg .yui-u {
}

#mainLeft { 
	background:#e8e8e8; 
	border: 0px solid #B6CDE1; 
	margin:0px; 
	padding-right:5px;
	padding-left:1px; 
	padding-top:1px; 
}

#mainRight { 
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

#rightHeaderDiv {
	background-color:#FFE19A;
	border: 1px solid #B6CDE1; 
	margin:0px; 
	margin-top:1px; 
	margin-bottom:3px; 
	padding:1px; 
}

#rightHeaderBottomDiv {
	height:20px;
}

#configDiv {
    overflow: hidden;
}

</style>

<%
  out.println( "<script type=\"text/javascript\">" );
  String height = request.getParameter( "height" );
  if ( height == null )
	  out.println( "var displayHeight = 0;" );
  else
	  out.println( "var displayHeight = " + height + ";" );
  String width = request.getParameter( "width" );
  if ( width == null )
	  out.println( "var displayWidth = 0;" );
  else
	  out.println( "var displayWidth = " + width + ";" );
  out.println( "</script>" );
%>

<script type="text/javascript">
// Patch for width and/or minWidth Column values bug in non-scrolling DataTables
//(function(){var B=YAHOO.widget.DataTable,A=YAHOO.util.Dom;B.prototype._setColumnWidth=function(I,D,J){I=this.getColumn(I);if(I){J=J||"hidden";if(!B._bStylesheetFallback){var N;if(!B._elStylesheet){N=document.createElement("style");N.type="text/css";B._elStylesheet=document.getElementsByTagName("head").item(0).appendChild(N)}if(B._elStylesheet){N=B._elStylesheet;var M=".yui-dt-col-"+I.getId();var K=B._oStylesheetRules[M];if(!K){if(N.styleSheet&&N.styleSheet.addRule){N.styleSheet.addRule(M,"overflow:"+J);N.styleSheet.addRule(M,"width:"+D);K=N.styleSheet.rules[N.styleSheet.rules.length-1]}else{if(N.sheet&&N.sheet.insertRule){N.sheet.insertRule(M+" {overflow:"+J+";width:"+D+";}",N.sheet.cssRules.length);K=N.sheet.cssRules[N.sheet.cssRules.length-1]}else{B._bStylesheetFallback=true}}B._oStylesheetRules[M]=K}else{K.style.overflow=J;K.style.width=D}return }B._bStylesheetFallback=true}if(B._bStylesheetFallback){if(D=="auto"){D=""}var C=this._elTbody?this._elTbody.rows.length:0;if(!this._aFallbackColResizer[C]){var H,G,F;var L=["var colIdx=oColumn.getKeyIndex();","oColumn.getThEl().firstChild.style.width="];for(H=C-1,G=2;H>=0;--H){L[G++]="this._elTbody.rows[";L[G++]=H;L[G++]="].cells[colIdx].firstChild.style.width=";L[G++]="this._elTbody.rows[";L[G++]=H;L[G++]="].cells[colIdx].style.width="}L[G]="sWidth;";L[G+1]="oColumn.getThEl().firstChild.style.overflow=";for(H=C-1,F=G+2;H>=0;--H){L[F++]="this._elTbody.rows[";L[F++]=H;L[F++]="].cells[colIdx].firstChild.style.overflow=";L[F++]="this._elTbody.rows[";L[F++]=H;L[F++]="].cells[colIdx].style.overflow="}L[F]="sOverflow;";this._aFallbackColResizer[C]=new Function("oColumn","sWidth","sOverflow",L.join(""))}var E=this._aFallbackColResizer[C];if(E){E.call(this,I,D,J);return }}}else{}};B.prototype._syncColWidths=function(){var J=this.get("scrollable");if(this._elTbody.rows.length>0){var M=this._oColumnSet.keys,C=this.getFirstTrEl();if(M&&C&&(C.cells.length===M.length)){var O=false;if(J&&(YAHOO.env.ua.gecko||YAHOO.env.ua.opera)){O=true;if(this.get("width")){this._elTheadContainer.style.width="";this._elTbodyContainer.style.width=""}else{this._elContainer.style.width=""}}var I,L,F=C.cells.length;for(I=0;I<F;I++){L=M[I];if(!L.width){this._setColumnWidth(L,"auto","visible")}}for(I=0;I<F;I++){L=M[I];var H=0;var E="hidden";if(!L.width){var G=L.getThEl();var K=C.cells[I];if(J){var N=(G.offsetWidth>K.offsetWidth)?G.firstChild:K.firstChild;if(G.offsetWidth!==K.offsetWidth||N.offsetWidth<L.minWidth){H=Math.max(0,L.minWidth,N.offsetWidth-(parseInt(A.getStyle(N,"paddingLeft"),10)|0)-(parseInt(A.getStyle(N,"paddingRight"),10)|0))}}else{if(K.offsetWidth<L.minWidth){E=K.offsetWidth?"visible":"hidden";H=Math.max(0,L.minWidth,K.offsetWidth-(parseInt(A.getStyle(K,"paddingLeft"),10)|0)-(parseInt(A.getStyle(K,"paddingRight"),10)|0))}}}else{H=L.width}if(L.hidden){L._nLastWidth=H;this._setColumnWidth(L,"1px","hidden")}else{if(H){this._setColumnWidth(L,H+"px",E)}}}if(O){var D=this.get("width");this._elTheadContainer.style.width=D;this._elTbodyContainer.style.width=D}}}this._syncScrollPadding()}})();
// Patch for initial hidden Columns bug
//(function(){var A=YAHOO.util,B=YAHOO.env.ua,E=A.Event,C=A.Dom,D=YAHOO.widget.DataTable;D.prototype._initTheadEls=function(){var X,V,T,Z,I,M;if(!this._elThead){Z=this._elThead=document.createElement("thead");I=this._elA11yThead=document.createElement("thead");M=[Z,I];E.addListener(Z,"focus",this._onTheadFocus,this);E.addListener(Z,"keydown",this._onTheadKeydown,this);E.addListener(Z,"mouseover",this._onTableMouseover,this);E.addListener(Z,"mouseout",this._onTableMouseout,this);E.addListener(Z,"mousedown",this._onTableMousedown,this);E.addListener(Z,"mouseup",this._onTableMouseup,this);E.addListener(Z,"click",this._onTheadClick,this);E.addListener(Z.parentNode,"dblclick",this._onTableDblclick,this);this._elTheadContainer.firstChild.appendChild(I);this._elTbodyContainer.firstChild.appendChild(Z)}else{Z=this._elThead;I=this._elA11yThead;M=[Z,I];for(X=0;X<M.length;X++){for(V=M[X].rows.length-1;V>-1;V--){E.purgeElement(M[X].rows[V],true);M[X].removeChild(M[X].rows[V])}}}var N,d=this._oColumnSet;var H=d.tree;var L,P;for(T=0;T<M.length;T++){for(X=0;X<H.length;X++){var U=M[T].appendChild(document.createElement("tr"));P=(T===1)?this._sId+"-hdrow"+X+"-a11y":this._sId+"-hdrow"+X;U.id=P;for(V=0;V<H[X].length;V++){N=H[X][V];L=U.appendChild(document.createElement("th"));if(T===0){N._elTh=L}P=(T===1)?this._sId+"-th"+N.getId()+"-a11y":this._sId+"-th"+N.getId();L.id=P;L.yuiCellIndex=V;this._initThEl(L,N,X,V,(T===1))}if(T===0){if(X===0){C.addClass(U,D.CLASS_FIRST)}if(X===(H.length-1)){C.addClass(U,D.CLASS_LAST)}}}if(T===0){var R=d.headers[0];var J=d.headers[d.headers.length-1];for(X=0;X<R.length;X++){C.addClass(C.get(this._sId+"-th"+R[X]),D.CLASS_FIRST)}for(X=0;X<J.length;X++){C.addClass(C.get(this._sId+"-th"+J[X]),D.CLASS_LAST)}var Q=(A.DD)?true:false;var c=false;if(this._oConfigs.draggableColumns){for(X=0;X<this._oColumnSet.tree[0].length;X++){N=this._oColumnSet.tree[0][X];if(Q){L=N.getThEl();C.addClass(L,D.CLASS_DRAGGABLE);var O=D._initColumnDragTargetEl();N._dd=new YAHOO.widget.ColumnDD(this,N,L,O)}else{c=true}}}for(X=0;X<this._oColumnSet.keys.length;X++){N=this._oColumnSet.keys[X];if(N.resizeable){if(Q){L=N.getThEl();C.addClass(L,D.CLASS_RESIZEABLE);var G=L.firstChild;var F=G.appendChild(document.createElement("div"));F.id=this._sId+"-colresizer"+N.getId();N._elResizer=F;C.addClass(F,D.CLASS_RESIZER);var e=D._initColumnResizerProxyEl();N._ddResizer=new YAHOO.util.ColumnResizer(this,N,L,F.id,e);var W=function(f){E.stopPropagation(f)};E.addListener(F,"click",W)}else{c=true}}}if(c){}}else{}}for(var a=0,Y=this._oColumnSet.keys.length;a<Y;a++){if(this._oColumnSet.keys[a].hidden){var b=this._oColumnSet.keys[a];var S=b.getThEl();b._nLastWidth=S.offsetWidth-(parseInt(C.getStyle(S,"paddingLeft"),10)|0)-(parseInt(C.getStyle(S,"paddingRight"),10)|0);this._setColumnWidth(b.getKeyIndex(),"1px")}}if(B.webkit&&B.webkit<420){var K=this;setTimeout(function(){K._elThead.style.display=""},0);this._elThead.style.display="none"}}})();

var configFrameUrl,
	configKey,
	dbIndex,
	myDataSource,
	myDataTable,
	myCallback,	
	Dom = YAHOO.util.Dom,
    Event = YAHOO.util.Event,
    mainLeft = null,
    mainRight = null,
    displayWidth,
    resize,
    oldWidth = "200px",
    detailsMode = true;
	
function init() 
{
    mainLeft = Dom.get('mainLeft');
    mainRight = Dom.get('mainRight');

	if ( displayHeight == 0 )
		displayHeight = Dom.getViewportHeight();
	if ( displayWidth == 0 )
		displayWidth = Dom.getViewportWidth();

    Dom.setStyle( 'expandDiv', 'visibility', 'collapse' );
    Dom.setStyle( mainRight, 'visibility', 'hidden' );
    Dom.setStyle(  'doc3', 'height',  displayHeight + 'px' );
    Dom.setStyle(  'pg', 'height',  displayHeight + 'px' );
    Dom.setStyle(  'pg', 'width',  displayWidth + 'px' );

    Dom.setStyle( 'treeFrame', 'height',  (displayHeight - 30) + 'px' );

    resize = new YAHOO.util.Resize('mainLeft', {
            proxy: true,
            handles: ['r'],
            maxWidth: displayWidth
        });
    resize.on('resize', function(ev) {
            var w = ev.width;
            var width = displayWidth - w - 8;
            Dom.setStyle( mainRight, 'height', displayHeight + 'px' );
            Dom.setStyle( mainRight, 'width', width + 'px');
            Dom.setStyle( 'configDiv', 'width', width + 'px');
        });

    resize.resize(null, displayHeight, 200, 0, 0, true);


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
			oldWidth = Dom.getStyle( mainLeft, 'width' );
	        resize.resize( null, displayHeight, 1, 0, 0, true);
            Dom.setStyle( mainLeft, 'visibility', 'hidden' );
            Dom.setStyle( 'expandDiv', 'visibility', 'visible' );
		});

	//handler for expandDiv
	Event.on( "expandDiv", "click", function(e) {
            Dom.setStyle( 'expandDiv', 'visibility', 'collapse' );
	        resize.resize( null, displayHeight, oldWidth, 0, 0, true);
            Dom.setStyle( mainLeft, 'visibility', 'visible' );
		});

  YAHOO.util.Event.on( "detailsButton", "click", selectView, "details" );
  YAHOO.util.Event.on( "summaryButton", "click", selectView, "summary" );

}
	
	
function labelClicked( node )
{
  if ( !node.data.key )
  	return;

  var mode = YAHOO.util.Cookie.get( 'mode' );
  if ( mode == "summary" )
  	detailsMode = false;
  else
  	detailsMode = true;

  Dom.setStyle( mainRight, 'visibility', 'visible' );

  configKey = node.data.key;
  dbIndex = node.data.dbIndex;
  Dom.get( 'fullNameTD' ).innerHTML = "<b>" + node.data.fullName + "</b>";
  var fileName = node.data.name.replace( '//s/g', '_' ) + "_V" + node.data.version;

  Dom.get( 'downloadTD' ).innerHTML = 'download ' 
    + '<a href="' + fileName + '.cfg?configId='+ node.data.key + '&dbIndex=' + node.data.dbIndex + '">cfg</a> '
    + '<a href="' + fileName + '.py?format=python&configId='+ node.data.key + '&dbIndex=' + node.data.dbIndex + '">py</a>';

  showConfig();
  treeReady();
}

function showConfig()
{  
  Dom.get( 'rightHeaderBottomDiv' ).innerHTML = '<img src="../assets/img/wait.gif">';
  if ( detailsMode == true )
  {
    Dom.setStyle( 'detailsButton', 'backgroundImage', 'url(../assets/img/menubaritem_submenuindicator.png)' );
    Dom.setStyle( 'summaryButton', 'backgroundImage', 'url(../assets/img/menuitem_submenuindicator.png)' );
    configFrameUrl = "../details/convert2Html.jsp?configKey=" + configKey + "&dbIndex=" + dbIndex + "&bgcolor=FFF5DF"; 
  }
  else
  {
    Dom.setStyle( 'summaryButton', 'backgroundImage', 'url(../assets/img/menubaritem_submenuindicator.png)' );
    Dom.setStyle( 'detailsButton', 'backgroundImage', 'url(../assets/img/menuitem_submenuindicator.png)' );
    configFrameUrl = "../summary/showSummary.jsp?configKey=" + configKey + "&dbIndex=" + dbIndex + "&bgcolor=FFF5DF"; 
  }
  var heightpx = Dom.getStyle( "rightHeaderDiv", "height" ).split( 'px' );
  var height = displayHeight - heightpx[0] - 12;
  Dom.get( 'configDiv' ).innerHTML = '<iframe src="' + configFrameUrl + '" name="configIFrame" id="configFrame" width="100%" height="'+ height + '" frameborder="0"></iframe>';
}
  
function updateJumpTo( list )
{   
  var html = "";
  for ( var i = 0; i < list.length; i++ )
  {
    html += '<a href="' + configFrameUrl + '#' + list[i] + '" target="configIFrame">' + list[i] + '</a>  ';
  }
  Dom.get( 'rightHeaderBottomDiv' ).innerHTML = html;
}
	
  
  
function iframeReady()
{
  if ( detailsMode == true )
    AjaxInfo.getAnchors( dbIndex, configKey, updateJumpTo );
  else
	Dom.get( 'rightHeaderBottomDiv' ).innerHTML = "";
}
	
function treeReady()
{
  document.getElementById( "info" ).innerHTML = "";
  document.getElementById( "info" ).style.border = '0px';
}
	
function selectView( event, selected )
{
  YAHOO.util.Event.preventDefault(event);
  if ( selected == "details"  &&  detailsMode == true )
  	return;
  if ( selected == "summary"  &&  detailsMode == false )
  	return;

  var expiresWhen = new Date();
  expiresWhen.setFullYear( expiresWhen.getFullYear() + 1 ); 
  if ( selected == "details" )
  {
  	detailsMode = true;
  	YAHOO.util.Cookie.set( 'mode', 'details', { expires: expiresWhen } );
  }
  else
  {
  	detailsMode = false;
  	YAHOO.util.Cookie.set( 'mode', 'summary', { expires: expiresWhen } );
  }
  
  showConfig();
}

	
	
//When the DOM is done loading, we can initialize our TreeView
//instance:
YAHOO.util.Event.onContentReady( "doc3", init );
	
</script>

</head>
<body class="yui-skin-sam">

<%
  String treeUrl = "../browser/treeFrame.jsp?db=" + request.getParameter( "db" );
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
         <div style="position:absolute; left:2px; top:2px; z-index:2; cursor:pointer" id="expandDiv" > &gt; </div>
    	 <div id="info"><img src="../assets/img/wait.gif"></div>
		 <iframe name="treeFrame" id="treeFrame" width="100%" frameborder="0" src="<%= treeUrl%>" ></iframe>
   	  </div>

      <div class="yui-u" id="mainRight">
        <div id="rightHeaderDiv">
  		  <table width='100%' class='blindTable'><tr>
  		    <td id='fullNameTD'><b>/PATH/CONFIG/VERSION</b></td>
  		    <td></td>
  		    <td><div class="dropDownButton" id="detailsButton">details</div></td>
  			<td><div class="dropDownButton" id="summaryButton">summary</div></td>
  			<td align="right" id='downloadTD'> download cfg py</td>
		  </tr></table>
          <div id="rightHeaderBottomDiv"></div>
        </div>
		<div id="configDiv"></div>
      </div>
    </div>
  </div>
</div>
</body>
</html>

