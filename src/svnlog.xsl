<?xml version="1.0" encoding="UTF-8" standalone="yes"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output
		method="html"
		encoding="utf-8"
		indent="yes"
		omit-xml-declaration="yes"
		doctype-public="-//W3C//DTD HTML 4.01//EN" />
			
	<xsl:template match="log">
		<html>
			<head>
				<title>PleoCommand Changelog</title>
			</head>
			<body>
				<xsl:apply-templates />
			</body>
		</html>
	</xsl:template>

	<xsl:template match="logentry">
		<div>
			<h2>SVN Commit <xsl:value-of select="@revision" />
			</h2>
			<p>From <xsl:value-of select="date" />
			</p>
			<xsl:apply-templates select="msg" />
			<xsl:apply-templates select="paths" />
		</div>
	</xsl:template>

	<xsl:template match="paths">
		<table border="0" summary="All files affected by this revision">
			<xsl:apply-templates select="path" />
		</table>
	</xsl:template>

	<xsl:template match="path">
		<tr>
			<xsl:if test="@action = 'M'">
				<xsl:attribute name="style">color:blue</xsl:attribute>
			</xsl:if>
			<xsl:if test="@action = 'A'">
				<xsl:attribute name="style">color:green</xsl:attribute>
			</xsl:if>
			<xsl:if test="@action = 'D'">
				<xsl:attribute name="style">color:red</xsl:attribute>
			</xsl:if>
			<td>
				<xsl:if test="@action = 'M'">(modified)</xsl:if>
				<xsl:if test="@action = 'A'">(added)</xsl:if>
				<xsl:if test="@action = 'D'">(deleted)</xsl:if>
			</td>
			<td>
				<xsl:value-of select="." />
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="msg">
		<pre>
			<xsl:value-of select="." />
		</pre>
	</xsl:template>

</xsl:stylesheet>
