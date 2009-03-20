<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>alloc</title>
</head>
<body>
<%
	String mb = request.getParameter( "mb" );
	if ( mb == null )
		mb = "20";
	int alloc = Integer.parseInt( mb ) * 1024 * 1024;
	byte[] tmp = new byte[ alloc ];
	tmp[0] = 0;
%>
</body>
</html>