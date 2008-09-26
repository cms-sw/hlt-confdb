// JavaScript Histogram
// by Kolja Prothmann
// based on :
// JavaScript Diagram Builder 3.31
// Copyright (c) 2001-2005 Lutz Tautenhahn, all rights reserved.

var histColor = new Array();

function Histogram(numLines) {
	this.diagram = new Diagram();
	this.numLines = numLines;
	this.numPoints = 0;
	
	this.startTime = (new Date()).getTime();;
	this.dt = 0;
	this.minEvents = 0;
	this.maxEvents = 0;
	
	this.points = new Array(0);
	this.scrollRatio = 0.1;

	this.SetFrame = _HSetFrame;
	this.SetBorder = _HSetBorder;
	this.SetXScale = _HSetXScale;
	this.SetYScale = _HSetXScale;
	this.SetText = _HSetText;
	this.SetGridColor = _HSetGridColor;
	this.Draw = _HDraw;
	this.ScreenY = _HScreenY;
	this.ScreenX = _HScreenX;
	this.RealY = _HRealY;
	this.RealX = _HRealX;
	
	this.UpdateRate = _HUpdateRate;
	this.Scroll = _HScroll;
	this.CreateDiagram = 0;
	this.PreFill = _PreFill;
}

function _PreFill(rateArray, j) {
	for(var i=0; i < this.numPoints && i < rateArray.length; i++) {
		// limit y to displayable area
		if( rateArray[i] >= this.maxEvents) {
			rateArray[i] = this.maxEvents;
		} else if( rateArray[i] < this.minEvents ) {
			rateArray[i] = this.minEvents;
		}
		
		//calculate screen coordinates
		var x = i;
		var y = this.ScreenY( rateArray[i] );

		this.points[j][x].left = x + this.diagram.left;
		this.points[j][x].top = y;
		this.points[j][x].MoveTo(x + this.diagram.left, y );
		this.points[j][x].SetVisibility( true );
	}
}

function _HScroll() {

	this.startTime += this.dt * this.scrollRatio;
	//calculating UTC timedifference
	var now = new Date();
	var UTCdiff = ( now.getHours() - now.getUTCHours() ) * 60 * 60 * 1000;
	//calling superfunction
	this.diagram.SetBorder(this.startTime + UTCdiff, this.startTime + UTCdiff + this.dt,
							this.minEvents, this.maxEvents);

	//TODO:
	this.Draw("#DDDDDD", "#000000", false);

	//number of pixels to scroll	
	var scrollPixels = this.numPoints * this.scrollRatio;
	
	//scroll every line
	for(var j=0; j<this.numLines; j++) {
		//move pixels to the left
		for (var i=0; i<this.numPoints-scrollPixels; i++) {
			//GfltInP[i+1].SetVisibility( false );
	
			this.points[j][i].top = this.points[j][i+scrollPixels].top;
			this.points[j][i].MoveTo( this.points[j][i].left, this.points[j][i+scrollPixels].top);
			if(this.points[j][i].top == 0 || this.points[j][i].left == 0) {
				this.points[j][i].SetVisibility( false );
			} else {
				this.points[j][i].SetVisibility( true );
			}
		}
		//clear remaining pixels
		for (var i=this.numPoints-scrollPixels; i<this.numPoints; i++) {
			this.points[j][i].SetVisibility( false );
			this.points[j][i].top = 0;
		}
	}
}

function _HUpdateRate(rate) {
	var now = new Date();
	var UTCdiff = ( now.getHours() - now.getUTCHours() ) * 60 * 60 * 1000;

	//loop through all rates and set the points
	for(var i=0; i<this.numLines; i++) {
		// limit y to displayable area
		if( rate[i] >= this.maxEvents) {
			rate[i] = this.maxEvents;
		} else if( rate[i] < this.minEvents ) {
			rate[i] = this.minEvents;
		}
		
		//calculate screen coordinates
		var x = this.ScreenX( now.getTime() + UTCdiff ) - this.diagram.left;
		var y = this.ScreenY( rate[i] );
	
		//overflow protection
		if( x >= this.numPoints ) {
			this.Scroll();
			x = this.ScreenX( now.getTime() + UTCdiff ) - this.diagram.left;
		} else if( x < 0 ) {
			this.Scroll();
			x = 0;
		}
		
		// draw points
		this.points[i][x].left = x + this.diagram.left;
		this.points[i][x].top = y;
		this.points[i][x].MoveTo(x + this.diagram.left, y );
		this.points[i][x].SetVisibility( true );
	}
}

// this function may only be called once per Histogram
function _HSetFrame(left, top, right, bottom) {
	//update numPoints for one Line
	this.numPoints = right - left;
	//call superfunction
	this.diagram.SetFrame(left, top, right, bottom);

	//allocate Array of point Arrays
	this.points = new Array(this.numLines);

	//color of the pixels created
	var color = "#0000FF";
	
	//create pixels loop
	for(var j=0; j<this.numLines; j++) {
		//choose color
                if ( histColor && histColor.length > j )
		       color = histColor[j];
		else if( j == 1 ) {
			color = "#00FFFF";
		} else if( j == 2 ) {
			color = "#FF00FF";
		} else if( j == 3 ) {
			color = "#FF0000";
		}
		
		//allocate Array
		this.points[j] = new Array(this.numPoints);
		//create Pixels
		//which are also created in the document
		for(var i=0; i < this.numPoints; i++) {
			this.points[j][i] = new Pixel(i+left, 0, color);
			this.points[j][i].SetVisibility( false );
		}
	}
	
}

function _HSetBorder(dt, maxEvents) {
	//set maxEvents
	this.maxEvents = maxEvents;
	//update time difference dt
	this.dt = dt;
	//calculating UTC timedifference
	var now = new Date();
	var UTCdiff = ( now.getHours() - now.getUTCHours() ) * 60 * 60 * 1000;

	//correction startTime, in order to display also history
	//this line is not needed if you want to start displaying data from scratch
	this.startTime -= (1.0 - this.scrollRatio) * this.dt;
	
	//calling superfunction
	this.diagram.SetBorder(this.startTime + UTCdiff,
							this.startTime + UTCdiff + this.dt,
							this.minEvents, this.maxEvents);
}

function _HSetXScale(factor) {
	this.diagram.XScale = factor;
}
function _HSetYScale(factor) {
	this.diagram.YScale = factor;
}
function _HSetText(theScaleX, theScaleY, theTitle) {
	this.diagram.SetText(theScaleX, theScaleY, theTitle);
}
function _HSetGridColor(theGridColor, theSubGridColor) {
	this.diagram.SetGridColor(theGridColor, theSubGridColor);
}
function _HDraw(theDrawColor, theTextColor, isScaleText, theTooltipText, theOnClickAction, theOnMouseoverAction, theOnMouseoutAction) {
	this.diagram.Draw(theDrawColor, theTextColor, isScaleText, theTooltipText, theOnClickAction, theOnMouseoverAction, theOnMouseoutAction);
}

function _HScreenX(theX) {
	return(this.diagram.ScreenX(theX));
}
function _HScreenY(theY) {
	return(this.diagram.ScreenY(theY));
}
function _HRealX(x) {
	return(this.diagram.RealX(x));
}
function _HRealY(y) {
	return(this.diagram.RealY(y));
}
