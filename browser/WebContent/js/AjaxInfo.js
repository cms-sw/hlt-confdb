// Provide a default path to dwr.engine
if (dwr == null) var dwr = {};
if (dwr.engine == null) dwr.engine = {};
if (DWREngine == null) var DWREngine = dwr.engine;

if (AjaxInfo == null) var AjaxInfo = {};
AjaxInfo._path = '/cms-project-confdb-hltdev/dwr';

AjaxInfo.listDBs = function(callback) {
  dwr.engine._execute(AjaxInfo._path, 'ajax.AjaxInfo', 'listDBs', callback);
}
AjaxInfo.getAnchors = function(p0, p1, callback) {
  dwr.engine._execute(AjaxInfo._path, 'ajax.AjaxInfo', 'getAnchors', 'int:' + p0, 'int:' + p1, callback);
}
AjaxInfo.getRcmsDbInfo = function(callback) {
  dwr.engine._execute(AjaxInfo._path, 'ajax.AjaxInfo', 'getRcmsDbInfo', callback);
}
AjaxInfo.clearCache = function(callback) {
  dwr.engine._execute(AjaxInfo._path, 'ajax.AjaxInfo', 'clearCache', callback);
}
AjaxInfo.getMemInfo = function(callback) {
  dwr.engine._execute(AjaxInfo._path, 'ajax.AjaxInfo', 'getMemInfo', callback);
}
