function EvalSafe(ss)
{ var jj="";
  if (ss.indexOf("^")>=0) return("");
  try
  { with (Math) jj=eval(ss);
  }
  catch(error)
  { return("");
  }
  return(jj);
}
