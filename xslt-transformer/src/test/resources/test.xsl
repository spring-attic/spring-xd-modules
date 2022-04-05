<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:java="https://xml.apache.org/xalan/java"
    exclude-result-prefixes="java">
<xsl:output method="text" omit-xml-declaration="yes" indent="no" media-type="text/plain"/>
<xsl:strip-space elements="*"/>
<xsl:template name="timestamp">
    <xsl:value-of select="java:format(java:java.text.SimpleDateFormat.new('yyyy-MM-dd hh-mm-ss.SSS'), java:java.util.Date.new())"/>
</xsl:template>
<xsl:template match="/">
      <xsl:for-each select="Records">
		<xsl:value-of select="ID"/><xsl:text>,</xsl:text>
		<xsl:value-of select="NAME"/>
      </xsl:for-each>
</xsl:template>
 <xsl:strip-space elements="*"/>
</xsl:stylesheet>
