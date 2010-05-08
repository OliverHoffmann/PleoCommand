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
			<h2>
				<xsl:value-of select="date" />
			</h2>
			<p style="color: darkGoldenRod;">
				Commit <xsl:value-of select="commit" />
				by <xsl:value-of select="author" />
			</p>
			<xsl:apply-templates select="message" />
			<xsl:apply-templates select="paths" />
		</div>
	</xsl:template>

	<xsl:template match="paths">
		<table border="0" summary="All files modified by this commit">
			<xsl:apply-templates select="path" />
		</table>
	</xsl:template>

	<xsl:template match="message">
		<pre style="color: blue;">
			<xsl:value-of select="." />
		</pre>
	</xsl:template>

	<xsl:template match="path">
		<tr>
			<td style="white-space: nowrap;">
				<xsl:value-of select="file" />
			</td>
			<xsl:apply-templates select="modifications" />
		</tr>
	</xsl:template>
	
	<xsl:template match="modifications">
		<td style="color: red; white-space: nowrap;">
			<xsl:call-template name="print.modcount">
				<xsl:with-param name="count">
					<xsl:value-of select="@deletions" />
				</xsl:with-param>
			</xsl:call-template>
		</td>
		<td style="color: green; white-space: nowrap;" >
			<xsl:call-template name="print.modcount">
				<xsl:with-param name="count">
					<xsl:value-of select="@additions" />
				</xsl:with-param>
			</xsl:call-template>
		</td>
		<td style="white-space: nowrap;">
			<xsl:call-template name="print.symbols">
				<xsl:with-param name="char">-</xsl:with-param>
				<xsl:with-param name="color">red</xsl:with-param>
				<xsl:with-param name="count">
					<xsl:value-of select="@deletions" />
				</xsl:with-param>
			</xsl:call-template>
			<xsl:call-template name="print.symbols">
				<xsl:with-param name="char">+</xsl:with-param>
				<xsl:with-param name="color">green</xsl:with-param>
				<xsl:with-param name="count">
					<xsl:value-of select="@additions" />
				</xsl:with-param>
			</xsl:call-template>
		</td>
	</xsl:template>
	
	<xsl:template name="print.modcount">
		<xsl:param name="count" />
		<xsl:if test="$count &lt; 0">
			<xsl:value-of select="-$count - 1" /> bytes
		</xsl:if>
		<xsl:if test="$count &gt;= 0">
			<xsl:value-of select="$count" />
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="print.symbols">
		<xsl:param name="char" />
		<xsl:param name="color" />
		<xsl:param name="count" />
		<xsl:if test="$count &gt; 0">
			<span>
				<xsl:attribute name="style">
					color:<xsl:value-of select="$color" />
				</xsl:attribute>
				<xsl:call-template name="for.loop">
					<xsl:with-param name="i">1</xsl:with-param>
					<xsl:with-param name="char">
						<xsl:value-of select="$char" />
					</xsl:with-param>
					<xsl:with-param name="count">
						<xsl:value-of select="$count div 5 + ($count mod 5 != 0)" />
					</xsl:with-param>
				</xsl:call-template>
			</span>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="for.loop">
		<xsl:param name="i" />
		<xsl:param name="count" />
		<xsl:param name="char" />
		<xsl:if test="$i &lt;= $count">
			<xsl:value-of select="$char" />
			<xsl:call-template name="for.loop">
				<xsl:with-param name="i">
					<xsl:value-of select="$i + 1" />
				</xsl:with-param>
				<xsl:with-param name="count">
					<xsl:value-of select="$count" />
				</xsl:with-param>
				<xsl:with-param name="char">
					<xsl:value-of select="$char" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>
