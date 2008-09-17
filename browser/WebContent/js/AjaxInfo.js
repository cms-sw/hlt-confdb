
if (HLTjs == null) var HLTjs = {};
if (HLTjs.json == null) HLTjs.json = {};

if (AjaxInfo == null) var AjaxInfo = {};
AjaxInfo._path = '../json/AjaxInfo.jsp';

AjaxInfo.listDBs = function(callback) {
  HLTjs.json._execute(AjaxInfo._path, 'confdb.converter.BrowserConverter', 'listDBs', callback);
}
AjaxInfo.getAnchors = function(p0, p1, callback) {
  HLTjs.json._execute(AjaxInfo._path, 'confdb.converter.BrowserConverter', 'getAnchors', 'int:' + p0, 'int:' + p1, callback);
}
AjaxInfo.getRcmsDbInfo = function(callback) {
  HLTjs.json._execute(AjaxInfo._path, null, 'getRcmsDbInfo', callback);
}
AjaxInfo.clearCache = function(callback) {
  HLTjs.json._execute(AjaxInfo._path, 'confdb.converter.BrowserConverter', 'clearCache', callback);
}
AjaxInfo.getMemInfo = function(callback) {
  HLTjs.json._execute(AjaxInfo._path, null, 'getMemInfo', callback);
}
AjaxInfo.getTree = function( p0, callback) {
  HLTjs.json._execute(AjaxInfo._path, null, 'getTree', 'string:' + p0, callback);
}
