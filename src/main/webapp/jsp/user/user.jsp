<%@ page language="java" contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script language="JavaScript" type="text/javascript"
	src="/js/My97DatePicker/WdatePicker.js"></script>	

<title>用户管理</title>
<link href="/css/style.css" rel="stylesheet" type="text/css" />
<style type="text/css">
<!--
.STYLE1 {
	color: #ffffff
}
.STYLE2 {
	color: #FF0000
}
.STYLE6 {color: #FF0000; font-size: 12px; }
-->
</style>

	</head>

<body>
	<div>  
        <%@ include file="/head.jsp" %>
    </div> 
	<header id="head" class="secondary"></header>

	<!-- container -->
	<div class="container" style="width:auto;font-size:14px">
		<ol class="breadcrumb">
			<li><a href="/">主页</a></li>
			<li class="active">用户管理</li>
		</ol>

		<div class="row">	
			<!-- Article main content -->
		<article class="col-sm-9 maincontent" style="width:100%;">
		 <header class="page-header">
				<h1 class="page-title" style="text-align:center;">用户管理</h1>
			</header>
	 
	 <sf:form method="post" modelAttribute="userinfo">
		<input name="page" id="page" type="hidden"  />					
		<div align="right"></div>
		
		<table width="100%" align="center" class="rect" height=40 cellPadding=1 border=1 bordercolor="#CCCCCC">
		  <tr>
		  <td width="25%" align="left" valign="middle">
		   <font size="2" color="black">用户名:&nbsp;</font>
			<sf:input path="usercode" id="usercode" style="width:150px;"/>			
			<input name="button" style="width:100px;height:28px" type="submit" class="button gray" id="button" align="right" value="查&nbsp;&nbsp;询" />	
			&nbsp;&nbsp;<a href="#" onclick="showDiv(0,1)" style="text-decoration: none;"> 
			<span class="btnold STYLE1" style="width:70px;background:#FFA54F;border:#FFA54F;"> 添加用户</span></a>&nbsp;&nbsp;
			<a href="#" onclick="showDiv(0,5)" style="text-decoration: none;"> 
			<span class="btnold STYLE1" style="width:70px;background:#FFA54F;border:#FFA54F;"> 添加角色</span></a>&nbsp;&nbsp;
			<a href="#" onclick="showDiv(0,4)" style="text-decoration: none;"> 
			<span class="btnold STYLE1" style="width:70px;background:#FFA54F;border:#FFA54F;"> 权限管理</span></a>&nbsp;&nbsp;
			<a href="#" onclick="showDiv(0,6)" style="text-decoration: none;"> 
			<span class="btnold STYLE1" style="width:70px;background:#FFA54F;border:#FFA54F;"> 项目管理</span></a>				
				</td>
		</tr>
		  </table> 
		
	   <table width="100%" align="center" class="bordered" style="table-layout: fixed">
          <tr bgcolor="#B9DCFF">                
				<th width="10%" height="40" nowrap="nowrap"  >姓名</th>
				<th width="15%" height="40" nowrap="nowrap"  >用户名</th>
				<th width="45%" height="40" nowrap="nowrap"  >角色</th>
				<th width="15%" height="40" nowrap="nowrap"  >所属部门</th>
				<th width="10%" height="40" nowrap="nowrap"  >默认项目</th>
				<th width="5%" height="40" nowrap="nowrap"  >操作</th>
		  </tr>
		  <c:forEach var="t" items="${splist}" begin="0" step="1" varStatus="i">
				<tr>
					<td height="25" align="center">${t.username }</td>
					<td height="25" align="center">${t.usercode }</td>
					<td height="25" align="center">${t.role }</td>
					<td height="25" align="center">${t.secondarySector.departmentname }</td>
					<td height="25" align="center">${t.projectname }</td>
					<%-- <td height="25" align="center">${t.remark }</td> --%>
					<td height="25" align="center" style="word-break: break-all">
						<a href="#" onclick="showDiv('${t.id}','2')"
						style="cursor: pointer;"><u>修改</u></a>&nbsp; <a href="#"
						onclick="showDiv('${t.id}','3')" style="cursor: pointer;"><u>删除</u></a>&nbsp;
					</td>
				</tr>
			</c:forEach>
		</table>
<center>
			<div id="pagelist" align="center">
				<c:if test="${allRows!=0 }">
					<ul>
						<li><a href="#" onclick="return setPage(1)">首页 </a></li>
						<li><a href="#" onclick="return frontPageCheck(${page-1});">上一页</a></li>
						<li><a href="#" onclick="return backPageCheck(${page+1});">下一页</a></li>
						<li><a href="#" onclick="return setPage(${allPage})">末页</a></li>
						<li>第${page}页</li>
						<li>共${allRows}条</li>
						<li>共${allPage}页</li>
					</ul>
				</c:if>
				<c:if test="${allRows==0 }">
					<font color="#FF0000">没有记录!</font>				</c:if>
			</div>
			<br/><br/>
			</center>
						
		</sf:form>
		<p>&nbsp;</p>
	</article>
	</div>
	</div>
	
	<script type="text/javascript">
	function frontPageCheck(page)
	{
		if(${page > 1})
		{
			document.getElementById("page").value=page;
			document.getElementById("userinfo").submit();
			return true;
		}
		return false;
	}
	
	function backPageCheck(page)
	{
		
		if(${page < allPage})
		{
			document.getElementById("page").value=page;
			document.getElementById("userinfo").submit();
			//$("#projectversion").submit();
			return true;
		}			
		return false;
	}
	
	
	function setPage(page){
		if(page==1){
			document.getElementById("page").value=1;
		}else{
			document.getElementById("page").value=page;
		}
		document.getElementById("userinfo").submit();
		return true;
	}
	
	function showDiv(userid,opr){
		var status = document.getElementById("loginstatus").value;
		if(status=="false"){
			if(window.confirm("你未登录哦，要先去首页登录吗？")){
				var url = '/progressus/signin.jsp';
				window.location.href=url;
			}else{
				return false; 
			} 	
		}else{
		if(opr=="1"){
			var url = '/userInfo/add.do';
			window.location.href=url;
	    }else if(opr=="2"){
			var url = '/userInfo/update.do?id='+userid;
			window.location.href=url;
	    }else if(opr=="3"){
	    	if(window.confirm("你确认要删除吗？")){ 
				var url = '/userInfo/delete.do?id='+userid;
				window.location.href=url;
	    		return true; 
	    		}else{ 
	    		return false; 
	    		}
	    }else if(opr=="4"){
			var url = '/userInfo/role.do';
			window.location.href=url;
	    }else if(opr=="5"){
			var url = '/userInfo/roleadd.do';
			window.location.href=url;
	    }else if(opr=="6"){
			var url = '/sectorProjects/list.do';
			window.location.href=url;
	    }else{ 
	       alert("操作码有误，是否有非法操作，请联系软件质量室！"); 
	       return false;
	      }
		}
	}
	
	</script>
</body>
</html>